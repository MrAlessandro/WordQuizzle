package dispatching;

import java.util.concurrent.LinkedBlockingQueue;

public class DelegationsDispenser
{
    private static final DelegationsDispenser instance = new DelegationsDispenser();
    private static final LinkedBlockingQueue<Delegation> Dispenser = new LinkedBlockingQueue<>();

    public DelegationsDispenser(){}

    public static void add(Delegation delegation)
    {
        Dispenser.add(delegation);
    }

    public static Delegation get() throws InterruptedException
    {
        return Dispenser.take();
    }
}