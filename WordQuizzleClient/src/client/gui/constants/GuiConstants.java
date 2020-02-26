package client.gui.constants;

import javax.swing.*;
import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class GuiConstants
{
    public static final ImageIcon LOGO = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/LogoWordQuizzle.png")));
    public static final ImageIcon LOADING_GIF = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/Loader.gif")));
    public static final ImageIcon STOPWATCH_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/Stopwatch.png")));
    public static final ImageIcon HANDSHAKE_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/Handshake.png")));
    public static final ImageIcon THUMB_UP_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/ThumbUp.png")));
    public static final ImageIcon THUMB_DOWN_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/ThumbDown.png")));
    public static final ImageIcon WARNING_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/Warning.png")));
    public static final ImageIcon CHALLENGE_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/ArmWrestling.png")));
    public static final ImageIcon TIMEOUT_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/TimeOut.png")));
    public static final ImageIcon LOGOUT_ICON = new ImageIcon(Objects.requireNonNull(GuiConstants.class.getClassLoader().getResource("client/resources/LogOut.png")));
    public static final Color MAIN_COLOR = new Color(51, 172, 224, 255);
    public static final Color BACKGROUND_COLOR = new Color(49, 57, 60, 255);
    public static final Color SCROLL_PANE_BACKGROUND_COLOR = new Color(56,61,59,255);
    public static final Color FOREGROUND_COLOR = new Color(34, 116, 165, 255);
}
