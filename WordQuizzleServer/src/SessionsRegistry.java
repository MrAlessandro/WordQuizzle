import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

class SessionsRegistry
{
    private static final SessionsRegistry instance = new SessionsRegistry();
    private static final ConcurrentHashMap<String, SocketChannel> Registry = new ConcurrentHashMap<>();

    protected SessionsRegistry() {}

}
