package server.challenges;

import commons.messages.Message;
import commons.messages.MessageType;
import org.json.simple.parser.ParseException;
import server.challenges.challege.Challenge;
import server.challenges.exceptions.*;
import server.settings.ServerConstants;
import server.loggers.Logger;
import server.sessions.SessionsManager;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.*;

public class ChallengesManager {
    private static final Timer TIMER = new Timer();

    private static ConcurrentHashMap<String, Challenge> challengesArchive;
    public static Logger translatorsLogger;
    public static Logger timerLogger;

    public static void setUp() throws IOException, ParseException
    {
        // Initialize challenges archive
        challengesArchive = new ConcurrentHashMap<>(ServerConstants.CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE);
        // Initialize translators logger
        translatorsLogger = new Logger("Translators");
        // Initialize timer logger
        timerLogger = new Logger("ChallengesTimer");
        // Setup challenges words from dictionary
        Challenge.setUp();
    }

    public static void shutdown()
    {
        // Cancel all timeout
        TIMER.cancel();
        TIMER.purge();
        // Cancel every translator
        Challenge.shutdown();
    }

    public static void checkEngagement(String from, String to) throws ApplicantEngagedInOtherChallengeException, ReceiverEngagedInOtherChallengeException
    {
        synchronized (ChallengesManager.class) {
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

    public static void recordChallenge(String from, String to) throws ApplicantEngagedInOtherChallengeException, ReceiverEngagedInOtherChallengeException
    {
        Challenge challenge = new Challenge(from, to);

        synchronized (ChallengesManager.class) {
            // Store challenge with player 1
            Challenge previousChallengeFrom = challengesArchive.putIfAbsent(from, challenge);
            if (previousChallengeFrom != null) {
                challenge.stopTranslations();
                throw new ApplicantEngagedInOtherChallengeException("USER \"" + from + "\" IS ENGAGED IN ANOTHER CHALLENGE");
            }

            // Store challenge with player 2
            Challenge previousChallengeTo = challengesArchive.putIfAbsent(to, challenge);
            if (previousChallengeTo != null) {
                challenge.stopTranslations();
                challengesArchive.remove(from, challenge);
                throw new ReceiverEngagedInOtherChallengeException("USER \"" + to + "\" IS ENGAGED IN ANOTHER CHALLENGE");
            }

            // Start words translation for challenge
            challenge.startTranslations();
        }

        // Schedule challenge timeout
        TIMER.schedule(challenge, ServerConstants.CHALLENGE_DURATION_SECONDS * 1000);
    }

    public static String discardChallengeIfPresent(String username)
    {
        Challenge consequentialChallenge;
        Challenge challenge;

        synchronized (ChallengesManager.class) {
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
        }

        // Cancel the timeout related to the request
        challenge.cancel();
        // Clean timer for canceled tasks
        TIMER.purge();
        timerLogger.println("Challenge between \"" + challenge.from + "\" and \"" + challenge.to + "\" expired.");

        // Return the username of the other user engaged in the challenge
        return username.equals(challenge.from) ? challenge.to : challenge.from;
    }

    public static void expireChallenge(String from, String to)
    {
        Challenge challengeFrom;
        Challenge challengeTo;

        synchronized (ChallengesManager.class) {
            // Remove challenge related to applicant user from the archive
            challengeFrom = challengesArchive.remove(from);
            if (challengeFrom == null)
                throw new Error("CHALLENGES MANAGER INCONSISTENCY");

            // Remove challenge related to receiver user from the archive
            challengeTo = challengesArchive.remove(to);
            if (challengeTo == null)
                throw new Error("CHALLENGES MANAGER INCONSISTENCY");

            // Requests removed must be the same
            if (challengeFrom != challengeTo)
                throw new Error("CHALLENGES MANAGER INCONSISTENCY");

            // Stop eventual translators still running
            challengeFrom.stopTranslations();
        }

        // Cancel the timeout related to the request
        challengeFrom.cancel();
        // Clean timer for canceled tasks
        TIMER.purge();
        timerLogger.println("Challenge between \"" + from + "\" and \"" + to + "\" expired.");

        // Send expiration challenge request message to both applicant and receiver
        SessionsManager.sendMessage(from, new Message(MessageType.CHALLENGE_EXPIRED));
        SessionsManager.sendMessage(to, new Message(MessageType.CHALLENGE_EXPIRED));
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

            if (challenge.isCompleted())
            {
                /*TODO*/
            }
        }

        return correct;
    }
}
