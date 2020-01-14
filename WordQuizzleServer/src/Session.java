import java.nio.channels.SocketChannel;

class Session
{
    private String sessionUser;
    private SocketChannel sessionSocket;
    private MessageType waitingFor;

    protected Session(String user, SocketChannel socket)
    {
        this.sessionSocket = socket;
        this.sessionUser = user;
        this.waitingFor = null;
    }

    protected void setWaitingFor(MessageType t)
    {
        this.waitingFor = t;
    }

    protected MessageType getWaitingFor()
    {
        return this.waitingFor;
    }
}
