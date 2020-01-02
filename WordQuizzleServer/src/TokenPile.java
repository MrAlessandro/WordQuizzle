
import java.util.concurrent.LinkedBlockingQueue;

class TokenPile
{
    private static final TokenPile Pile = new TokenPile();
    private static final LinkedBlockingQueue<Token> CommunicationLine = new LinkedBlockingQueue<Token>();

    public TokenPile(){}

    public static TokenPile getChain()
    {
        return Pile;
    }

    public static void add(Token token)
    {
        CommunicationLine.add(token);
    }

    public static Token get() throws InterruptedException {
        return CommunicationLine.take();
    }
}
