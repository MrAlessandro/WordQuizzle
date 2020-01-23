package sessions;

import messages.Message;

import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class Session
{
    private String user;
    private LinkedList<Message> backLog;
    private DatagramChannel notificationChannel;
    private ReentrantLock sessionLock;

    protected Session(String username)
    {
        this.user = username;
        this.backLog = new LinkedList<>();
        this.sessionLock = new ReentrantLock();
    }

    protected Session(String username, Collection<Message> backLog)
    {
        this.user = username;
        this.backLog = new LinkedList<>(backLog);
        this.sessionLock = new ReentrantLock();
    }

    protected Message retrieveMessage()
    {
        Message taken;

        this.sessionLock.lock();
        taken = this.backLog.pollFirst();
        this.sessionLock.unlock();

        return taken;
    }

    protected void appendMessage(Message message)
    {
        this.sessionLock.lock();
        this.backLog.addLast(message);
        this.sessionLock.unlock();
    }

    protected void prependMessage(Message message)
    {
        this.sessionLock.lock();
        this.backLog.addFirst(message);
        this.sessionLock.unlock();
    }

    protected LinkedList<Message> retrieveBackLog()
    {
        Message current;
        LinkedList<Message> retrieved = new LinkedList<>();

        this.sessionLock.lock();

        while ((current = this.backLog.pollFirst()) != null)
        {
            retrieved.addLast(current);
        }

        this.sessionLock.unlock();


        return retrieved;
    }
}
