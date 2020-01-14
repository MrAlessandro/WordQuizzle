import org.omg.IOP.Encoding;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

class Executor implements Runnable
{
    private ByteBuffer buffer = ByteBuffer.allocate(2048);
    private ByteBuffer intBuffer = ByteBuffer.allocate(4);

    @Override
    public void run()
    {
        SocketChannel client;
        MessageType type;
        Token token;
        boolean stop = false;

        while (!stop)
        {
            try
            {
                token = TokenStack.get();
                client = token.ClientSocket;

                if (token.OpType == OperationType.READ)
                {
                    type = readMessageType(client);

                    switch (type)
                    {
                        case LOG_IN:
                        {
                            byte[] read = readMessage(client);
                            UserLogInInfo userInfo = decodeLoginMessage(read);

                            System.out.println("Username: " + userInfo.username + "; Password: " + userInfo.password.toString());

                            UserNet.logInUser(userInfo.username, userInfo.password);
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
            if (raw[i] == '\n')
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

        UserLogInInfo info = new UserLogInInfo(byteUsername, bytePassword);

        return info;
    }

    private class UserLogInInfo
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

