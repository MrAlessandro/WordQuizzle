package client.main;

import client.constants.ClientConstants;
import messages.Message;
import messages.MessageType;
import messages.exceptions.InvalidMessageFormatException;
import remote.Registrable;
import remote.VoidPasswordException;
import remote.VoidUsernameException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

class WordQuizzleClient
{
    public static void main(String[] args) throws IOException, InvalidMessageFormatException
    {
        //client.gui.WelcomeFrame client.gui = new client.gui.WelcomeFrame();
        //SwingUtilities.invokeLater(client.gui);

        char[] password = {'1', '2', '3', '4'};

        ByteBuffer buffer = ByteBuffer.allocate(2048);

        SocketAddress address = new InetSocketAddress(ClientConstants.HOST_NAME,ClientConstants.CONNECTION_PORT);
        SocketChannel server = SocketChannel.open(address);
        server.configureBlocking(true);
        buffer.putInt(MessageType.LOG_IN.getValue());

        Message message = new Message(MessageType.LOG_IN, "Alessandro");
        message.addField(password);

        System.out.println("Sending message: " + message.toString());
        Message.writeMessage(server, buffer, message);

        message = Message.readMessage(server, buffer);

        assert message != null;
        System.out.println("Received message: "+ message.toString());

        server.close();
    }

    protected static boolean register(String username, char[] password)
    {
        boolean retValue = false;

        try
        {
            Registry r = LocateRegistry.getRegistry();
            Registrable remoteNet = (Registrable) r.lookup("WordQuizzleServer");
            retValue = remoteNet.registerUser(username, password);
        }
        catch (RemoteException | NotBoundException | VoidUsernameException | VoidPasswordException e)
        {
            e.printStackTrace();
        }

        return retValue;
    }

}
