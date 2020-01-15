package Utility;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants
{
    public static final Path UserNetBackUpPath = Paths.get(System.getProperty("user.dir"), "WordQuizzleServer/resources/UserNetBackUp.json");
    public static final int UserMapSize = 16384;
    public static final short UserMapBunchSize = 16;
    public static final int UserNetRegistryPort = 1099;
}
