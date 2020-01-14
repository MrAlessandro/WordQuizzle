import java.util.concurrent.LinkedBlockingQueue;

class TokenStack
{
    private static final TokenStack Stack = new TokenStack();
    private static final LinkedBlockingQueue<Token> CommunicationLine = new LinkedBlockingQueue<>();

    public TokenStack(){}

    public static TokenStack getTokenPileInstance()
    {
        return Stack;
    }

    public static void add(Token token)
    {
        CommunicationLine.add(token);
    }

    public static Token get() throws InterruptedException
    {
        return CommunicationLine.take();
    }
}
