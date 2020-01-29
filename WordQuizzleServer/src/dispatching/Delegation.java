package dispatching;

import java.nio.channels.SelectionKey;

public class Delegation
{
    private OperationType type;
    private SelectionKey selection;

    public Delegation(SelectionKey selection, OperationType opType)
    {
        this.type = opType;
        this.selection = selection;
    }

    public OperationType getType()
    {
        return this.type;
    }

    public SelectionKey getSelection()
    {
        return this.selection;
    }

    public void setType(OperationType opType)
    {
        this.type = opType;
    }
}
