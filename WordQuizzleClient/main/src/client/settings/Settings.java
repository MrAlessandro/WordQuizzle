package client.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class Settings
{
    // Properties
    public static final Properties PROPERTIES = new Properties();

    public static int CONNECTION_PORT;
    public static String SERVER_HOST_NAME;

    public static boolean COLORED_LOGS;

    public static void loadProperties() throws IOException
    {
        URL resourcesDirectoryURL;
        URL propertiesFileURL;

        // Get resources directory URL
        resourcesDirectoryURL = Settings.class.getClassLoader().getResource("");
        if (resourcesDirectoryURL == null)
            throw new FileNotFoundException("RESOURCES DIRECTORY NOT FOUND");

        // Get properties file URL
        propertiesFileURL = Settings.class.getClassLoader().getResource("config.properties");
        if (propertiesFileURL == null)
            throw new FileNotFoundException("PROPERTIES FILE NOT FOUND");

        // Reading properties file
        PROPERTIES.load(new FileInputStream(propertiesFileURL.getPath()));

        CONNECTION_PORT = Integer.parseInt(PROPERTIES.getProperty("CONNECTION_PORT"));
        SERVER_HOST_NAME = PROPERTIES.getProperty("SERVER_HOST_NAME");
        COLORED_LOGS = Boolean.parseBoolean(PROPERTIES.getProperty("COLORED_LOGS"));
    }
}
