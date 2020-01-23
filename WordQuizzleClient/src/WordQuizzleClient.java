import messages.Message;
import messages.MessageType;
import messages.exceptions.InvalidMessageFormatException;

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
    private final static int CONNECTION_PORT = 50500;
    private final static String HOST_NAME = "localhost";

    public static void main(String[] args) throws IOException, InvalidMessageFormatException
    {
        //WelcomeFrame gui = new WelcomeFrame();
        //SwingUtilities.invokeLater(gui);

        char[] password = {'1', '2', '3', '4'};

        ByteBuffer buffer = ByteBuffer.allocate(2048);

        SocketAddress address = new InetSocketAddress(HOST_NAME, CONNECTION_PORT);
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
        catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }

        return retValue;
    }

}
