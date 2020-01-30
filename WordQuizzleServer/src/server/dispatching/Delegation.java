package server.dispatching;

import java.nio.channels.SocketChannel;

public class Delegation
{
    private OperationType type;
    private SocketChannel selection;
    private Object attachment;

    public Delegation(SocketChannel selection, OperationType opType, Object attachment)
    {
        this.type = opType;
        this.selection = selection;
        this.attachment = attachment;
    }

    public void attach(Object attachment)
    {
        this.attachment = attachment;
    }

    public Object attachment()
    {
        return this.attachment;
    }

    public OperationType getType()
    {
        return this.type;
    }

    public SocketChannel getSelection()
    {
        return this.selection;
    }

    public void setType(OperationType opType)
    {
        this.type = opType;
    }
}
