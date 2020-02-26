package server.main;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

public class TraducingTest
{

    public static void main(String[] args)
    {
        try
        {
            StringBuilder builder = new StringBuilder();
            URL url = new URL("https://api.mymemory.translated.net/get?q=paracadute&langpair=it|en");
            BufferedReader reader;
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setDoOutput(true);

            reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }

            System.out.println(builder.toString());

            JSONObject obj = (JSONObject) JSONValue.parse(builder.toString());
            JSONArray arr = (JSONArray) ((JSONObject)obj).get("matches");
            String[] matches = new String[arr.size()];
            int i = 0;
            for (JSONObject mtc : (Iterable<JSONObject>) arr)
            {
                String tmp = (String) mtc.get("translation");
                tmp = tmp.replaceAll("[^a-zA-Z ]", "");
                matches[i] = tmp.toLowerCase();
                i++;
            }

            String translated = (String) ((JSONObject)obj.get("responseData")).get("translatedText");
            System.out.println(translated);

            for (String match : matches)
            {
                System.out.println(match);
            }

            reader.close();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
