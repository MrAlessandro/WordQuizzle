package dispatching;

import java.nio.channels.SelectionKey;

public class Delegation
{
    private OperationType type;
    private SelectionKey delegation;

    public Delegation(SelectionKey key, OperationType opType)
    {
        this.delegation = key;
        this.type = opType;
    }

    public OperationType getType()
    {
        return this.type;
    }

    public SelectionKey getDelegation()
    {
        return this.delegation;
    }
}
