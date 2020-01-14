import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

class Token
{
    protected SocketChannel ClientSocket;
    protected Session clientSession;
    protected SelectionKey Key;
    protected OperationType OpType;

    protected Token(SocketChannel clientSocket, Session session, SelectionKey key, OperationType opType)
    {
        this.ClientSocket = clientSocket;
        this.clientSession = session;
        this.Key = key;
        this.OpType = opType;
    }
}
