import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

class Executor implements Runnable
{

    @Override
    public void run()
    {
        SocketChannel client;
        ByteBuffer buffer;
        Token token;
        boolean stop = false;

        while (!stop)
        {
            try {
                token = TokenPile.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
