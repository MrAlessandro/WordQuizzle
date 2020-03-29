package server.sessions;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.challenges.ChallengesManager;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.friendship.FriendshipRequestsManager;
import server.sessions.session.Session;
import server.settings.ServerConstants;
import server.users.UsersManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.Selector;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;

public class TestSessionsManager
{
    private static ConcurrentHashMap<String, Session> sessionsArchive;

    @BeforeAll
    public static void setUpProperties()
    {
        try
        {
            ServerConstants.loadProperties();
        }
        catch (IOException e)
        {
            fail("ERROR LOADING PROPERTIES");
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
        assertDoesNotThrow(() -> session.set(SessionsManager.openSession(username, password, Selector.open())));
    }
}
