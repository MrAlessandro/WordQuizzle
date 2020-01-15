package Sessions;

import Messages.Message;

import java.nio.channels.DatagramChannel;
import java.util.concurrent.LinkedBlockingDeque;

class Session
{
    private String SessionUser;
    private LinkedBlockingDeque<Message> SessionMessageBuffer;
    private DatagramChannel NotificationChannel;

    protected Session(String user)
    {
        this.SessionUser = user;
        this.SessionMessageBuffer = new LinkedBlockingDeque<>();
    }

    protected Message consumeMessage()
    {
        Message taken = null;
        try
        {
            taken = this.SessionMessageBuffer.take();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return taken;
    }

    protected void appendMessage(Message message)
    {
        this.SessionMessageBuffer.add(message);
    }
}
