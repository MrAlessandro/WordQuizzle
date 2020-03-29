package server.sessions;

import commons.exceptions.CommunicableException;
import commons.messages.Message;
import commons.messages.MessageType;
import commons.messages.exceptions.UnexpectedMessageException;
import server.challenges.reports.ChallengeReport;
import server.challenges.reports.ChallengeReportDelegation;
import server.challenges.ChallengesManager;
import server.challenges.exceptions.*;
import server.settings.ServerConstants;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.friendship.FriendshipRequestsManager;
import server.sessions.exceptions.AlreadyFriendsException;
import server.sessions.exceptions.ReceiverOfflineException;
import server.sessions.exceptions.UnknownReceiverException;
import server.sessions.exceptions.UserAlreadyLoggedException;
import server.sessions.session.Session;
import server.users.UsersManager;
import server.requests.friendship.exceptions.FriendshipRequestAlreadyReceived;
import server.requests.friendship.exceptions.FriendshipRequestAlreadySent;
import server.users.exceptions.UnknownUserException;
import server.users.exceptions.WrongPasswordException;
import server.users.user.User;

import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class SessionsManager
{
    private static final Object FRIENDSHIPS_MONITOR = new Object();
    private static final Object CHALLENGES_MONITOR = new Object();

    private static ConcurrentHashMap<String, Session> sessionsArchive;

    public static void setUp()
    {
        sessionsArchive = new ConcurrentHashMap<>(ServerConstants.SESSIONS_ARCHIVE_INITIAL_SIZE);
    }

    public static Session openSession(String username, char[] password, Selector selector, SocketAddress address) throws UnknownUserException, WrongPasswordException, UserAlreadyLoggedException
    {
        Session session;

        // Get the user
        User user = UsersManager.getUser(username);
        if (user == null)
            throw new UnknownUserException(username, "UNKNOWN USER \"" + username + "\"");

        // Check password
        user.checkPassword(password);

        // Create the session
        session = new Session(username, selector, address, user.getBacklog());

        // Put the session in the sessions archive if it is not already present
        if (sessionsArchive.putIfAbsent(username, session) != null)
            throw new UserAlreadyLoggedException("USER \"" + username + "\" ALREADY LOGGED");

        return session;
    }

    /*TODO*/
    public static void closeSession(Session session)
    {
        // Remove session from the archive
        sessionsArchive.remove(session.getUsername(), session);

        // Close session
        session.close();

        synchronized (CHALLENGES_MONITOR)
        {
            // Discard eventual active challenge
            String eventualOpponent = ChallengesManager.cancelChallenge(session.getUsername());
            if (eventualOpponent != null)
            {
                /*TODO*/
            }

            // Discard eventual challenge request
            String eventualRequestPeer = ChallengeRequestsManager.cancelChallengeRequest(session.getUsername());
            if (eventualRequestPeer != null)
            {
                /*TODO*/
            }
        }
    }

    public static void terminateSession(Session session)
    {
        // Remove session from the archive
        sessionsArchive.remove(session.getUsername(), session);

        // Close session
        session.close();
    }

    public static void sendFriendshipRequest(String from, String to) throws UnknownReceiverException, AlreadyFriendsException, FriendshipRequestAlreadySent, FriendshipRequestAlreadyReceived
    {
        User userFrom;
        User userTo;

        synchronized (FRIENDSHIPS_MONITOR)
        {
            // Get server.users
            userFrom = UsersManager.getUser(from);
            if (userFrom == null)
                throw new Error("SESSIONS SYSTEM INCONSISTENCY");

            userTo = UsersManager.getUser(to);
            if (userTo == null)
                throw new UnknownReceiverException("UNKNOWN USER \"" + to + "\"");

            // Check if server.users exist and if they are friends
            if (UsersManager.areFriends(userFrom, userTo))
                throw new AlreadyFriendsException("USER \"" + from + "\" AND USER \"" + to + "\" ARE ALREADY FRIENDS");

            // Store the friendship request in the friendship server.requests archive
            FriendshipRequestsManager.recordFriendshipRequest(from, to);
        }

        // Prepare message
        Message message = new Message(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, from);

        // Send directly to receiver if is online
        if (!sendMessage(to, message))
            // Store in user backlog otherwise
            userTo.getBacklog().add(message);
    }

    public static void confirmFriendshipRequest(String whoSentRequest, String whoConfirmedRequest) throws UnexpectedMessageException, UnknownUserException
    {
        User whoSentRequestUser;
        User whoConfirmedRequestUser;

        synchronized (FRIENDSHIPS_MONITOR)
        {
            // Get server.users
            whoConfirmedRequestUser = UsersManager.getUser(whoConfirmedRequest);
            if (whoConfirmedRequestUser == null)
                throw new Error("SESSIONS SYSTEM INCONSISTENCY");

            whoSentRequestUser = UsersManager.getUser(whoSentRequest);
            if (whoSentRequestUser == null)
                throw new UnknownUserException("UNKNOWN USER \"" + whoSentRequest + "\"");

            // Remove friendship request from the friendship server.requests archive
            if (!FriendshipRequestsManager.discardFriendshipRequest(whoSentRequest, whoConfirmedRequest))
                throw new UnexpectedMessageException("DO NOT EXIST ANY FRIENDSHIP REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoConfirmedRequest + "\"");

            // Make server.users friends
            UsersManager.makeFriends(whoSentRequestUser, whoConfirmedRequestUser);
        }

        // Send confirmation message to applicant user if is online
        sendMessage(whoSentRequest, new Message(MessageType.FRIENDSHIP_REQUEST_CONFIRMED, whoConfirmedRequest));
    }

    public static void declineFriendshipRequest(String whoSentRequest, String whoDeclinedRequest) throws UnexpectedMessageException
    {
        // Remove friendship request from the friendship server.requests archive
        if (!FriendshipRequestsManager.discardFriendshipRequest(whoSentRequest, whoDeclinedRequest))
            throw new UnexpectedMessageException("DO NOT EXIST ANY FRIENDSHIP REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoDeclinedRequest + "\"");

        // Send declining message to applicant user if is online
        sendMessage(whoSentRequest, new Message(MessageType.FRIENDSHIP_REQUEST_DECLINED, whoDeclinedRequest));
    }

    public static void sendChallengeRequest(String from, String to) throws CommunicableException
    {
        AtomicReference<CommunicableException> exception = new AtomicReference<>(null);
        Session receiverSession;
        User receiverUser;

        // Get the receiver user
        receiverUser = UsersManager.getUser(to);
        if (receiverUser == null)
            throw new UnknownReceiverException("UNKNOWN USER \"" + to + "\"");

        // Check if receiver is online. If it is check if is engaged in a challenge or in a challenge request and then store challenge request archive.
        receiverSession = sessionsArchive.computeIfPresent(to, (key, session) -> {
            try
            {
                synchronized (CHALLENGES_MONITOR)
                {
                    // Check if both server.users are already engaged in others challenge
                    ChallengesManager.checkEngagement(from, to);
                    // Record the challenge request
                    ChallengeRequestsManager.recordChallengeRequest(from, to, () -> {
                        // Send expiration challenge request message to both applicant and receiver
                        sendMessage(from, new Message(MessageType.CHALLENGE_REQUEST_EXPIRED));
                        sendMessage(to, new Message(MessageType.CHALLENGE_REQUEST_EXPIRED));
                    });
                }
            }
            catch (CommunicableException e)
            {
                exception.set(e);
            }

            return session;
        });
        if (receiverSession == null)
            // Receiver is offline. Challenge request cannot be forwarded.
            throw new ReceiverOfflineException("RECEIVER \"" + to + "\" IS OFFLINE");

        // Check if exceptions has been thrown during the operation
        if (exception.get() != null)
            throw exception.get();

        // Send challenge request message to receiver
        receiverSession.appendMessage(new Message(MessageType.REQUEST_FOR_CHALLENGE, from));
    }

    public static void confirmChallengeRequest(String whoSentRequest, String whoConfirmedRequest) throws UnexpectedMessageException
    {
        AtomicReference<UnexpectedMessageException> exception = new AtomicReference<>(null);
        Session whoSentRequestSession;

        // Lock the request applicant session
        whoSentRequestSession = sessionsArchive.computeIfPresent(whoSentRequest, (key, session) -> {
            try
            {
                synchronized (CHALLENGES_MONITOR)
                {
                    // Discard challenge request
                    if (!ChallengeRequestsManager.discardChallengeRequest(whoSentRequest, whoConfirmedRequest))
                        throw new UnexpectedMessageException("DO NOT EXIST ANY CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoConfirmedRequest + "\"");

                    // Record challenge
                    ChallengesManager.recordChallenge(whoSentRequest, whoConfirmedRequest,
                            new ChallengeReportDelegation()
                            {
                                @Override
                                public void run()
                                {
                                    ChallengeReport fromReport = this.getFromChallengeReport();
                                    ChallengeReport toReport = this.getToChallengeReport();
                                    sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_REPORT, String.valueOf(fromReport.winStatus), String.valueOf(fromReport.challengeProgress), String.valueOf(fromReport.scoreGain)));
                                    sendMessage(whoConfirmedRequest, new Message(MessageType.CHALLENGE_REPORT, String.valueOf(toReport.winStatus), String.valueOf(toReport.challengeProgress), String.valueOf(toReport.scoreGain)));
                                }
                            },
                            new ChallengeReportDelegation()
                            {
                                @Override
                                public void run()
                                {
                                    // Send expiration challenge request message containing challenge reports to both applicant and receiver
                                    ChallengeReport fromReport = this.getFromChallengeReport();
                                    ChallengeReport toReport = this.getToChallengeReport();
                                    sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_EXPIRED, String.valueOf(fromReport.winStatus), String.valueOf(fromReport.challengeProgress), String.valueOf(fromReport.scoreGain)));
                                    sendMessage(whoConfirmedRequest, new Message(MessageType.CHALLENGE_EXPIRED, String.valueOf(toReport.winStatus), String.valueOf(toReport.challengeProgress), String.valueOf(toReport.scoreGain)));
                                }
                            });
                }
            }
            catch (UnexpectedMessageException e)
            {
                exception.set(e);
            }
            catch (ApplicantEngagedInOtherChallengeException | ReceiverEngagedInOtherChallengeException e)
            {
                throw new Error("CHALLENGES SYSTEM INCONSISTENCY");
            }

            return session;
        });
        if (whoSentRequestSession == null)
            throw new UnexpectedMessageException("DO NOT EXIST ANY CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoConfirmedRequest + "\"");

        // Check if exceptions has been thrown during the operation
        if (exception.get() != null)
            throw exception.get();

        //Send confirmation message to applicant
        sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_REQUEST_CONFIRMED, whoConfirmedRequest));
    }

    public static void declineChallengeRequest(String whoSentRequest, String whoDeclinedRequest) throws UnexpectedMessageException
    {
        // Discard challenge request
        if (!ChallengeRequestsManager.discardChallengeRequest(whoSentRequest, whoDeclinedRequest))
            throw new UnexpectedMessageException("DO NOT EXIST ANY CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoDeclinedRequest + "\"");

        // Send declining message to applicant user
        sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_REQUEST_DECLINED, whoDeclinedRequest));
    }

    /* TODO: voluntary cancellation of challenge -> ChallengesManager.discardChallenge() */

    public static String retrieveNextWord(String username) throws UnexpectedMessageException
    {
        String word;

        try
        {
            word = ChallengesManager.retrieveNextWord(username);
        }
        catch (NoChallengeRelatedException | NoFurtherWordsToGetException | WordRetrievalOutOfSequenceException e)
        {
            throw new UnexpectedMessageException(e.getMessage());
        }

        return word;
    }

    public static boolean provideTranslation(String username, String translation) throws UnexpectedMessageException
    {
        boolean correct;

        try
        {
            correct = ChallengesManager.provideTranslation(username, translation);
        }
        catch (NoChallengeRelatedException | TranslationProvisionOutOfSequenceException e)
        {
            throw new UnexpectedMessageException(e.getMessage());
        }

        return correct;
    }

    public static boolean sendMessage(String username, Message message)
    {
        // Get the session by username
        Session session = sessionsArchive.get(username);
        if (session == null)
            // User is not online
            return false;
        else
        {// Append to session backlog and wake up relative selector
            session.appendMessage(message);
            session.wakeUp();
            return true;
        }
    }
}
