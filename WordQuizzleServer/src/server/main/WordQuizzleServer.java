package server.main;

import constants.Constants;
import remote.Registrable;
import server.constants.ServerConstants;
import server.printer.Printer;
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
    private static final Printer PRINTER = new Printer("Main");
    public static volatile boolean shut = false;
    private static ServerSocketChannel serverSocket;
    private static Thread mainThread;

    public static void main(String[] args)
    {

        InetSocketAddress serverAddress;
        Deputy[] deputies;
        Registrable stub;
        Registry registry;

        Thread.currentThread().setName("Main");

        PRINTER.printCyan("Initializing WordQuizzle server... ");

        // Setting socket address on which server listens for incoming connections
        serverAddress = new InetSocketAddress(ServerConstants.HOST_NAME, ServerConstants.CONNECTION_PORT);

        // Registering a shutdown hook
        mainThread = Thread.currentThread();
        Runtime.getRuntime().addShutdownHook(new Thread(new Shutter()));

        // Restoring previous server state
        UsersManager.restore();

        // Initialize and starts deputies
        deputies = new Deputy[ServerConstants.DEPUTIES_POOL_SIZE];
        for (int i = 0; i < deputies.length; i++)
        {
            deputies[i] = new Deputy("Deputy_" + i+1, ServerConstants.UDP_BASE_PORT+i);
            deputies[i].start();
        }


        try
        {
            // Variable for select deputies sequentially
            short dispatchingIndex = 0;

            // Enabling RMI support for registration operation
            stub = (Registrable) UnicastRemoteObject.exportObject(UsersManager.getInstance(), 0);
            LocateRegistry.createRegistry(Constants.USERS_DATABASE_REGISTRY_PORT);
            registry = LocateRegistry.getRegistry(Constants.USERS_DATABASE_REGISTRY_PORT);
            registry.bind(Constants.USERS_DATABASE_REMOTE_NAME, stub);

            // Opening server's connection socket
            serverSocket = ServerSocketChannel.open();
            serverSocket.bind(serverAddress);

            PRINTER.printlnGreen("INITIALIZED");

            // Start server listening cycle
            while (!shut)
            {
                PRINTER.print("Waiting for new connections... ");

                try
                {
                    // Wait for connection
                    SocketChannel connection = serverSocket.accept();
                    PRINTER.printlnGreen("ACCEPTED");

                    // Dispatch the socket to be served to one of the deputies
                    deputies[dispatchingIndex].dispatch.add(connection);
                    deputies[dispatchingIndex].selector.wakeup();
                    dispatchingIndex = (short) (++dispatchingIndex % ServerConstants.DEPUTIES_POOL_SIZE);
                }
                catch (AsynchronousCloseException ignored)
                {
                    PRINTER.printlnYellow("STOP TO INCOMING CONNECTIONS");
                }
            }

            PRINTER.printCyan("Server is shutting down... ");

            // Signal to terminate to others threads and wait for them to do so
            for (Deputy deputy : deputies)
            {
                deputy.shutDown();
                deputy.join();
            }

            // Unbind the RMI service
            registry.unbind(Constants.USERS_DATABASE_REMOTE_NAME);
            // Close the server's connection socket
            serverSocket.close();
        }
        catch (IOException | InterruptedException | AlreadyBoundException | NotBoundException ignored)
        {
            System.out.println("Useless to close again");
        }

        // Backup users system in order to make it persistent
        //UsersManager.backUp();

        PRINTER.printlnGreen("OFF");
    }


    public static void shutDown()
    {
        // Set the shutdown flag
        shut = true;

        try
        {
            // Close the server's connection socket
            serverSocket.close();
            // Wait for main thread to finish
            mainThread.join();
        }
        catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
            throw new Error("During shutdown");
        }
    }
}