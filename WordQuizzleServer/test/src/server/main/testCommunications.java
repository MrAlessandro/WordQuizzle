package server.main;

import commons.messages.Message;
import commons.messages.MessageType;
import commons.remote.Registrable;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.settings.Settings;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class testCommunications
{
    private Thread serverThread;
    private WordQuizzleServer server;
    private ByteBuffer buffer = ByteBuffer.allocate(2048);
    private DatagramChannel notificationChannel = null;
    private SocketChannel serverChannel = null;
    private Registrable remoteUsersManager;

    @BeforeEach
    void setUp() throws ParseException, IOException, AlreadyBoundException
    {
        SocketAddress TCPaddress;
        SocketAddress UDPaddress;

        // Initialize server
        server = new WordQuizzleServer();

        // Ensure debug mode to be enabled
        Settings.DEBUG = true;

        // Lunch server
        serverThread = new Thread(() -> server.run());
        serverThread.start();

        buffer = ByteBuffer.allocate(2048);

        try
        {
            // Initialize TCP socket
            TCPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, Settings.SERVER_CONNECTION_PORT);
            serverChannel = SocketChannel.open(TCPaddress);
            serverChannel.configureBlocking(true);

            // Initialize the UDP socket
            UDPaddress = new InetSocketAddress(Settings.SERVER_HOST_NAME, 0);
            notificationChannel = DatagramChannel.open();
            notificationChannel.bind(UDPaddress);

            // Initialize RMI
            Registry registry = LocateRegistry.getRegistry();
            remoteUsersManager = (Registrable) registry.lookup(Settings.USERS_MANAGER_REMOTE_NAME);
        }
        catch (UnresolvedAddressException | IOException | NotBoundException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

    }

    @AfterEach
    public void shutDownServer() throws InterruptedException, IOException
    {
        server.shutDown();
        serverThread.join();
    }

    @Test
    public void testRegistration()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();

        assertDoesNotThrow(() -> remoteUsersManager.registerUser(username, password));
    }

    @Test
    public void testLogin()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        AtomicReference<InetSocketAddress> clientNotificationChannelAddress = new AtomicReference<>(null);

        assertDoesNotThrow(() -> remoteUsersManager.registerUser(username, password));

        // Preparing login message
        Message loginMessage = new Message(MessageType.LOG_IN, username);
        loginMessage.addField(passwordCopy);
        assertDoesNotThrow(() -> clientNotificationChannelAddress.set((InetSocketAddress) notificationChannel.getLocalAddress()));
        int port = clientNotificationChannelAddress.get().getPort();
        loginMessage.addField(String.valueOf(port).toCharArray());

        assertDoesNotThrow(() -> Message.writeMessage(serverChannel, buffer, loginMessage));
        AtomicReference<Message> responseMessage = new AtomicReference<>(null);
        assertDoesNotThrow(() -> responseMessage.set(Message.readMessage(serverChannel, buffer)));

        assertEquals(MessageType.OK, responseMessage.get().getType());
    }
}
