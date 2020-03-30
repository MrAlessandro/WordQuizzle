package server.sessions;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.challenges.ChallengesManager;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.friendship.FriendshipRequestsManager;
import server.sessions.exceptions.UserAlreadyLoggedException;
import server.sessions.session.Session;
import server.settings.ServerConstants;
import server.users.UsersManager;
import server.users.exceptions.UnknownUserException;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestSessionsManager
{
    private ConcurrentHashMap<String, Session> sessionsArchive;
    private static SocketAddress fakeAddress;
    private static Selector fakeSelector;


/*    @BeforeAll
    public static void setUpProperties()
    {
        try
        {
            ServerConstants.loadProperties();
            fakeSelector = Selector.open();
            fakeAddress = InetSocketAddress.createUnresolved("", 0);
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
            // Setup other managers
            UsersManager.setUp();
            FriendshipRequestsManager.setUp();
            ChallengeRequestsManager.setUp();
            ChallengesManager.setUp((t, e) -> e.printStackTrace());

            SessionsManager.setUp();
            Field archiveField = SessionsManager.class.getDeclaredField("sessionsArchive");
            archiveField.setAccessible(true);
            sessionsArchive = (ConcurrentHashMap<String, Session>) archiveField.get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            fail("ERROR GETTING PRIVATE FIELD");
        }
        catch (ParseException e)
        {
            fail("ERROR PARSING DICTIONARY");
        }
        catch (IOException e)
        {
            fail("ERROR READING DICTIONARY");
        }
    }

    @Test
    public void testSessionOpening()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        AtomicReference<Session> session = new AtomicReference<>();

        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
        assertDoesNotThrow(() -> session.set(SessionsManager.openSession(username, passwordCopy, fakeSelector, fakeAddress)));
        assertEquals(1, sessionsArchive.size());
    }

    @Test
    public void testUnknownUserSessionOpening()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);

        assertThrows(UnknownUserException.class, () -> SessionsManager.openSession(username, passwordCopy, fakeSelector, fakeAddress));
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

        assertDoesNotThrow(() -> UsersManager.getUsersManager().registerUser(username, password));
        assertDoesNotThrow(() -> session.set(SessionsManager.openSession(username, passwordCopy1, fakeSelector, fakeAddress)));
        assertEquals(1, sessionsArchive.size());

        assertThrows(UserAlreadyLoggedException.class, () -> session.set(SessionsManager.openSession(username, passwordCopy2, fakeSelector, fakeAddress)));
        assertEquals(1, sessionsArchive.size());
    }*/
}
