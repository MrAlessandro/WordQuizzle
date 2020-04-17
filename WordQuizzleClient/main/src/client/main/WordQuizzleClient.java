package client.main;

import client.gui.WordQuizzleClientFrame;
import client.operators.FriendshipRequestConfirmedOperator;
import client.operators.FriendshipRequestDeclinedOperator;
import client.operators.ReplyFriendshipRequestOperator;
import client.settings.Settings;
import commons.loggers.Logger;
import commons.messages.Message;
import commons.messages.exceptions.InvalidMessageFormatException;
import commons.remote.Registrable;
import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.atomic.AtomicReference;

public class WordQuizzleClient
{
    public static final AtomicReference<String> SESSION_USERNAME = new AtomicReference<>();

    private static Logger logger;

    private static final ByteBuffer TCP_BUFFER = ByteBuffer.allocateDirect(2048);
    private static final ByteBuffer UDP_BUFFER = ByteBuffer.allocateDirect(2048);
    private static final Thread MAIN_THREAD = Thread.currentThread();
    public static final Thread.UncaughtExceptionHandler ERRORS_HANDLER = (thread, throwable) -> {
        if (logger != null)
        {
            logger.printlnRed("FATAL ERROR ⟶ " + (throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getName()));
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
            System.out.println("FATAL ERROR ⟶ " + (throwable.getMessage() != null ? throwable.getMessage() : throwable.getClass().getName()));
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
    };

    public static DatagramChannel notificationChannel = null;
    public static SocketChannel server = null;

    private static boolean shut = false;


    public static void main(String[] args)
    {
        WordQuizzleClientFrame frame;
        SocketAddress TCPaddress;
        SocketAddress UDPaddress;

        // Setup thread name
        Thread.currentThread().setName("Main");

        // Set error handler for this main thread (current)
        Thread.currentThread().setUncaughtExceptionHandler(ERRORS_HANDLER);

        // Registering a shutdown hook for main thread (current)
        Runtime.getRuntime().addShutdownHook(new Thread(WordQuizzleClient::shutDown));

        // Load client properties
        try
        {
            Settings.loadProperties();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        // Setup logger
        logger = new Logger(Settings.COLORED_LOGS, false);

        // Server initialization
        logger.printlnCyan("CLIENT INITIALIZATION");

        // Initialize gui
        logger.print("Initializing GUI... ");
        frame = new WordQuizzleClientFrame();
        logger.printlnGreen("INITIALIZED");

        try
        {
            // Initialize the TCP connection
            SwingUtilities.invokeLater(() -> frame.loading("Connecting to WordQuizzleServer... "));
            TCPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, Settings.CONNECTION_PORT);
            logger.print("Opening connection with server at \"" + TCPaddress + "\"... ");
            while (server == null && !shut)
            {
                try
                {
                    server = SocketChannel.open(TCPaddress);
                    server.configureBlocking(true);
                    logger.printlnGreen("CONNECTED");
                    SwingUtilities.invokeLater(frame::welcome);

                    // Initialize the UDP socket
                    logger.print("Opening notification channel towards the server... ");
                    UDPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, 0);
                    notificationChannel = DatagramChannel.open();
                    notificationChannel.bind(UDPaddress);
                    logger.printlnGreen("OPENED");
                }
                catch (UnresolvedAddressException | AsynchronousCloseException e)
                {
                    throw new Error(e.getMessage().toUpperCase(), e);
                }
                catch (IOException e)
                {
                    logger.printlnYellow(e.getMessage().toUpperCase() + " ⟶  New connection attempt in 5 seconds");
                    try
                    {
                        Thread.sleep(5000);
                        logger.print("Trying again... ");
                    }
                    catch (InterruptedException ex)
                    {
                        break;
                    }
                }
            }

            while (!shut)
            {
                try
                {
                    logger.print("Listening for notifications from server... ");
                    Message message = Message.readNotification(notificationChannel, UDP_BUFFER);
                    logger.printlnGreen("RECEIVED NOTIFICATION ⟶ " + message);

                    switch (message.getType())
                    {
                        case REQUEST_FOR_FRIENDSHIP_CONFIRMATION:
                            (new ReplyFriendshipRequestOperator(frame, String.valueOf(message.getFields()[0].getBody()))).execute();
                            break;
                        case FRIENDSHIP_REQUEST_CONFIRMED:
                            (new FriendshipRequestConfirmedOperator(frame,String.valueOf(message.getFields()[0].getBody()))).execute();
                            break;
                        case FRIENDSHIP_REQUEST_DECLINED:
                            (new FriendshipRequestDeclinedOperator(frame,String.valueOf(message.getFields()[0].getBody()))).execute();
                            break;
                        case REQUEST_FOR_CHALLENGE_CONFIRMATION:
                            break;
                        case CHALLENGE_REQUEST_CONFIRMED:
                            break;
                        case CHALLENGE_REQUEST_DECLINED:
                            break;
                        case CHALLENGE_REQUEST_EXPIRED:
                            break;
                        case CHALLENGE_REQUEST_OPPONENT_LOGGED_OUT:
                            break;
                        case CHALLENGE_EXPIRED:
                            break;
                        case CHALLENGE_REPORT:
                            break;
                        case CHALLENGE_OPPONENT_LOGGED_OUT:
                            break;
                        default:
                        {}
                    }

                }
                catch (AsynchronousCloseException e)
                {
                    logger.printlnYellow("CLOSED NOTIFICATION CHANNEL");
                }
            }

            // Start client's termination process
            logger.printlnCyan("TERMINATING CLIENT");

            // Close TCP socket channel
            if (server != null)
            {
                logger.print("Closing connection with the server... ");
                server.close();
                logger.printlnGreen("CLOSED");
            }

            // Server closed
            logger.printlnCyan("CLIENT TERMINATED");

        }
        catch (IOException | InvalidMessageFormatException e)
        {
            throw new Error(e.getMessage().toUpperCase(), e);
        }
    }

    public static void register(String username, char[] password) throws VoidUsernameException, VoidPasswordException, UsernameAlreadyUsedException
    {
        try
        {
            Registry r = LocateRegistry.getRegistry();
            Registrable remoteNet = (Registrable) r.lookup(Settings.USERS_MANAGER_REMOTE_NAME);
            remoteNet.registerUser(username, password);
        }
        catch (RemoteException | NotBoundException e)
        {
            throw new Error("Inconsistency");
        }
    }

    public static Message require(Message message)
    {
        Message response = null;

        try
        {
            synchronized (TCP_BUFFER)
            {
                Message.writeMessage(server, TCP_BUFFER, message);
                response = Message.readMessage(server, TCP_BUFFER);
            }
        }
        catch (IOException | InvalidMessageFormatException e)
        {
            e.printStackTrace();
        }

        return response;

    }

    public static void shutDown()
    {
        // Set the shutdown flag
        shut = true;

        try
        {
            // Interrupt main thread if sleeping is sleeping
            if (MAIN_THREAD.getState() == Thread.State.TIMED_WAITING)
                MAIN_THREAD.interrupt();
            // Close notification channel
            if (notificationChannel != null)
                notificationChannel.close();
            // Wait for main thread to finish
            MAIN_THREAD.join();
        }
        catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
            throw new Error("During shutdown");
        }
    }
}
