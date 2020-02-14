package client.main;

import client.constants.ClientConstants;
import client.gui.WordQuizzleClientFrame;
import client.gui.dialogs.ChallengeDialog;
import client.operators.*;
import constants.Constants;
import messages.Message;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

public class WordQuizzleClient
{
    private static final Thread MAIN_THREAD = Thread.currentThread();
    private static final ReentrantLock TCP_LOCK = new ReentrantLock();
    private static final ByteBuffer BUFFER = ByteBuffer.allocateDirect(2048);
    public static final ExecutorService POOL = Executors.newCachedThreadPool();

    public static DatagramChannel notificationChannel;
    public static SocketChannel server;

    private static boolean shut = false;

    public static void main(String[] args) throws IOException
    {
        // Initialize the TCP connection
        SocketAddress TCPaddress = new InetSocketAddress(ClientConstants.HOST_NAME, ClientConstants.CONNECTION_PORT);
        server = SocketChannel.open(TCPaddress);
        server.configureBlocking(true);

        // Initialize the UDP socket
        SocketAddress UDPaddress = new InetSocketAddress(ClientConstants.HOST_NAME, 0);
        notificationChannel = DatagramChannel.open();
        notificationChannel.bind(UDPaddress);

        // Start gui
        SwingUtilities.invokeLater(WordQuizzleClientFrame::welcome);

        // Client's notifications listening cycle
        while (!shut)
        {
            try
            {
                Message message = Message.readNotification(notificationChannel, BUFFER);

                switch (message.getType())
                {
                    case REQUEST_FOR_FRIENDSHIP_CONFIRMATION:
                        POOL.execute(new ReplyFriendshipRequestOperator(String.valueOf(message.getField(0))));
                        break;
                    case FRIENDSHIP_CONFIRMED:
                        POOL.execute(new FriendshipRequestConfirmedOperator(String.valueOf(message.getField(1))));
                        break;
                    case FRIENDSHIP_DECLINED:
                        POOL.execute(new FriendshipRequestDeclinedOperator(String.valueOf(message.getField(1))));
                        break;
                    case REQUEST_FOR_CHALLENGE_CONFIRMATION:
                        SwingUtilities.invokeLater(() -> {
                            ChallengeDialog.CHALLENGE_DIALOG.requestFrom(String.valueOf(message.getField(0)));
                            ChallengeDialog.CHALLENGE_DIALOG.setVisible(true);
                        });
                        break;
                    case OPPONENT_DID_NOT_REPLY:
                        POOL.execute(new OpponentDidNotReplyOperator(String.valueOf(message.getField(1))));
                        break;
                    case CHALLENGE_REQUEST_TIMEOUT_EXPIRED:
                        POOL.execute(new RequestExpiredOperator());
                        SwingUtilities.invokeLater(() -> ChallengeDialog.CHALLENGE_DIALOG.setVisible(false));
                        break;
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

    public static Message send(Message message)
    {
        Message response = null;

        try
        {
            TCP_LOCK.lock();
            Message.writeMessage(server, BUFFER, message);
            response = Message.readMessage(server, BUFFER);
            TCP_LOCK.unlock();
        }
        catch (IOException | InvalidMessageFormatException e)
        {
            e.printStackTrace();
        }

        return response;

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
