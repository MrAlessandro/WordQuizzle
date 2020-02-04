package client.main;

import client.constants.ClientConstants;
import client.gui.WordQuizzleClientFrame;
import constants.Constants;
import messages.Message;
import messages.MessageType;
import messages.exceptions.InvalidMessageFormatException;
import remote.Registrable;
import remote.VoidPasswordException;
import remote.VoidUsernameException;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class WordQuizzleClient
{
    private static ByteBuffer buffer = ByteBuffer.allocate(2048);
    private static SocketChannel server;
    private static SocketAddress address;

    public static void main(String[] args) throws IOException
    {

        address = new InetSocketAddress(ClientConstants.HOST_NAME,ClientConstants.CONNECTION_PORT);
        server = SocketChannel.open(address);
        server.configureBlocking(true);

        //just take the idea of this line
        SwingUtilities.invokeLater(WordQuizzleClientFrame::new);


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
            Message.writeMessage(server, buffer, message);
            message = Message.readMessage(server, buffer);
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

    public static boolean shut()
    {
        try {
            server.close();
        } catch (IOException e) {}

        return true;
    }

}
