package server.sessions;

import commons.messages.Message;
import commons.messages.MessageType;
import commons.messages.exceptions.UnexpectedMessageException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import server.challenges.ChallengesManager;
import server.challenges.challege.Challenge;
import server.challenges.exceptions.ApplicantEngagedInOtherChallengeException;
import server.challenges.exceptions.ReceiverEngagedInOtherChallengeException;
import server.challenges.reports.ChallengeReportDelegation;
import server.requests.challenge.ChallengeRequest;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.challenge.exceptions.PreviousChallengeRequestReceivedException;
import server.requests.challenge.exceptions.PreviousChallengeRequestSentException;
import server.requests.challenge.exceptions.ReceiverEngagedInOtherChallengeRequestException;
import server.requests.friendship.FriendshipRequestsManager;
import server.requests.friendship.exceptions.FriendshipRequestAlreadyReceived;
import server.requests.friendship.exceptions.FriendshipRequestAlreadySent;
import server.sessions.exceptions.AlreadyFriendsException;
import server.sessions.exceptions.ReceiverOfflineException;
import server.sessions.exceptions.UnknownReceiverException;
import server.sessions.exceptions.UserAlreadyLoggedException;
import server.sessions.session.Session;
import server.settings.ServerConstants;
import server.users.UsersManager;
import server.users.exceptions.UnknownUserException;
import server.users.user.User;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestSessionsManager
{
    private SessionsManager sessionsManager;
    private UsersManager usersManager;
    private FriendshipRequestsManager friendshipRequestsManager;
    private ChallengeRequestsManager challengeRequestsManager;
    private ChallengesManager challengesManager;

    private ConcurrentHashMap<String, Session> sessionsArchive;
    private ConcurrentHashMap<String, Set<String>> friendshipRequestsArchive;
    private ConcurrentHashMap<String, ChallengeRequest> challengeRequestsArchive;
    private ConcurrentHashMap<String, Challenge> challengesArchive;

    private SocketAddress socketAddress;
    private Selector selector;

    private static final Thread.UncaughtExceptionHandler errorsHandler = (thread, throwable) -> {
        throwable.printStackTrace();
        System.exit(1);
    };

    @BeforeAll
    public static void setUpProperties()
    {
        try
        {
            Thread.currentThread().setUncaughtExceptionHandler(errorsHandler);
            ServerConstants.loadProperties();
        }
        catch (IOException e)
        {
            fail("ERROR ON SETUP");
        }
    }

    @BeforeEach
    public void setUpSessionsManager()
    {
        try
        {
            // Setup Managers
            this.usersManager = new UsersManager();
            this.friendshipRequestsManager = new FriendshipRequestsManager();
            this.challengeRequestsManager = new ChallengeRequestsManager();
            this.challengesManager = new ChallengesManager(errorsHandler);

            this.sessionsManager = new SessionsManager(usersManager, friendshipRequestsManager, challengeRequestsManager, challengesManager);
            Field archiveField = SessionsManager.class.getDeclaredField("sessionsArchive");
            archiveField.setAccessible(true);
            this.sessionsArchive = (ConcurrentHashMap<String, Session>) archiveField.get(this.sessionsManager);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            fail("ERROR GETTING PRIVATE FIELD");
        }
    }

    @Test
    public void testSessionOpening()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        AtomicReference<Session> session = new AtomicReference<>();

        assertDoesNotThrow(() -> usersManager.registerUser(username, password));
        assertDoesNotThrow(() -> session.set(sessionsManager.openSession(username, passwordCopy, selector, socketAddress)));
        assertEquals(username, session.get().getUsername());
        assertEquals(1, sessionsArchive.size());
    }

    @Test
    public void testUnknownUserSessionOpening()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);

        assertThrows(UnknownUserException.class, () -> sessionsManager.openSession(username, passwordCopy, selector, socketAddress));
        assertEquals(0, sessionsArchive.size());
    }

    @Test
    public void testUserAlreadyLoggedOpeningSession()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy1 = Arrays.copyOf(password, password.length);
        char[] passwordCopy2 = Arrays.copyOf(password, password.length);
        AtomicReference<Session> session = new AtomicReference<>();

        assertDoesNotThrow(() -> usersManager.registerUser(username, password));
        assertDoesNotThrow(() -> session.set(sessionsManager.openSession(username, passwordCopy1, selector, socketAddress)));
        assertEquals(username, session.get().getUsername());
        assertEquals(1, sessionsArchive.size());

        assertThrows(UserAlreadyLoggedException.class, () -> session.set(sessionsManager.openSession(username, passwordCopy2, selector, socketAddress)));
        assertEquals(1, sessionsArchive.size());
    }

    @Nested
    class TestSessionedFriendshipRequests
    {
        @BeforeEach
        public void setUp()
        {
            try
            {
                Field field = FriendshipRequestsManager.class.getDeclaredField("friendshipRequestsArchive");
                field.setAccessible(true);
                friendshipRequestsArchive = (ConcurrentHashMap<String, Set<String>>) field.get(friendshipRequestsManager);
            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                fail("ERROR GETTING PRIVATE FIELD");
            }

        }

        @Test
        public void testFriendshipRequestSending_ReceiverLogged()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendFriendshipRequest(username1, username2));
            assertEquals(session2.get().getMessage(), new Message(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, username1));
            assertEquals(1, friendshipRequestsArchive.size());
        }

        @Test
        public void testFriendshipRequestSending_ReceiverUnlogged()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();


            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));

            assertDoesNotThrow(() -> sessionsManager.sendFriendshipRequest(username1, username2));
            assertEquals(usersManager.getUser(username2).getBacklog().pollFirst(), new Message(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, username1));
        }

        @Test
        public void testFriendshipRequestSending_UnknownSender_ERROR()
        {
            String username1 = UUID.randomUUID().toString();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertThrows(Error.class, () -> sessionsManager.sendFriendshipRequest(username1, username2));
        }

        @Test
        public void testFriendshipRequestSending_UnknownReceiver()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertThrows(UnknownReceiverException.class, () -> sessionsManager.sendFriendshipRequest(username1, username2));
        }

        @Test
        public void testFriendshipRequestSending_AlreadyFriends()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            User user1 = usersManager.getUser(username1);
            User user2 = usersManager.getUser(username2);
            assertNotNull(user1);
            assertNotNull(user2);
            assertDoesNotThrow(() -> usersManager.makeFriends(user1, user2));

            assertThrows(AlreadyFriendsException.class, () -> sessionsManager.sendFriendshipRequest(username1, username2));
            assertThrows(AlreadyFriendsException.class, () -> sessionsManager.sendFriendshipRequest(username2, username1));
        }

        @Test
        public void testFriendshipRequestSending_RequestAlreadySent_RequestAlreadyReceived()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendFriendshipRequest(username1, username2));
            assertThrows(FriendshipRequestAlreadySent.class, () -> sessionsManager.sendFriendshipRequest(username1, username2));
            assertThrows(FriendshipRequestAlreadyReceived.class, () -> sessionsManager.sendFriendshipRequest(username2, username1));
        }

        @Test
        public void testFriendshipRequestConfirmation()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendFriendshipRequest(username1, username2));
            assertEquals(session2.get().getMessage(), new Message(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, username1));

            assertDoesNotThrow(() -> sessionsManager.confirmFriendshipRequest(username1, username2));
            assertEquals(session1.get().getMessage(), new Message(MessageType.FRIENDSHIP_REQUEST_CONFIRMED, username2));

            User user1 = usersManager.getUser(username1);
            User user2 = usersManager.getUser(username2);
            assertNotNull(user1);
            assertNotNull(user2);

            assertTrue(usersManager.areFriends(user1, user2));
            assertEquals(0, friendshipRequestsArchive.size());
        }

        @Test
        public void testFriendshipRequestConfirmation_UnknownRequestSender()
        {
            String username1 = UUID.randomUUID().toString();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertThrows(UnknownUserException.class, () -> sessionsManager.confirmFriendshipRequest(username1, username2));
        }

        @Test
        public void testFriendshipRequestConfirmation_UnknownRequestConfirmer_ERROR()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();


            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertThrows(Error.class, () -> sessionsManager.confirmFriendshipRequest(username1, username2));
        }

        @Test
        public void testFriendshipRequestConfirmation_NoCorrespondingRequest()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertThrows(UnexpectedMessageException.class, () -> sessionsManager.confirmFriendshipRequest(username1, username2));

            User user1 = usersManager.getUser(username1);
            User user2 = usersManager.getUser(username2);
            assertNotNull(user1);
            assertNotNull(user2);
            assertFalse(usersManager.areFriends(user1, user2));
        }

        @Test
        public void testFriendshipDeclination()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendFriendshipRequest(username1, username2));
            assertEquals(session2.get().getMessage(), new Message(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, username1));

            assertDoesNotThrow(() -> sessionsManager.declineFriendshipRequest(username1, username2));
            assertEquals(session1.get().getMessage(), new Message(MessageType.FRIENDSHIP_REQUEST_DECLINED, username2));

            User user1 = usersManager.getUser(username1);
            User user2 = usersManager.getUser(username2);
            assertNotNull(user1);
            assertNotNull(user2);

            assertFalse(usersManager.areFriends(user1, user2));
            assertEquals(0, friendshipRequestsArchive.size());
        }

        @Test
        public void testFriendshipDeclination_NoCorrespondingRequest()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertThrows(UnexpectedMessageException.class, () -> sessionsManager.declineFriendshipRequest(username1, username2));
            assertNull(session1.get().getMessage());
        }
    }

    @Nested
    class TestSessionedChallengeRequests
    {
        private ChallengeReportDelegation voidDelegation = new ChallengeReportDelegation() {
            @Override
            public void run()
            {

            }
        };

        @BeforeEach
        public void setUp()
        {
            try
            {
                Field challengeRequestsArchiveField = ChallengeRequestsManager.class.getDeclaredField("challengeRequestsArchive");
                challengeRequestsArchiveField.setAccessible(true);
                challengeRequestsArchive = (ConcurrentHashMap<String, ChallengeRequest>) challengeRequestsArchiveField.get(challengeRequestsManager);

                Field challengesArchiveField = ChallengesManager.class.getDeclaredField("challengesArchive");
                challengesArchiveField.setAccessible(true);
                challengesArchive = (ConcurrentHashMap<String, Challenge>) challengesArchiveField.get(challengesManager);

            }
            catch (NoSuchFieldException | IllegalAccessException e)
            {
                fail("ERROR GETTING PRIVATE FIELD");
            }

        }

        @Test
        public void testChallengeRequestSending_ReceiverLogged()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendChallengeRequest(username1, username2));
            assertEquals(session2.get().getMessage(), new Message(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, username1));
            assertEquals(2, challengeRequestsArchive.size());
        }

        @Test
        public void testChallengeRequestSending_ReceiverUnlogged()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();


            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));

            assertThrows(ReceiverOfflineException.class, () -> sessionsManager.sendChallengeRequest(username1, username2));
            assertEquals(0, challengeRequestsArchive.size());
        }

        @Test
        public void testChallengeRequestSending_UnknownSender_ERROR()
        {
            String username1 = UUID.randomUUID().toString();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertThrows(Error.class, () -> sessionsManager.sendChallengeRequest(username1, username2));
        }

        @Test
        public void testChallengeRequestSending_UnknownReceiver()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertThrows(UnknownReceiverException.class, () -> sessionsManager.sendChallengeRequest(username1, username2));
        }

        @Test
        public void testChallengeRequestSending_SenderAlreadyEngageInOtherChallenge()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            String username3 = UUID.randomUUID().toString();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> challengesManager.recordChallenge(username1, username3, voidDelegation, voidDelegation));

            assertThrows(ApplicantEngagedInOtherChallengeException.class, () -> sessionsManager.sendChallengeRequest(username1, username2));
            assertEquals(0, challengeRequestsArchive.size());
        }

        @Test
        public void testChallengeRequestSending_ReceiverAlreadyEngageInOtherChallenge()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            String username3 = UUID.randomUUID().toString();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> challengesManager.recordChallenge(username2, username3, voidDelegation, voidDelegation));

            assertThrows(ReceiverEngagedInOtherChallengeException.class, () -> sessionsManager.sendChallengeRequest(username1, username2));
            assertEquals(0, challengeRequestsArchive.size());
        }

        @Test
        public void testChallengeRequestSending_RequestAlreadySent_RequestAlreadyReceived()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendChallengeRequest(username1, username2));
            assertThrows(PreviousChallengeRequestSentException.class, () -> sessionsManager.sendChallengeRequest(username1, username2));
            assertThrows(PreviousChallengeRequestReceivedException.class, () -> sessionsManager.sendChallengeRequest(username2, username1));
            assertEquals(2, challengeRequestsArchive.size());
        }

        @Test
        public void testChallengeRequestSending_ReceiverEngagedInOtherChallengeRequest()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            String username3 = UUID.randomUUID().toString();
            char[] password3 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy3 = Arrays.copyOf(password3, password3.length);
            AtomicReference<Session> session3 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username3, password3));
            assertDoesNotThrow(() -> session3.set(sessionsManager.openSession(username3, passwordCopy3, selector, socketAddress)));
            assertEquals(username3, session3.get().getUsername());
            assertEquals(3, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendChallengeRequest(username2, username3));
            assertThrows(ReceiverEngagedInOtherChallengeRequestException.class, () -> sessionsManager.sendChallengeRequest(username1, username2));
            assertEquals(2, challengeRequestsArchive.size());
        }

        @Test
        public void testChallengeRequestConfirmation()
        {
            String username1 = UUID.randomUUID().toString();
            char[] password1 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
            AtomicReference<Session> session1 = new AtomicReference<>();

            String username2 = UUID.randomUUID().toString();
            char[] password2 = UUID.randomUUID().toString().toCharArray();
            char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
            AtomicReference<Session> session2 = new AtomicReference<>();

            assertDoesNotThrow(() -> usersManager.registerUser(username1, password1));
            assertDoesNotThrow(() -> session1.set(sessionsManager.openSession(username1, passwordCopy1, selector, socketAddress)));
            assertEquals(username1, session1.get().getUsername());
            assertEquals(1, sessionsArchive.size());

            assertDoesNotThrow(() -> usersManager.registerUser(username2, password2));
            assertDoesNotThrow(() -> session2.set(sessionsManager.openSession(username2, passwordCopy2, selector, socketAddress)));
            assertEquals(username2, session2.get().getUsername());
            assertEquals(2, sessionsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.sendChallengeRequest(username1, username2));
            assertEquals(session2.get().getMessage(), new Message(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, username1));
            assertEquals(2, challengeRequestsArchive.size());

            assertDoesNotThrow(() -> sessionsManager.confirmChallengeRequest(username1, username2));
            assertEquals(2, challengesArchive.size());
            assertEquals(0, challengeRequestsArchive.size());
        }
    }
}
