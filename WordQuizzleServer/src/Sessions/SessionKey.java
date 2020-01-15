package Sessions;

import java.nio.channels.SocketChannel;

class SessionKey
{
    protected SocketChannel socket;
    protected String username;

    protected SessionKey(SocketChannel socket, String username)
    {
        this.socket = socket;
        this.username = username;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof SocketChannel)
            return this.socket.equals((SocketChannel) obj);
        else if (obj instanceof String)
            return this.username.equals((String) obj);
        else
            return false;
    }

    @Override
    public int hashCode()
    {
        return this.socket.hashCode() ^ this.username.hashCode();
    }
}
