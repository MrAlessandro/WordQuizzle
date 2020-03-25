package server.challenges.challege;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.challenges.reports.ChallengeReport;
import server.challenges.reports.ChallengeReportDelegation;
import server.challenges.exceptions.NoFurtherWordsToGetException;
import server.challenges.exceptions.TranslationProvisionOutOfSequenceException;
import server.challenges.exceptions.WordRetrievalOutOfSequenceException;
import server.challenges.translators.Translator;
import server.settings.ServerConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.*;

public class Challenge implements Runnable
{
    // Translator threads pool
    private static ExecutorService translators = Executors.newCachedThreadPool();

    // Randomizer
    private static final Random RANDOMIZER = new Random();

    // Dictionary
    private static String[] dictionary;

    // Timeout operation
    private ChallengeReportDelegation timeoutOperation;

    // completion operation
    private ChallengeReportDelegation completionOperation;

    // Players username
    public String from;
    public String to;

    // Words and translations
    private String[] words;
    private Future<String[]>[] translations;

    // Players words statuses
    private int fromWordsProgress;
    private int toWordsProgress;

    // Players translations statuses
    private int fromTranslationsProgress;
    private int toTranslationsProgress;

    // Players scores
    private int fromScore;
    private int toScore;

    public static void setUp(Thread.UncaughtExceptionHandler exceptionHandler) throws IOException, ParseException
    {
        // Read dictionary
        String JSONdictionary = new String(Files.readAllBytes(Paths.get(ServerConstants.DICTIONARY_PATH)));
        JSONParser parser = new JSONParser();
        JSONArray words = (JSONArray) parser.parse(JSONdictionary);

        dictionary = new String[words.size()];

        int i = 0;
        for (String word : (Iterable<String>) words)
        {
            dictionary[i++] = word;
        }

        // Setup translators pool
        translators = Executors.newCachedThreadPool(r -> {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler(exceptionHandler);
            return thread;
        });
    }

    public static void shutdown()
    {
        translators.shutdownNow();
    }

    public Challenge(String from, String to, ChallengeReportDelegation completionOperation, ChallengeReportDelegation timeoutOperation)
    {
        // Set timeout operation
        this.timeoutOperation = timeoutOperation;

        // Set completion operation
        this.completionOperation = completionOperation;

        // Set players
        this.from = from;
        this.to = to;

        // Initialize progresses
        this.fromWordsProgress = -1;
        this.toWordsProgress = -1;
        this.fromTranslationsProgress = -1;
        this.toTranslationsProgress = -1;

        // Initialize scores
        this.fromScore = 0;
        this.toScore = 0;

        // Initialize words getting them from the deserialized dictionary.json
        this.words = new String[ServerConstants.CHALLENGE_WORDS_QUANTITY];
        for (int i = 0; i < this.words.length; i++)
        {
            int randomIndex = Math.abs(RANDOMIZER.nextInt() % dictionary.length);
            this.words[i] = dictionary[randomIndex];
        }

        // Initialize translations storage for future filling
        this.translations = new Future[ServerConstants.CHALLENGE_WORDS_QUANTITY];
    }

    public void startTranslations()
    {
        for (int i = 0; i < ServerConstants.CHALLENGE_WORDS_QUANTITY; i++)
        {
            this.translations[i] = translators.submit(new Translator(this.words[i]));
        }
    }

    public void stopTranslations()
    {
        for (int i = 0; i < ServerConstants.CHALLENGE_WORDS_QUANTITY; i++)
        {
            this.translations[i].cancel(true);
        }
    }

    @Override
    public void run()
    {
        stopTranslations();

        int fromStatus;
        int toStatus;
        if (this.fromScore > this.toScore)
        {
            fromStatus = 1;
            toStatus = -1;
        }
        else if (this.fromScore < this.toScore)
        {
            fromStatus = -1;
            toStatus = 1;
        }
        else
        {
            fromStatus = 0;
            toStatus = 0;
        }

        this.timeoutOperation.setFromChallengeReport(new ChallengeReport(this.from, fromStatus,this.fromTranslationsProgress, this.fromScore));
        this.timeoutOperation.setToChallengeReport(new ChallengeReport(this.to, toStatus, this.toTranslationsProgress, this.toScore));
        this.timeoutOperation.run();
    }

    public void complete()
    {
        stopTranslations();

        int fromStatus;
        int toStatus;
        if (this.fromScore > this.toScore)
        {
            fromStatus = 1;
            toStatus = -1;
        }
        else if (this.fromScore < this.toScore)
        {
            fromStatus = -1;
            toStatus = 1;
        }
        else
        {
            fromStatus = 0;
            toStatus = 0;
        }

        this.completionOperation.setFromChallengeReport(new ChallengeReport(this.from, fromStatus,this.fromTranslationsProgress, this.fromScore));
        this.completionOperation.setToChallengeReport(new ChallengeReport(this.to, toStatus, this.toTranslationsProgress, this.toScore));
        this.completionOperation.run();
    }

