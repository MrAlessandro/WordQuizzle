import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

class Token
{
    protected String Request;
    protected SocketChannel ClientSocket;
    protected ByteBuffer BufferLink;
    protected SelectionKey Key;

    protected Token(String request, SocketChannel clientSocket, ByteBuffer bufferLink, SelectionKey key)
    {
        this.Request = request;
        this.ClientSocket = clientSocket;
        this.BufferLink = bufferLink;
        this.Key = key;
    }
}
