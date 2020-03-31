package server.sessions.session;

import commons.messages.Message;
import commons.messages.MessageType;

import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.util.Deque;
import java.util.concurrent.locks.ReentrantLock;

public class Session
{
    private String username;
    private Selector selector;
    private SocketAddress address;
    private Deque<Message> backlog;

    public Session(String username, Selector selector, SocketAddress address, Deque<Message> backlog)
    {
        this.username = username;
        this.selector = selector;
        this.address = address;
        this.backlog = backlog;
    }

    public String getUsername()
    {
        return this.username;
    }

    public SocketAddress getAddress()
    {
        return this.address;
    }

    public void prependMessage(Message message)
    {
        this.backlog.addFirst(message);
    }

    public void appendMessage(Message message)
    {
        this.backlog.addLast(message);
    }

    public Message getMessage()
    {
        return this.backlog.pollFirst();
    }

    public boolean hasPendingMessages()
    {
        return !this.backlog.isEmpty();
    }

    public void wakeUp()
    {
        if (this.selector != null && this.selector.isOpen())
            this.selector.wakeup();
    }

    public void close()
    {
        this.backlog.removeIf(message -> message.getType() != MessageType.REQUEST_FOR_FRIENDSHIP);
    }
}
