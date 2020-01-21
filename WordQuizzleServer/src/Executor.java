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
import java.util.LinkedList;

class Executor implements Runnable
{
    private static final ByteBuffer buffer = ByteBuffer.allocate(2048);

    @Override
    public void run()
    {
        SocketChannel clientSocket;
        Delegation delegation = null;
        boolean stop = false;

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
                        // Read message
                        Message message = Message.readMessage(clientSocket, buffer);
                        String username = (String) delegation.getDelegation().attachment();

                        // Consistence condition
                        if (username == null && message.getType() != MessageType.LOG_IN)
                            throw new UnexpectedMessageException("Expected LogIn message");
                        else if (username != null && message.getType() == MessageType.LOG_IN)
                            throw new UnexpectedMessageException("Unexpected LogIn message");

                        switch (message.getType())
                        {
                            // LogIn operation
                            case LOG_IN:
                            {
                                username = String.valueOf(message.getField(0));
                                char[] password = message.getField(1);
                                Session session;

                                System.out.print("Logging in user \"" + username + "\"... ");

                                LinkedList<Message> backLog = UsersManager.validatePasswordRetrieveBackLog(username, password);
                                if (backLog != null)
                                {// Password correct
                                    backLog.addFirst(new Message(MessageType.OK, String.valueOf(backLog.size())));
                                    SessionsManager.openSession(clientSocket, username, backLog);
                                    delegation.getDelegation().attach(username);
                                    AnsiColors.printlnGreen("LOGGED");
                                }
                                else
                                {// Password wrong
                                    AnsiColors.printlnRed("PASSWORD WRONG");
                                    delegation.getDelegation().attach(new Message(MessageType.PASSWORD_WRONG));
                                }

                                // Reinsert socket in the communication dispatching
                                DelegationsDispenser.delegateBack(delegation.getDelegation(), OperationType.WRITE);
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
                        // Error message for users which are not logged in
                            Message.writeMessage(clientSocket, buffer, (Message) delegation.getDelegation().attachment());
                        else if (delegation.getDelegation().attachment() instanceof String)
                        {// Send pending messages stored on session
                            Message toSend = null;
                            while ((toSend = SessionsManager.getPendingSessionMessage(clientSocket)) != null)
                                Message.writeMessage(clientSocket, buffer, toSend);
                        }

                        DelegationsDispenser.delegateBack(delegation.getDelegation(), OperationType.READ);

                        break;
                    }
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (InvalidMessageFormatException e)
            {
                e.printStackTrace();
            }
            catch (UnexpectedMessageException e)
            {
                e.printStackTrace();
            }
            catch (SessionsArchiveInconsistanceException e)
            {
                e.printStackTrace();
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

