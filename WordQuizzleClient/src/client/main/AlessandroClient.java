package client.main;

import client.constants.ClientConstants;
import messages.Message;
import messages.MessageType;
import messages.exceptions.InvalidMessageFormatException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;

public class AlessandroClient
{
    public static void main(String[] args) throws IOException, InvalidMessageFormatException
    {
        char[] password = {'1', '2', '3', '4'};

        ByteBuffer buffer = ByteBuffer.allocate(2048);

        SocketAddress address = new InetSocketAddress(ClientConstants.HOST_NAME,ClientConstants.CONNECTION_PORT);
        SocketChannel server = SocketChannel.open(address);
        server.configureBlocking(true);

        DatagramChannel udpChannel = DatagramChannel.open();
        udpChannel.socket().bind(new InetSocketAddress(ClientConstants.HOST_NAME, 0));

        buffer.putInt(MessageType.LOG_IN.getValue());

        Message message = new Message(MessageType.LOG_IN, "Alessandro");
        message.addField(password);
        message.addField(String.valueOf(((InetSocketAddress) udpChannel.getLocalAddress()).getPort()).toCharArray());

        System.out.println("Sending message: " + message.toString());
        Message.writeMessage(server, buffer, message);

        message = Message.readMessage(server, buffer);
        System.out.println("Received message: "+ message.toString());

        message = new Message(MessageType.REQUEST_FOR_FRIENDSHIP, "Alessandro", "Andrea");
        System.out.println("Sending message: " + message.toString());
        Message.writeMessage(server, buffer, message);

        message = Message.readMessage(server, buffer);
        System.out.println("Received message: "+ message.toString());

        message = Message.readNotification(udpChannel, buffer);
        System.out.println("Received message: "+ message.toString());


        server.close();
    }
}
