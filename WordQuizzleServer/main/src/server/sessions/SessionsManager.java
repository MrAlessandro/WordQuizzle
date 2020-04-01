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
    // Monitor for requests
    private final Object friendshipsMonitor = new Object();
    private final Object challengesMonitor = new Object();

    // Sessions archive
    private ConcurrentHashMap<String, Session> sessionsArchive;

    // Managers
    private UsersManager usersManager;
    private FriendshipRequestsManager friendshipRequestsManager;
    private ChallengeRequestsManager challengeRequestsManager;
    private ChallengesManager challengesManager;

    public SessionsManager(UsersManager usersManager, FriendshipRequestsManager friendshipRequestsManager, ChallengeRequestsManager challengeRequestsManager, ChallengesManager challengesManager)
    {
        this.sessionsArchive = new ConcurrentHashMap<>(ServerConstants.SESSIONS_ARCHIVE_INITIAL_SIZE);

        // Set managers
        this.friendshipRequestsManager = friendshipRequestsManager;
        this.challengeRequestsManager = challengeRequestsManager;
        this.challengesManager = challengesManager;
        this.usersManager = usersManager;
    }

    public Session openSession(String username, char[] password, Selector selector, SocketAddress address) throws UnknownUserException, WrongPasswordException, UserAlreadyLoggedException
    {
        Session session;

        // Get the user
        User user = this.usersManager.getUser(username);
        if (user == null)
            throw new UnknownUserException(username, "UNKNOWN USER \"" + username + "\"");

        // Check password
        user.checkPassword(password);

        // Create the session
        session = new Session(username, selector, address, user.getBacklog());

        // Put the session in the sessions archive if it is not already present
        if (this.sessionsArchive.putIfAbsent(username, session) != null)
            throw new UserAlreadyLoggedException("USER \"" + username + "\" ALREADY LOGGED");

        return session;
    }

    public void closeSession(Session session)
    {
        // Remove session from the archive
        this.sessionsArchive.remove(session.getUsername(), session);

        // Close session
        session.close();

        synchronized (challengesMonitor)
        {
            // Discard eventual active challenge
            ChallengeReport eventualOpponentReport = this.challengesManager.cancelChallenge(session.getUsername());
            if (eventualOpponentReport != null)
                sendMessage(eventualOpponentReport.player, new Message(MessageType.CHALLENGE_OPPONENT_LOGGED_OUT,
                        String.valueOf(eventualOpponentReport.winStatus),
                        String.valueOf(eventualOpponentReport.challengeProgress),
                        String.valueOf(eventualOpponentReport.scoreGain)));

            // Discard eventual challenge request
            String eventualRequestPeer = this.challengeRequestsManager.cancelChallengeRequest(session.getUsername());
            if (eventualRequestPeer != null)
                sendMessage(eventualRequestPeer, new Message(MessageType.CHALLENGE_REQUEST_OPPONENT_LOGGED_OUT, session.getUsername()));
        }
    }

    public void terminateSession(Session session)
    {
        // Remove session from the archive
        this.sessionsArchive.remove(session.getUsername(), session);

        // Close session
        session.close();
    }

    public void sendFriendshipRequest(String from, String to) throws UnknownReceiverException, AlreadyFriendsException, FriendshipRequestAlreadySent, FriendshipRequestAlreadyReceived
    {
        User userFrom;
        User userTo;

        // Get users
        userFrom = this.usersManager.getUser(from);
        if (userFrom == null)
            throw new Error("SESSIONS SYSTEM INCONSISTENCY");

        userTo = this.usersManager.getUser(to);
        if (userTo == null)
            throw new UnknownReceiverException("UNKNOWN USER \"" + to + "\"");

        synchronized (friendshipsMonitor)
        {
            // Check if server.users exist and if they are friends
            if (this.usersManager.areFriends(userFrom, userTo))
                throw new AlreadyFriendsException("USER \"" + from + "\" AND USER \"" + to + "\" ARE ALREADY FRIENDS");

            // Store the friendship request in the friendship server.requests archive
            this.friendshipRequestsManager.recordFriendshipRequest(from, to);
        }

        // Prepare message
        Message message = new Message(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, from);

        // Send directly to receiver if is online
        if (!sendMessage(to, message))
            // Store in user backlog otherwise
            userTo.getBacklog().add(message);
    }

    public void confirmFriendshipRequest(String whoSentRequest, String whoConfirmedRequest) throws UnexpectedMessageException, UnknownUserException
    {
        User whoSentRequestUser;
        User whoConfirmedRequestUser;

        synchronized (friendshipsMonitor)
        {
            // Get server.users
            whoConfirmedRequestUser = this.usersManager.getUser(whoConfirmedRequest);
            if (whoConfirmedRequestUser == null)
                throw new Error("SESSIONS SYSTEM INCONSISTENCY");

            whoSentRequestUser = this.usersManager.getUser(whoSentRequest);
            if (whoSentRequestUser == null)
                throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");

            // Remove friendship request from the friendship server.requests archive
            if (!this.friendshipRequestsManager.discardFriendshipRequest(whoSentRequest, whoConfirmedRequest))
                throw new UnexpectedMessageException("DO NOT EXIST ANY FRIENDSHIP REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoConfirmedRequest + "\"");

            // Make server.users friends
            this.usersManager.makeFriends(whoSentRequestUser, whoConfirmedRequestUser);
        }

        // Send confirmation message to applicant user if is online
        sendMessage(whoSentRequest, new Message(MessageType.FRIENDSHIP_REQUEST_CONFIRMED, whoConfirmedRequest));
    }

    public void declineFriendshipRequest(String whoSentRequest, String whoDeclinedRequest) throws UnexpectedMessageException
    {
        // Remove friendship request from the friendship server.requests archive
        if (!this.friendshipRequestsManager.discardFriendshipRequest(whoSentRequest, whoDeclinedRequest))
            throw new UnexpectedMessageException("DO NOT EXIST ANY FRIENDSHIP REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoDeclinedRequest + "\"");

        // Send declining message to applicant user if is online
        sendMessage(whoSentRequest, new Message(MessageType.FRIENDSHIP_REQUEST_DECLINED, whoDeclinedRequest));
    }

    public void sendChallengeRequest(String from, String to) throws CommunicableException
    {
        AtomicReference<CommunicableException> exception = new AtomicReference<>(null);
        Session receiverSession;
        User receiverUser;
        User senderUser;

        // Get the sender user
        senderUser = this.usersManager.getUser(from);
        if (senderUser == null)
            throw new Error("SESSIONS SYSTEM INCONSISTENCY");

        // Get the receiver user
        receiverUser = this.usersManager.getUser(to);
        if (receiverUser == null)
            throw new UnknownReceiverException("UNKNOWN USER \"" + to + "\"");

        // Check if receiver is online. If it is check if is engaged in a challenge or in a challenge request and then store challenge request archive.
        receiverSession = this.sessionsArchive.computeIfPresent(to, (key, session) -> {
            try
            {
                synchronized (this.challengesMonitor)
                {
                    // Check if both server.users are already engaged in others challenge
                    this.challengesManager.checkEngagement(from, to);
                    // Record the challenge request
                    this.challengeRequestsManager.recordChallengeRequest(from, to, () -> {
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
        receiverSession.appendMessage(new Message(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, from));
    }

    public void confirmChallengeRequest(String whoSentRequest, String whoConfirmedRequest) throws UnexpectedMessageException
    {
        AtomicReference<UnexpectedMessageException> exception = new AtomicReference<>(null);
        Session whoSentRequestSession;

        // Lock the request applicant session
        whoSentRequestSession = this.sessionsArchive.computeIfPresent(whoSentRequest, (key, session) -> {
            try
            {
                synchronized (challengesMonitor)
                {
                    // Discard challenge request
                    if (!this.challengeRequestsManager.discardChallengeRequest(whoSentRequest, whoConfirmedRequest))
                        throw new UnexpectedMessageException("DO NOT EXIST ANY CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoConfirmedRequest + "\"");

                    // Record challenge
                    this.challengesManager.recordChallenge(whoSentRequest, whoConfirmedRequest,
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
        if (whoSentRequestSession == null) // -> It should be error but can happen som concurrency issues
            throw new UnexpectedMessageException("DO NOT EXIST ANY CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoConfirmedRequest + "\"");

        // Check if exceptions has been thrown during the operation
        if (exception.get() != null)
            throw exception.get();

        //Send confirmation message to applicant
        sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_REQUEST_CONFIRMED, whoConfirmedRequest));
    }

    public void declineChallengeRequest(String whoSentRequest, String whoDeclinedRequest) throws UnexpectedMessageException
    {
        // Discard challenge request
        if (!this.challengeRequestsManager.discardChallengeRequest(whoSentRequest, whoDeclinedRequest))
            throw new UnexpectedMessageException("DO NOT EXIST ANY CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoDeclinedRequest + "\"");

        // Send declining message to applicant user
        sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_REQUEST_DECLINED, whoDeclinedRequest));
    }

    /* TODO: voluntary cancellation of challenge -> this.challengesManager.discardChallenge() */

    public String retrieveNextWord(String username) throws UnexpectedMessageException
    {
        String word;

        try
        {
            word = this.challengesManager.retrieveNextWord(username);
        }
        catch (NoChallengeRelatedException | NoFurtherWordsToGetException | WordRetrievalOutOfSequenceException e)
        {
            throw new UnexpectedMessageException(e.getMessage());
        }

        return word;
    }

    public boolean provideTranslation(String username, String translation) throws UnexpectedMessageException
    {
        boolean correct;

        try
        {
            correct = this.challengesManager.provideTranslation(username, translation);
        }
        catch (NoChallengeRelatedException | TranslationProvisionOutOfSequenceException e)
        {
            throw new UnexpectedMessageException(e.getMessage());
        }

        return correct;
    }

    public boolean sendMessage(String username, Message message)
    {
        // Get the session by username
        Session session = this.sessionsArchive.get(username);
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
