package dispatching;

import java.nio.channels.SelectionKey;
import java.util.concurrent.LinkedBlockingQueue;

public class DelegationsDispenser
{
    private static final DelegationsDispenser INSTANCE = new DelegationsDispenser();
    private static final LinkedBlockingQueue<SelectionKey> READ_DISPENSER = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<SelectionKey> WRITE_DISPENSER = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<SelectionKey> BACK_DISPENSER = new LinkedBlockingQueue<>();

    public DelegationsDispenser(){}

    public static void delegateRead(SelectionKey delegation)
    {
        READ_DISPENSER.add(delegation);
    }

    public static SelectionKey getReadDelegation() throws InterruptedException
    {
        return READ_DISPENSER.take();
    }

    public static void delegateWrite(SelectionKey delegation)
    {
        WRITE_DISPENSER.add(delegation);
    }

    public static SelectionKey getWriteDelegation() throws InterruptedException
    {
        return WRITE_DISPENSER.take();
    }

    public static void backDelegate(SelectionKey delegation)
    {
        BACK_DISPENSER.add(delegation);
    }

    public static SelectionKey getDelegationBack() throws InterruptedException
    {
        return BACK_DISPENSER.poll();
    }
}