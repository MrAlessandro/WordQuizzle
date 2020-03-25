package server.requests.challenge;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.requests.challenge.ChallengeRequest;
import server.requests.challenge.ChallengeRequestsManager;
import server.settings.ServerConstants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    public void testChallengeRequestRecording()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestDiscarding()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
        assertDoesNotThrow(() -> ChallengeRequestsManager.discardChallengeRequest(username1, username2));
        assertEquals(0, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestCanceling()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        final String[] usernameTaken = new String[1];

        assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
        assertDoesNotThrow(() -> {usernameTaken[0] = ChallengeRequestsManager.cancelChallengeRequest(username1);});
        assertEquals(0, challengeRequestsArchive.size());
        assertEquals(username2, usernameTaken[0]);
    }

    @Test
    public void testChallengeRequestExpiration()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        AtomicBoolean timeoutFlag = new AtomicBoolean(false);

        assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2, () -> timeoutFlag.set(true)));
        assertDoesNotThrow(() -> Thread.sleep(ServerConstants.CHALLENGE_REQUEST_TIMEOUT * 2));
        assertEquals(0, challengeRequestsArchive.size());
        assertTrue(timeoutFlag.get());
    }

    @Nested
    class TestChallengeRequestsManagerConcurrently
    {
        private ExecutorService pool;

        @BeforeEach
        public void setUp()
        {
            this.pool = Executors.newFixedThreadPool(ServerConstants.DEPUTIES_POOL_SIZE);
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentChallengeRequestRecording(int tasksNum)
        {
            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String username1 = UUID.randomUUID().toString();
                    String username2 = UUID.randomUUID().toString();

                    assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
                });
            }

            pool.shutdownNow();
            assertDoesNotThrow(() -> pool.awaitTermination(tasksNum, TimeUnit.SECONDS));
            assertEquals(tasksNum * 2, challengeRequestsArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentChallengeRequestExpiration(int tasksNum)
        {
            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String username1 = UUID.randomUUID().toString();
                    String username2 = UUID.randomUUID().toString();

                    assertDoesNotThrow(() -> ChallengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
                });
            }

            pool.shutdownNow();
            assertDoesNotThrow(() -> Thread.sleep(ServerConstants.CHALLENGE_REQUEST_TIMEOUT * 2));
            assertEquals(0, challengeRequestsArchive.size());
        }
    }
}
