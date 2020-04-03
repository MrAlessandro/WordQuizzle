package client.main;

import client.settings.Settings;
import commons.loggers.Logger;
import commons.messages.Message;
import commons.messages.exceptions.InvalidMessageFormatException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public class WordQuizzleClient
{
    private static Logger logger;

    private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(2048);
    private static final Thread MAIN_THREAD = Thread.currentThread();
    public static final Thread.UncaughtExceptionHandler ERRORS_HANDLER = (thread, throwable) -> {
        logger.printlnRed("FATAL ERROR FROM THREAD: " + thread.getName() + " ⟶ " + throwable.getMessage());
        StackTraceElement[] stackTraceElements = throwable.getCause().getStackTrace();
        for (int i = stackTraceElements.length - 1; i >= 0 ; i--)
        {
            logger.printlnRed("\t" + stackTraceElements[i]);
        }
        System.exit(1);
    };

    public static DatagramChannel notificationChannel;
    public static SocketChannel server;

    private static boolean shut = false;


    public static void main(String[] args)
    {
        SocketAddress TCPaddress;
        SocketAddress UDPaddress;

        // Setup thread name
        Thread.currentThread().setName("Main");

        // Set error handler for this main thread (current)
        Thread.currentThread().setUncaughtExceptionHandler(ERRORS_HANDLER);

        // Registering a shutdown hook for main thread (current)
        Runtime.getRuntime().addShutdownHook(new Thread(WordQuizzleClient::shutDown));

        System.out.println("Loading properties");
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
        System.out.println("LOADED");

        // Setup logger
        logger = new Logger(Settings.COLORED_LOGS);

        // Server initialization
        logger.printlnCyan("CLIENT INITIALIZATION");

        try
        {
            // Initialize the TCP connection
            logger.print("Opening connection with server... ");
            TCPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, Settings.CONNECTION_PORT);
            server = SocketChannel.open(TCPaddress);
            server.configureBlocking(true);
            logger.printlnGreen("CONNECTED");

            // Initialize the UDP socket
            logger.print("Opening notification channel towards the server... ");
            UDPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, 0);
            notificationChannel = DatagramChannel.open();
            notificationChannel.bind(UDPaddress);
            logger.printlnGreen("OPENED");

            while (!shut)
            {
                try
                {
                    logger.print("Listening for notifications from server... ");
                    Message message = Message.readNotification(notificationChannel, BUFFER);
                    logger.printlnGreen("RECEIVED NOTIFICATION ⟶ " + message);
                }
                catch (AsynchronousCloseException e)
                {
                    logger.printlnYellow("CLOSED NOTIFICATION CHANNEL");
                }
            }

            // Start client's termination process
            logger.printlnCyan("TERMINATING CLIENT");

            // Close TCP socket channel
            logger.print("Closing connection with the server... ");
            server.close();
            logger.printlnGreen("CLOSED");

            // Server closed
            logger.printlnCyan("CLIENT TERMINATED");

        }
        catch (IOException | InvalidMessageFormatException e)
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
            server.close();
            // Close notification channel
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
