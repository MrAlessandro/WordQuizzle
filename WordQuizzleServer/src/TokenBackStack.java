import java.util.concurrent.LinkedBlockingQueue;

public class TokenBackStack
{
    private static final TokenBackStack BackStack = new TokenBackStack();
    private static final LinkedBlockingQueue<Token> BackLine = new LinkedBlockingQueue<>();

    public TokenBackStack(){}

    public static TokenBackStack getTokenPileInstance()
    {
        return BackStack;
    }

    public static void add(Token token)
    {
        BackLine.add(token);
    }

    public static Token get() throws InterruptedException
    {
        return BackLine.take();
    }
}
