package client.gui.constants;

import javax.swing.*;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GuiConstants
{
    public static final ImageIcon LOGO = new ImageIcon(Paths.get(System.getProperty("user.dir"), "WordQuizzleClient", "resources", "LogoWordQuizzle.png").toString());
    public static final ImageIcon LOADING_GIF = new ImageIcon(Paths.get(System.getProperty("user.dir"), "WordQuizzleClient", "resources", "Loader.gif").toString());
    public static final ImageIcon STOPWATCH_ICON = new ImageIcon(Paths.get(System.getProperty("user.dir"), "WordQuizzleClient", "resources", "Stopwatch.png").toString());
    public static final ImageIcon HANDSHAKE_ICON = new ImageIcon(Paths.get(System.getProperty("user.dir"), "WordQuizzleClient", "resources", "Handshake.png").toString());
    public static final ImageIcon THUMB_UP_ICON = new ImageIcon(Paths.get(System.getProperty("user.dir"), "WordQuizzleClient", "resources", "ThumbUp.png").toString());
    public static final ImageIcon THUMB_DOWN_ICON = new ImageIcon(Paths.get(System.getProperty("user.dir"), "WordQuizzleClient", "resources", "ThumbDown.png").toString());
    public static final ImageIcon WARNING_ICON = new ImageIcon(Paths.get(System.getProperty("user.dir"), "WordQuizzleClient", "resources", "Warning.png").toString());
    public static final Color MAIN_COLOR = new Color(51, 172, 224, 255);
    public static final Color BACKGROUND_COLOR = new Color(49, 57, 60, 255);
    public static final Color SCROLL_PANE_BACKGROUND_COLOR = new Color(56,61,59,255);
    public static final Color FOREGROUND_COLOR = new Color(34, 116, 165, 255);
}
