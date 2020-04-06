package server.main;

import commons.remote.Registrable;
import commons.loggers.Logger;

import server.challenges.ChallengesManager;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.friendship.FriendshipRequestsManager;
import server.sessions.SessionsManager;
import server.settings.Settings;
import server.users.UsersManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

class WordQuizzleServer
{
    private static final Thread MAIN_THREAD = Thread.currentThread();
    private static Logger logger;
    public static final Thread.UncaughtExceptionHandler ERRORS_HANDLER = (thread, throwable) -> {
        logger.printlnRed("FATAL ERROR FROM THREAD: " + thread.getName() + " ⟶ " + throwable.getMessage());
        StackTraceElement[] stackTraceElements = throwable.getCause().getStackTrace();
        for (int i = stackTraceElements.length - 1; i >= 0 ; i--)
        {
            logger.printlnRed("\t" + stackTraceElements[i]);
        }
        Runtime.getRuntime().halt(1);
    };

    private static ServerSocketChannel serverSocket;
    public static volatile boolean shut = false;


    public static void main(String[] args)
    {
        // Managers
        UsersManager usersManager;
        FriendshipRequestsManager friendshipRequestsManager;
        ChallengeRequestsManager challengeRequestsManager;
        ChallengesManager challengesManager;
        SessionsManager sessionsManager;

        InetSocketAddress serverAddress;
        Registry registry;
        Deputy[] deputies;
        Registrable stub;

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
        logger = new Logger(Settings.COLORED_LOGS);

        // Server initialization
        logger.printlnCyan("SERVER INITIALIZATION");

        /*TODO*/
        // Restoring eventual previous server.users' manager state
        //UsersManager.restore();

        // Setup server.users manager
        logger.print("Initializing users manager... ");
        usersManager = new UsersManager();
        logger.printlnGreen("INITIALIZED");

        // Set up friendship server.requests manager
        logger.print("Initializing friendship requests manager... ");
        friendshipRequestsManager = new FriendshipRequestsManager();
        logger.printlnGreen("INITIALIZED");

        // Set up challenge server.requests manager
        logger.print("Initializing challenge requests manager... ");
        challengeRequestsManager = new ChallengeRequestsManager();
        logger.printlnGreen("INITIALIZED");

        // Setup challenges manager
        logger.print("Initializing challenges manager... ");
        challengesManager = new ChallengesManager(ERRORS_HANDLER);
        logger.printlnGreen("INITIALIZED");

        // Setup sessions manager
        logger.print("Initializing sessions manager... ");
        sessionsManager = new SessionsManager(usersManager, friendshipRequestsManager, challengeRequestsManager, challengesManager);
        logger.printlnGreen("INITIALIZED");

        // Initialize and starts deputies
        logger.println("Initializing and starting " + Settings.DEPUTIES_POOL_SIZE + " deputies... ");
        deputies = new Deputy[Settings.DEPUTIES_POOL_SIZE];
        for (int i = 0; i < deputies.length; i++)
        {
            logger.print("\t\tStarting deputy \"Deputy_" + (i+1) + "\"... ");
            deputies[i] = new Deputy("Deputy_" + (i+1), Settings.UDP_BASE_PORT+i, usersManager, sessionsManager);
            deputies[i].start();
            logger.printlnGreen("STARTED");
        }


        try
        {
            // Enabling RMI support for registration operation
            logger.print("Setting up RMI support... ");
            stub = (Registrable) UnicastRemoteObject.exportObject(usersManager, 0);
            LocateRegistry.createRegistry(Settings.USERS_MANAGER_REGISTRY_PORT);
            registry = LocateRegistry.getRegistry(Settings.USERS_MANAGER_REGISTRY_PORT);
            registry.bind(Settings.USERS_MANAGER_REMOTE_NAME, stub);
            logger.printlnGreen("OK");

            // Variable for select deputies sequentially
            short dispatchingIndex = 0;

            // The opening server's connection socket
            logger.print("The opening server's connection channel on \"" + Settings.SERVER_HOST_NAME + ":" + Settings.SERVER_CONNECTION_PORT + "\"... ");
            serverAddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, Settings.SERVER_CONNECTION_PORT);
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(serverAddress);
            logger.printlnGreen("OPENED");

            // Initialization completed
            logger.printlnCyan("INITIALIZATION COMPLETED");

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

            // Backup server.users' system in order to make it persistent
            //UsersManager.backUp();

            // Unbind the RMI service
            logger.print("Unbinding RMI service... ");
            registry.unbind(Settings.USERS_MANAGER_REMOTE_NAME);
            logger.printlnGreen("UNBOUND");

            // Server closed
            logger.printlnCyan("SERVER SHUTTED DOWN");
        }
        catch (IOException | NotBoundException | AlreadyBoundException | InterruptedException e)
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