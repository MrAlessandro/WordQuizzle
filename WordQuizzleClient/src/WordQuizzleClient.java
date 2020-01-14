import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

class WordQuizzleClient
{
    private final static int CONNECTION_PORT = 50500;
    private final static String HOST_NAME = "localhost";

    public static void main(String[] args) throws IOException
    {
        //WelcomeFrame gui = new WelcomeFrame();
        //SwingUtilities.invokeLater(gui);

        char[] password = {'1', '2', '3', '4'};
        register("Paolino", password);

        ByteBuffer buffer = ByteBuffer.allocate(2048);

        SocketAddress address = new InetSocketAddress("localhost", CONNECTION_PORT);
        SocketChannel server = SocketChannel.open(address);
        server.configureBlocking(true);
        buffer.putInt(MessageType.LOG_IN.getValue());

        int written = 0;
        buffer.flip();

        while (buffer.hasRemaining())
            written += server.write(buffer);


        buffer.clear();
        String username = new String("Paolino\n");
        buffer.put(username.getBytes());
        buffer.flip();
        while (buffer.hasRemaining())
            server.write(buffer);

        buffer.clear();
        password = new char[]{'1', '2', '3', '4', '\n'};
        buffer.put(toBytes(password));
        buffer.flip();
        while (buffer.hasRemaining())
            server.write(buffer);

        buffer.clear();

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
        } catch (RemoteException | NotBoundException e)
        {
            e.printStackTrace();
        }

        return retValue;
    }

    static byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = Charset.forName("UTF-8").encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
                byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }
}
