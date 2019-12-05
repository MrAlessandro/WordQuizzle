import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;


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

            ObjectMapper mapper = new ObjectMapper();
            List<String> values = mapper.readTree(builder.toString()).findValuesAsText("translatedText");

            for (String str : values)
            {
                System.out.println(str);
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
