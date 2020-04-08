package server.main;

import commons.exceptions.CommunicableException;
import commons.messages.Message;
import commons.messages.MessageType;
import commons.messages.exceptions.InvalidMessageFormatException;
import commons.messages.exceptions.UnexpectedMessageException;
import org.json.simple.JSONArray;
import server.settings.Settings;
import commons.loggers.Logger;
import server.sessions.SessionsManager;
import server.sessions.session.Session;
import server.users.UsersManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Deputy extends Thread
{
    private static final AtomicBoolean SHUT = new AtomicBoolean(false);

    protected final ConcurrentLinkedQueue<SocketChannel> dispatch = new ConcurrentLinkedQueue<>();

    private final ByteBuffer buffer = ByteBuffer.allocateDirect(Settings.BUFFERS_SIZE);

    private Selector selector;

    private DatagramChannel UDPchannel;
    private int UDPport;

    public Logger logger;

    // Managers
    private UsersManager usersManager;
    private SessionsManager sessionsManager;

    public Deputy(String name, int UDPport, UsersManager usersManager, SessionsManager sessionsManager)
    {
        super(name);

        // Set the handler for uncaught exception
        this.setUncaughtExceptionHandler(WordQuizzleServer.ERRORS_HANDLER);

        try
        {
            if (Settings.LOG_FILES)
                // Create thread specific logger with related log file
                this.logger = new Logger(Settings.COLORED_LOGS, name, Settings.LOG_FILES_PATH);
            else
                // Create thread specific logger
                this.logger = new Logger(Settings.COLORED_LOGS);
        }
        catch (IOException e)
        {
            throw new Error("ERROR CREATING LOGGER", e);
        }

        // Assign UDP port
        this.UDPport = UDPport;

        // Set managers
        this.usersManager = usersManager;
        this.sessionsManager = sessionsManager;

        try
        {
            this.logger.printlnCyan("DEPUTY INITIALIZATION");

            // Open the selector which detects ready operations on the registered connections
            this.logger.print("Opening selector... ");
            this.selector = Selector.open();
            this.logger.printlnGreen("OPENED");

            // Open UDP socket
            this.logger.print("Opening deputy specific UDP channel on port " + UDPport + "... ");
            this.UDPchannel = DatagramChannel.open();
            // Set address for UDP socket
            this.UDPchannel.socket().bind(new InetSocketAddress(UDPport));
            this.logger.printlnGreen("OPENED");

            this.logger.printlnCyan("DEPUTY INITIALIZED");
        }
        catch (IOException e)
        {
            this.logger.printlnRed("FAILED");
            this.logger.printlnRed(e.getStackTrace());
            throw new Error("ERROR INITIALIZING DEPUTY \"" + Thread.currentThread().getName() + "\"", e);
        }
    }

    @Override
    public void run()
    {
        // Deputy listening cycle
        this.logger.printlnCyan("DEPUTY STARTED");
        while (!SHUT.get())
        {
            // Extract any eventual new incoming connections
            SocketChannel incoming;
            while ((incoming = dispatch.poll()) != null)
            {// There are new connections
                try
                {
                    this.logger.print("Registering new connection to selector... ");

                    // Configure as non-blocking
                    incoming.configureBlocking(false);
                    // Register to be selected
                    incoming.register(selector, SelectionKey.OP_READ, null);

                    this.logger.printlnGreen("REGISTERED ⟶ " +  incoming.getRemoteAddress());
                }
                catch (ClosedChannelException e)
                {
                    this.logger.printlnRed("CLOSED UNEXPECTEDLY");
                    this.logger.printlnRed(e.getStackTrace());
                }
                catch (IOException e)
                {
                    this.logger.printlnRed("FAILED");
                    this.logger.printlnRed(e.getStackTrace());
                    throw new Error("ERROR REGISTERING CHANNEL IN DEPUTY \"" + Thread.currentThread().getName() + "\"", e);
                }
            }

            try
            {
                this.logger.print("Selecting channels... ");

                // Channel selection to detect ready channels
                int ready = selector.select();

                this.logger.printlnGreen("SELECTED " + ready + " CHANNELS");
            }
            catch (IOException e)
            {
                this.logger.printlnRed("FAILED");
                this.logger.printlnRed(e.getStackTrace());
                throw new Error("ERROR SELECTING CHANNELS IN DEPUTY \"" + Thread.currentThread().getName() + "\"", e);
            }


            // Iterate the ready channels set
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext())
            {// Serve one ready channel per time
                SelectionKey currentKey = iter.next();
                // Remove the current serving channel from the ready set, in order to prevent subsequent takes
                iter.remove();

                // Check if the serving channel is readable
                if (currentKey.isValid() && currentKey.isReadable())
                    receive(currentKey);

                // Check if the serving channel is writable
                if (currentKey.isValid() && currentKey.isWritable())
                    send(currentKey);
            }

            // Checking if some server.users relative to the registered channels have pending messages
            for (SelectionKey currentKey : selector.keys())
            {
                if (currentKey.isValid())
                {
                    if ((currentKey.attachment() instanceof Session && ((Session) currentKey.attachment()).hasPendingMessages()) || (currentKey.attachment() instanceof Message))
                        currentKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    else
                        currentKey.interestOps(SelectionKey.OP_READ);
                }
            }
        }

        // Deputy termination
        this.logger.printlnCyan("SHUTTING DOWN DEPUTY");

        // Closing all channels
        if (!selector.keys().isEmpty())
        {
            try
            {
                this.logger.print("Closing channels... ");
                for (SelectionKey key : selector.keys())
                {
                    if (key.isValid())
                    {
                        // Check if channel is bind with a session, if yes close it.
                        if (key.attachment() instanceof Session)
                        {
                            this.logger.print("\tClosing session with \"" + ((Session) key.attachment()).getUsername() + "\"... ");
                            this.sessionsManager.terminateSession(((Session) key.attachment()));
                            this.logger.printlnGreen("CLOSED");
                        }

                        // Close the channel
                        this.logger.print("\tClosing channel \"" + ((SocketChannel) key.channel()).getRemoteAddress() + "\"... ");
                        key.cancel();
                        key.channel().close();
                        this.logger.printlnGreen("CLOSED");
                    }
                }
            }
            catch (IOException e)
            {
                this.logger.printlnRed("ERROR CLOSING CHANNEL");
                this.logger.printlnRed(e.getStackTrace());
                throw new Error("ERROR SHUTTING DOWN DEPUTY \"" + Thread.currentThread().getName() + "\"", e);
            }
        }

        try
        {
            // Close selector
            this.logger.print("Closing selector... ");
            this.selector.close();
            this.logger.printlnGreen("CLOSED");

            // Close UDP channel
            this.logger.print("Closing deputy specific UDP channel on port " + UDPport + "... ");
            this.UDPchannel.close();
            this.logger.printlnGreen("CLOSED");
        }
        catch (IOException e)
        {
            this.logger.printlnRed("FAILED");
            this.logger.printlnRed(e.getStackTrace());
            throw new Error("ERROR SHUTTING DOWN DEPUTY \"" + Thread.currentThread().getName() + "\"", e);
        }

        // Deputy closed
        this.logger.printlnCyan("DEPUTY SHUTTED DOWN");
    }

    public void receive(SelectionKey selected)
    {
        SocketChannel client = (SocketChannel) selected.channel();
        Session session = null;
        Message message;

        try
        {
            if (selected.attachment() instanceof Session)
            {
                // Get the related session
                session = (Session) selected.attachment();
                this.logger.print("Reading message from \"" + ((Session) selected.attachment()).getUsername() + "\"... ");
            }
            else
                this.logger.print("Reading message from the client on " + client.getRemoteAddress() + "... ");

            // Reading message
            message = Message.readMessage(client, buffer);
            this.logger.printlnGreen("READ ⟶ " + message.toString());

            // Check the correctness of the message
            this.logger.print("Checking message format... ");
            message.checkValidity();
            if (!message.getType().isRequest())
                throw new UnexpectedMessageException("RECEIVED A NON REQUEST MESSAGE");
            if (message.getType() == MessageType.LOG_IN && selected.attachment() != null)
                throw new UnexpectedMessageException("RECEIVED A NON LOGIN MESSAGE FROM A CONNECTION WHICH HAS NO SESSION RELATED");
            else if (message.getType() != MessageType.LOG_IN && !(selected.attachment() instanceof Session))
                throw new UnexpectedMessageException("RECEIVED LOGIN MESSAGE FROM A CONNECTION WHICH HAS A SESSION RELATED");
            this.logger.printlnGreen("CHECKED");
        }
        catch (IOException e)
        {// IOException has been thrown during reading

            this.logger.printlnRed("FAILED");

            if (selected.attachment() instanceof Session)
            {// Exception has been thrown reading from a connection with a logged client
                // Close session
                this.logger.print("Closing session with user \"" + ((Session) selected.attachment()).getUsername() + "\"... ");
                this.sessionsManager.closeSession((Session) selected.attachment());
                this.logger.printlnRed("CLOSED");
            }

            // Close connection
            try
            {
                this.logger.print("Closing connection with the client at " + ((SocketChannel) selected.channel()).getRemoteAddress() + "... ");
                selected.cancel();
                client.close();
                this.logger.printlnRed("CLOSED");
            }
            catch (IOException ex)
            {// IOException closing connection. Just report it.
                this.logger.printlnRed("FAILED");
            }

            return;
        }
        catch (UnexpectedMessageException | InvalidMessageFormatException e)
        {// UnexpectedMessageException or UnexpectedMessageException has been thrown during reading or validation
            this.logger.printlnRed(e.getMessage());

            if (selected.attachment() instanceof Session)
                ((Session) selected.attachment()).prependMessage(new Message(e.getResponseType()));
            else
                selected.attach(new Message(e.getResponseType()));

            return;
        }

        // Select behaviour according to the message type of the received message
        switch (message.getType())
        {
            case LOG_IN:
            {
                try
                {
                    // Extract fields from message
                    String username = String.valueOf(message.getFieldDataAt(0));
                    char[] password = message.getFieldDataAt(1);
                    int UDPclientPort = Integer.parseInt(String.valueOf(message.getFieldDataAt(2)));

                    // Get client address
                    InetAddress clientAddress = ((InetSocketAddress) ((SocketChannel) selected.channel()).getRemoteAddress()).getAddress();
                    SocketAddress UDPclientAddress = new InetSocketAddress(clientAddress, UDPclientPort);

                    // Opening session
                    this.logger.print("Opening the session for user \"" + username + "\"... ");
                    Session openedSession = this.sessionsManager.openSession(username, password, selector, UDPclientAddress);
                    this.logger.printlnGreen("OPENED");

                    // Send OK response
                    openedSession.prependMessage(new Message(MessageType.OK));

                    // Associate session with connection
                    selected.attach(openedSession);
                }
                catch (CommunicableException e)
                {
                    this.logger.printlnRed(e.getMessage());
                    selected.attach(new Message(e.getResponseType()));
                }
                catch (IOException e)
                {
                    // Exception has been thrown getting the client address
                    this.logger.printlnRed("INVALID UDP ADDRESS");
                    this.logger.printlnRed(e.getStackTrace());

                    // Close connection
                    try
                    {
                        this.logger.print("Closing connection with the client at " + ((SocketChannel) selected.channel()).getRemoteAddress() + "... ");
                        selected.cancel();
                        client.close();
                        this.logger.printlnRed("CLOSED");
                    }
                    catch (IOException ex)
                    {// IOException closing connection. Just report it.
                        this.logger.printlnRed("FAILED");
                    }
                }

                break;
            }
            case REQUEST_FOR_FRIENDSHIP:
            {
                assert session != null;

                Message response;
                String to;

                // Get the receiver of the request
                to = String.valueOf(message.getFieldDataAt(0));

                try
                {
                    this.logger.print("Sending friendship request from \"" + session.getUsername() + "\" to \"" + to + "\"... ");
                    // Send the friendship request to receiver
                    this.sessionsManager.sendFriendshipRequest(session.getUsername(), to);
                    this.logger.printlnGreen("SENT");

                    // Prepare response message for applicant
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {// Impossible to forward the request
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message for applicant
                    response = new Message(e.getResponseType());
                }

                // Store response for the applicant
                session.prependMessage(response);

                break;
            }
            case CONFIRM_FRIENDSHIP_REQUEST:
            {
                assert session != null;

                Message response;
                String from;

                // Get the applicant of the request
                from = String.valueOf(message.getFieldDataAt(0));

                // Confirm the friendship request
                try
                {
                    this.logger.print("Confirming friendship request between \"" + from + "\" and \"" + session.getUsername() + "\"... ");
                    this.sessionsManager.confirmFriendshipRequest(from, session.getUsername());
                    this.logger.printlnGreen("CONFIRMED");

                    // Prepare response message for applicant
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {// Impossible to confirm the request
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message
                    response = new Message(e.getResponseType());
                }

                // Store response message
                session.prependMessage(response);

                break;
            }
            case DECLINE_FRIENDSHIP_REQUEST:
            {
                assert session != null;

                Message response;
                String from;

                try
                {
                    // Get the applicant of the request
                    from = String.valueOf(message.getFieldDataAt(0));

                    // Decline the friendship request
                    this.logger.print("Declining friendship request between \"" + from + "\" and \"" + session.getUsername() + "\"... ");
                    this.sessionsManager.declineFriendshipRequest(from, session.getUsername());
                    this.logger.printlnGreen("DECLINED");

                    // Prepare response message
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {// Impossible to decline the request
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message
                    response = new Message(e.getResponseType());
                }

                // Store response message
                session.prependMessage(response);

                break;
            }
            case REQUEST_FOR_FRIENDS_LIST:
            {
                assert session != null;

                JSONArray friends;

                // Get serialized friends list
                this.logger.print("Getting serialized friends list of \"" + session.getUsername() + "\"... ");
                friends = usersManager.getSerializedFriendsList(session.getUsername());
                this.logger.printlnGreen("GOT");

                // Store the response message
                session.prependMessage(new Message(MessageType.FRIENDS_LIST, friends.toJSONString()));
                break;
            }
            case REQUEST_FOR_CHALLENGE:
            {
                assert session != null;

                Message response;
                String to;

                // Get the receiver of the request
                to = String.valueOf(message.getFieldDataAt(0));

                try
                {
                    this.logger.print("Sending challenge request from \"" + session.getUsername() + "\" to \"" + to + "\"... ");
                    // Send the challenge request to receiver
                    this.sessionsManager.sendChallengeRequest(session.getUsername(), to);
                    this.logger.printlnGreen("SENT");

                    // Prepare response message
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {// Impossible to forward the request
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message for applicant
                    response = new Message(e.getResponseType());
                }

                // Store response for the applicant
                session.prependMessage(response);

                break;
            }
            case CONFIRM_CHALLENGE_REQUEST:
            {
                assert session != null;

                Message response;
                String from;

                // Get the applicant of the request
                from = String.valueOf(message.getFieldDataAt(0));

                // Confirm the challenge request
                try
                {
                    this.logger.print("Confirming challenge request between \"" + from + "\" and \"" + session.getUsername() + "\"... ");
                    this.sessionsManager.confirmChallengeRequest(from, session.getUsername());
                    this.logger.printlnGreen("CONFIRMED");

                    // Prepare response message
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {// Impossible to confirm the request
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message
                    response = new Message(e.getResponseType());
                }

                // Store response message
                session.prependMessage(response);

                break;
            }
            case DECLINE_CHALLENGE_REQUEST:
            {
                assert session != null;

                Message response;
                String from;

                try
                {
                    // Get the applicant of the request
                    from = String.valueOf(message.getFieldDataAt(0));

                    // Decline the challenge request
                    this.logger.print("Declining challenge request between \"" + from + "\" and \"" + session.getUsername() + "\"... ");
                    this.sessionsManager.declineChallengeRequest(from, session.getUsername());
                    this.logger.printlnGreen("DECLINED");

                    // Prepare response message
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {// Impossible to decline the request
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message
                    response = new Message(e.getResponseType());
                }

                // Store response message
                session.prependMessage(response);

                break;
            }
            case CHALLENGE_GET_WORD:
            {
                assert session != null;

                Message response;
                String word;

                try
                {
                    // Retrieve next word to translate for this challenging user
                    this.logger.print("Retrieving next word for challenging user \"" + session.getUsername() + "\"... ");
                    word = this.sessionsManager.retrieveNextWord(session.getUsername());
                    this.logger.printlnGreen("RETRIEVED ⟶ \"" + word + "\"");

                    // Prepare response
                    response = new Message(MessageType.OK, word);
                }
                catch (UnexpectedMessageException e)
                {
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message
                    response = new Message(e.getResponseType());
                }

                // Store response message
                session.prependMessage(response);

                break;
            }
            case CHALLENGE_PROVIDE_TRANSLATION:
            {
                assert session != null;

                Message response;
                String translation;
                boolean correct;

                translation = String.valueOf(message.getFieldDataAt(0));

                try
                {
                    // Check given translation for this challenging user
                    this.logger.print("Checking given translation for challenging user \"" + session.getUsername() + "\" ⟶ " + translation + " ... ");
                    correct = this.sessionsManager.provideTranslation(session.getUsername(), translation);
                    if (correct)
                    {// Given translation is correct
                        this.logger.printlnGreen("TRANSLATION CORRECT");
                        // Prepare response
                        response = new Message(MessageType.TRANSLATION_CORRECT);
                    }
                    else
                    {// Given translation is wrong
                        this.logger.printlnGreen("TRANSLATION WRONG");
                        // Prepare response
                        response = new Message(MessageType.TRANSLATION_WRONG);
                    }
                }
                catch (UnexpectedMessageException e)
                {
                    this.logger.printlnRed(e.getMessage());
                    // Prepare response message
                    response = new Message(e.getResponseType());
                }

                // Store response message
                session.prependMessage(response);

                break;
            }
        }
    }

    public void send(SelectionKey selected)
    {
        SocketChannel client = (SocketChannel) selected.channel();

        if (selected.attachment() instanceof Message)
        {
            Message message = (Message) selected.attachment();
            try
            {
                this.logger.print("Writing message to not logged user: {" + message + "}... ");
                Message.writeMessage(client, buffer, (Message) selected.attachment());
                selected.attach(null);
                this.logger.printlnGreen("WRITTEN");
            }
            catch (IOException e)
            {
                this.logger.printlnRed("CLIENT CLOSED CONNECTION");

                // Close connection
                try
                {
                    this.logger.print("Closing connection with the client at " + ((SocketChannel) selected.channel()).getRemoteAddress() + "... ");
                    selected.cancel();
                    client.close();
                    this.logger.printlnRed("CLOSED");
                }
                catch (IOException ex)
                {// IOException closing connection. Just report it.
                    this.logger.printlnRed("FAILED");
                }
            }
        }
        else if (selected.attachment() instanceof Session)
        {
            Session session = (Session) selected.attachment();
            Message outcome = null;

            try
            {
                outcome = session.getMessage();
                if (outcome.getType().isNotification())
                {
                    this.logger.print("Writing notification to user \"" + session.getUsername() + "\": {" + outcome + "}... ");
                    Message.writeNotification(this.UDPchannel, session.getAddress(), buffer, outcome);
                }
                else if (outcome.getType().isResponse())
                {
                    this.logger.print("Writing message to user \"" + session.getUsername() + "\": {" + outcome + "}... ");
                    Message.writeMessage(client, buffer, outcome);
                }

                this.logger.printlnGreen("WRITTEN");
            }
            catch (IOException e)
            {
                this.logger.printlnRed("FAILED");
                if (outcome.getType() == MessageType.REQUEST_FOR_FRIENDSHIP)
                    session.prependMessage(outcome);

                // Close session
                this.logger.print("Closing session with user \"" + session.getUsername() + "\"... ");
                this.sessionsManager.closeSession((Session) selected.attachment());
                this.logger.printlnRed("CLOSED");

                // Close connection
                try
                {
                    this.logger.print("Closing connection with client at " + ((SocketChannel) selected.channel()).getRemoteAddress() + "... ");
                    selected.cancel();
                    client.close();
                    this.logger.printlnRed("CLOSED");
                }
                catch (IOException ex)
                {// IOException closing connection. Just report
                    this.logger.printlnRed("FAILED");
                }
            }
        }
    }

    public void wakeUp()
    {
        this.selector.wakeup();
    }

    public void shutDown()
    {
        SHUT.set(true);
        this.selector.wakeup();
    }
}
