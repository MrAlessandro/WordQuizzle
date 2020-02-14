package server.challenges;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.challenges.challenge.Challenge;
import server.challenges.timers.RequestTimeOut;
import server.constants.ServerConstants;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class ChallengesManager
{
    private static final ChallengesManager INSTANCE = new ChallengesManager();
    private static final Set<RequestTimeOut> TIMEOUT_COLLECTION = ConcurrentHashMap.newKeySet();
    private static final Set<Challenge> CHALLENGES_ARCHIVE = ConcurrentHashMap.newKeySet();
    private static final Timer TIMER = new Timer();

    private ChallengesManager()
    {}

    public static void registerChallenge(String applicant, String opponent)
    {
        String[] toTranslate = new String[ServerConstants.CHALLENGE_WORDS_QUANTITY];
        String[] translated = new String[ServerConstants.CHALLENGE_WORDS_QUANTITY];
        HttpURLConnection connection;
        BufferedReader reader = null;
        URL url;

        // Get italians words from dictionary
        try
        {
            // Initialize randomizer
            Random randomizer = new Random();

            // Open dictionary
            RandomAccessFile dictionary = new RandomAccessFile(ServerConstants.DICTIONARY_PATH.toString(), "r");

            // Initialize words to translate relative to this challenge;
            int index = 0;
            while (index < toTranslate.length)
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
                {
                    // Word usable
                    toTranslate[index] = word;

                    index++;
                }
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

        // Translate got words
        try
        {
            for (int i=0; i < toTranslate.length; i++)
            {
                // Generate URL and send request
                url = ServerConstants.urlFactory(toTranslate[i]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);

                // Analyze response code
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode > 299)
                {
                    throw new IOException();
                }

                // Initialize reader
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Get response body
                StringBuilder builder = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    builder.append(inputLine);

                // Deserialize response and get translation
                JSONObject body = (JSONObject) JSONValue.parse(builder.toString());
                translated[i] = (String) ((JSONObject)body.get("responseData")).get("translatedText");
            }

            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Getting translations");
        }

        boolean check = CHALLENGES_ARCHIVE.add(new Challenge(applicant, opponent, toTranslate, translated));
        if (!check)
            throw new Error("Challenge registry inconsistency");
    }

    public static void scheduleRequestTimeOut(String requestFrom, String requestTo, Selector toWake)
    {
        RequestTimeOut timeOut = new RequestTimeOut(requestFrom, requestTo, toWake);

        boolean check = TIMEOUT_COLLECTION.add(timeOut);
        if (!check)
            throw new Error("Timers storing inconsistency");

        TIMER.schedule(timeOut, ServerConstants.CHALLENGE_REQUEST_TIMEOUT);
    }

    public static void quitScheduledRequestTimeOut(String requestFrom, String requestTo)
    {
        boolean check = false;

        Iterator<RequestTimeOut> iterator = TIMEOUT_COLLECTION.iterator();
        while (iterator.hasNext())
        {
            RequestTimeOut current = iterator.next();
            if (current.isRelativeTo(requestFrom, requestTo))
            {
                check = true;
                current.cancel();
                iterator.remove();
            }
        }

        if (!check)
            throw new Error("Timers storing inconsistency");
    }

    public static void dequeueScheduledRequestTimeOut(String requestFrom, String requestTo)
    {
        boolean check = false;

        Iterator<RequestTimeOut> iterator = TIMEOUT_COLLECTION.iterator();
        while (iterator.hasNext())
        {
            RequestTimeOut current = iterator.next();
            if (current.isRelativeTo(requestFrom, requestTo))
            {
                check = true;
                iterator.remove();
            }
        }

        if (!check)
            throw new Error("Timers storing inconsistency");
    }

    public static String[] getWordsTranslation(String[] toTranslate)
    {
        String[] translated = new String[toTranslate.length];
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


                // Analyze response code
                int responseCode = connection.getResponseCode();
                if (responseCode < 200 || responseCode > 299)
                {
                    throw new IOException();
                }

                // Initialize reader
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Get response body
                StringBuilder builder = new StringBuilder();
                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    builder.append(inputLine);

                // Deserialize response and get translation
                JSONObject body = (JSONObject) JSONValue.parse(builder.toString());
                translated[i] = (String) ((JSONObject)body.get("responseData")).get("translatedText");
            }

            if (reader != null)
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
