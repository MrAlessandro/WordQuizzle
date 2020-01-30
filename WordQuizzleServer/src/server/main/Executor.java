package server.main;

import messages.MessageType;
import messages.exceptions.UnexpectedMessageException;
import server.dispatching.Delegation;
import server.dispatching.DelegationsDispenser;
import server.dispatching.OperationType;
import exceptions.CommunicableException;
import messages.Message;
import messages.exceptions.InvalidMessageFormatException;
import server.users.UsersManager;
import server.users.exceptions.UnknownUserException;
import server.printer.AnsiColors;

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
            clientSocket = (SocketChannel) delegation.getSelection();
            attachment = delegation.attachment();
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
                            UsersManager.closeSession((String) attachment);
                        delegation.setType(OperationType.CLOSE);
                        DelegationsDispenser.delegateBack(delegation);
                        break;

                    }
                    catch (InvalidMessageFormatException e)
                    {// Invalid message format
                        Message response = new Message(e.getResponseType());
                        if (attachment instanceof String)
                            UsersManager.sendResponse((String) attachment, response);
                        else
                            delegation.attach(response);

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
                                UsersManager.openSession(username, password);
                                AnsiColors.printlnGreen("LOGGED");
                                delegation.attach(username);
                            }
                            catch (CommunicableException e)
                            {
                                AnsiColors.printlnRed(e.getMessage());
                                delegation.attach(new Message(e.getResponseType()));
                            }

                            // Reinsert socket in the communication server.dispatching
                            DelegationsDispenser.delegateBack(delegation);
                            break;
                        }
                        case REQUEST_FOR_FRIENDSHIP:
                        {
                            if(!isAuthorizedMessage(delegation))
                                break;

                            Message response = null;
                            String applicant = (String) delegation.attachment();
                            String friend = String.valueOf(message.getField(0));

                            System.out.print("Sending a friendship request from \"" + applicant + "\" to \"" + friend + "\"... ");

                            // Send confirmation message to requested user
                            try
                            {
                                UsersManager.sendFriendshipRequest(applicant, friend);
                                AnsiColors.printlnGreen("SENT");
                                response = new Message(MessageType.OK);
                            }
                            catch (CommunicableException e)
                            {
                                AnsiColors.printlnRed(e.getMessage());
                                response = new Message(e.getResponseType());
                            }

                            // Send response to applicant
                            UsersManager.sendResponse(applicant, response);

                            DelegationsDispenser.delegateBack(delegation);

                            break;
                        }
                        case CONFIRM_FRIENDSHIP:
                        {
                            if(!isAuthorizedMessage(delegation))
                                break;

                            String friend = (String) delegation.attachment();
                            String applicant = String.valueOf(message.getField(0));
                            Message response;

                            System.out.print("Confirming friendship between \"" + applicant + "\" and \"" + friend + "\"... ");

                            try
                            {   // Constructing friendship
                                UsersManager.makeFriends(applicant, friend);
                                AnsiColors.printlnGreen("CONFIRMED");

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
                                AnsiColors.printlnRed(e.getMessage());
                                response = new Message(MessageType.UNEXPECTED_MESSAGE);
                            }

                            // Send response to applicant
                            UsersManager.sendResponse(friend, response);

                            DelegationsDispenser.delegateBack(delegation);

                            break;
                        }
                        case DECLINE_FRIENDSHIP:
                        {
                            if(!isAuthorizedMessage(delegation))
                                break;

                            String friend = (String) delegation.attachment();
                            String applicant = String.valueOf(message.getField(0));
                            Message response;

                            System.out.print("Declining friendship between \"" + applicant + "\" and \"" + friend + "\"... ");

                            try
                            {   // Removing request from system
                                UsersManager.cancelFriendshipRequest(applicant, friend);
                                AnsiColors.printlnGreen("DECLINED");

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
                                AnsiColors.printlnRed(e.getMessage());
                                response = new Message(MessageType.UNEXPECTED_MESSAGE);
                            }

                            // Send response to applicant
                            UsersManager.sendResponse(friend, response);

                            DelegationsDispenser.delegateBack(delegation);

                            break;
                        }
                        default:
                        { }
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
                            outcome = UsersManager.retrieveMessage((String) attachment);
                            if (outcome != null)
                                Message.writeMessage(clientSocket, buffer, outcome);
                        }
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

    private boolean isAuthorizedMessage(Delegation delegation)
    {
        if (!(delegation.attachment() instanceof String))
        {
            AnsiColors.printlnRed("Received invalid message.");
            delegation.attach(new Message(MessageType.UNEXPECTED_MESSAGE));
            DelegationsDispenser.delegateBack(delegation);
            return false;
        }
        else
            return true;

    }
}

