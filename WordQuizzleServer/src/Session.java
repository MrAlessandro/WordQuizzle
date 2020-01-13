import java.nio.channels.SocketChannel;

class Session
{
    private SocketChannel sessionSocket;
    private String sessionUser;

    protected Session(String user, SocketChannel socket)
    {
        this.sessionSocket = socket;
        this.sessionUser = user;
    }
}
