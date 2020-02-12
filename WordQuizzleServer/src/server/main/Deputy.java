package server.main;

import exceptions.CommunicableException;
import messages.Message;
import messages.MessageType;
import messages.exceptions.InvalidMessageFormatException;
import messages.exceptions.UnexpectedMessageException;
import server.constants.ServerConstants;
import server.printer.Printer;
import server.users.UsersManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;

class Deputy extends Thread
{
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(ServerConstants.BUFFERS_SIZE);
    protected LinkedBlockingQueue<SocketChannel> dispatch;
    private static volatile boolean shut = false;
    private DatagramChannel UDPchannel;
    public Selector selector;
    private Printer printer;
    private int UDPport;

    Deputy(String name, int UDPport)
    {
        super();
        // Initialization of the dispatch queue from which the deputy thread extract new connections
        this.dispatch = new LinkedBlockingQueue<>();
        this.printer = new Printer(name);
        this.setName(name);
        this.UDPport = UDPport;
    }

    @Override
    public void run()
    {
        try
        {
            // Open the selector which detects ready operations on the registered connections
            selector = Selector.open();

            // Open UDP socket
            this.UDPchannel = DatagramChannel.open();
            // Set address for UDP socket
            this.UDPchannel.socket().bind(new InetSocketAddress(UDPport));

            // Deputy listening cycle
            while (!shut)
            {
                // Iterator of the ready channel set
                Iterator<SelectionKey> iter;
                // Ready channels counter
                int ready;
                // Extract any eventual new incoming connections
                SocketChannel incoming;
                while ((incoming = dispatch.poll()) != null)
                {// There are new connections
                    // Configure as non-blocking
                    incoming.configureBlocking(false);
                    // Register to be selected
                    incoming.register(selector, SelectionKey.OP_READ, null);
                }

                // Channel selection to detect ready channels
                ready = selector.select();
                // Check if there are ready channels
                if (ready == 0)
                    // No ready operations, come back to the beginning of the listening cycle
                    continue;

                // There are some ready channel to read/write;
                // Iterate the ready channels set
                iter = selector.selectedKeys().iterator();
                while (iter.hasNext())
                {// Serve one ready channel per time
                    SelectionKey currentKey = iter.next();
                    // Remove the current serving channel from the ready set, in order to prevent subsequent takes
                    iter.remove();

                    // Check if the serving channel is readable
                    if (currentKey.isValid() && currentKey.isReadable())
                    {
                         receive(currentKey);
                    }
                    // Check if the serving channel is writable
                    if (currentKey.isValid() && currentKey.isWritable())
                    {
                        send(currentKey);
                    }
                }

                for (SelectionKey currentKey : selector.keys())
                {
                    // Check if some logged users have pending messages
                    if (currentKey.isValid() && ((currentKey.attachment() instanceof String && UsersManager.hasPendingMessages((String) currentKey.attachment())) || (currentKey.attachment() instanceof Message)))
                        currentKey.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                }

            }

            // Close the selector
            selector.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("During thread \"" + this.getName() + "\" listening cycle");
        }
    }

