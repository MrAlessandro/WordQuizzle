import CommunicationDispatching.Delegation;
import CommunicationDispatching.DelegationsBackDispenser;
import CommunicationDispatching.OperationType;
import CommunicationDispatching.DelegationsDispenser;
import Exceptions.SessionsArchiveInconsistanceException;
import Exceptions.UnknownUserException;
import Messages.Message;
import Messages.MessageType;
import Sessions.SessionsManager;
import Users.UsersManager;
import Utility.AnsiColors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
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
        MessageType messageType;
        Delegation delegation = null;
        boolean stop = false;

        while (!stop)
        {
            try
            {
                // Gets the SocketChannel and relative details from the inter-thread communication structure
                delegation = DelegationsDispenser.get();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                System.exit(1);
            }

            clientSocket = (SocketChannel) delegation.getKey().channel();
            operationType = delegation.getOpType();

            // Discern between write/read operations
            if (operationType == OperationType.READ)
            {
                // Read the message header containing the type of the incoming message
                messageType = readMessageType(clientSocket);

                // Discern between the various types of messages
                switch (messageType)
                {
                    // LogIn operation
                    case LOG_IN:
                    {
                        // Read the rest of the message
                        byte[] read = readMessage(clientSocket);
                        // Decode message contents: Username and Password
                        UserLogInInfo userInfo = decodeLoginMessage(read);
                        // Performing logIn operation
                        try
                        {
                            System.out.print("Check password for user \"" + userInfo.username + "\"... ");
                            // Validate the password and retrieve possible backlog messages
                            LinkedList<Message> backLog = UsersManager.checkUserPasswordRetrieveBackLog(userInfo.username, userInfo.password);

                            if (backLog != null)
                            {// Password OK
                                AnsiColors.printlnGreen("VERIFIED");
                                System.out.print("Logging in user \"" + userInfo.username + "\"... ");
                                // Open session for logged user
                                SessionsManager.openSession(clientSocket, userInfo.username, backLog);
                                AnsiColors.printlnGreen("LOGGED");
                                // Insert an OK message in the session backlog
                                SessionsManager.storePendingSessionMessage(userInfo.username, new Message(MessageType.OK));
                                // Setting option for delegation
                                delegation.setOpType(OperationType.WRITE);
                                delegation.getKey().attach(userInfo.username);
                            } else
                            {// Password wrong
                                AnsiColors.printlnRed("WRONG");
                                // Insert a PASSWORD_WRONG message in the session backlog
                                SessionsManager.storePendingSessionMessage(userInfo.username, new Message(MessageType.PASSWORD_WRONG));
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
                                SessionsManager.storePendingSessionMessage(userInfo.username, new Message(MessageType.USERNAME_UNKNOWN));
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

    private MessageType readMessageType(SocketChannel client)
    {
        int intType;
        MessageType type;

        try
        {
            client.read(intBuffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();;
        }

        intBuffer.flip();

        intType = intBuffer.getInt();
        type = MessageType.valueOf(intType);

        intBuffer.clear();

        return type;
    }

    private int sendMessageType(SocketChannel client, MessageType type)
    {
        int bytesWritten = 0;
        intBuffer.putInt(type.getValue());

        try
        {
            bytesWritten = client.write(intBuffer);
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        return bytesWritten;
    }

    private byte[] readMessage(SocketChannel client)
    {
        int numReadBytes = 0;
        try
        {
            numReadBytes = client.read(buffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        buffer.flip();

        byte[] read = new byte[numReadBytes];
        buffer.get(read);

        buffer.clear();

        return read;
    }

    private UserLogInInfo decodeLoginMessage(byte[] raw)
    {
        int i = 0;
        int mark = 0;
        byte[] byteUsername = null;
        byte[] bytePassword = null;

        while (i < raw.length)
        {
            if (raw[i] == '\0')
            {
                if(mark == 0)
                {
                    byteUsername = new byte[i];
                    System.arraycopy(raw, 0, byteUsername, 0, byteUsername.length);
                    mark = i;
                }
                else
                {
                    bytePassword = new byte[i-mark-1];
                    System.arraycopy(raw, mark+1, bytePassword, 0, bytePassword.length);
                    break;
                }
            }

            i++;
        }

        return new UserLogInInfo(byteUsername, bytePassword);
    }

    private static class UserLogInInfo
    {
        private String username;
        private char[] password;

        private UserLogInInfo(byte[] byteUsername, byte[] bytePassword)
        {
            this.username = new String(byteUsername);
            this.password = new char[bytePassword.length];

            for (int i = 0; i < bytePassword.length; i++)
            {
                this.password[i] = (char) bytePassword[i];
            }
        }
    }
}

