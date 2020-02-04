package server.main;

import remote.Registrable;
import server.constants.ServerConstants;
import server.printer.Printer;
import server.users.UsersManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.rmi.registry.Registry;

class WordQuizzleServer
{
    private static final Printer PRINTER = new Printer("Main");
    public static volatile boolean shut = false;

    public static void main(String[] args)
    {
        ServerSocketChannel serverSocket;
        InetSocketAddress serverAddress;
        Deputy[] deputies;
        Registrable stub;
        Registry registry;

        Thread.currentThread().setName("Main");

        PRINTER.printCyan("Initializing WordQuizzle server... ");

        // Setting socket address on which server listens for incoming connections
        serverAddress = new InetSocketAddress(ServerConstants.HOST_NAME, ServerConstants.CONNECTION_PORT);

        // Registering a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutter()));

        // Restoring previous server state
        UsersManager.restore();

        // Initialize and starts deputies
        deputies = new Deputy[ServerConstants.DEPUTIES_POOL_SIZE];
        for (int i = 0; i < deputies.length; i++)
        {
            deputies[i] = new Deputy("Deputy_" + i);
            deputies[i].start();
        }


        try
        {
            // Variable for select deputies sequentially
            short selector = 0;

            // Enabling RMI support for registration operation
            /*
            stub = (Registrable) UnicastRemoteObject.exportObject(UsersManager.getInstance(), 0);
            LocateRegistry.createRegistry(Constants.USERS_DATABASE_REGISTRY_PORT);
            registry = LocateRegistry.getRegistry(Constants.USERS_DATABASE_REGISTRY_PORT);
            registry.bind(Constants.USERS_DATABASE_REMOTE_NAME, stub);
            */

            // Opening server's connection socket
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(serverAddress);

            PRINTER.printlnGreen("INITIALIZED");

            // Start server listening cycle
            while (!isShutDown())
            {
                // Wait for connection
                PRINTER.print("Waiting for new connections... ");
                SocketChannel connection = serverSocket.accept();
                PRINTER.printlnGreen("ACCEPTED");

                // Dispatch the socket to be served to one of the deputies
                deputies[selector++].dispatch.add(connection);
                selector = (short) (selector % ServerConstants.DEPUTIES_POOL_SIZE);
            }

            PRINTER.printCyan("Server is shutting down");

            // Signal to terminate to others threads
            Deputy.shutDown();

            // Waiting for other threads to terminate
            for (Deputy deputy : deputies)
                deputy.join();

            // Unbind the RMI service
            //registry.unbind(Constants.USERS_DATABASE_REMOTE_NAME);
            // Close the server's connection socket
            serverSocket.close();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }

        // Backup users system in order to make it persistent
        UsersManager.backUp();

        PRINTER.printlnGreen("OFF");
    }

    public static synchronized void shutDown()
    {
        shut = true;
    }

    private static synchronized boolean isShutDown()
    {
        return shut;
    }

}