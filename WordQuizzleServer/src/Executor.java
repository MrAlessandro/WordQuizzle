import CommunicationDispatching.Delegation;
import CommunicationDispatching.DelegationsBackDispenser;
import CommunicationDispatching.OperationType;
import CommunicationDispatching.DelegationsDispenser;
import Exceptions.InvalidMessageFormatException;
import Exceptions.SessionsArchiveInconsistanceException;
import Exceptions.UnexpectedMessageException;
import Exceptions.UnknownUserException;
import Messages.Message;
import Messages.MessageType;
import Sessions.SessionsManager;
import Users.UsersManager;
import Utility.AnsiColors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

class Executor implements Runnable
{
    private ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
    private ByteBuffer intBuffer = ByteBuffer.allocateDirect(4);

    @Override
    public void run()
    {
        SocketChannel clientSocket ;
        OperationType operationType;
        Delegation delegation = null;
        boolean stop = false;

        try
        {
            while (!stop)
            {

                // Gets the SocketChannel and relative details from the inter-thread communication structure
                delegation = DelegationsDispenser.get();
                clientSocket = (SocketChannel) delegation.getKey().channel();
                String username = (String) delegation.getKey().attachment();
                operationType = delegation.getOpType();

                // Discern between write/read operations
                if (operationType == OperationType.READ)
                {
                    // Read message
                    Message message = Message.readMessage(clientSocket);

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
                            Iterator<Messages.Field> iter = message.getFieldsIterator();
                            username = String.valueOf(iter.next().getBody());
                            char[] password = iter.next().getBody();

                            try
                            {
                                System.out.print("Check password for user \"" + username + "\"... ");
                                // Validate the password and retrieve possible backlog messages
                                LinkedList<Message> backLog = UsersManager.checkUserPasswordRetrieveBackLog(username, password);

                                /*TODO*/
                                if (backLog != null)
                                {// Password OK
                                    AnsiColors.printlnGreen("VERIFIED");
                                    System.out.print("Logging in user \"" + username + "\"... ");
                                    // Open session for logged user
                                    SessionsManager.openSession(clientSocket, username, new Message(MessageType.OK, String.valueOf(backLog.size())));
                                    AnsiColors.printlnGreen("LOGGED");
                                    // Insert an OK message in the session backlog
                                    SessionsManager.storePendingSessionMessage(username, backLog);
                                    // Setting option for delegation
                                    delegation.setOpType(OperationType.WRITE);
                                    delegation.getKey().attach(username);
                                } else
                                {// Password wrong
                                    AnsiColors.printlnRed("WRONG");
                                    // Insert a PASSWORD_WRONG message in the session backlog
                                    SessionsManager.storePendingSessionMessage(username, new Message(MessageType.PASSWORD_WRONG));
                                    // Setting option for delegation
                                    delegation.setOpType(OperationType.WRITE);
                                }
                            }
                            catch (UnknownUserException e)
                            {
                                AnsiColors.printlnRed("UNKNOWN USER");
                                // Insert a PASSWORD_WRONG message in the session backlog
                                try
                                {
                                    SessionsManager.storePendingSessionMessage(username, new Message(MessageType.USERNAME_UNKNOWN));
                                }
                                catch (SessionsArchiveInconsistanceException ex)
                                {
                                    ex.printStackTrace();
                                }
                                delegation.setOpType(OperationType.WRITE);
                            }
                            catch (SessionsArchiveInconsistanceException e)
                            {
                                e.printStackTrace();
                                System.exit(1);
                            }
                            // Reinsert socket in the communication dispatching
                            DelegationsBackDispenser.add(delegation);
                        }
                        default:
                        {}
                    }

                }
                else if (operationType == OperationType.WRITE)
                {
                    try
                    {
                        Message toSend = SessionsManager.getPendingSessionMessage(clientSocket);

                    } catch (SessionsArchiveInconsistanceException e)
                    {
                        e.printStackTrace();
                    }
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
        } catch (UnexpectedMessageException e)
        {
            e.printStackTrace();
        }
    }
}

