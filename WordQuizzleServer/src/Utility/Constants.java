package Utility;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants
{
    public static final Path UserNetBackUpPath = Paths.get(System.getProperty("user.dir"), "WordQuizzleServer/resources/UserNetBackUp.json");
    public static final int UserMapSize = 16384;
    public static final short UserMapBunchSize = 16;
    public static final int UserNetRegistryPort = 1099;
    public static final int SessionMapSize = 8192;
    public static final Charset DefaultCharSet = StandardCharsets.UTF_8;

    public static byte[] intToByteArray(int value)
    {
        return new byte[] {
                (byte)(value >>> 24),
                (byte)(value >>> 16),
                (byte)(value >>> 8),
                (byte)value};
    }
}
