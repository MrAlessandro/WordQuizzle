import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class TraducingTest
{

    public static void main(String[] args)
    {
        try
        {
            StringBuilder builder = new StringBuilder();
            URL url = new URL("https://api.mymemory.translated.net/get?q=Hello!&langpair=en|it");
            BufferedReader reader;
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");

            reader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null)
            {
                builder.append(line);
            }

            JSONObject obj = (JSONObject) JSONValue.parse(builder.toString());
            String translated = (String) ((JSONObject)obj.get("responseData")).get("translatedText");
            System.out.println(translated);

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
