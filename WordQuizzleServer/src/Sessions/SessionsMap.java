package Sessions;


import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

public class SessionsMap
{
    private static final SessionsMap instance = new SessionsMap();
    private static final ConcurrentHashMap<SessionKey, Session> Map = new ConcurrentHashMap<>();

    private SessionsMap(){}

    public static Session createSession(SocketChannel socket, String username)
    {
        return Map.put(new SessionKey(socket, username), new Session(username));
    }
}
