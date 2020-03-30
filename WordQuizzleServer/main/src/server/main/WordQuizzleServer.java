package server.main;

import commons.remote.Registrable;
import server.challenges.ChallengesManager;
import server.loggers.Logger;
import server.requests.challenge.ChallengeRequestsManager;
import server.requests.friendship.FriendshipRequestsManager;
import server.sessions.SessionsManager;
import server.sessions.session.Session;
import server.settings.ServerConstants;
import server.users.UsersManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;

class WordQuizzleServer
{
    public static Thread.UncaughtExceptionHandler errorsHandler;
    public static volatile boolean shut = false;
    private static ServerSocketChannel serverSocket;
    private static Thread mainThread;

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
        Logger logger;

        // Setup thread name
        Thread.currentThread().setName("Main");

        // Logger setup for main thread
        logger = new Logger("Main");

        // Server initialization
        logger.printlnCyan("SERVER INITIALIZATION");

        // Load properties
        try
        {
            logger.print("Loading properties... ");
            ServerConstants.loadProperties();
            logger.printlnGreen("LOADED");
        }
        catch (FileNotFoundException e)
        {
            logger.printlnRed("PROPERTIES FILE NOT FOUND");
            logger.printlnRed(e.getStackTrace());
            System.exit(1);
        }
        catch (IOException e)
        {
            logger.printlnRed("ERROR READING");
            logger.printlnRed(e.getStackTrace());
            System.exit(1);
        }

        // Registering a shutdown hook
        logger.print("Setting up termination hook... ");
        mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(WordQuizzleServer::shutDown));
        logger.printlnGreen("OK");

        // Setting uncaught exception handler
        errorsHandler = (thread, throwable) -> {
            logger.printlnRed("FATAL ERROR FROM THREAD: " + thread.getName() + " ⟶ " + throwable.getMessage());
            logger.printlnRed(Arrays.toString(throwable.getStackTrace()));
            System.exit(1);
        };

        // Set error handler for this main thread (current)
        Thread.currentThread().setUncaughtExceptionHandler(errorsHandler);

        // Restoring eventual previous server.users' manager state
        //UsersManager.restore();

        // Setup server.loggers for deputies
        Logger.setUp();

        // Setup server.users manager
        logger.print("Initializing server.users manager... ");
        usersManager = new UsersManager();
        logger.printlnGreen("INITIALIZED");

        // Set up friendship server.requests manager
        logger.print("Initializing friendship server.requests manager... ");
        friendshipRequestsManager = new FriendshipRequestsManager();
        logger.printlnGreen("INITIALIZED");

        // Set up challenge server.requests manager
        logger.print("Initializing challenge server.requests manager... ");
        challengeRequestsManager = new ChallengeRequestsManager();
        logger.printlnGreen("INITIALIZED");

        // Setup challenges manager
        logger.print("Initializing challenges manager... ");
        challengesManager = new ChallengesManager(errorsHandler);
        logger.printlnGreen("INITIALIZED");

        // Setup sessions manager
        logger.print("Initializing sessions manager... ");
        sessionsManager = new SessionsManager(usersManager, friendshipRequestsManager, challengeRequestsManager, challengesManager);
        logger.printlnGreen("INITIALIZED");

        // Initialize and starts deputies
        logger.println("Initializing and starting " + ServerConstants.DEPUTIES_POOL_SIZE + " deputies... ");
        deputies = new Deputy[ServerConstants.DEPUTIES_POOL_SIZE];
        for (int i = 0; i < deputies.length; i++)
        {
            logger.print("\t\tStarting deputy \"Deputy_" + (i+1) + "\"... ");
            deputies[i] = new Deputy("Deputy_" + (i+1), ServerConstants.UDP_BASE_PORT+i, usersManager, sessionsManager);
            deputies[i].start();
            logger.printlnGreen("STARTED");
        }

        try
        {
            // Enabling RMI support for registration operation
            logger.print("Setting up RMI support... ");
            stub = (Registrable) UnicastRemoteObject.exportObject(usersManager, 0);
            LocateRegistry.createRegistry(ServerConstants.USERS_MANAGER_REGISTRY_PORT);
            registry = LocateRegistry.getRegistry(ServerConstants.USERS_MANAGER_REGISTRY_PORT);
            registry.bind(ServerConstants.USERS_MANAGER_REMOTE_NAME, stub);
            logger.printlnGreen("OK");
        }
        catch (AlreadyBoundException | RemoteException e)
        {
            throw new Error(e.getMessage().toUpperCase());
        }

        try
        {
            // Variable for select deputies sequentially
            short dispatchingIndex = 0;

            // The opening server's connection socket
            logger.print("The opening server's connection channel on \"" + ServerConstants.SERVER_HOST_NAME + ":" + ServerConstants.SERVER_CONNECTION_PORT + "\"... ");
            serverAddress = new InetSocketAddress(ServerConstants.SERVER_HOST_NAME, ServerConstants.SERVER_CONNECTION_PORT);
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
                    dispatchingIndex = (short) (++dispatchingIndex % ServerConstants.DEPUTIES_POOL_SIZE);
                }
                catch (AsynchronousCloseException e)
                {
                    logger.printlnRed("CLOSED SERVER CONNECTION CHANNEL");
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
            registry.unbind(ServerConstants.USERS_MANAGER_REMOTE_NAME);
            logger.printlnGreen("UNBOUND");

            // Server closed
            logger.printlnCyan("SERVER SHUTTED DOWN");
        }
        catch (IOException | NotBoundException e)
        {
            throw new Error(e.getMessage().toUpperCase());
        }
        catch (InterruptedException e)
        {
            throw new Error("UNEXPECTED INTERRUPTION");
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
            mainThread.join();
        }
        catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
            throw new Error("During shutdown");
        }
    }
}