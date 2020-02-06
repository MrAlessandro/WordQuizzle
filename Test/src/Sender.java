import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class Sender
{
    public static void main(String[] args) throws IOException
    {
        ByteBuffer buffer = ByteBuffer.allocate(1028);
        DatagramChannel datagramChannel = DatagramChannel.open();
        datagramChannel.socket().bind(new InetSocketAddress("localhost", 0));

        buffer.putInt(((InetSocketAddress) datagramChannel.getLocalAddress()).getPort());
        buffer.flip();
        datagramChannel.send(buffer, new InetSocketAddress("localhost", 9999));

        System.out.println(datagramChannel.getLocalAddress());

        datagramChannel.close();
    }
}
