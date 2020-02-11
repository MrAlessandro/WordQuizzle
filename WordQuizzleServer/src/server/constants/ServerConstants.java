package server.constants;

import constants.Constants;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConstants extends Constants
{
    public static final Path USERS_DATABASE_BACKUP_PATH = Paths.get(System.getProperty("user.dir"), "WordQuizzleServer/resources/UserNetBackUp.json");
    public static final int INITIAL_USERS_DATABASE_SIZE = 16384;
    public static final int CONNECTION_PORT = 50500;
    public static final int UDP_BASE_PORT = 60000;
    public static final String HOST_NAME = "localhost";
    public static final int DEPUTIES_POOL_SIZE = 1;
    public static final int BUFFERS_SIZE = 2048;
    public static final Path SERVER_LOG_FILES_PATH = Paths.get(System.getProperty("user.dir"), "WordQuizzleServer/logs/");
    public static final int CHALLENGE_REQUEST_TIMEOUT = 30000;
    public static final boolean LOG_IN_FILE = false;
}