    public String getWord(String player) throws NoFurtherWordsToGetException, WordRetrievalOutOfSequenceException
    {
        if (player.equals(this.from))
        {
            // Check if player has already reached the end of the challenge
            if (this.fromWordsProgress >= this.words.length - 1)
                throw new NoFurtherWordsToGetException("USER \"" + player + "\" HAS NO FURTHER WORDS TO GET IN THIS CHALLENGE");

            if (this.fromWordsProgress != this.fromTranslationsProgress)
                throw new WordRetrievalOutOfSequenceException("USER \"" + player + "\" HAS TO PROVIDE A TRANSLATION BEFORE TO GET A NEW WORD");
            else
                return this.words[++this.fromWordsProgress];
        }
        else
        {
            // Check if player has already reached the end of the challenge
            if (this.toWordsProgress >= this.words.length - 1)
                throw new NoFurtherWordsToGetException("USER \"" + player + "\" HAS NO FURTHER WORDS TO GET IN THIS CHALLENGE");

            if (this.toWordsProgress != this.toTranslationsProgress)
                throw new WordRetrievalOutOfSequenceException("USER \"" + player + "\" HAS TO PROVIDE A TRANSLATION BEFORE TO GET A NEW WORD");
            else
                return this.words[++this.toWordsProgress];
        }
    }

    public Boolean checkTranslation(String player, String translation) throws TranslationProvisionOutOfSequenceException
    {
        String[] alternativeTranslations;
        boolean checked = false;
        int index = 0;

        try
        {
            // Select player
            if (player.equals(this.from))
            {
                // Check translation status consistency respect to the words' status for player
                if (this.fromTranslationsProgress != this.fromWordsProgress - 1)
                    throw new TranslationProvisionOutOfSequenceException("USER \"" + player + "\" HAS TO GET A WORD BEFORE TO PROVIDE A NEW TRANSLATION");

                // Get translations alternatives for actual word related to given player status
                alternativeTranslations = this.translations[++this.fromTranslationsProgress].get();

                // If the given translation is void then skip -> sore gain is 0
                if (!translation.equals(""))
                {
                    // Compare the translations alternatives with the given translation
                    while (!checked && index < alternativeTranslations.length)
                    {
                        if (translation.equals(alternativeTranslations[index++]))
                            checked = true;
                    }

                    if (checked)
                        this.fromScore += ServerConstants.CHALLENGE_RIGHT_TRANSLATION_SCORE;
                    else
                        this.fromScore += ServerConstants.CHALLENGE_WRONG_TRANSLATION_SCORE;
                }
            }
            else
            {
                // Check translation status consistency respect to the words' status for player
                if (this.toTranslationsProgress != this.toWordsProgress - 1)
                    throw new TranslationProvisionOutOfSequenceException("USER \"" + player + "\" HAS TO GET A WORD BEFORE TO PROVIDE A NEW TRANSLATION");

                // Get translations alternatives for actual word related to given player status
                alternativeTranslations = this.translations[++this.toTranslationsProgress].get();

                // If the given translation is void then skip -> sore gain is 0
                if (!translation.equals(""))
                {
                    // Compare the translations alternatives with the given translation
                    while (!checked && index < alternativeTranslations.length)
                    {
                        if (translation.equals(alternativeTranslations[index++]))
                            checked = true;
                    }

                    if (checked)
                        this.toScore += ServerConstants.CHALLENGE_RIGHT_TRANSLATION_SCORE;
                    else
                        this.toScore += ServerConstants.CHALLENGE_WRONG_TRANSLATION_SCORE;
                }
            }
        }
        catch (CancellationException e)
        {// Translator thread has been canceled
            throw new Error("UNEXPECTED TRANSLATOR CANCELING");
        }
        catch (InterruptedException e)
        {// Current thread has been interrupted getting the translation
            throw new Error("UNEXPECTED INTERRUPTION");
        }
        catch (ExecutionException e)
        {// Translation retrieving thrown an exception
            throw new Error("TRANSLATION RETRIEVAL ERROR");
        }

        // Check if challenge is completed
        if (this.fromTranslationsProgress == ServerConstants.CHALLENGE_WORDS_QUANTITY - 1 && this.toTranslationsProgress == ServerConstants.CHALLENGE_WORDS_QUANTITY - 1)
            this.complete();

        return checked;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Challenge))
            return false;

        Challenge challenge = (Challenge) o;
        return this.from.equals(challenge.from) && this.to.equals(challenge.to);
    }
}
