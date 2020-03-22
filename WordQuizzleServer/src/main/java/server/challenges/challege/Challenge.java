package server.challenges.challege;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.challenges.ChallengesManager;
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

public class Challenge extends TimerTask
{
    // Translator threads pool
    private static final ExecutorService TRANSLATORS = Executors.newCachedThreadPool();
    // Randomizer
    private static final Random RANDOMIZER = new Random();

    // Dictionary
    private static String[] dictionary;

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

    public static void setUp() throws IOException, ParseException
    {
        String JSONdictionary = new String(Files.readAllBytes(Paths.get(ServerConstants.DICTIONARY_PATH)));
        JSONParser parser = new JSONParser();
        JSONArray words = (JSONArray) parser.parse(JSONdictionary);

        dictionary = new String[words.size()];

        int i = 0;
        for (String word : (Iterable<String>) words)
        {
            dictionary[i++] = word;
        }
    }

    public static void shutdown()
    {
        TRANSLATORS.shutdownNow();
    }

    public Challenge(String from, String to)
    {
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
            int randomIndex = RANDOMIZER.nextInt() % dictionary.length;
            this.words[i] = dictionary[randomIndex];
        }

        // Initialize translations storage for future filling
        this.translations = new Future[ServerConstants.CHALLENGE_WORDS_QUANTITY];
    }

    public void startTranslations()
    {
        for (int i = 0; i < ServerConstants.CHALLENGE_WORDS_QUANTITY; i++)
        {
            this.translations[i] = TRANSLATORS.submit(new Translator(this.words[i]));
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
        ChallengesManager.expireChallenge(this.from, this.to);
    }

    /* TODO: Termination */
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

    /* TODO: Termination */
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
            else
            {
                // Check translation status consistency respect to the words' status for player
                if (this.toTranslationsProgress != this.toWordsProgress - 1)
                    throw new TranslationProvisionOutOfSequenceException("USER \"" + player + "\" HAS TO GET A WORD BEFORE TO PROVIDE A NEW TRANSLATION");

                // Get translations alternatives for actual word related to given player status
                alternativeTranslations = this.translations[++this.toTranslationsProgress].get();

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

        return checked;
    }

    public boolean isCompleted()
    {
        return this.fromWordsProgress == ServerConstants.CHALLENGE_WORDS_QUANTITY - 1 && this.toWordsProgress == ServerConstants.CHALLENGE_WORDS_QUANTITY - 1;
    }



    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (!(o instanceof Challenge))
            return false;

        Challenge challenge = (Challenge) o;
        return from.equals(challenge.from) && to.equals(challenge.to);
    }
}
