import java.nio.file.Path;
import java.nio.file.Paths;

class Constants
{
    protected static final Path UserNetBackUpPath = Paths.get(System.getProperty("user.dir"), "/resources/UserNetBackUp.json");
    protected static final int UserMapSize = 16384;
    protected static final short UserMapBunchSize = 16;
}
