package client.settings;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    // Challenge playground model html file path
    public static String CHALLENGE_PLAYGROUND_MODEL;

    // Response values
    public static final int CHALLENGE_REQUEST_APPLICANT_OFFLINE_NOTIFICATION_VALUE = -100;
    public static final int CHALLENGE_REQUEST_TIMER_EXPIRED_NOTIFICATION_VALUE = -200;

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
        MAIN_COLOR = extractColor(PROPERTIES.getProperty("MAIN_COLOR"));
        BACKGROUND_COLOR = extractColor(PROPERTIES.getProperty("BACKGROUND_COLOR"));
        SCROLL_PANE_BACKGROUND_COLOR = extractColor(PROPERTIES.getProperty("SCROLL_PANE_BACKGROUND_COLOR"));
        FOREGROUND_COLOR = extractColor(PROPERTIES.getProperty("FOREGROUND_COLOR"));

        // Load all icons
        try(
                InputStream logoIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("LOGO_ICON_PATH"));
                InputStream loadingIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("LOADING_ICON_PATH"));
                InputStream stopwatchIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("STOPWATCH_ICON_PATH"));
                InputStream handshakeIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("HANDSHAKE_ICON_PATH"));
                InputStream thumbUpIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("THUMB_UP_ICON_PATH"));
                InputStream thumbDownIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("THUMB_DOWN_ICON_PATH"));
                InputStream warningIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("WARNING_ICON_PATH"));
                InputStream challengeIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("CHALLENGE_ICON_PATH"));
                InputStream timeoutIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("TIMEOUT_ICON_PATH"));
                InputStream logOutIconInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("LOGOUT_ICON_PATH"));
        )
        {
            assert logoIconInputStream != null;
            assert loadingIconInputStream != null;
            assert stopwatchIconInputStream != null;
            assert handshakeIconInputStream != null;
            assert thumbUpIconInputStream != null;
            assert thumbDownIconInputStream != null;
            assert warningIconInputStream != null;
            assert challengeIconInputStream != null;
            assert timeoutIconInputStream != null;
            assert logOutIconInputStream != null;

            LOGO_ICON = new ImageIcon(ImageIO.read(logoIconInputStream));
            LOADING_ICON = new ImageIcon(ImageIO.read(loadingIconInputStream));
            STOPWATCH_ICON = new ImageIcon(ImageIO.read(stopwatchIconInputStream));
            HANDSHAKE_ICON = new ImageIcon(ImageIO.read(handshakeIconInputStream));
            THUMB_UP_ICON = new ImageIcon(ImageIO.read(thumbUpIconInputStream));
            THUMB_DOWN_ICON = new ImageIcon(ImageIO.read(thumbDownIconInputStream));
            WARNING_ICON = new ImageIcon(ImageIO.read(warningIconInputStream));
            CHALLENGE_ICON = new ImageIcon(ImageIO.read(challengeIconInputStream));
            TIMEOUT_ICON = new ImageIcon(ImageIO.read(timeoutIconInputStream));
            LOGOUT_ICON = new ImageIcon(ImageIO.read(logOutIconInputStream));
        }

        // Load challenge playground model
        try(InputStream challengePlaygroundModelInputStream = Settings.class.getClassLoader().getResourceAsStream(PROPERTIES.getProperty("CHALLENGE_PLAYGROUND_MODEL")))
        {
            assert challengePlaygroundModelInputStream != null;

            StringBuilder builder = new StringBuilder();
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = challengePlaygroundModelInputStream.read(chunk)) != -1)
            {
                builder.append(new String(chunk));
            }

            CHALLENGE_PLAYGROUND_MODEL = builder.toString();
        }
    }

    private static Color extractColor(String colorStr)
    {
        return new Color(
                Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }
}
