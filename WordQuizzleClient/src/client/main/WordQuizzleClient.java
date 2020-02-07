package client.main;

import client.constants.ClientConstants;
import client.gui.WordQuizzleClientFrame;
import client.operators.FriendshipRequestOperator;
import constants.Constants;
import messages.Message;
import messages.MessageType;
import messages.exceptions.InvalidMessageFormatException;
import remote.Registrable;
import remote.VoidPasswordException;
import remote.VoidUsernameException;

import javax.swing.SwingUtilities;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.locks.ReentrantLock;

public class WordQuizzleClient
{
    private static final Thread MAIN_THREAD = Thread.currentThread();
    private static final ReentrantLock TCPlock = new ReentrantLock();
    private static ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
    private static DatagramChannel notificationChannel;
    private static SocketChannel server;

    private static boolean shut = false;

    public static void main(String[] args) throws IOException
    {
        SocketAddress TCPaddress = new InetSocketAddress(ClientConstants.HOST_NAME, ClientConstants.CONNECTION_PORT);
        server = SocketChannel.open(TCPaddress);
        server.configureBlocking(true);

        SocketAddress UDPaddress = new InetSocketAddress(ClientConstants.HOST_NAME, 0);
        notificationChannel = DatagramChannel.open();
        notificationChannel.bind(UDPaddress);

        //just take the idea of this line
        SwingUtilities.invokeLater(WordQuizzleClientFrame::new);

        while (!shut)
        {
            try
            {
                Message message = Message.readNotification(notificationChannel, buffer);

                switch (message.getType())
                {
                    case REQUEST_FOR_FRIENDSHIP:
                    {
                        FriendshipRequestOperator operator = new FriendshipRequestOperator(String.valueOf(message.getField(0)), String.valueOf(message.getField(1)));
                        operator.execute();
                    }
                    case FRIENDSHIP_CONFIRMED:
                    {

                    }
                    case FRIENDSHIP_DECLINED:
                    {

                    }
                    default:
                    {}
                }
            }
            catch (InvalidMessageFormatException e)
            {
                e.printStackTrace();
            }
            catch (AsynchronousCloseException ignored)
            {

            }
        }
    }

    public static boolean register(String username, char[] password)
    {
        boolean retValue;

        try
        {
            Registry r = LocateRegistry.getRegistry();
            Registrable remoteNet = (Registrable) r.lookup(Constants.USERS_DATABASE_REMOTE_NAME);
            retValue = remoteNet.registerUser(username, password);
        }
        catch (RemoteException | NotBoundException | VoidUsernameException | VoidPasswordException e)
        {
            throw new Error("Inconsistency");
        }

        return retValue;
    }

    public static Message logIn(String username, char[] password)
    {
        Message message = new Message(MessageType.LOG_IN, username);
        message.addField(password);


        try
        {
            message.addField(String.valueOf(((InetSocketAddress) notificationChannel.getLocalAddress()).getPort()).toCharArray());

            TCPlock.lock();
            Message.writeMessage(server, buffer, message);
            message = Message.readMessage(server, buffer);
            TCPlock.unlock();
        }
        catch (IOException e)
        {
            throw new Error("Server is unreachable");
        }
        catch (InvalidMessageFormatException e)
        {
            throw new Error("Invalid message received");
        }

        return message;
    }

    public static boolean confirmFriendshipRequest(String from, String to)
    {
        Message message = new Message(MessageType.CONFIRM_FRIENDSHIP, from, to);

        try
        {
            TCPlock.lock();
            Message.writeMessage(server, buffer, message);
            message = Message.readMessage(server, buffer);
            TCPlock.unlock();
        }
        catch (IOException e)
        {
            throw new Error("Server is unreachable");
        }
        catch (InvalidMessageFormatException e)
        {
            throw new Error("Invalid message received");
        }

        return true;
    }

    public static boolean declineFriendshipRequest(String from, String to)
    {
        Message message = new Message(MessageType.DECLINE_FRIENDSHIP, from, to);

        try
        {
            TCPlock.lock();
            Message.writeMessage(server, buffer, message);
            message = Message.readMessage(server, buffer);
            TCPlock.unlock();
        }
        catch (IOException e)
        {
            throw new Error("Server is unreachable");
        }
        catch (InvalidMessageFormatException e)
        {
            throw new Error("Invalid message received");
        }

        return true;
    }

    public void shutDown()
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
