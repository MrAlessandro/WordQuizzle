package server.sessions;

import commons.exceptions.CommunicableException;
import commons.messages.Message;
import commons.messages.MessageType;
import commons.messages.exceptions.UnexpectedMessageException;
import server.challenges.ChallengesManager;
import server.challenges.exceptions.*;
import server.challenges.reports.ChallengeReport;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.friendship.FriendshipRequestsManager;
import server.requests.friendship.exceptions.FriendshipRequestAlreadyReceived;
import server.requests.friendship.exceptions.FriendshipRequestAlreadySent;
import server.sessions.exceptions.AlreadyFriendsException;
import server.sessions.exceptions.ReceiverOfflineException;
import server.sessions.exceptions.UnknownReceiverException;
import server.sessions.exceptions.UserAlreadyLoggedException;
import server.sessions.session.Session;
import server.settings.Settings;
import server.users.UsersManager;
import server.users.exceptions.UnknownUserException;
import server.users.exceptions.WrongPasswordException;
import server.users.user.User;

import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.util.Set;
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
        this.sessionsArchive = new ConcurrentHashMap<>(Settings.SESSIONS_ARCHIVE_INITIAL_SIZE);

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
            ChallengeReport[] reports = this.challengesManager.cancelChallenge(session.getUsername());
            if (reports != null)
            {
                // Send message to eventual opponent
                if (reports[0].player.equals(session.getUsername()))
                    sendMessage(reports[1].player, new Message(MessageType.CHALLENGE_OPPONENT_LOGGED_OUT,
                            String.valueOf(reports[1].winStatus),
                            String.valueOf(reports[1].challengeProgress + 1),
                            String.valueOf(reports[1].scoreGain)));
                else
                    sendMessage(reports[0].player, new Message(MessageType.CHALLENGE_OPPONENT_LOGGED_OUT,
                            String.valueOf(reports[0].winStatus),
                            String.valueOf(reports[0].challengeProgress + 1),
                            String.valueOf(reports[0].scoreGain)));

                // Update both scores
                usersManager.updateUserScore(reports[0].player, reports[0].scoreGain);
                usersManager.updateUserScore(reports[1].player, reports[1].scoreGain);
                sendMessageToAllFriends(reports[0].player, new Message(MessageType.FRIEND_SCORE_UPDATE, reports[0].player, String.valueOf(usersManager.getUser(reports[0].player).getScore())));
                sendMessageToAllFriends(reports[1].player, new Message(MessageType.FRIEND_SCORE_UPDATE, reports[1].player, String.valueOf(usersManager.getUser(reports[1].player).getScore())));
            }

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
        int receiverScore = usersManager.getUsersScore(whoConfirmedRequest);
        sendMessage(whoSentRequest, new Message(MessageType.FRIENDSHIP_REQUEST_CONFIRMED, whoConfirmedRequest, String.valueOf(receiverScore)));
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

        // Check if users are friends
        boolean checkFriendship = usersManager.areFriends(senderUser, receiverUser);
        if (!checkFriendship)
            throw new UnexpectedMessageException("USER \"" + from + "\" AND USER \"" + to + "\" ARE NOT FRIENDS");

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
                        sendMessage(from, new Message(MessageType.CHALLENGE_REQUEST_EXPIRED_APPLICANT));
                        sendMessage(to, new Message(MessageType.CHALLENGE_REQUEST_EXPIRED_RECEIVER));
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
                            challengeReportDelegation -> {
                                ChallengeReport fromReport = challengeReportDelegation.getFromChallengeReport();
                                ChallengeReport toReport = challengeReportDelegation.getToChallengeReport();
                                usersManager.updateUserScore(whoSentRequest, fromReport.scoreGain);
                                usersManager.updateUserScore(whoConfirmedRequest, toReport.scoreGain);
                                sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_REPORT, String.valueOf(fromReport.winStatus), String.valueOf(fromReport.challengeProgress + 1), String.valueOf(fromReport.scoreGain)));
                                sendMessage(whoConfirmedRequest, new Message(MessageType.CHALLENGE_REPORT, String.valueOf(toReport.winStatus), String.valueOf(toReport.challengeProgress + 1), String.valueOf(toReport.scoreGain)));
                                sendMessageToAllFriends(whoSentRequest, new Message(MessageType.FRIEND_SCORE_UPDATE, whoSentRequest, String.valueOf(usersManager.getUser(whoSentRequest).getScore())));
                                sendMessageToAllFriends(whoConfirmedRequest, new Message(MessageType.FRIEND_SCORE_UPDATE, whoConfirmedRequest, String.valueOf(usersManager.getUser(whoConfirmedRequest).getScore())));
                            },
                            challengeReportDelegation -> {
                                // Send expiration challenge request message containing challenge reports to both applicant and receiver
                                ChallengeReport fromReport = challengeReportDelegation.getFromChallengeReport();
                                ChallengeReport toReport = challengeReportDelegation.getToChallengeReport();
                                usersManager.updateUserScore(whoSentRequest, fromReport.scoreGain);
                                usersManager.updateUserScore(whoConfirmedRequest, toReport.scoreGain);
                                sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_EXPIRED, String.valueOf(fromReport.winStatus), String.valueOf(fromReport.challengeProgress + 1), String.valueOf(fromReport.scoreGain)));
                                sendMessage(whoConfirmedRequest, new Message(MessageType.CHALLENGE_EXPIRED, String.valueOf(toReport.winStatus), String.valueOf(toReport.challengeProgress + 1), String.valueOf(toReport.scoreGain)));
                                sendMessageToAllFriends(whoSentRequest, new Message(MessageType.FRIEND_SCORE_UPDATE, whoSentRequest, String.valueOf(usersManager.getUser(whoSentRequest).getScore())));
                                sendMessageToAllFriends(whoConfirmedRequest, new Message(MessageType.FRIEND_SCORE_UPDATE, whoConfirmedRequest, String.valueOf(usersManager.getUser(whoConfirmedRequest).getScore())));
                            });
                }
            }
            catch (UnexpectedMessageException e)
            {
                exception.set(e);
            }
            catch (ApplicantEngagedInOtherChallengeException | ReceiverEngagedInOtherChallengeException e)
            {
                throw new Error("CHALLENGES SYSTEM INCONSISTENCY", e);
            }

            return session;
        });
        if (whoSentRequestSession == null) // -> It should be error but can happen som concurrency issues
            throw new UnexpectedMessageException("DO NOT EXIST ANY CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" and \"" + whoConfirmedRequest + "\"");

        // Check if exceptions has been thrown during the operation
        if (exception.get() != null)
            throw exception.get();

        //Send confirmation message to applicant
        sendMessage(whoSentRequest, new Message(MessageType.CHALLENGE_REQUEST_CONFIRMED,
                whoConfirmedRequest,
                String.valueOf(Settings.CHALLENGE_DURATION_SECONDS),
                String.valueOf(Settings.CHALLENGE_WORDS_QUANTITY)));
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

    public boolean sendMessageToAllFriends(String username, Message message)
    {
        User user = usersManager.getUser(username);
        Set<String> friends = user.getFriends();

        for (String friend : friends)
        {
            sendMessage(friend, message);
        }

        return true;
    }
}
