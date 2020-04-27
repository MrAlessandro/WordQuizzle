package client.settings;

import javax.swing.ImageIcon;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class Settings
{
    // Properties
    public static final Properties PROPERTIES = new Properties();

    // Server coordinates
    public static int CONNECTION_PORT;
    public static String SERVER_HOST_NAME;

    // RMI service coordinates
    public static String USERS_MANAGER_REMOTE_NAME;
    public static int USERS_MANAGER_REGISTRY_PORT;

    // Log files flag
    public static boolean COLORED_LOGS;

    // Gui sizes
    public static int SESSION_PANE_WIDTH;
    public static int SESSION_PANE_HEIGHT;
    public static int FRIENDS_PANE_WIDTH;
    public static int CHALLENGE_PANE_WIDTH;
    public static int SCORE_PANE_WIDTH;

    // Icons paths
    public static ImageIcon LOGO_ICON;
    public static ImageIcon LOADING_ICON;
    public static ImageIcon STOPWATCH_ICON;
    public static ImageIcon HANDSHAKE_ICON;
    public static ImageIcon THUMB_UP_ICON;
    public static ImageIcon THUMB_DOWN_ICON;
    public static ImageIcon WARNING_ICON;
    public static ImageIcon CHALLENGE_ICON;
    public static ImageIcon TIMEOUT_ICON;
    public static ImageIcon LOGOUT_ICON;

    // Colors
    public static Color MAIN_COLOR;
    public static Color BACKGROUND_COLOR;
    public static Color SCROLL_PANE_BACKGROUND_COLOR;
    public static Color FOREGROUND_COLOR;

    // Response values
    public static final int CHALLENGE_REQUEST_APPLICANT_OFFLINE_NOTIFICATION_VALUE = -100;
    public static final int CHALLENGE_REQUEST_TIMER_EXPIRED_NOTIFICATION_VALUE = -200;

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
        CONNECTION_PORT = Integer.parseInt(PROPERTIES.getProperty("CONNECTION_PORT"));
        SERVER_HOST_NAME = PROPERTIES.getProperty("SERVER_HOST_NAME");
        USERS_MANAGER_REMOTE_NAME = PROPERTIES.getProperty("USERS_MANAGER_REMOTE_NAME");
        USERS_MANAGER_REGISTRY_PORT = Integer.parseInt(PROPERTIES.getProperty("USERS_MANAGER_REGISTRY_PORT"));
        COLORED_LOGS = Boolean.parseBoolean(PROPERTIES.getProperty("COLORED_LOGS"));
        SESSION_PANE_WIDTH = Integer.parseInt(PROPERTIES.getProperty("SESSION_PANE_WIDTH"));
        SESSION_PANE_HEIGHT = Integer.parseInt(PROPERTIES.getProperty("SESSION_PANE_HEIGHT"));
        FRIENDS_PANE_WIDTH = Integer.parseInt(PROPERTIES.getProperty("FRIENDS_PANE_WIDTH"));
        CHALLENGE_PANE_WIDTH = Integer.parseInt(PROPERTIES.getProperty("CHALLENGE_PANE_WIDTH"));
        SCORE_PANE_WIDTH = Integer.parseInt(PROPERTIES.getProperty("SCORE_PANE_WIDTH"));
        LOGO_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("LOGO_ICON_PATH"));
        LOADING_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("LOADING_ICON_PATH"));
        STOPWATCH_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("STOPWATCH_ICON_PATH"));
        HANDSHAKE_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("HANDSHAKE_ICON_PATH"));
        THUMB_UP_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("THUMB_UP_ICON_PATH"));
        THUMB_DOWN_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("THUMB_DOWN_ICON_PATH"));
        WARNING_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("WARNING_ICON_PATH"));
        CHALLENGE_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("CHALLENGE_ICON_PATH"));
        TIMEOUT_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("TIMEOUT_ICON_PATH"));
        LOGOUT_ICON = new ImageIcon(resourcesDirectoryURL.getPath() + PROPERTIES.getProperty("LOGOUT_ICON_PATH"));
        MAIN_COLOR = extractColor(PROPERTIES.getProperty("MAIN_COLOR"));
        BACKGROUND_COLOR = extractColor(PROPERTIES.getProperty("BACKGROUND_COLOR"));
        SCROLL_PANE_BACKGROUND_COLOR = extractColor(PROPERTIES.getProperty("SCROLL_PANE_BACKGROUND_COLOR"));
        FOREGROUND_COLOR = extractColor(PROPERTIES.getProperty("FOREGROUND_COLOR"));
    }

    private static Color extractColor(String colorStr)
    {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }
}
