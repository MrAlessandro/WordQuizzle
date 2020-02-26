package server.constants;

import constants.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Random;

public class ServerConstants extends Constants
{
    public static final URL USERS_DATABASE_BACKUP_PATH = Objects.requireNonNull(ServerConstants.class.getClassLoader().getResource("server/resources/UsersDatabaseBackUp.json"));
    public static final URL DICTIONARY_PATH = Objects.requireNonNull(ServerConstants.class.getClassLoader().getResource("server/resources/Dictionary"));
    public static final URL SERVER_LOG_FILES_PATH = Objects.requireNonNull(ServerConstants.class.getClassLoader().getResource("server/resources/logs"));
    public static final String TRANSLATION_SERVICE_URL = "https://api.mymemory.translated.net";
    public static final int INITIAL_USERS_DATABASE_SIZE = 16384;
    public static final int CONNECTION_PORT = 50500;
    public static final int UDP_BASE_PORT = 60000;
    public static final String HOST_NAME = "localhost";
    public static final short CHALLENGE_WORDS_QUANTITY = 15;
    public static final int  CHALLENGE_DURATION_SECONDS = CHALLENGE_WORDS_QUANTITY * 4;
    public static final int DEPUTIES_POOL_SIZE = 10;
    public static final int BUFFERS_SIZE = 2048;
    public static final int CHALLENGE_REQUEST_TIMEOUT = 50000;
    public static final int RIGHT_TRANSLATION_SCORE = 5;
    public static final int WRONG_TRANSLATION_SCORE = -3;
    public static final int WINNER_EXTRA_SCORE = 5;
    public static final boolean LOG_FILES = false;

    public static URL urlFactory(String word)
    {
        URL generated;

        try
        {
            generated = new URL(TRANSLATION_SERVICE_URL + "/get?q=" + word + "&langpair=it|en&mt=0");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            throw new Error("Generating URL");
        }

        return generated;
    }

    public static String[] getBulkOfWords()
    {
        String[] bulk = new String[CHALLENGE_WORDS_QUANTITY];

        try
        {
            // Initialize randomizer
            Random randomizer = new Random();

            // Open dictionary
            RandomAccessFile dictionary = new RandomAccessFile(ServerConstants.DICTIONARY_PATH.getPath(), "r");

            // Initialize words to translate relative to this challenge;
            int index = 0;
            while (index < bulk.length)
            {
                // Generate a random position in the dictionary
                long seek = Math.abs(randomizer.nextLong() % dictionary.length());
                // Set the random position
                dictionary.seek(seek);

                // Read the wasted line
                dictionary.readLine();
                // Read the good line
                String word = dictionary.readLine();

                // Check if end-of-file has been encountered
                if (word != null)
                // Word usable
                    bulk[index++] = word;
            }

            // Stop reading dictionary
            dictionary.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            throw new Error("Dictionary not found");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Reading dictionary");
        }

        return bulk;
    }

    public static String[][] translateBulkOfWords(String[] toTranslate)
    {
        String[][] translated = new String[ServerConstants.CHALLENGE_WORDS_QUANTITY][];
        HttpURLConnection connection;
        BufferedReader reader = null;
        URL url;

        try
        {
            for (int i=0; i < toTranslate.length; i++)
            {
                // Generate URL and send request
                url = ServerConstants.urlFactory(toTranslate[i]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);

                // Initialize reader
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Get response body
                String inputLine = reader.readLine();

                // Deserialize response and get translations
                JSONObject body = (JSONObject) JSONValue.parse(inputLine);
                JSONArray matches = (JSONArray) body.get("matches");
                translated[i] = new String[matches.size()];
                int j = 0;
                for (JSONObject match : (Iterable<JSONObject>) matches)
                {
                    String translation = (String) match.get("translation");
                    translation = translation.replaceAll("[^a-zA-Z ]", "");
                    translation = translation.toLowerCase();
                    translated[i][j] = translation;
                }
            }

            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Getting translations");
        }

        return translated;
    }
}
