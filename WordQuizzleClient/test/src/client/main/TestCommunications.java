package client.main;

import client.settings.Settings;
import commons.messages.Message;
import commons.messages.exceptions.InvalidMessageFormatException;
import commons.remote.Registrable;
import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

public class TestCommunications
{
    private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(2048);
    public static DatagramChannel notificationChannel = null;
    public static SocketChannel server = null;

    @BeforeAll
    static void setUp()
    {
        SocketAddress TCPaddress;
        SocketAddress UDPaddress;

        try
        {
            Settings.loadProperties();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        TCPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, Settings.CONNECTION_PORT);
        while (server == null)
        {
            try
            {
                System.out.print("Trying to reach the server (REMEMBER TO RUN IT IN DEBUG MODE)... ");
                server = SocketChannel.open(TCPaddress);
                System.out.println("REACHED");

                server.configureBlocking(true);

                // Initialize the UDP socket
                UDPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, 0);
                notificationChannel = DatagramChannel.open();
                notificationChannel.bind(UDPaddress);
            }
            catch (UnresolvedAddressException | AsynchronousCloseException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
            catch (IOException e)
            {
                try
                {
                    System.out.println("SERVER NOT REACHED, RUN IT! ⟶ NEW CONNECTION ATTEMPT IN 1 SECOND");
                    Thread.sleep(1000);
                }
                catch (InterruptedException ex)
                {
                    break;
                }
            }
        }

    }

    public void register(String username, char[] password) throws VoidUsernameException, VoidPasswordException, UsernameAlreadyUsedException
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
            synchronized (BUFFER)
            {
                Message.writeMessage(server, BUFFER, message);
                response = Message.readMessage(server, BUFFER);
            }
        }
        catch (IOException | InvalidMessageFormatException e)
        {
            e.printStackTrace();
        }

        return response;

    }

    @Test
    public void testRegistration()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();

        try
        {
            register(username, password);
        }
        catch (UsernameAlreadyUsedException | VoidPasswordException | VoidUsernameException e)
        {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Test
    public void testLogIn()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
    }


}
