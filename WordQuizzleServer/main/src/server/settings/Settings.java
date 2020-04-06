package server.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings
{
    // Properties
    public static final Properties PROPERTIES = new Properties();

    // RMI service coordinates
    public static String USERS_MANAGER_REMOTE_NAME;
    public static int USERS_MANAGER_REGISTRY_PORT;

    // Server coordinates
    public static String SERVER_HOST_NAME;
    public static int SERVER_CONNECTION_PORT;

    // Base port number for UDP channels
    public static int UDP_BASE_PORT;

    // Buffers size
    public static int BUFFERS_SIZE;

    // Deputy threads amount
    public static int DEPUTIES_POOL_SIZE;

    // Users database properties
    public static String USERS_ARCHIVE_BACKUP_PATH;
    public static int USERS_ARCHIVE_INITIAL_SIZE;

    // Session archive properties
    public static int SESSIONS_ARCHIVE_INITIAL_SIZE;

    // Log files properties
    public static String LOG_FILES_PATH;
    public static boolean LOG_FILES;
    public static boolean COLORED_LOGS;

    // Words dictionary.json
    public static String DICTIONARY_PATH;

    // Translation service
    public static String TRANSLATION_SERVICE_URL;

    // Friendship server.requests properties
    public static int FRIENDSHIP_REQUESTS_ARCHIVE_INITIAL_SIZE;

    // Challenge server.requests properties
    public static int CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE;
    public static int CHALLENGE_REQUEST_TIMEOUT;

    // Challenges properties
    public static int CHALLENGES_ARCHIVE_INITIAL_SIZE;
    public static int CHALLENGE_WORDS_QUANTITY;
    public static int CHALLENGE_DURATION_SECONDS;
    public static int CHALLENGE_RIGHT_TRANSLATION_SCORE;
    public static int CHALLENGE_WRONG_TRANSLATION_SCORE;
    public static int CHALLENGE_WINNER_EXTRA_SCORE;


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

        // Getting properties
        USERS_MANAGER_REMOTE_NAME = PROPERTIES.getProperty("USERS_MANAGER_REMOTE_NAME");
        USERS_MANAGER_REGISTRY_PORT = Integer.parseInt(PROPERTIES.getProperty("USERS_MANAGER_REGISTRY_PORT"));
        SERVER_HOST_NAME = PROPERTIES.getProperty("SERVER_HOST_NAME");
        SERVER_CONNECTION_PORT = Integer.parseInt(PROPERTIES.getProperty("SERVER_CONNECTION_PORT"));
        UDP_BASE_PORT = Integer.parseInt(PROPERTIES.getProperty("UDP_BASE_PORT"));
        BUFFERS_SIZE = Integer.parseInt(PROPERTIES.getProperty("BUFFERS_SIZE"));
        DEPUTIES_POOL_SIZE = Integer.parseInt(PROPERTIES.getProperty("DEPUTIES_POOL_SIZE"));
        USERS_ARCHIVE_BACKUP_PATH = resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("USERS_ARCHIVE_BACKUP_PATH");
        USERS_ARCHIVE_INITIAL_SIZE = Integer.parseInt(PROPERTIES.getProperty("USERS_ARCHIVE_INITIAL_SIZE"));
        SESSIONS_ARCHIVE_INITIAL_SIZE = Integer.parseInt(PROPERTIES.getProperty("SESSIONS_ARCHIVE_INITIAL_SIZE"));
        LOG_FILES_PATH = resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("LOG_FILES_PATH");
        LOG_FILES = Boolean.parseBoolean(PROPERTIES.getProperty("LOG_FILES"));
        COLORED_LOGS = Boolean.parseBoolean(PROPERTIES.getProperty("COLORED_LOGS"));
        DICTIONARY_PATH = resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("DICTIONARY_PATH");
        TRANSLATION_SERVICE_URL = PROPERTIES.getProperty("TRANSLATION_SERVICE_URL");
        FRIENDSHIP_REQUESTS_ARCHIVE_INITIAL_SIZE = Integer.parseInt(PROPERTIES.getProperty("FRIENDSHIP_REQUESTS_ARCHIVE_INITIAL_SIZE"));
        CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_REQUESTS_ARCHIVE_INITIAL_SIZE"));
        CHALLENGE_REQUEST_TIMEOUT = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_REQUEST_TIMEOUT"));
        CHALLENGES_ARCHIVE_INITIAL_SIZE = Integer.parseInt(PROPERTIES.getProperty("CHALLENGES_ARCHIVE_INITIAL_SIZE"));
        CHALLENGE_WORDS_QUANTITY = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_WORDS_QUANTITY"));
        CHALLENGE_DURATION_SECONDS = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_DURATION_SECONDS"));
        CHALLENGE_RIGHT_TRANSLATION_SCORE = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_RIGHT_TRANSLATION_SCORE"));
        CHALLENGE_WRONG_TRANSLATION_SCORE = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_WRONG_TRANSLATION_SCORE"));
        CHALLENGE_WINNER_EXTRA_SCORE = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_WINNER_EXTRA_SCORE"));

        // Create necessary directories
        if (LOG_FILES && !(Files.exists(Paths.get(LOG_FILES_PATH))))
            Files.createDirectory(Paths.get(LOG_FILES_PATH));
    }
}
