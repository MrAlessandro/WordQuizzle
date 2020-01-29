import dispatching.Delegation;
import dispatching.DelegationsDispenser;
import dispatching.OperationType;
import exceptions.CommunicableException;
import messages.Message;
import messages.exceptions.InvalidMessageFormatException;
import users.UsersManager;
import users.exceptions.UnknownUserException;
import util.AnsiColors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Executor implements Runnable
{
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

    @Override
    public void run()
    {
        SocketChannel clientSocket;
        Delegation delegation;
        Object attachment;
        OperationType opType;
        boolean stop = false;

        while (!stop)
        {
            // Gets the delegation from the inter-thread communication structure
            delegation = DelegationsDispenser.getDelegation();

            // Extracts details from the delegation
            clientSocket = (SocketChannel) delegation.getSelection().channel();
            attachment = delegation.getSelection().attachment();
            opType = delegation.getType();

            switch (opType)
            {
                case READ:
                {
                    // Read message
                    Message message = null;
                    try
                    {
                        message = Message.readMessage(clientSocket, buffer);
                    }
                    catch (IOException e)
                    {// Close connection
                        if (attachment instanceof String)
                            UsersManager.closeSession((String) attachment, delegation.getSelection());
                        delegation.setType(OperationType.CLOSE);
                        DelegationsDispenser.delegateBack(delegation);
                        break;
                    }
                    catch (InvalidMessageFormatException e)
                    {// Invalid message format
                        Message response = new Message(e.getResponseType());
                        if (attachment instanceof String)
                        {
                            try
                            {
                                UsersManager.sendResponse((String) attachment, response);
                            }
                            catch (UnknownUserException ex)
                            {
                                throw new Error("Dispatching system inconsistency");
                            }
                        }
                        else
                            delegation.getSelection().attach(response);

                        DelegationsDispenser.delegateBack(delegation);
                        break;
                    }

                    switch (message.getType())
                    {
                        // LogIn operation
                        case LOG_IN:
                        {
                            String username = String.valueOf(message.getField(0));
                            char[] password = message.getField(1);

                            System.out.print("Logging in user \"" + username + "\"... ");

                            try
                            {
                                UsersManager.openSession(username, password, delegation.getSelection());
                                AnsiColors.printlnGreen("LOGGED");
                                delegation.getSelection().attach(username);
                            }
                            catch (CommunicableException e)
                            {
                                AnsiColors.printlnRed(e.getMessage());
                                delegation.getSelection().attach(new Message(e.getResponseType()));
                            }

                            // Reinsert socket in the communication dispatching
                            DelegationsDispenser.delegateBack(delegation);
                            break;
                        }
                        case ADD_FRIEND:
                        {
                            /*Message confirmMessage;
                            String friend = String.valueOf(message.getField(0));
                            System.out.println("Sending a friendship request from \"" + username + "\" to \"" + friend + "\"... ");

                            confirmMessage = new Message(MessageType.CONFIRM_FRIENDSHIP, username);

                            AnsiColors.printlnGreen("SENT");

                            UsersManager.makeFriends(username, friend);*/
                        }
                        default:
                        {}
                    }

                    break;
                }
                case WRITE:
                {
                    if (attachment instanceof Message)
                    {
                        try
                        {
                            Message.writeMessage(clientSocket, buffer, (Message) attachment);
                        } catch (IOException e) {delegation.setType(OperationType.CLOSE);}
                    }
                    else if (attachment instanceof String)
                    {
                        Message outcome = null;
                        try
                        {
                            outcome = UsersManager.retrieveMessage((String) attachment, delegation.getSelection());
                            Message.writeMessage(clientSocket, buffer, outcome);
                        }
                        catch (UnknownUserException e) {throw new Error("Dispatching system inconsistency");}
                        catch (IOException e)
                        {
                            UsersManager.restoreUnsentMessage((String) attachment, outcome);
                            delegation.setType(OperationType.CLOSE);
                        }
                    }

                    DelegationsDispenser.delegateBack(delegation);

                    break;
                }
                default:
                    throw new Error("Unknown operation type");
            }

        }
    }
}

