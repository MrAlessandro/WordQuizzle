package server.main;

import exceptions.CommunicableException;
import messages.Message;
import messages.MessageType;
import messages.exceptions.UnexpectedMessageException;
import server.constants.ServerConstants;
import server.printer.Printer;
import server.users.UsersManager;
import server.users.exceptions.UnknownUserException;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    private Printer printer;

    Deputy(String name)
    {
        super();
        // Initialization of the dispatch queue from which the deputy thread extract new connections
        this.dispatch = new LinkedBlockingQueue<>();
        this.printer = new Printer(name);
        this.setName(name);
    }

    @Override
    public void run()
    {
        // Selector which detects ready operations
        Selector selector = null;

        try
        {
            // Open the selector which detects ready operations on the registered connections
            selector = Selector.open();

            // Deputy listening cycle
            while (!isShutDown())
            {
                // Iterator of the ready channel set
                Iterator<SelectionKey> iter;
                // Ready channels counter
                int ready;
                // Extract any eventual new incoming connections
                SocketChannel incoming = dispatch.poll();
                if (incoming != null)
                {// There are new connections
                    // Configure as non-blocking
                    incoming.configureBlocking(false);
                    // Register to be selected
                    incoming.register(selector, SelectionKey.OP_READ, null);
                }

                // Channel selection to detect ready channels
                ready = selector.select(ServerConstants.SELECTION_TIMEOUT);
                // Check if there are ready channels
                if (ready > 0)
                    // No ready operations, come back to the beginning of  the listening cycle
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
                        processRead(currentKey);
                    }
                    // Check if the serving channel is writable
                    if (currentKey.isValid() && currentKey.isWritable())
                    {
                        processWrite(currentKey);
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
        catch (IOException e) {throw new Error("Closing selector");}

    }

    private void processRead(SelectionKey selected) throws IOException
    {
        SocketChannel client = (SocketChannel) selected.channel();
        Object attachment = selected.attachment();
        Message message = null;

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
                String username = String.valueOf(message.getField(0));
                char[] password = message.getField(1);

                printer.print("Logging in user \"" + username + "\"... ");

                try
                {
                    UsersManager.openSession(username, password);
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
                String applicant = (String) attachment;
                String friend = String.valueOf(message.getField(0));
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
                String friend = (String) attachment;
                String applicant = String.valueOf(message.getField(0));
                Message response;

                printer.print("Confirming friendship between \"" + applicant + "\" and \"" + friend + "\"... ");

                try
                {   // Constructing friendship
                    UsersManager.makeFriends(applicant, friend);
                    printer.printlnGreen("CONFIRMED");

                    // Sending confirm message to the applicant
                    response = new Message(MessageType.OK);
                    UsersManager.sendMessage(applicant, new Message(MessageType.CONFIRM_FRIENDSHIP, friend));
                }
                catch (UnknownUserException e)
                {
                    throw new Error("System inconsistency");
                }
                catch (UnexpectedMessageException e)
                {
                    printer.printlnRed(e.getMessage());
                    response = new Message(MessageType.UNEXPECTED_MESSAGE);
                }

                // Send response to applicant
                UsersManager.sendResponse(friend, response);

                break;
            }
            case DECLINE_FRIENDSHIP:
            {
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
                    UsersManager.sendMessage(applicant, new Message(MessageType.DECLINE_FRIENDSHIP, friend));
                }
                catch (UnknownUserException e)
                {
                    throw new Error("System inconsistency");
                }
                catch (UnexpectedMessageException e)
                {
                    printer.printlnRed(e.getMessage());
                    response = new Message(MessageType.UNEXPECTED_MESSAGE);
                }

                // Send response to applicant
                UsersManager.sendResponse(friend, response);

                break;
            }
            default:
            {}
        }
    }

    private void processWrite(SelectionKey selected) throws IOException
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
            }
        }
        else if (attachment instanceof String)
        {
            Message outcome = null;

            printer.print("Writing message to user \"" + attachment + "\"... ");
            try
            {
                outcome = UsersManager.retrieveMessage((String) attachment);
                Message.writeMessage(client, buffer, outcome);
                printer.printlnGreen("WRITTEN");
            }
            catch (IOException e)
            {
                printer.printlnYellow("CLIENT CLOSED CONNECTION");
                UsersManager.restoreUnsentMessage((String) attachment, outcome);
                UsersManager.closeSession((String) attachment);
                printer.print("Closing connection with \"" + attachment + "\"... ");
                selected.cancel();
                client.close();
                printer.printlnGreen("CLOSED");
            }
        }

        selected.interestOps(SelectionKey.OP_READ);
    }

    public static synchronized void shutDown()
    {
        shut = true;
    }

    private static synchronized boolean isShutDown()
    {
        return shut;
    }
}

