package server.requests.challenge;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.requests.challenge.exceptions.PreviousChallengeRequestReceivedException;
import server.requests.challenge.exceptions.PreviousChallengeRequestSentException;
import server.requests.challenge.exceptions.ReceiverEngagedInOtherChallengeRequestException;
import server.settings.Settings;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class TestChallengeRequestsManager
{
    private ChallengeRequestsManager challengeRequestsManager;
    private ConcurrentHashMap<String, ChallengeRequest> challengeRequestsArchive;
    private ScheduledThreadPoolExecutor timer;

    @BeforeAll
    public static void setUpProperties()
    {
        try
        {
            Settings.loadProperties();
        }
        catch (IOException e)
        {
            fail("ERROR LOADING PROPERTIES");
        }
    }

    @BeforeEach
    public void setUpChallengeRequestsManager()
    {
        try
        {
            challengeRequestsManager = new ChallengeRequestsManager();
            Field timerField = ChallengeRequestsManager.class.getDeclaredField("timer");
            Field archiveField = ChallengeRequestsManager.class.getDeclaredField("challengeRequestsArchive");
            timerField.setAccessible(true);
            archiveField.setAccessible(true);
            challengeRequestsArchive = (ConcurrentHashMap<String, ChallengeRequest>) archiveField.get(this.challengeRequestsManager);
            timer = (ScheduledThreadPoolExecutor) timerField.get(this.challengeRequestsManager);
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

        assertDoesNotThrow(() -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestPreviousChallengeRequestReceived()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());

        assertThrows(PreviousChallengeRequestReceivedException.class, () -> this.challengeRequestsManager.recordChallengeRequest(username2, username1, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestPreviousChallengeRequestSent()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());

        assertThrows(PreviousChallengeRequestSentException.class, () -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestReceiverAlreadyEngagedInOtherChallengeRequest()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        String username3 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());

        assertThrows(ReceiverEngagedInOtherChallengeRequestException.class, () -> this.challengeRequestsManager.recordChallengeRequest(username3, username1, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestDiscarding()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
        assertDoesNotThrow(() -> this.challengeRequestsManager.discardChallengeRequest(username1, username2));
        assertEquals(0, challengeRequestsArchive.size());
    }

    @Test
    public void testChallengeRequestCanceling()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        final String[] usernameTaken = new String[1];

        assertDoesNotThrow(() -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
        assertEquals(2, challengeRequestsArchive.size());
        assertDoesNotThrow(() -> {usernameTaken[0] = this.challengeRequestsManager.cancelChallengeRequest(username1);});
        assertEquals(0, challengeRequestsArchive.size());
        assertEquals(username2, usernameTaken[0]);
    }

    @Test
    public void testChallengeRequestExpiration()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        AtomicBoolean timeoutFlag = new AtomicBoolean(false);

        assertDoesNotThrow(() -> this.challengeRequestsManager.recordChallengeRequest(username1, username2, () -> timeoutFlag.set(true)));
        assertEquals(2, challengeRequestsArchive.size());
        timer.shutdown();
        assertDoesNotThrow(() -> timer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));
        assertTrue(timeoutFlag.get());
        assertEquals(0, challengeRequestsArchive.size());
    }

    @Nested
    class TestChallengeRequestsManagerConcurrently
    {
        private ExecutorService pool;

        @BeforeEach
        public void setUp()
        {
            this.pool = Executors.newFixedThreadPool(Settings.DEPUTIES_POOL_SIZE);
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

                    assertDoesNotThrow(() -> challengeRequestsManager.recordChallengeRequest(username1, username2, () -> {}));
                });
            }

            pool.shutdown();
            assertDoesNotThrow(() -> pool.awaitTermination(tasksNum, TimeUnit.SECONDS));
            assertEquals(tasksNum * 2, challengeRequestsArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentChallengeRequestExpiration(int tasksNum)
        {
            AtomicBoolean[] expirationFlags = new AtomicBoolean[tasksNum];

            for (int i = 0; i < tasksNum; i++)
            {
                expirationFlags[i] = new AtomicBoolean(false);

                int finalI = i;
                pool.submit(() ->
                {
                    String username1 = UUID.randomUUID().toString();
                    String username2 = UUID.randomUUID().toString();

                    assertDoesNotThrow(() -> challengeRequestsManager.recordChallengeRequest(username1, username2, () -> expirationFlags[finalI].set(true)));
                });
            }

            pool.shutdown();
            assertDoesNotThrow(() -> pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));

            timer.shutdown();
            assertDoesNotThrow(() -> timer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));
            for (int i = 0; i < tasksNum; i++)
            {
                assertTrue(expirationFlags[i].get());
            }

            assertEquals(0, challengeRequestsArchive.size());
        }
    }
}
