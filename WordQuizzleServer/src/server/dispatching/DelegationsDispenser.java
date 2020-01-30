package server.dispatching;

import java.nio.channels.SelectionKey;
import java.util.concurrent.LinkedBlockingQueue;

public class DelegationsDispenser
{
    private static final LinkedBlockingQueue<Delegation> DISPENSER = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<Delegation> BACK_DISPENSER = new LinkedBlockingQueue<>();

    public DelegationsDispenser(){}

    public static void delegateRead(SelectionKey delegation)
    {
        DISPENSER.add(new Delegation(delegation, OperationType.READ));
    }

    public static void delegateWrite(SelectionKey delegation)
    {
        DISPENSER.add(new Delegation(delegation, OperationType.WRITE));
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