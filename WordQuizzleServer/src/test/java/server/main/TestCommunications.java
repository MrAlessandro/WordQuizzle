package server.main;

import commons.messages.Message;
import commons.messages.MessageType;
import commons.messages.exceptions.InvalidMessageFormatException;
import commons.remote.Registrable;
import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeAll;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class TestCommunications
{
    private static WordQuizzleServer server;

    @BeforeAll
    static void setUp() throws ParseException, IOException, AlreadyBoundException
    {
        // Initialize server
        server = new WordQuizzleServer();

        // Ensure debug mode to be enabled
        Settings.DEBUG = true;

        // Lunch server
        Thread serverThread = new Thread(() -> server.run());
        serverThread.start();
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
    public void testLogOut()
    {
        String username = UUID.randomUUID().toString();
        char[] password = UUID.randomUUID().toString().toCharArray();
        char[] passwordCopy = Arrays.copyOf(password, password.length);
        Client client = new Client();

        assertDoesNotThrow(() -> client.register(username, password));

        AtomicReference<Message> responseMessage = new AtomicReference<>(null);
        assertDoesNotThrow(() -> responseMessage.set(client.logIn(username, passwordCopy)));
        assertEquals(MessageType.OK, responseMessage.get().getType());

        assertDoesNotThrow(() -> responseMessage.set(client.require(new Message(MessageType.LOG_OUT))));
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
    public void testFriendshipDeclination()
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

        // Client2 decline request
        assertDoesNotThrow(() -> responseMessage2.set(client2.require(new Message(MessageType.DECLINE_FRIENDSHIP_REQUEST, username1))));
        assertEquals(MessageType.OK, responseMessage2.get().getType());

        // Client1 receive declination message
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.FRIENDSHIP_REQUEST_DECLINED, notificationMessage1.get().getType());
        assertEquals(username2, String.valueOf(notificationMessage1.get().getFields()[0].getBody()));
    }

    @Test
    @SuppressWarnings("unchecked")
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

        // Parse response
        AtomicReference<JSONArray> serializedFriends = new AtomicReference<>(new JSONArray());
        ArrayList<String> friends = new ArrayList<>();
        JSONParser parser = new JSONParser();
        assertDoesNotThrow(() -> serializedFriends.set((JSONArray) parser.parse(String.valueOf(responseMessage1.get().getFields()[0]))));
        for (String friend : (Iterable<String>) serializedFriends.get())
        {
            friends.add(friend);
        }

        assertEquals(1, friends.size());
        assertEquals(username2, friends.get(0));
    }

    @Test
    public void testChallengeRequestExpiration()
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

        // Client 1 send challenge request to Client 2
        assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.REQUEST_FOR_CHALLENGE, username2))));
        assertEquals(MessageType.OK, responseMessage1.get().getType());

        // Client2 receive the request
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, notificationMessage2.get().getType());
        assertEquals(username1, String.valueOf(notificationMessage2.get().getFields()[0].getBody()));

        // Wait for challenge expiration
        assertDoesNotThrow(() -> Thread.sleep(Settings.CHALLENGE_REQUEST_TIMEOUT * 1000));

        // Both clients receive notification for challenge request expiration
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.CHALLENGE_REQUEST_EXPIRED_APPLICANT, notificationMessage1.get().getType());
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.CHALLENGE_REQUEST_EXPIRED_RECEIVER, notificationMessage2.get().getType());
    }

    @Test
    public void testChallenge()
    {
        // Set longer timeouts for both challenge request and roper challenge
        Settings.CHALLENGE_DURATION_SECONDS = 1000;

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

        // Client 1 send challenge request to Client 2
        assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.REQUEST_FOR_CHALLENGE, username2))));
        assertEquals(MessageType.OK, responseMessage1.get().getType());

        // Client2 receive the request
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, notificationMessage2.get().getType());
        assertEquals(username1, String.valueOf(notificationMessage2.get().getFields()[0].getBody()));

        // Client 2 confirm request
        assertDoesNotThrow(() -> responseMessage2.set(client2.require(new Message(MessageType.CONFIRM_CHALLENGE_REQUEST, username1))));
        assertEquals(MessageType.OK, responseMessage2.get().getType());

        // Client1 receive confirmation message
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.CHALLENGE_REQUEST_CONFIRMED, notificationMessage1.get().getType());
        assertEquals(username2, String.valueOf(notificationMessage1.get().getFields()[0].getBody()));

        // Both clients play the challenge
        for (int i = 0; i < Settings.CHALLENGE_WORDS_QUANTITY; i++)
        {
            // Get word to translate
            assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.CHALLENGE_GET_WORD))));
            assertEquals(MessageType.OK, responseMessage1.get().getType());
            assertDoesNotThrow(() -> responseMessage2.set(client2.require(new Message(MessageType.CHALLENGE_GET_WORD, username1))));
            assertEquals(MessageType.OK, responseMessage2.get().getType());

            // Send translation
            assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.CHALLENGE_PROVIDE_TRANSLATION, "A"))));
            assertTrue(responseMessage1.get().getType() == MessageType.TRANSLATION_WRONG || responseMessage1.get().getType() == MessageType.TRANSLATION_CORRECT);
            assertDoesNotThrow(() -> responseMessage2.set(client2.require(new Message(MessageType.CHALLENGE_PROVIDE_TRANSLATION, "A"))));
            assertTrue(responseMessage2.get().getType() == MessageType.TRANSLATION_WRONG || responseMessage2.get().getType() == MessageType.TRANSLATION_CORRECT);
        }

        // Both clients receive challenge report
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.CHALLENGE_REPORT, notificationMessage1.get().getType());
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.CHALLENGE_REPORT, notificationMessage1.get().getType());
    }

    @Test
    public void testChallengeExpiration()
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

        // Client 1 send challenge request to Client 2
        assertDoesNotThrow(() -> responseMessage1.set(client1.require(new Message(MessageType.REQUEST_FOR_CHALLENGE, username2))));
        assertEquals(MessageType.OK, responseMessage1.get().getType());

        // Client2 receive the request
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, notificationMessage2.get().getType());
        assertEquals(username1, String.valueOf(notificationMessage2.get().getFields()[0].getBody()));

        // Client 2 confirm request
        assertDoesNotThrow(() -> responseMessage2.set(client2.require(new Message(MessageType.CONFIRM_CHALLENGE_REQUEST, username1))));
        assertEquals(MessageType.OK, responseMessage2.get().getType());

        // Client1 receive confirmation message
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.CHALLENGE_REQUEST_CONFIRMED, notificationMessage1.get().getType());
        assertEquals(username2, String.valueOf(notificationMessage1.get().getFields()[0].getBody()));

        // Wait for challenge expiration
        assertDoesNotThrow(() ->Thread.sleep(Settings.CHALLENGE_DURATION_SECONDS * 1000));

        // Both clients receive challenge report
        assertDoesNotThrow(() -> notificationMessage1.set(client1.readNotification()));
        assertEquals(MessageType.CHALLENGE_EXPIRED, notificationMessage1.get().getType());
        assertDoesNotThrow(() -> notificationMessage2.set(client2.readNotification()));
        assertEquals(MessageType.CHALLENGE_EXPIRED, notificationMessage1.get().getType());
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
