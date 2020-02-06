package server.main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

public class LocalRemoteAddressTest
{
    public static void main(String[] args) throws IOException
    {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("www.google.com", 80));
        System.out.println(socketChannel.getLocalAddress());
        System.out.println(socketChannel.getRemoteAddress());
        socketChannel.close();
    }
}
