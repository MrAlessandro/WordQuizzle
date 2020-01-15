package CommunicationDispatching;

import java.util.concurrent.LinkedBlockingQueue;

public class DelegationsBackDispenser
{
    private static final DelegationsBackDispenser instance = new DelegationsBackDispenser();
    private static final LinkedBlockingQueue<Delegation> BackDispenser = new LinkedBlockingQueue<>();

    public DelegationsBackDispenser(){}

    public static void add(Delegation delegation)
    {
        BackDispenser.add(delegation);
    }

    public static Delegation get() throws InterruptedException
    {
        return BackDispenser.take();
    }
}
