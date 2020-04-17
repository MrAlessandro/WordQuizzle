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
import java.net.URL;
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

class WordQuizzleServer
{
    // Main thread's logger
    private static Logger logger = null;

    // Server connection socket channel
    private static ServerSocketChannel serverSocket;

    // Shutdown flag
    private static volatile boolean shut = false;

    // Managers
    private final UsersManager usersManager;
    private final FriendshipRequestsManager friendshipRequestsManager;
    private final Registry registry;
    private final Deputy[] deputies;

    public WordQuizzleServer() throws IOException, ParseException, AlreadyBoundException
    {
        // Loading properties
        Settings.loadProperties();

        // Setup logger
        logger = new Logger(Settings.COLORED_LOGS, Settings.DEBUG);

        // Server initialization
        logger.printlnCyan("SERVER INITIALIZATION");

        // Setup users manager
        logger.print("Initializing users manager... ");
        if (!Settings.DEBUG && Files.exists(Settings.USERS_ARCHIVE_BACKUP_PATH))
        {
            logger.printBlue("RESTORING FROM \"" + Settings.USERS_ARCHIVE_BACKUP_PATH + "\"... ");
            String jsonString = new String(Files.readAllBytes(Settings.USERS_ARCHIVE_BACKUP_PATH));
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

        // Set up friendship requests manager
        logger.print("Initializing friendship requests manager... ");
        if (!Settings.DEBUG && Files.exists(Settings.FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH))
        {
            logger.printBlue("RESTORING FROM \"" + Settings.FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH + "\"... ");
            String jsonString = new String(Files.readAllBytes(Settings.FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH));
            JSONParser parser = new JSONParser();
            JSONArray serializedFriendshipRequestsArchive = (JSONArray) parser.parse(jsonString);
            friendshipRequestsManager = new FriendshipRequestsManager(serializedFriendshipRequestsArchive);
            logger.printlnGreen("RESTORED");
        }
        else
        {
            friendshipRequestsManager = new FriendshipRequestsManager();
            logger.printlnGreen("INITIALIZED");
        }

        // Set up challenge requests manager
        logger.print("Initializing challenge requests manager... ");
        ChallengeRequestsManager challengeRequestsManager = new ChallengeRequestsManager();
        logger.printlnGreen("INITIALIZED");

        // Setup challenges manager
        logger.print("Initializing challenges manager... ");
        ChallengesManager challengesManager = new ChallengesManager(Thread.currentThread().getUncaughtExceptionHandler());
        logger.printlnGreen("INITIALIZED");

        // Setup sessions manager
        logger.print("Initializing sessions manager... ");
        SessionsManager sessionsManager = new SessionsManager(usersManager, friendshipRequestsManager, challengeRequestsManager, challengesManager);
        logger.printlnGreen("INITIALIZED");

        // Initialize and starts deputies
        logger.println("Initializing " + Settings.DEPUTIES_POOL_SIZE + " deputies... ");
        deputies = new Deputy[Settings.DEPUTIES_POOL_SIZE];
        for (int i = 0; i < deputies.length; i++)
        {
            logger.print("\t\tInitializing deputy \"Deputy_" + (i + 1) + "\"... ");
            deputies[i] = new Deputy("Deputy_" + (i + 1), Settings.UDP_BASE_PORT + i, usersManager, sessionsManager);
            logger.printlnGreen("INITIALIZED");
        }

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

        // Initialization completed
        logger.printlnCyan("INITIALIZATION COMPLETED");
    }


    public static void main(String[] args)
    {
        try
        {
            WordQuizzleServer server;
            Thread mainThread;

            // Set error handler for this main thread (current)
            Thread.currentThread().setUncaughtExceptionHandler((thread, throwable) -> {
                if (logger != null)
                {
                    logger.printlnRed("FATAL ERROR FROM THREAD: " + thread.getName() + " ⟶ " + (throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getName()));
                    if (throwable.getCause() != null)
                    {
                        StackTraceElement[] stackTraceElements = throwable.getCause().getStackTrace();
                        for (int i = stackTraceElements.length - 1; i >= 0; i--)
                        {
                            logger.printlnRed("\t" + stackTraceElements[i]);
                        }
                    }
                    else
                    {
                        for (int i = 0; i < throwable.getStackTrace().length; i++)
                        {
                            logger.printlnRed("\t" + throwable.getStackTrace()[i]);
                        }
                    }
                }
                else
                {
                    System.out.println("FATAL ERROR FROM THREAD: " + thread.getName() + " ⟶ " + (throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getName()));
                    if (throwable.getCause() != null)
                    {
                        StackTraceElement[] stackTraceElements = throwable.getCause().getStackTrace();
                        for (int i = stackTraceElements.length - 1; i >= 0; i--)
                        {
                            System.out.println("\t" + stackTraceElements[i]);
                        }
                    }
                    else
                    {
                        for (int i = 0; i < throwable.getStackTrace().length; i++)
                        {
                            System.out.println("\t" + throwable.getStackTrace()[i]);
                        }
                    }
                }

                System.exit(1);
            });

            // Save current thread reference
            mainThread = Thread.currentThread();

            // Setup thread name
            Thread.currentThread().setName("Main");

            // Initialize server
            server = new WordQuizzleServer();

            // Registering a shutdown hook for main thread (current)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try
                {
                    server.shutDown();
                    mainThread.join();
                }
                catch (InterruptedException | IOException e)
                {
                    e.printStackTrace();
                    Runtime.getRuntime().halt(1);
                }
            }));

            // Run server
            server.run();
        }
        catch (ParseException | AlreadyBoundException | IOException e)
        {
            if (logger != null)
                logger.printlnRed(e.getMessage().toUpperCase());
            else
                System.out.println(e.getMessage().toUpperCase());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void run()
    {
        // Initialize and starts deputies
        logger.println("Starting " + Settings.DEPUTIES_POOL_SIZE + " deputies... ");
        for (int i = 0; i < deputies.length; i++)
        {
            logger.print("\t\tStarting deputy \"Deputy_" + (i + 1) + "\"... ");
            deputies[i].start();
            logger.printlnGreen("STARTED");
        }

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
                    deputies[dispatchingIndex].wakeup();
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

                    Files.deleteIfExists(Settings.USERS_ARCHIVE_BACKUP_PATH);
                    Files.write(Settings.USERS_ARCHIVE_BACKUP_PATH, jsonBytes, StandardOpenOption.CREATE_NEW);
                    logger.printlnGreen("BACKED UP");
                }
                catch (IOException e)
                {
                    throw new Error("ERROR WRITING BACKUP ON FILE", e);
                }
            }

            // Backup friendship requests manager in order to make it persistent
            if (!Settings.DEBUG)
            {
                logger.print("Backing up of friendship requests manager... ");
                try
                {
                    JSONArray serializedFriendshipRequestsArchive = friendshipRequestsManager.serialize();
                    byte[] jsonBytes = serializedFriendshipRequestsArchive.toJSONString().getBytes();

                    Files.deleteIfExists(Settings.FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH);
                    Files.write(Settings.FRIENDSHIP_REQUESTS_ARCHIVE_BACKUP_PATH, jsonBytes, StandardOpenOption.CREATE_NEW);
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

    public void shutDown() throws IOException
    {
        // Set the shutdown flag
        shut = true;

        // Close the server's connection socket
        serverSocket.close();
    }
}