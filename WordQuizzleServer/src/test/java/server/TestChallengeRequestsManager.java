package server;

import commons.messages.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.requests.challenge.ChallengeRequest;
import server.requests.challenge.ChallengeRequestsManager;
import server.sessions.SessionsManager;
import server.settings.ServerConstants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestChallengeRequestsManager
{
    private ConcurrentHashMap<String, ChallengeRequest> challengeRequestsArchive;

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
    public void setUpChallengeRequestsManager()
    {
        ChallengeRequestsManager.setUp();

        try
        {
            Field field = ChallengeRequestsManager.class.getDeclaredField("challengeRequestsArchive");
            field.setAccessible(true);
            challengeRequestsArchive = (ConcurrentHashMap<String, ChallengeRequest>) field.get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            fail("ERROR GETTING CHALLENGE REQUESTS ARCHIVE PRIVATE FIELD");
        }
    }

    @AfterEach
    public void stopTimers()
    {
        ChallengeRequestsManager.timer.cancel();
        ChallengeRequestsManager.timer.purge();
    }

    @Test
    public void testChallengeRequestRecording()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2));
        assertEquals(2, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestExpiration()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2));
        assertDoesNotThrow(() -> Thread.sleep(ServerConstants.CHALLENGE_REQUEST_TIMEOUT));
        assertEquals(0, challengeRequestsArchive.size());
    }

    /*@Nested
    class TestChallengeRequestsManagerConcurrently
    {
        private ExecutorService pool;

        @BeforeEach
        public void setUp()
        {
            this.pool = Executors.newFixedThreadPool(ServerConstants.DEPUTIES_POOL_SIZE);
        }
    }*/
}
