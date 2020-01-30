package client.gui.constants;

import java.awt.Color;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GuiConstants
{
    public static final Path WordQuizzleLogoPath = Paths.get(System.getProperty("user.dir"), "WordQuizzleClient/resources/LogoWordQuizzle.png");
    public static final Path LoadingGifPAth = Paths.get(System.getProperty("user.dir"), "WordQuizzleClient/resources/loader.gif");
    public static final Color MainColor = new Color(51, 172, 224, 255);
    public static final Color BackgroundColor = new Color(49, 57, 60, 255);
    public static final Color ForegroundColor = new Color(34, 116, 165, 255);
}
