import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Receiver
{
    public static void main(String[] args) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(1028);
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.socket().bind(new InetSocketAddress(9999));

        SocketAddress senderAddress = datagramChannel.receive(buffer);
        buffer.flip();
        int port = buffer.getInt();

        System.out.println(((InetSocketAddress) senderAddress).getPort());
        System.out.println(port);

        datagramChannel.close();
    }
}
