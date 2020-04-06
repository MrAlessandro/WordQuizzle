package server.challenges.translators;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import commons.loggers.Logger;
import server.settings.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class Translator implements Callable<String[]>
{
    private Logger translatorsLogger;
    private String word;

    public Translator(Logger translatorsLogger, String word)
    {
        this.translatorsLogger = translatorsLogger;
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
            url = new URL(Settings.TRANSLATION_SERVICE_URL + "/get?q=" + word + "&langpair=it|en&mt=0");
        }
        catch (MalformedURLException e)
        {
            this.translatorsLogger.printlnRed("ERROR GENERATING URL");
            throw new Error("ERROR GENERATING URL", e);
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

            this.translatorsLogger.println("Retrieved translations for \"" + this.word +"\" ⟶ " + Arrays.toString(translated));

            // Close reader
            reader.close();
        }
        catch (IOException e)
        {
            this.translatorsLogger.printlnRed("ERROR GETTING TRANSLATIONS FROM " + url);
            throw new Error("ERROR GETTING TRANSLATIONS FROM " + url, e);
        }

        // Return translations for given word
        return translated;
    }
}
