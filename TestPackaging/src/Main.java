import java.io.*;
import java.util.Properties;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        Properties properties = new Properties();
        properties.load(Main.class.getClassLoader().getResourceAsStream("config.properties"));
        OutputStream outputStream = new FileOutputStream(Main.class.getClassLoader().getResource("config.properties").getPath());

        System.out.println(properties.getProperty("PIPPO"));

        StringBuilder longString = new StringBuilder();
        longString.append("{");
        for (int i = 0; i < 1000; i++)
        {
            longString.append("CIAO").append(i).append("! ");
        }
        longString.append("}");

        properties.setProperty("LONG_STRING", longString.toString());

        properties.store(outputStream, "config.properties");
    }
}
