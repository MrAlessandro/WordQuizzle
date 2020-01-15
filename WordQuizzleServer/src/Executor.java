import CommunicationDispatching.Delegation;
import CommunicationDispatching.OperationType;
import CommunicationDispatching.DelegationsDispenser;
import Exceptions.UnknownUserException;
import Messages.MessageType;
import Sessions.SessionsMap;
import UsersNetwork.UserNet;
import Utility.AnsiColors;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Executor implements Runnable
{
    private ByteBuffer buffer = ByteBuffer.allocateDirect(2048);
    private ByteBuffer intBuffer = ByteBuffer.allocateDirect(4);

    @Override
    public void run()
    {
        SocketChannel clientSocket;
        OperationType operationType;
        MessageType messageType;
        Delegation delegation;
        boolean stop = false;

        while (!stop)
        {
            try
            {
                // Gets the SocketChannel and relative details from the inter-thread communication structure
                delegation = DelegationsDispenser.get();
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
                        case LOG_IN:
                        {// LogIn operation
                            boolean check = false;

                            // Read the rest of the message
                            byte[] read = readMessage(clientSocket);
                            // Decode message contents: Username and Password
                            UserLogInInfo userInfo = decodeLoginMessage(read);

                            System.out.print("Check password for user \"" + userInfo.username + "\"... ");

                            try
                            {
                                check = UserNet.checkUserPassword(userInfo.username, userInfo.password);

                                if (check)
                                {
                                    AnsiColors.printlnGreen("VERIFIED");

                                    /*TODO
                                    if (SessionsMap.createSession(clientSocket, userInfo.username) != null)
                                    {

                                    }*/
                                }
                                else
                                {
                                    AnsiColors.printlnRed("WRONG");

                                }
                            }
                            catch (UnknownUserException e)
                            {
                                AnsiColors.printlnRed("FAILED");
                                AnsiColors.printlnRed(e.getMessage());
                            }

                        }
                    }

                }

            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected MessageType readMessageType(SocketChannel client)
    {
        int intType;
        MessageType type;

        try
        {
            client.read(intBuffer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        intBuffer.flip();

        intType = intBuffer.getInt();
        type = MessageType.valueOf(intType);

        intBuffer.clear();

        return type;
    }

    protected byte[] readMessage(SocketChannel client)
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

    protected UserLogInInfo decodeLoginMessage(byte[] raw)
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

