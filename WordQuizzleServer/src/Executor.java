import dispatching.Delegation;
import dispatching.DelegationsDispenser;
import dispatching.OperationType;
import messages.exceptions.UnexpectedMessageException;
import messages.exceptions.InvalidMessageFormatException;
import sessions.exceptions.SessionsArchiveInconsistanceException;
import users.exceptions.UnknownUserException;
import messages.Message;
import messages.MessageType;
import sessions.Session;
import sessions.SessionsManager;
import users.UsersManager;
import util.AnsiColors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Collection;

class Executor implements Runnable
{
    private static final ByteBuffer buffer = ByteBuffer.allocate(2048);
    private static final UsersManager usersManager = UsersManager.getInstance();

    @Override
    public void run()
    {
        SocketChannel clientSocket;
        Delegation delegation = null;
        boolean stop = false;
        String costumer = null;

        while (!stop)
        {
            try
            {
                // Gets the delegation from the inter-thread communication structure
                delegation = DelegationsDispenser.getDelegation();
                // Extracts details from the delegation
                clientSocket = (SocketChannel) delegation.getDelegation().channel();

                switch (delegation.getType())
                {
                    case READ:
                    {
                        // Get the username associated with the connection
                        costumer = (String) delegation.getDelegation().attachment();

                        // Read message
                        Message message = Message.readMessage(clientSocket, buffer);
                        if (message == null)
                        {
                            if (costumer != null)
                                SessionsManager.closeSession(costumer);
                            delegation.getDelegation().cancel();
                            delegation.getDelegation().channel().close();
                            break;
                        }

                        // Consistence condition
                        if (costumer == null && message.getType() != MessageType.LOG_IN)
                            throw new UnexpectedMessageException("Expected LogIn message");
                        else if (costumer != null && message.getType() == MessageType.LOG_IN)
                            throw new UnexpectedMessageException("Unexpected LogIn message");

                        switch (message.getType())
                        {
                            // LogIn operation
                            case LOG_IN:
                            {
                                costumer = String.valueOf(message.getField(0));
                                char[] password = message.getField(1);
                                Session session;

                                System.out.print("Logging in user \"" + costumer + "\"... ");

                                Collection<Message> backLog = UsersManager.grantAccess(costumer, password);
                                if (backLog != null)
                                {// Password correct
                                    SessionsManager.openSession(costumer, backLog);
                                    SessionsManager.prependMessage(costumer, new Message(MessageType.OK, String.valueOf(backLog.size())));
                                    delegation.getDelegation().attach(costumer);
                                    AnsiColors.printlnGreen("LOGGED");
                                }
                                else
                                {// Password wrong
                                    AnsiColors.printlnRed("PASSWORD WRONG");
                                    delegation.getDelegation().attach(new Message(MessageType.PASSWORD_WRONG));
                                }

                                // Reinsert socket in the communication dispatching
                                delegation.setType(OperationType.WRITE);
                                DelegationsDispenser.delegateBack(delegation);
                                break;
                            }
                            default:
                            {
                                throw new InvalidMessageFormatException("Unknown message type");
                            }
                        }

                        break;
                    }
                    case WRITE:
                    {
                        if (delegation.getDelegation().attachment() instanceof Message)
                        {// Error message for users which are not logged in
                            Message.writeMessage(clientSocket, buffer, (Message) delegation.getDelegation().attachment());
                            delegation.getDelegation().attach(null);
                        }
                        else if (delegation.getDelegation().attachment() instanceof String)
                        {// Send pending messages stored on session
                            String username = (String) delegation.getDelegation().attachment();
                            Message toSend = null;
                            while ((toSend = SessionsManager.retrieveMessage(username)) != null)
                                Message.writeMessage(clientSocket, buffer, toSend);
                        }

                        delegation.setType(OperationType.READ);
                        DelegationsDispenser.delegateBack(delegation);

                        break;
                    }
                }
            }
            catch (InterruptedException | IOException | SessionsArchiveInconsistanceException e)
            {
                e.printStackTrace();
                System.exit(1);
            }
            catch (InvalidMessageFormatException e)
            {
                if (costumer == null)
                    delegation.getDelegation().attach(new Message(MessageType.INVALID_MESSAGE));
                else
                {
                    try
                    {
                        SessionsManager.prependMessage(costumer, new Message(MessageType.INVALID_MESSAGE));
                    }
                    catch (SessionsArchiveInconsistanceException ex)
                    {
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

                delegation.setType(OperationType.WRITE);
                DelegationsDispenser.delegateBack(delegation);
            }
            catch (UnexpectedMessageException e)
            {
                if (costumer == null)
                    delegation.getDelegation().attach(new Message(MessageType.UNEXPECTED_MESSAGE));
                else
                {
                    try
                    {
                        SessionsManager.prependMessage(costumer, new Message(MessageType.UNEXPECTED_MESSAGE));
                    }
                    catch (SessionsArchiveInconsistanceException ex)
                    {
                        ex.printStackTrace();
                    }
                }

                delegation.setType(OperationType.WRITE);
                DelegationsDispenser.delegateBack(delegation);
            }
            catch (UnknownUserException e)
            {
                AnsiColors.printlnRed("UNKNOWN USER");
                delegation.getDelegation().attach(new Message(MessageType.USERNAME_UNKNOWN));
                DelegationsDispenser.delegateBack(delegation);
            }
        }
    }
}

