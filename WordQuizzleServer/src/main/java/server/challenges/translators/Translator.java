package server.challenges.translators;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import server.challenges.ChallengesManager;
import server.settings.ServerConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

public class Translator implements Callable<String[]>
{
    private String word;

    public Translator(String word)
    {
        this.word = word;
    }

    @Override
    public String[] call()
    {
        HttpURLConnection connection;
        BufferedReader reader;
        String[] translated;
        URL url = null;

        // Generate URL
        try
        {
            url = new URL(ServerConstants.TRANSLATION_SERVICE_URL + "/get?q=" + word + "&langpair=it|en&mt=0");
        }
        catch (MalformedURLException e)
        {
            ChallengesManager.translatorsLogger.printlnRed("ERROR GENERATING URL");
            throw new Error("ERROR GENERATING URL");
        }

        try
        {
            // Open HTTP connection
            connection = (HttpURLConnection) url.openConnection();

            // Set up HTTP connection
            connection.setRequestMethod("GET");
            connection.setDoOutput(false);
            connection.setDoInput(true);

            // Initialize reader
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            // Get response body
            String inputLine = reader.readLine();

            // Deserialize response
            JSONObject body = (JSONObject) JSONValue.parse(inputLine);
            JSONArray matches = (JSONArray) body.get("matches");
            translated = new String[matches.size()];

            // Get translations
            int j = 0;
            for (JSONObject match : (Iterable<JSONObject>) matches)
            {
                String translation = (String) match.get("translation");
                translation = translation.replaceAll("[^a-zA-Z ]", "");
                translation = translation.toLowerCase();
                translated[j++] = translation;
            }

            // Close reader
            reader.close();
        }
        catch (IOException e)
        {
            ChallengesManager.translatorsLogger.printlnRed("ERROR GETTING TRANSLATIONS FROM " + url);
            throw new Error("ERROR GETTING TRANSLATIONS FROM " + url);
        }

        // Return translations for given word
        return translated;
    }
}