    private void receive(SelectionKey selected) throws IOException
    {
        SocketChannel client = (SocketChannel) selected.channel();
        Object attachment = selected.attachment();
        Message message;

        try
        {
            if (attachment instanceof String)
                printer.print("Reading message from user \"" + attachment + "\"... ");
            else
                printer.print("Reading message from unlogged user... ");

            message = Message.readMessage(client, buffer);
            if ((message.getType() == MessageType.LOG_IN && attachment != null) || (message.getType() != MessageType.LOG_IN && !(attachment instanceof String)))
                throw new UnexpectedMessageException();

            printer.printlnGreen("READ");
        }
        catch (IOException e)
        {// Close connection
            printer.printlnYellow("CLIENT CLOSED CONNECTION");

            if (attachment instanceof String)
            {
                printer.print("Closing connection with \"" + attachment + "\"... ");
                UsersManager.closeSession((String) attachment);
            }
            else
                printer.print("Closing connection with unlogged client... ");

            selected.cancel();
            client.close();
            printer.printlnGreen("CLOSED");

            return;
        }
        catch (CommunicableException e)
        {// Invalid message format
            printer.printlnRed(e.getMessage());
            Message response = new Message(e.getResponseType());
            if (attachment instanceof String)
                UsersManager.sendResponse((String) attachment, response);
            else
                selected.attach(response);

            selected.interestOps(SelectionKey.OP_WRITE);
            return;
        }

        switch (message.getType())
        {
            case LOG_IN:
            {
                try
                {
                    if (message.getField(0) == null || message.getField(1) == null)
                        throw new InvalidMessageFormatException("INVALID LOGIN MESSAGE");

                    String username = String.valueOf(message.getField(0));
                    char[] password = message.getField(1);
                    int UDPclientPort = Integer.parseInt(String.valueOf(message.getField(2)));
                    InetAddress clientAddress = ((InetSocketAddress) ((SocketChannel) selected.channel()).getRemoteAddress()).getAddress();
                    SocketAddress UDPclientAddress = new InetSocketAddress(clientAddress, UDPclientPort);

                    printer.print("Logging in user \"" + username + "\"... ");

                    // Opening session
                    UsersManager.openSession(username, password, UDPclientAddress);

                    // Send response containing friends list
                    String serializedFriendsList = UsersManager.retrieveSerializedFriendList(username);
                    Message response = new Message(MessageType.OK, serializedFriendsList);
                    UsersManager.sendResponse(username, response);

                    printer.printlnGreen("LOGGED");
                    selected.attach(username);
                }
                catch (CommunicableException e)
                {
                    printer.printlnRed(e.getMessage());
                    selected.attach(new Message(e.getResponseType()));
                }

                break;
            }
            case REQUEST_FOR_FRIENDSHIP:
            {
                assert attachment instanceof String;
                String applicant = (String) attachment;
                String friend = String.valueOf(message.getField(1));
                Message response;

                printer.print("Sending a friendship request from \"" + applicant + "\" to \"" + friend + "\"... ");

                // Send confirmation message to requested user
                try
                {
                    UsersManager.sendFriendshipRequest(applicant, friend);
                    printer.printlnGreen("SENT");
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {
                    printer.printlnRed(e.getMessage());
                    response = new Message(e.getResponseType());
                }

                // Send response to applicant
                UsersManager.sendResponse(applicant, response);
                break;
            }
            case CONFIRM_FRIENDSHIP:
            {
                assert attachment instanceof String;
                String friend = (String) attachment;
                String applicant = String.valueOf(message.getField(0));
                Message response;

                printer.print("Confirming friendship between \"" + applicant + "\" and \"" + friend + "\"... ");

                try
                {   // Constructing friendship
                    UsersManager.confirmFriendshipRequest(applicant, friend);
                    printer.printlnGreen("CONFIRMED");

                    // Sending updated friends list to friend
                    String serializedFriendList = UsersManager.retrieveSerializedFriendList(friend);
                    response = new Message(MessageType.FRIENDS_LIST, serializedFriendList);

                    // Sending confirm message to the applicant
                    UsersManager.sendMessage(applicant, new Message(MessageType.FRIENDSHIP_CONFIRMED, applicant, friend));
                }
                catch (CommunicableException e)
                {
                    printer.printlnRed(e.getMessage());
                    response = new Message(e.getResponseType());
                }

                // Send response to applicant
                UsersManager.sendResponse(friend, response);

                break;
            }
            case DECLINE_FRIENDSHIP:
            {
                assert attachment instanceof String;
                String friend = (String) attachment;
                String applicant = String.valueOf(message.getField(0));
                Message response;

                printer.print("Declining friendship between \"" + applicant + "\" and \"" + friend + "\"... ");

                try
                {   // Removing request from system
                    UsersManager.cancelFriendshipRequest(applicant, friend);
                    printer.printlnGreen("DECLINED");

                    // Sending confirm message to the applicant
                    response = new Message(MessageType.OK);
                    UsersManager.sendMessage(applicant, new Message(MessageType.FRIENDSHIP_DECLINED, applicant, friend));
                }
                catch (CommunicableException e)
                {
                    printer.printlnRed(e.getMessage());
                    response = new Message(e.getResponseType());
                }

                // Send response to applicant
                UsersManager.sendResponse(friend, response);

                break;
            }
            case REQUEST_FOR_FRIENDS_LIST:
            {
                assert attachment instanceof String;
                Message response;

                printer.print("Sending friends list to \"" + attachment + "\"... ");

                String serializedFriendsList = UsersManager.retrieveSerializedFriendList((String) attachment);
                response = new Message(MessageType.FRIENDS_LIST, serializedFriendsList);
                UsersManager.sendResponse((String) attachment, response);
                printer.printlnGreen("SENT");
            }
            case REQUEST_FOR_CHALLENGE:
            {
                assert attachment instanceof String;
                String applicant = (String) attachment;
                String opponent = String.valueOf(message.getField(1));
                Message response;

                printer.print("Sending a challenge request from \"" + applicant + "\" to \"" + opponent + "\"... ");

                // Send confirmation message to requested user
                try
                {
                    UsersManager.sendChallengeRequest(applicant, opponent);
                    printer.printlnGreen("SENT");
                    response = new Message(MessageType.OK);
                }
                catch (CommunicableException e)
                {
                    printer.printlnRed(e.getMessage());
                    response = new Message(e.getResponseType());
                }

                // Send response to applicant
                UsersManager.sendResponse(applicant, response);
                break;
            }
            case DECLINE_CHALLENGE:
            {
                assert attachment instanceof String;
                String opponent = (String) attachment;
                String applicant = String.valueOf(message.getField(0));
                Message response;

                printer.print("Declining challenge between \"" + applicant + "\" and \"" + opponent + "\"... ");

                try
                {   // Removing request from system
                    UsersManager.cancelChallengeRequest(applicant, opponent, false);
                    printer.printlnGreen("DECLINED");

                    // Sending confirm message to the applicant
                    response = new Message(MessageType.OK);
                    UsersManager.sendMessage(applicant, new Message(MessageType.CHALLENGE_DECLINED, applicant, opponent));
                }
                catch (CommunicableException e)
                {
                    printer.printlnRed(e.getMessage());
                    response = new Message(e.getResponseType());
                }

                // Send response to applicant
                UsersManager.sendResponse(opponent, response);

                break;
            }
            default:
            {}
        }
    }

    private void send(SelectionKey selected) throws IOException
    {
        SocketChannel client = (SocketChannel) selected.channel();
        Object attachment = selected.attachment();

        if (attachment instanceof Message)
        {
            printer.print("Writing message to unlogged user... ");
            try
            {
                selected.attach(null);
                Message.writeMessage(client, buffer, (Message) attachment);
                printer.printlnGreen("WRITTEN");
            }
            catch (IOException e)
            {
                printer.printlnYellow("CLIENT CLOSED CONNECTION");
                printer.print("Closing connection with unlogged client... ");
                selected.cancel();
                client.close();
                printer.printlnGreen("CLOSED");
                return;
            }
        }
        else if (attachment instanceof String)
        {
            Message outcome = null;

            try
            {
                outcome = UsersManager.retrieveMessage((String) attachment);
                if (outcome.getType().isNotification())
                {
                    printer.print("Writing notification to user \"" + attachment + "\"... ");
                    Message.writeNotification(this.UDPchannel, UsersManager.getUserAddress((String) attachment), buffer, outcome);
                }
                else
                {
                    printer.print("Writing message to user \"" + attachment + "\"... ");
                    Message.writeMessage(client, buffer, outcome);
                }

                printer.printlnGreen("WRITTEN");
            }
            catch (IOException e)
            {
                printer.printlnYellow("CLIENT CLOSED CONNECTION");
                if (outcome.getType().isNotification())
                    UsersManager.restoreUnsentMessage((String) attachment, outcome);
                UsersManager.closeSession((String) attachment);
                printer.print("Closing connection with \"" + attachment + "\"... ");
                selected.cancel();
                client.close();
                printer.printlnGreen("CLOSED");
                return;
            }
        }

        selected.interestOps(SelectionKey.OP_READ);
    }

    public void shutDown()
    {
        shut = true;
        selector.wakeup();
    }
}

