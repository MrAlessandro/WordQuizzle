package server.constants;

import constants.Constants;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConstants extends Constants
{
    public static final Path USERS_DATABASE_BACKUP_PATH = Paths.get(System.getProperty("user.dir"), "server.main.WordQuizzleServer/resources/UserNetBackUp.json");
    public static final int INITIAL_USERS_DATABASE_SIZE = 16384;
}
