package server.settings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class ServerConstants
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
    public static boolean COLORED_LOG_FILES;

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

        // Get class loader directory

        // Get resources directory URL
        resourcesDirectoryURL = ServerConstants.class.getClassLoader().getResource("");
        if (resourcesDirectoryURL == null)
            throw new FileNotFoundException("RESOURCES DIRECTORY NOT FOUND");

        // Get properties file URL
        propertiesFileURL = ServerConstants.class.getClassLoader().getResource("config.properties");
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
        COLORED_LOG_FILES = Boolean.parseBoolean(PROPERTIES.getProperty("COLORED_LOG_FILES"));
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
    }

    /*public static URL urlFactory(String word)
    {
        URL generated;

        try
        {
            generated = new URL(TRANSLATION_SERVICE_URL + "/get?q=" + word + "&langpair=it|en&mt=0");
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
            throw new Error("Generating URL");
        }

        return generated;
    }

    public static String[] getBulkOfWords()
    {
        String[] bulk = new String[CHALLENGE_WORDS_QUANTITY];

        try
        {
            // Initialize randomizer
            Random randomizer = new Random();

            // Open dictionary.json
            RandomAccessFile dictionary.json = new RandomAccessFile(ServerConstants.DICTIONARY_PATH.getPath(), "r");

            // Initialize words to translate relative to this challenge;
            int index = 0;
            while (index < bulk.length)
            {
                // Generate a random position in the dictionary.json
                long seek = Math.abs(randomizer.nextLong() % dictionary.json.length());
                // Set the random position
                dictionary.json.seek(seek);

                // Read the wasted line
                dictionary.json.readLine();
                // Read the good line
                String word = dictionary.json.readLine();

                // Check if end-of-file has been encountered
                if (word != null)
                // Word usable
                    bulk[index++] = word;
            }

            // Stop reading dictionary.json
            dictionary.json.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            throw new Error("Dictionary not found");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Reading dictionary.json");
        }

        return bulk;
    }

    public static String[][] translateBulkOfWords(String[] toTranslate)
    {
        String[][] translated = new String[ServerConstants.CHALLENGE_WORDS_QUANTITY][];
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

                // Initialize reader
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                // Get response body
                String inputLine = reader.readLine();

                // Deserialize response and get translations
                JSONObject body = (JSONObject) JSONValue.parse(inputLine);
                JSONArray matches = (JSONArray) body.get("matches");
                translated[i] = new String[matches.size()];
                int j = 0;
                for (JSONObject match : (Iterable<JSONObject>) matches)
                {
                    String translation = (String) match.get("translation");
                    translation = translation.replaceAll("[^a-zA-Z ]", "");
                    translation = translation.toLowerCase();
                    translated[i][j] = translation;
                }
            }

            reader.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Getting translations");
        }

        return translated;
    }*/
}
