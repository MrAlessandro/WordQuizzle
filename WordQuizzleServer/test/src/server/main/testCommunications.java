package server.main;

import commons.messages.Message;
import commons.messages.MessageType;
import commons.messages.exceptions.InvalidMessageFormatException;
import commons.remote.Registrable;
import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;
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
import java.rmi.RemoteException;
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

    @BeforeEach
    void setUp() throws ParseException, IOException, AlreadyBoundException
    {
        // Initialize server
        server = new WordQuizzleServer();

        // Ensure debug mode to be enabled
        Settings.DEBUG = true;

        // Lunch server
        serverThread = new Thread(() -> server.run());
        serverThread.start();
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
        Client client = new Client();

        assertDoesNotThrow(() -> client.register(username, password));
    }

    @Test
    public void testLogin()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        Client client = new Client();

        assertDoesNotThrow(() -> client.register(username, password));

        AtomicReference<Message> responseMessage = new AtomicReference<>(null);
        assertDoesNotThrow(() -> responseMessage.set(client.logIn(username, passwordCopy)));

        assertEquals(MessageType.OK, responseMessage.get().getType());
    }

    @Test
    public void testFriendshipCreation()
    {
        String username1 = UUID.randomUUID().toString();
        char[] password1 = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
        AtomicReference<Message> responseMessage1 = new AtomicReference<>(null);
        AtomicReference<Message> notificationMessage1 = new AtomicReference<>(null);
        Client client1 = new Client();

        String username2 = UUID.randomUUID().toString();
        char[] password2 = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
        AtomicReference<Message> responseMessage2 = new AtomicReference<>(null);
        AtomicReference<Message> notificationMessage2 = new AtomicReference<>(null);
        Client client2 = new Client();

        // Register and login client 1
        assertDoesNotThrow(() -> client1.register(username1, password1));
        assertDoesNotThrow(() -> responseMessage1.set(client1.logIn(username1, passwordCopy1)));
        assertEquals(MessageType.OK, responseMessage1.get().getType());

        // Register and login client 2
        assertDoesNotThrow(() -> client2.register(username2, password2));
        assertDoesNotThrow(() -> responseMessage2.set(client2.logIn(username2, passwordCopy2)));
        assertEquals(MessageType.OK, responseMessage2.get().getType());

        // Client1 send friendship request to client2
        assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.REQUEST_FOR_FRIENDSHIP, username2))));
        assertEquals(MessageType.OK, responseMessage1.get().getType());

        // Client2 receive the request
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, notificationMessage2.get().getType());
        assertEquals(username1, String.valueOf(notificationMessage2.get().getFields()[0].getBody()));

        // Client2 confirm request
        assertDoesNotThrow(() -> responseMessage2.set(client2.require(new Message(MessageType.CONFIRM_FRIENDSHIP_REQUEST, username1))));
        assertEquals(MessageType.OK, responseMessage2.get().getType());

        // Client1 receive confirmation message
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.FRIENDSHIP_REQUEST_CONFIRMED, notificationMessage1.get().getType());
        assertEquals(username2, String.valueOf(notificationMessage1.get().getFields()[0].getBody()));
    }

    @Test
    public void testFriendsListRetrieval()
    {
        String username1 = UUID.randomUUID().toString();
        char[] password1 = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy1 = Arrays.copyOf(password1, password1.length);
        AtomicReference<Message> responseMessage1 = new AtomicReference<>(null);
        AtomicReference<Message> notificationMessage1 = new AtomicReference<>(null);
        Client client1 = new Client();

        String username2 = UUID.randomUUID().toString();
        char[] password2 = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy2 = Arrays.copyOf(password2, password2.length);
        AtomicReference<Message> responseMessage2 = new AtomicReference<>(null);
        AtomicReference<Message> notificationMessage2 = new AtomicReference<>(null);
        Client client2 = new Client();

        // Register and login client 1
        assertDoesNotThrow(() -> client1.register(username1, password1));
        assertDoesNotThrow(() -> responseMessage1.set(client1.logIn(username1, passwordCopy1)));
        assertEquals(MessageType.OK, responseMessage1.get().getType());

        // Register and login client 2
        assertDoesNotThrow(() -> client2.register(username2, password2));
        assertDoesNotThrow(() -> responseMessage2.set(client2.logIn(username2, passwordCopy2)));
        assertEquals(MessageType.OK, responseMessage2.get().getType());

        // Client1 send friendship request to client2
        assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.REQUEST_FOR_FRIENDSHIP, username2))));
        assertEquals(MessageType.OK, responseMessage1.get().getType());

        // Client2 receive the request
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, notificationMessage2.get().getType());
        assertEquals(username1, String.valueOf(notificationMessage2.get().getFields()[0].getBody()));

        // Client2 confirm request
        assertDoesNotThrow(() -> responseMessage2.set(client2.require(new Message(MessageType.CONFIRM_FRIENDSHIP_REQUEST, username1))));
        assertEquals(MessageType.OK, responseMessage2.get().getType());

        // Client1 receive confirmation message
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.FRIENDSHIP_REQUEST_CONFIRMED, notificationMessage1.get().getType());
        assertEquals(username2, String.valueOf(notificationMessage1.get().getFields()[0].getBody()));

        // ---> User1 and user2 now are friends

        // Client1 require friends list
        assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.REQUEST_FOR_FRIENDS_LIST))));
        assertEquals(MessageType.FRIENDS_LIST, responseMessage1.get().getType());

        System.out.println(responseMessage1.get()); /*TODO*/
    }

    static class Client
    {
        private final ByteBuffer buffer;
        private DatagramChannel notificationChannel;
        private SocketChannel serverChannel;
        private Registrable remoteUsersManager;

        public Client()
        {
            SocketAddress TCPaddress;
            SocketAddress UDPaddress;

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

        public void register(String username, char[] password) throws RemoteException, VoidUsernameException, UsernameAlreadyUsedException, VoidPasswordException
        {
            remoteUsersManager.registerUser(username, password);
        }

        public Message logIn(String username, char[] password) throws IOException, InvalidMessageFormatException
        {
            // Preparing login message
            Message loginMessage = new Message(MessageType.LOG_IN, username);
            loginMessage.addField(password);
            loginMessage.addField(String.valueOf(((InetSocketAddress) notificationChannel.getLocalAddress()).getPort()).toCharArray());

            Message.writeMessage(serverChannel, buffer, loginMessage);
            return Message.readMessage(serverChannel, buffer);
        }

        public Message require(Message request) throws IOException, InvalidMessageFormatException
        {
            Message.writeMessage(serverChannel, buffer, request);
            return Message.readMessage(serverChannel, buffer);
        }

        public Message readNotification() throws IOException, InvalidMessageFormatException
        {
            return Message.readNotification(notificationChannel, buffer);
        }
    }
}
