package server.dispatching;

import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;

public class DelegationsDispenser
{
    private static final LinkedBlockingQueue<Delegation> DISPENSER = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<Delegation> BACK_DISPENSER = new LinkedBlockingQueue<>();

    public DelegationsDispenser(){}

    public static void delegateRead(SocketChannel delegation, Object attachment)
    {
        DISPENSER.add(new Delegation(delegation, OperationType.READ, attachment));
    }

    public static void delegateWrite(SocketChannel delegation, Object attachment)
    {
        DISPENSER.add(new Delegation(delegation, OperationType.WRITE, attachment));
    }

    public static Delegation getDelegation()
    {
        try
        {
            return DISPENSER.take();
        }
        catch (InterruptedException e)
        {
            throw new Error("Unexpected interruption");
        }
    }

    public static void delegateBack(Delegation delegation)
    {
        BACK_DISPENSER.add(delegation);
    }

    public static Delegation getDelegationBack()
    {
        return BACK_DISPENSER.poll();
    }
}