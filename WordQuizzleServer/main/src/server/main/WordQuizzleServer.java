package server.main;

import commons.remote.Registrable;
import commons.loggers.Logger;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import server.challenges.ChallengesManager;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.friendship.FriendshipRequestsManager;
import server.sessions.SessionsManager;
import server.settings.Settings;
import server.users.UsersManager;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class WordQuizzleServer implements Runnable
{
    private static Logger logger;

    private static final Thread MAIN_THREAD = Thread.currentThread();
    public static final Thread.UncaughtExceptionHandler ERRORS_HANDLER = (thread, throwable) -> {
        logger.printlnRed("FATAL ERROR FROM THREAD: " + thread.getName() + " ⟶ " + throwable.getMessage());
        StackTraceElement[] stackTraceElements = throwable.getCause().getStackTrace();
        for (int i = stackTraceElements.length - 1; i >= 0; i--)
        {
            logger.printlnRed("\t" + stackTraceElements[i]);
        }
        Runtime.getRuntime().halt(1);
    };

    private static ServerSocketChannel serverSocket;
    public static volatile boolean shut = false;

    // Managers
    private UsersManager usersManager;
    private Registry registry;
    private Deputy[] deputies;

    public WordQuizzleServer()
    {
        // Setup thread name
        Thread.currentThread().setName("Main");

        // Set error handler for this main thread (current)
        Thread.currentThread().setUncaughtExceptionHandler(ERRORS_HANDLER);

        // Registering a shutdown hook for main thread (current)
        Runtime.getRuntime().addShutdownHook(new Thread(WordQuizzleServer::shutDown));

        // Loading properties
        System.out.print("Loading properties... ");
        try
        {
            Settings.loadProperties();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("LOADED");

        // Setup logger
        logger = new Logger(Settings.COLORED_LOGS, Settings.DEBUG);

        // Server initialization
        logger.printlnCyan("SERVER INITIALIZATION");

        // Setup users manager
        logger.print("Initializing users manager... ");
        try
        {
            if (!Settings.DEBUG)
            {
                File backUpFile = new File(Settings.USERS_ARCHIVE_BACKUP_PATH);
                if (backUpFile.exists())
                {
                    logger.printBlue("RESTORING FROM \"" + Settings.USERS_ARCHIVE_BACKUP_PATH + "\"... ");
                    String jsonString = new String(Files.readAllBytes(backUpFile.toPath()));

                    JSONParser parser = new JSONParser();
                    JSONArray serializedUsersArchive = (JSONArray) parser.parse(jsonString);
                    usersManager = new UsersManager(serializedUsersArchive);
                    logger.printlnGreen("RESTORED");
                }
                else
                {
                    usersManager = new UsersManager();
                    logger.printlnGreen("INITIALIZED");
                }
            }
            else
            {
                usersManager = new UsersManager();
                logger.printlnGreen("INITIALIZED");
            }
        }
        catch (IOException e)
        {
            throw new Error("RESTORING BACKUP FROM FILE", e);
        }
        catch (ParseException e)
        {
            throw new Error("PARSING BACKUP FILE", e);
        }

        // Set up friendship requests manager
        logger.print("Initializing friendship requests manager... ");
        FriendshipRequestsManager friendshipRequestsManager = new FriendshipRequestsManager();
        logger.printlnGreen("INITIALIZED");

        // Set up challenge requests manager
        logger.print("Initializing challenge requests manager... ");
        ChallengeRequestsManager challengeRequestsManager = new ChallengeRequestsManager();
        logger.printlnGreen("INITIALIZED");

        // Setup challenges manager
        logger.print("Initializing challenges manager... ");
        ChallengesManager challengesManager = new ChallengesManager(ERRORS_HANDLER);
        logger.printlnGreen("INITIALIZED");

        // Setup sessions manager
        logger.print("Initializing sessions manager... ");
        SessionsManager sessionsManager = new SessionsManager(usersManager, friendshipRequestsManager, challengeRequestsManager, challengesManager);
        logger.printlnGreen("INITIALIZED");

        // Initialize and starts deputies
        logger.println("Initializing and starting " + Settings.DEPUTIES_POOL_SIZE + " deputies... ");
        deputies = new Deputy[Settings.DEPUTIES_POOL_SIZE];
        for (int i = 0; i < deputies.length; i++)
        {
            logger.print("\t\tStarting deputy \"Deputy_" + (i + 1) + "\"... ");
            deputies[i] = new Deputy("Deputy_" + (i + 1), Settings.UDP_BASE_PORT + i, usersManager, sessionsManager);
            deputies[i].start();
            logger.printlnGreen("STARTED");
        }

        try
        {
            // Enabling RMI support for registration operation
            logger.print("Setting up RMI support... ");
            Registrable stub = (Registrable) UnicastRemoteObject.exportObject(usersManager, 0);
            LocateRegistry.createRegistry(Settings.USERS_MANAGER_REGISTRY_PORT);
            registry = LocateRegistry.getRegistry(Settings.USERS_MANAGER_REGISTRY_PORT);
            registry.bind(Settings.USERS_MANAGER_REMOTE_NAME, stub);
            logger.printlnGreen("OK");

            // The opening server's connection socket
            logger.print("The opening server's connection channel on \"" + Settings.SERVER_HOST_NAME + ":" + Settings.SERVER_CONNECTION_PORT + "\"... ");
            InetSocketAddress serverAddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, Settings.SERVER_CONNECTION_PORT);
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(serverAddress);
            logger.printlnGreen("OPENED");
        }
        catch (IOException | AlreadyBoundException  e)
        {
            throw new Error(e.getMessage().toUpperCase(), e);
        }


        // Initialization completed
        logger.printlnCyan("INITIALIZATION COMPLETED");
    }


    public static void main(String[] args)
    {
        WordQuizzleServer server = new WordQuizzleServer();
        server.run();
    }

    @Override
    public void run()
    {
        try
        {
            // Variable for select deputies sequentially
            short dispatchingIndex = 0;

            // Start server listening cycle
            while (!shut)
            {
                try
                {
                    // Wait for connection
                    logger.print("Wait for new income connections... ");
                    SocketChannel connection = serverSocket.accept();
                    logger.printlnGreen("ACCEPTED ⟶ " + connection.getRemoteAddress());

                    // Dispatch the socket to be served to one of the deputies
                    logger.print("Delegate connection to... ");
                    deputies[dispatchingIndex].dispatch.add(connection);
                    deputies[dispatchingIndex].wakeUp();
                    logger.printlnGreen("Deputy_" + dispatchingIndex);

                    // Increment dispatching index
                    dispatchingIndex = (short) (++dispatchingIndex % Settings.DEPUTIES_POOL_SIZE);
                }
                catch (AsynchronousCloseException e)
                {
                    logger.printlnYellow("CLOSED SERVER CONNECTION CHANNEL");
                }
            }

            // Start server's termination process
            logger.printlnCyan("SHUTTING DOWN THE SERVER");

            // Signal to terminate to others threads and wait for them to do so
            logger.print("Signaling deputies to terminate... ");
            for (Deputy deputy : deputies)
            {
                deputy.shutDown();
                deputy.join();
            }
            logger.printlnGreen("DEPUTIES TERMINATED");

            // Backup users manager in order to make it persistent
            if (!Settings.DEBUG)
            {
                logger.print("Backing up of users manager... ");
                try
                {
                    JSONArray serializedUsersArchive = usersManager.serialize();
                    byte[] jsonBytes = serializedUsersArchive.toJSONString().getBytes();

                    Files.deleteIfExists(Paths.get(Settings.USERS_ARCHIVE_BACKUP_PATH));
                    Files.write(Paths.get(Settings.USERS_ARCHIVE_BACKUP_PATH), jsonBytes, StandardOpenOption.CREATE_NEW);
                    logger.printlnGreen("BACKED UP");
                }
                catch (IOException e)
                {
                    throw new Error("ERROR WRITING BACKUP ON FILE", e);
                }
            }

            // Unbind the RMI service
            logger.print("Unbinding RMI service... ");
            registry.unbind(Settings.USERS_MANAGER_REMOTE_NAME);
            logger.printlnGreen("UNBOUND");

            // Server closed
            logger.printlnCyan("SERVER SHUTTED DOWN");
        }
        catch (IOException | NotBoundException | InterruptedException e)
        {
            throw new Error(e.getMessage().toUpperCase(), e);
        }
    }

    public static void shutDown()
    {
        // Set the shutdown flag
        shut = true;

        try
        {
            // Close the server's connection socket
            serverSocket.close();
            // Wait for the main thread to finish
            MAIN_THREAD.join();
        }
        catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
            throw new Error("During shutdown", e);
        }
    }
}