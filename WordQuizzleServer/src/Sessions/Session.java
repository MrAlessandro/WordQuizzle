package Sessions;

import Messages.Message;

import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

class Session
{
    private String sessionUser;
    private LinkedList<Message> sessionMessageBuffer;
    private DatagramChannel notificationChannel;

    protected Session(String username)
    {
        this.sessionUser = username;
        this.sessionMessageBuffer = new LinkedList<>();
    }

    protected Session(String username, LinkedList<Message> backLog)
    {
        this.sessionUser = username;
        this.sessionMessageBuffer = backLog;
    }

    protected Message getPendingMessage()
    {
        return this.sessionMessageBuffer.pollFirst();
    }

    protected void storePendingMessage(Message message)
    {
        this.sessionMessageBuffer.addLast(message);
    }
}
