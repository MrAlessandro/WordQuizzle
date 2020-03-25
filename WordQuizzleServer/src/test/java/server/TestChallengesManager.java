package server;

import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.challenges.ChallengesManager;
import server.challenges.challege.Challenge;
import server.challenges.exceptions.ApplicantEngagedInOtherChallengeException;
import server.challenges.exceptions.ReceiverEngagedInOtherChallengeException;
import server.challenges.reports.ChallengeReportDelegation;
import server.settings.ServerConstants;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class TestChallengesManager
{
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
            ChallengesManager.setUp((t, e) -> e.printStackTrace());
            Field timerField = ChallengesManager.class.getDeclaredField("timer");
            Field archiveField = ChallengesManager.class.getDeclaredField("challengesArchive");
            timerField.setAccessible(true);
            archiveField.setAccessible(true);
            challengesArchive = (ConcurrentHashMap<String, Challenge>) archiveField.get(null);
            timer = (ScheduledThreadPoolExecutor) timerField.get(null);
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
    public void testChallengeRecording()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> ChallengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
    }

    @Test
    public void testChallengeRecordingApplicantEngaged()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        String username3 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> ChallengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
        assertThrows(ApplicantEngagedInOtherChallengeException.class, () -> ChallengesManager.recordChallenge(username1, username3, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
    }

    @Test
    public void testChallengeRecordingReceiverEngaged()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        String username3 = UUID.randomUUID().toString();

        assertDoesNotThrow(() -> ChallengesManager.recordChallenge(username1, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
        assertThrows(ReceiverEngagedInOtherChallengeException.class, () -> ChallengesManager.recordChallenge(username3, username2, voidDelegation, voidDelegation));
        assertEquals(2, challengesArchive.size());
    }

    @Test
    public void testChallengeExpiration()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        AtomicBoolean expirationFlag = new AtomicBoolean(false);

        assertDoesNotThrow(() -> ChallengesManager.recordChallenge(username1, username2, voidDelegation, new ChallengeReportDelegation()
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
        assertEquals(0, challengesArchive.size());
    }

    @Test
    public void testChallengeCompletion()
    {
        String username1 = UUID.randomUUID().toString();
        String username2 = UUID.randomUUID().toString();
        AtomicBoolean completionFlag = new AtomicBoolean(false);

        assertDoesNotThrow(() -> ChallengesManager.recordChallenge(username1, username2,
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
        /* TODO: complete this test */
    }
}
