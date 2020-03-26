package server.challenges;

import org.json.simple.parser.ParseException;
import server.challenges.challege.Challenge;
import server.challenges.exceptions.*;
import server.challenges.reports.ChallengeReportDelegation;
import server.settings.ServerConstants;
import server.loggers.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.*;

public class ChallengesManager
{
    private static ConcurrentHashMap<String, Challenge> challengesArchive;
    private static ConcurrentHashMap<Challenge, ScheduledFuture<?>> timeOutsArchive;
    private static ScheduledThreadPoolExecutor timer;
    public static Logger translatorsLogger;
    public static Logger timerLogger;

    public static void setUp(Thread.UncaughtExceptionHandler errorsHandler) throws IOException, ParseException
    {
        // Initialize challenges archive
        challengesArchive = new ConcurrentHashMap<>(ServerConstants.CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE);
        // Initialize timeouts archive
        timeOutsArchive = new ConcurrentHashMap<>(128);
        // Initialize timer
        timer = new ScheduledThreadPoolExecutor(5);
        timer.setRemoveOnCancelPolicy(true);
        // Initialize translators logger
        translatorsLogger = new Logger("Translators");
        // Initialize timer logger
        timerLogger = new Logger("ChallengeTimers");
        // Setup challenges words from dictionary
        Challenge.setUp(errorsHandler);
    }

    public static void shutdown()
    {
        // Cancel all timeout
        timer.shutdownNow();
        timer.purge();
        // Cancel every translator
        Challenge.shutdown();
    }

    public static void checkEngagement(String from, String to) throws ApplicantEngagedInOtherChallengeException, ReceiverEngagedInOtherChallengeException
    {
        synchronized (ChallengesManager.class)
        {
            // Check if applicant is engaged in other challenge
            Challenge eventualPreviousChallengeFrom = challengesArchive.get(from);
            if (eventualPreviousChallengeFrom != null)
                throw new ApplicantEngagedInOtherChallengeException("USER \"" + from + "\" IS ENGAGED IN ANOTHER CHALLENGE");

            // Check if opponent is engaged in other challenge
            Challenge eventualPreviousChallengeTo = challengesArchive.get(to);
            if (eventualPreviousChallengeTo != null)
                throw new ReceiverEngagedInOtherChallengeException("USER \"" + to + "\" IS ENGAGED IN ANOTHER CHALLENGE");
        }
    }

    public static void recordChallenge(String from, String to, ChallengeReportDelegation completionOperation, ChallengeReportDelegation timeoutOperation) throws ApplicantEngagedInOtherChallengeException, ReceiverEngagedInOtherChallengeException
    {
        Challenge challenge = new Challenge(from, to,
                new ChallengeReportDelegation() // Completion operation
                {
                    @Override
                    public void run()
                    {
                        closeChallenge(from, to);
                        completionOperation.run();
                    }
                },
                new ChallengeReportDelegation() // Timeout operation
                {
                    @Override
                    public void run()
                    {
                        expireChallenge(from, to);
                        timeoutOperation.run();
                    }
                });

        synchronized (ChallengesManager.class)
        {
            // Store challenge with player 1
            Challenge previousChallengeFrom = challengesArchive.putIfAbsent(from, challenge);
            if (previousChallengeFrom != null)
            {
                throw new ApplicantEngagedInOtherChallengeException("USER \"" + from + "\" IS ENGAGED IN ANOTHER CHALLENGE");
            }

            // Store challenge with player 2
            Challenge previousChallengeTo = challengesArchive.putIfAbsent(to, challenge);
            if (previousChallengeTo != null)
            {
                challengesArchive.remove(from, challenge);
                throw new ReceiverEngagedInOtherChallengeException("USER \"" + to + "\" IS ENGAGED IN ANOTHER CHALLENGE");
            }

            // Start words translation for challenge
            challenge.startTranslations();

            // Schedule challenge timeout
            ScheduledFuture<?> scheduledFuture = timer.schedule(challenge, ServerConstants.CHALLENGE_DURATION_SECONDS, TimeUnit.SECONDS);
            timeOutsArchive.put(challenge, scheduledFuture);
            timerLogger.println("Challenge between \"" + from + "\" and \"" + to + "\" has been scheduled.");
        }
    }

