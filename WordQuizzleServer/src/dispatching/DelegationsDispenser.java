package dispatching;

import java.nio.channels.SelectionKey;
import java.util.concurrent.LinkedBlockingQueue;

public class DelegationsDispenser
{
    private static final DelegationsDispenser INSTANCE = new DelegationsDispenser();
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

    public static Delegation getDelegation() throws InterruptedException
    {
        return DISPENSER.take();
    }


    public static void delegateBack(SelectionKey delegation, OperationType type)
    {
        BACK_DISPENSER.add(new Delegation(delegation, type));
    }

    public static void delegateBack(Delegation delegation)
    {
        BACK_DISPENSER.add(delegation);
    }

    public static Delegation getDelegationBack() throws InterruptedException
    {
        return BACK_DISPENSER.poll();
    }
}