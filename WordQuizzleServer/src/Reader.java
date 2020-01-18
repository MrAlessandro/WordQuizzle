import dispatching.DelegationsDispenser;
import exceptions.InvalidMessageFormatException;
import exceptions.SessionsArchiveInconsistanceException;
import exceptions.UnexpectedMessageException;
import exceptions.UnknownUserException;
import messages.Message;
import messages.MessageType;
import sessions.SessionsManager;
import users.UsersManager;
import util.AnsiColors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

class Reader implements Runnable
{
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

    @Override
    public void run()
    {
        SelectionKey delegation = null;
        SocketChannel clientSocket = null;
        boolean stop = false;

        while (!stop)
        {
            try
            {
                // Gets the delegation from the inter-thread communication structure
                delegation = DelegationsDispenser.getReadDelegation();
                // Extracts details from the delegation
                clientSocket = (SocketChannel) delegation.channel();
                String username = (String) delegation.attachment();

                // Read message
                Message message = Message.readMessage(clientSocket, buffer);

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

                        System.out.print("Logging in user \"" + username + "\"... ");

                        LinkedList<Message> backLog = UsersManager.validatePasswordRetrieveBackLog(username, password);
                        if (backLog != null)
                        {// Password correct
                            backLog.addFirst(new Message(MessageType.OK, String.valueOf(backLog.size())));
                            SessionsManager.openSession(clientSocket, username, backLog);
                            AnsiColors.printlnGreen("LOGGED");
                            delegation.attach(username);
                        }
                        else
                        {// Password wrong
                            AnsiColors.printlnRed("PASSWORD WRONG");
                            delegation.attach(new Message(MessageType.PASSWORD_WRONG));
                        }

                        // Reinsert socket in the communication dispatching
                        DelegationsDispenser.backDelegate(delegation);
                        break;
                    }
                    default:
                    {}
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
                delegation.attach(new Message(MessageType.USERNAME_UNKNOWN));
                DelegationsDispenser.backDelegate(delegation);
            }
        }
    }
}

