package server.challenges;

import commons.messages.exceptions.UnexpectedMessageException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.challenges.challege.Challenge;
import server.challenges.exceptions.*;
import server.challenges.reports.ChallengeReport;
import server.challenges.reports.ChallengeReportDelegation;
import server.challenges.translators.Translator;
import server.settings.Settings;
import commons.loggers.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ChallengesManager
{
    // Challenges archive
    private ConcurrentHashMap<String, Challenge> challengesArchive;

    // Archive for timeouts registering
    private ConcurrentHashMap<Challenge, ScheduledFuture<?>> timeOutsArchive;

    // Thread pool for timing purpose
    private ScheduledThreadPoolExecutor timer;

    // Loggers for translators and timeouts
    public Logger translatorsLogger;
    public Logger timerLogger;

    // Dictionary
    private String[] dictionary;

    // Randomizer
    private Random randomizer;

    // Translator threads pool
    private ExecutorService translators;

    public ChallengesManager(Thread.UncaughtExceptionHandler errorsHandler)
    {
        // Initialize challenges archive
        this.challengesArchive = new ConcurrentHashMap<>(Settings.CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE);
        // Initialize timeouts archive
        this.timeOutsArchive = new ConcurrentHashMap<>(128);
        // Initialize timer
        this.timer = new ScheduledThreadPoolExecutor(5);
        this.timer.setRemoveOnCancelPolicy(true);

        try
        {
            if (Settings.LOG_FILES)
            {
                // Initialize translators logger with related log file
                this.translatorsLogger = new Logger(Settings.COLORED_LOGS, "Translators", Settings.LOG_FILES_DIR_PATH);
                // Initialize timer logger  with related log file
                this.timerLogger = new Logger(Settings.COLORED_LOGS, "ChallengeTimers", Settings.LOG_FILES_DIR_PATH);
            }
            else
            {
                // Initialize translators logger
                this.translatorsLogger = new Logger(Settings.COLORED_LOGS, Settings.DEBUG);
                // Initialize timer logger
                this.timerLogger = new Logger(Settings.COLORED_LOGS, Settings.DEBUG);
            }
        }
        catch (IOException e)
        {
            throw new Error("ERROR CREATING LOGGERS", e);
        }

        // Setup challenges words from dictionary
        try
        {
            File dictionaryFile = new File(Settings.DICTIONARY_URL.toURI());
            String JSONdictionary = new String(Files.readAllBytes(dictionaryFile.toPath()));
            JSONParser parser = new JSONParser();
            JSONArray words = (JSONArray) parser.parse(JSONdictionary);
            this.dictionary = new String[words.size()];
            int i = 0;
            for (String word : (Iterable<String>) words)
            {
                dictionary[i++] = word;
            }
        }
        catch (NoSuchFileException | URISyntaxException e)
        {
            throw new Error("DICTIONARY FILE NOT FOUND", e);
        }
        catch (ParseException e)
        {
            throw new Error("ERROR PARSING DICTIONARY", e);
        }
        catch (IOException e)
        {
            throw new Error("ERROR READING DICTIONARY", e);
        }


        // Setup randomizer
        this.randomizer = new Random();

        // Setup translators pool with proper error management
        this.translators = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler(errorsHandler);
            return thread;
        });
    }

    public void shutdown()
    {
        // Cancel all timeout
        this.timer.shutdownNow();
        this.timer.purge();
    }

    public void checkEngagement(String from, String to) throws ApplicantEngagedInOtherChallengeException, ReceiverEngagedInOtherChallengeException
    {
        synchronized (ChallengesManager.class)
        {
            // Check if applicant is engaged in other challenge
            Challenge eventualPreviousChallengeFrom = this.challengesArchive.get(from);
            if (eventualPreviousChallengeFrom != null)
                throw new ApplicantEngagedInOtherChallengeException("USER \"" + from + "\" IS ENGAGED IN ANOTHER CHALLENGE");

            // Check if opponent is engaged in other challenge
            Challenge eventualPreviousChallengeTo = this.challengesArchive.get(to);
            if (eventualPreviousChallengeTo != null)
                throw new ReceiverEngagedInOtherChallengeException("USER \"" + to + "\" IS ENGAGED IN ANOTHER CHALLENGE");
        }
    }

    public void recordChallenge(String from, String to, Consumer<ChallengeReportDelegation> completionOperation, Consumer<ChallengeReportDelegation> timeoutOperation) throws ApplicantEngagedInOtherChallengeException, ReceiverEngagedInOtherChallengeException
    {
        // Retrieve bulk of word associating to this challenge
        String[] words = new String[Settings.CHALLENGE_WORDS_QUANTITY];
        for (int i = 0; i < words.length; i++)
        {
            int randomIndex = Math.abs(randomizer.nextInt() % dictionary.length);
            words[i] = dictionary[randomIndex];
        }

        // Initialize challenge
        // Completion operation
        // Timeout operation
        Challenge challenge = new Challenge(from, to, words,
                challengeReportDelegation -> {
                    closeChallenge(from, to);
                    completionOperation.accept(challengeReportDelegation);
                },
                challengeReportDelegation -> {
                    expireChallenge(from, to);
                    timeoutOperation.accept(challengeReportDelegation);
                });

        synchronized (ChallengesManager.class)
        {
            // Store challenge for player 1
            Challenge previousChallengeFrom = this.challengesArchive.putIfAbsent(from, challenge);
            if (previousChallengeFrom != null)
            {
                throw new ApplicantEngagedInOtherChallengeException("USER \"" + from + "\" IS ENGAGED IN ANOTHER CHALLENGE");
            }

            // Store challenge for player 2
            Challenge previousChallengeTo = this.challengesArchive.putIfAbsent(to, challenge);
            if (previousChallengeTo != null)
            {
                this.challengesArchive.remove(from, challenge);
                throw new ReceiverEngagedInOtherChallengeException("USER \"" + to + "\" IS ENGAGED IN ANOTHER CHALLENGE");
            }

            // Start words translation for challenge
            Future<String[]>[] translations = new Future[Settings.CHALLENGE_WORDS_QUANTITY];
            for (int i = 0; i < Settings.CHALLENGE_WORDS_QUANTITY; i++)
            {
                translations[i] = this.translators.submit(new Translator(this.translatorsLogger, words[i]));
            }
            challenge.setTranslations(translations);

            // Schedule challenge timeout
            ScheduledFuture<?> scheduledFuture = this.timer.schedule(challenge, Settings.CHALLENGE_DURATION_SECONDS, TimeUnit.SECONDS);
            this.timeOutsArchive.put(challenge, scheduledFuture);
            this.timerLogger.println("Challenge between \"" + from + "\" and \"" + to + "\" has been scheduled.");
        }
    }

    // Utility method for remove challenge from system
    private void unregisterChallenge(String from, String to)
    {
        Challenge challengeFrom;
        Challenge challengeTo;

        synchronized (ChallengesManager.class)
        {
            // Remove entry for applicant user
            challengeFrom = this.challengesArchive.remove(from);
            if (challengeFrom == null)
                throw new Error("CHALLENGE SYSTEM INCONSISTENCY");

            // Remove entry for receiver user
            challengeTo = this.challengesArchive.remove(to);
            if (challengeTo == null)
                throw new Error("CHALLENGE SYSTEM INCONSISTENCY");

            // Check consistency
            if (challengeFrom != challengeTo)
                throw new Error("CHALLENGE SYSTEM INCONSISTENCY");

            // Stop eventual pending translations
            challengeFrom.stopTranslations();

            // Cancel the timeout related to the challenge
            ScheduledFuture<?> scheduledFuture = this.timeOutsArchive.remove(challengeFrom);
            scheduledFuture.cancel(true);
        }
    }

    // Called when challenge expire
    public void expireChallenge(String from, String to)
    {
        unregisterChallenge(from, to);
        this.timerLogger.println("Challenge between \"" + from + "\" and \"" + to + "\" has been expired.");
    }

    // Called when challenge is completed
    public void closeChallenge(String from, String to)
    {
        unregisterChallenge(from, to);
        this.timerLogger.println("Challenge between \"" + from + "\" and \"" + to + "\" has been completed.");
    }

    // Called when a player logout
    public ChallengeReport[] cancelChallenge(String username)
    {
        Challenge consequentialChallenge;
        Challenge challenge;
        ChallengeReport report;

        synchronized (ChallengesManager.class)
        {
            // Remove eventual challenge related to given user from the archive
            challenge = this.challengesArchive.remove(username);
            if (challenge == null)
                return null;

            // Remove challenge request related to other user engaged from the archive
            if (username.equals(challenge.from))
                consequentialChallenge = this.challengesArchive.remove(challenge.to);
            else if (username.equals(challenge.to))
                consequentialChallenge = this.challengesArchive.remove(challenge.from);
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
            ScheduledFuture<?> scheduledFuture = this.timeOutsArchive.remove(challenge);
            scheduledFuture.cancel(true);
            this.timerLogger.println("Challenge between \"" + challenge.from + "\" and \"" + challenge.to + "\" has been canceled.");
        }

        // Return challenge's reports
        return challenge.getReports();
    }

    public String retrieveNextWord(String player) throws NoChallengeRelatedException, NoFurtherWordsToGetException, WordRetrievalOutOfSequenceException
    {
        Challenge challenge;
        String word;

        synchronized (ChallengesManager.class)
        {
            challenge = this.challengesArchive.get(player);
            if (challenge == null)
                throw new NoChallengeRelatedException("USER \"" + player + "\" IS NOT ENGAGED IN ANY CHALLENGE");

            word = challenge.getWord(player);
        }

        return word;
    }

    public boolean provideTranslation(String player, String translation) throws NoChallengeRelatedException, TranslationProvisionOutOfSequenceException, UnexpectedMessageException
    {
        Challenge challenge;
        Boolean correct;

        synchronized (ChallengesManager.class)
        {
            challenge = this.challengesArchive.get(player);
            if (challenge == null)
                throw new NoChallengeRelatedException("USER \"" + player + "\" IS NOT ENGAGED IN ANY CHALLENGE");

            correct = challenge.checkTranslation(player, translation);
        }

        return correct;
    }
}
