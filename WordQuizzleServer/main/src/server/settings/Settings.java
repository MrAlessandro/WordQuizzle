package server.settings;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class Settings
{
    // Properties
    public static final Properties PROPERTIES = new Properties();

    // Debug flag
    public static boolean DEBUG;

    // Server coordinates
    public static String SERVER_HOST_NAME;
    public static int SERVER_CONNECTION_PORT;

    // RMI service coordinates
    public static String USERS_MANAGER_REMOTE_NAME;
    public static int USERS_MANAGER_REGISTRY_PORT;

    // Base port number for UDP channels
    public static int UDP_BASE_PORT;

    // Buffers size
    public static int BUFFERS_SIZE;

    // Deputy threads amount
    public static int DEPUTIES_POOL_SIZE;

    // Users database properties
    public static Path USERS_ARCHIVE_BACKUP_PATH;
    public static int USERS_ARCHIVE_INITIAL_SIZE;

    // Session archive properties
    public static int SESSIONS_ARCHIVE_INITIAL_SIZE;

    // Log files properties
    public static Path LOG_FILES_DIR_PATH;
    public static boolean LOG_FILES;
    public static boolean COLORED_LOGS;

    // Words dictionary.json
    public static URL DICTIONARY_URL;

    // Translation service
    public static String TRANSLATION_SERVICE_URL;

    // Friendship requests properties
    public static int FRIENDSHIP_REQUESTS_ARCHIVE_INITIAL_SIZE;
    public static Path FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH;

    // Challenge requests properties
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
        InputStream propertiesFileStream;

        // Get properties file URL
        propertiesFileStream = Settings.class.getClassLoader().getResourceAsStream("config.properties");
        if (propertiesFileStream == null)
            throw new FileNotFoundException("PROPERTIES FILE NOT FOUND");

        // Load properties file
        PROPERTIES.load(propertiesFileStream);

        // Getting properties
        DEBUG = Boolean.parseBoolean(PROPERTIES.getProperty("DEBUG"));
        SERVER_HOST_NAME = PROPERTIES.getProperty("SERVER_HOST_NAME");
        SERVER_CONNECTION_PORT = Integer.parseInt(PROPERTIES.getProperty("SERVER_CONNECTION_PORT"));
        USERS_MANAGER_REMOTE_NAME = PROPERTIES.getProperty("USERS_MANAGER_REMOTE_NAME");
        USERS_MANAGER_REGISTRY_PORT = Integer.parseInt(PROPERTIES.getProperty("USERS_MANAGER_REGISTRY_PORT"));
        UDP_BASE_PORT = Integer.parseInt(PROPERTIES.getProperty("UDP_BASE_PORT"));
        BUFFERS_SIZE = Integer.parseInt(PROPERTIES.getProperty("BUFFERS_SIZE"));
        DEPUTIES_POOL_SIZE = Integer.parseInt(PROPERTIES.getProperty("DEPUTIES_POOL_SIZE"));
        USERS_ARCHIVE_INITIAL_SIZE = Integer.parseInt(PROPERTIES.getProperty("USERS_ARCHIVE_INITIAL_SIZE"));
        SESSIONS_ARCHIVE_INITIAL_SIZE = Integer.parseInt(PROPERTIES.getProperty("SESSIONS_ARCHIVE_INITIAL_SIZE"));
        LOG_FILES = Boolean.parseBoolean(PROPERTIES.getProperty("LOG_FILES"));
        COLORED_LOGS = Boolean.parseBoolean(PROPERTIES.getProperty("COLORED_LOGS"));
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

        // Getting dictionary resource
        DICTIONARY_URL = Settings.class.getClassLoader().getResource(PROPERTIES.getProperty("DICTIONARY_PATH"));

        // Getting saved files
        Path serverSaveDir;
        String saveDir = PROPERTIES.getProperty("SERVER_SAVE_DIR");
        if (saveDir.equals("#"))
            serverSaveDir = Paths.get(Settings.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        else
            serverSaveDir = Paths.get(PROPERTIES.getProperty("SERVER_SAVE_DIR"));
        LOG_FILES_DIR_PATH = Paths.get(serverSaveDir.toString(), PROPERTIES.getProperty("LOG_FILES_DIR_PATH"));
        USERS_ARCHIVE_BACKUP_PATH = Paths.get(serverSaveDir.toString(), PROPERTIES.getProperty("USERS_ARCHIVE_BACKUP_PATH"));
        FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH = Paths.get(serverSaveDir.toString(), PROPERTIES.getProperty("FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH"));

        // Create necessary directories
        if (!Files.exists(serverSaveDir))
            Files.createDirectory(serverSaveDir);
        if (LOG_FILES && !Files.exists(LOG_FILES_DIR_PATH))
            Files.createDirectory(LOG_FILES_DIR_PATH);
    }
}
