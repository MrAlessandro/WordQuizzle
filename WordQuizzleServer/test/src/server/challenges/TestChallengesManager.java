package server.challenges;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import server.challenges.challege.Challenge;
import server.challenges.exceptions.*;
import server.challenges.reports.ChallengeReport;
import server.challenges.reports.ChallengeReportDelegation;
import server.settings.ServerConstants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestChallengesManager
{
    private ChallengesManager challengesManager;
    private ConcurrentHashMap<String, Challenge> challengesArchive;
    private ScheduledThreadPoolExecutor timer;

    private ChallengeReportDelegation voidDelegation = new ChallengeReportDelegation() {
        @Override
        public void run()
        {

        }
    };

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

        try
        {
            this.challengesManager = new ChallengesManager((t, e) -> e.printStackTrace());
            Field timerField = ChallengesManager.class.getDeclaredField("timer");
            Field archiveField = ChallengesManager.class.getDeclaredField("challengesArchive");
            timerField.setAccessible(true);
            archiveField.setAccessible(true);
            challengesArchive = (ConcurrentHashMap<String, Challenge>) archiveField.get(this.challengesManager);
            timer = (ScheduledThreadPoolExecutor) timerField.get(this.challengesManager);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            fail("ERROR GETTING PRIVATE FIELD");
        }
    }

    @Test
    public void testChallengeRecording()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
    }

    @Test
    public void testChallengeRecordingApplicantEngaged()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        String username3 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
        assertThrows(ApplicantEngagedInOtherChallengeException.class, () -> this.challengesManager.recordChallenge(username1, username3, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
    }

    @Test
    public void testChallengeRecordingReceiverEngaged()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        String username3 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
        assertThrows(ReceiverEngagedInOtherChallengeException.class, () -> this.challengesManager.recordChallenge(username3, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
    }

    @Test
    public void testChallengeExpiration()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        AtomicBoolean expirationFlag = new AtomicBoolean(false);

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2, voidDelegation, new ChallengeReportDelegation()
        {
            @Override
            public void run()
            {
                expirationFlag.set(true);
            }
        }));
        assertEquals(2, challengesArchive.size());
        timer.shutdown();
        assertDoesNotThrow(() -> timer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));
        assertTrue(expirationFlag.get());
        assertEquals(0, challengesArchive.size());
    }

    @Test
    public void testChallengeCanceling()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
        timer.shutdownNow();

        AtomicReference<ChallengeReport> opponent = new AtomicReference<>(null);
        assertDoesNotThrow(() -> {opponent.set(this.challengesManager.cancelChallenge(username1));});
        assertEquals(username2, opponent.get().player);
        assertEquals(0, challengesArchive.size());
    }

    @Test
    public void testChallengeCompletion()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        AtomicBoolean completionFlag = new AtomicBoolean(false);

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2,
                new ChallengeReportDelegation()
                {
                    @Override
                    public void run()
                    {
                        completionFlag.set(true);
                    }
                },
                voidDelegation));
        assertEquals(2, challengesArchive.size());
        timer.shutdownNow();

        for (int i = 0; i < ServerConstants.CHALLENGE_WORDS_QUANTITY; i++)
        {
            assertDoesNotThrow(() -> this.challengesManager.retrieveNextWord(username1));
            assertDoesNotThrow(() -> this.challengesManager.retrieveNextWord(username2));
            assertDoesNotThrow(() -> this.challengesManager.provideTranslation(username1, UUID.randomUUID().toString()));
            assertDoesNotThrow(() -> this.challengesManager.provideTranslation(username2, UUID.randomUUID().toString()));
        }
        assertTrue(completionFlag.get());
    }

    @Test
    public void testNoChallengeRelated()
    {
        String username1 = UUID.randomUUID().toString();

        assertThrows(NoChallengeRelatedException.class, () -> this.challengesManager.retrieveNextWord(username1));
    }

    @Test
    public void testChallengeWordRetrievalOutOfSequence()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2,
                voidDelegation,
                voidDelegation));
        assertEquals(2, challengesArchive.size());
        timer.shutdownNow();

        assertDoesNotThrow(() -> this.challengesManager.retrieveNextWord(username1));
        assertThrows(WordRetrievalOutOfSequenceException.class, () -> this.challengesManager.retrieveNextWord(username1));
    }

    @Test
    public void testChallengeTranslationProvisionOutOfSequence()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2,
                voidDelegation,
                voidDelegation));
        assertEquals(2, challengesArchive.size());
        timer.shutdownNow();

        assertDoesNotThrow(() -> this.challengesManager.retrieveNextWord(username1));
        assertDoesNotThrow(() -> this.challengesManager.provideTranslation(username1, ""));
        assertThrows(TranslationProvisionOutOfSequenceException.class, () -> this.challengesManager.provideTranslation(username1, ""));
    }

    @Test
    public void testChallengeNoFurtherWordsToRetrieve()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        AtomicBoolean completionFlag = new AtomicBoolean(false);

        assertDoesNotThrow(() -> this.challengesManager.recordChallenge(username1, username2,
                new ChallengeReportDelegation()
                {
                    @Override
                    public void run()
                    {
                        completionFlag.set(true);
                    }
                },
                voidDelegation));
        assertEquals(2, challengesArchive.size());
        timer.shutdownNow();

        for (int i = 0; i < ServerConstants.CHALLENGE_WORDS_QUANTITY; i++)
        {
            assertDoesNotThrow(() -> this.challengesManager.retrieveNextWord(username1));
            assertDoesNotThrow(() -> this.challengesManager.provideTranslation(username1, ""));
        }

        assertFalse(completionFlag.get());
        assertThrows(NoFurtherWordsToGetException.class, () -> this.challengesManager.retrieveNextWord(username1));
    }

    @Nested
    class TestChallengesManagerConcurrently
    {
        private ExecutorService pool;

        @BeforeEach
        public void setUp()
        {
            this.pool = Executors.newFixedThreadPool(ServerConstants.DEPUTIES_POOL_SIZE);
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentChallengeRecording(int tasksNum)
        {
            for (int i = 0; i < tasksNum; i++)
            {
                pool.submit(() ->
                {
                    String username1 = UUID.randomUUID().toString();
                    String username2 = UUID.randomUUID().toString();

                    assertDoesNotThrow(() -> challengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
                });
            }

            timer.shutdownNow();
            pool.shutdown();
            assertDoesNotThrow(() -> pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));
            assertEquals(tasksNum * 2, challengesArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentChallengeExpiration(int tasksNum)
        {
            AtomicBoolean[] expirationFlags = new AtomicBoolean[tasksNum];

            for (int i = 0; i < tasksNum; i++)
            {
                String username1 = UUID.randomUUID().toString();
                String username2 = UUID.randomUUID().toString();
                expirationFlags[i] = new AtomicBoolean(false);

                int finalI = i;
                pool.submit(() -> assertDoesNotThrow(() -> challengesManager.recordChallenge(username1, username2, voidDelegation, new ChallengeReportDelegation()
                {
                    @Override
                    public void run()
                    {
                        expirationFlags[finalI].set(true);
                    }
                })));
            }

            pool.shutdown();
            assertDoesNotThrow(() -> pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));

            timer.shutdown();
            assertDoesNotThrow(() -> timer.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));
            for (int i = 0; i < tasksNum; i++)
            {
                assertTrue(expirationFlags[i].get());
            }
            assertEquals(0, challengesArchive.size());
        }

        @ParameterizedTest
        @ValueSource(ints = {10})
        public void testConcurrentChallengeCompletion(int tasksNum)
        {
            AtomicBoolean[] completionFlags = new AtomicBoolean[tasksNum];

            for (int i = 0; i < tasksNum; i++)
            {
                String username1 = UUID.randomUUID().toString();
                String username2 = UUID.randomUUID().toString();
                completionFlags[i] = new AtomicBoolean(false);

                int finalI = i;
                assertDoesNotThrow(() -> challengesManager.recordChallenge(username1, username2,
                        new ChallengeReportDelegation()
                        {
                            @Override
                            public void run()
                            {
                                completionFlags[finalI].set(true);
                            }
                        },
                        voidDelegation));

                pool.submit(() -> {
                    for (int i1 = 0; i1 < ServerConstants.CHALLENGE_WORDS_QUANTITY; i1++)
                    {
                        assertDoesNotThrow(() -> challengesManager.retrieveNextWord(username1));
                        assertDoesNotThrow(() -> challengesManager.retrieveNextWord(username2));
                        assertDoesNotThrow(() -> challengesManager.provideTranslation(username1, UUID.randomUUID().toString()));
                        assertDoesNotThrow(() -> challengesManager.provideTranslation(username2, UUID.randomUUID().toString()));
                    }
                });
            }
            timer.shutdownNow();
            pool.shutdown();
            assertDoesNotThrow(() -> pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS));

            for (int i = 0; i < tasksNum; i++)
            {
                assertTrue(completionFlags[i].get());
            }
        }
    }
}