    private static Challenge unregisterChallenge(String from, String to)
    {
        Challenge challengeFrom;
        Challenge challengeTo;

        synchronized (ChallengesManager.class)
        {
            // Remove entry for applicant user
            challengeFrom = challengesArchive.remove(from);
            if (challengeFrom == null)
                throw new Error("CHALLENGE SYSTEM INCONSISTENCY");

            // Remove entry for receiver user
            challengeTo = challengesArchive.remove(to);
            if (challengeTo == null)
                throw new Error("CHALLENGE SYSTEM INCONSISTENCY");

            // Check consistency
            if (challengeFrom != challengeTo)
                throw new Error("CHALLENGE SYSTEM INCONSISTENCY");

            // Stop eventual pending translations
            challengeFrom.stopTranslations();

            // Cancel the timeout related to the challenge
            ScheduledFuture<?> scheduledFuture = timeOutsArchive.remove(challengeFrom);
            scheduledFuture.cancel(true);
        }

        return challengeFrom;
    }

    public static void expireChallenge(String from, String to)
    {
        unregisterChallenge(from, to);
        timerLogger.println("Challenge between \"" + from + "\" and \"" + to + "\" has been expired.");
    }

    public static void closeChallenge(String from, String to)
    {
        unregisterChallenge(from, to);
        timerLogger.println("Challenge between \"" + from + "\" and \"" + to + "\" has been completed.");
    }

    public static String cancelChallenge(String username)
    {
        Challenge consequentialChallenge;
        Challenge challenge;

        synchronized (ChallengesManager.class)
        {
            // Remove eventual challenge related to given user from the archive
            challenge = challengesArchive.remove(username);
            if (challenge == null)
                return null;

            // Remove challenge request related to other user engaged from the archive
            if (username.equals(challenge.from))
                consequentialChallenge = challengesArchive.remove(challenge.to);
            else if (username.equals(challenge.to))
                consequentialChallenge = challengesArchive.remove(challenge.from);
            else
                throw new Error("CHALLENGES MANAGER INCONSISTENCY");

            // Check consistency
            if (consequentialChallenge == null)
                throw new Error("CHALLENGES MANAGER INCONSISTENCY");
            if (challenge != consequentialChallenge)
                throw new Error("CHALLENGES MANAGER INCONSISTENCY");

            // Stop eventual translators still running
            challenge.stopTranslations();

            // Cancel the timeout related to the challenge
            ScheduledFuture<?> scheduledFuture = timeOutsArchive.remove(challenge);
            scheduledFuture.cancel(true);
            timerLogger.println("Challenge between \"" + challenge.from + "\" and \"" + challenge.to + "\" has been canceled.");
        }

        // Return the username of the other user engaged in the challenge
        return username.equals(challenge.from) ? challenge.to : challenge.from;
    }

    public static String retrieveNextWord(String player) throws NoChallengeRelatedException, NoFurtherWordsToGetException, WordRetrievalOutOfSequenceException
    {
        Challenge challenge;
        String word;

        synchronized (ChallengesManager.class)
        {
            challenge = challengesArchive.get(player);
            if (challenge == null)
                throw new NoChallengeRelatedException("USER \"" + player + "\" IS NOT ENGAGED IN ANY CHALLENGE");

            word = challenge.getWord(player);
        }

        return word;
    }

    public static boolean provideTranslation(String player, String translation) throws NoChallengeRelatedException, TranslationProvisionOutOfSequenceException
    {
        Challenge challenge;
        Boolean correct;

        synchronized (ChallengesManager.class)
        {
            challenge = challengesArchive.get(player);
            if (challenge == null)
                throw new NoChallengeRelatedException("USER \"" + player + "\" IS NOT ENGAGED IN ANY CHALLENGE");

            correct = challenge.checkTranslation(player, translation);
        }

        return correct;
    }
}
