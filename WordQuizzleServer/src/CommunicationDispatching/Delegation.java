package CommunicationDispatching;

import java.nio.channels.SelectionKey;

public class Delegation
{
    protected SelectionKey Key;
    protected OperationType OpType;

    public Delegation(SelectionKey key, OperationType opType)
    {
        this.Key = key;
        this.OpType = opType;
    }

    public SelectionKey getKey()
    {
        return this.Key;
    }

    public OperationType getOpType()
    {
        return this.OpType;
    }
}

