package dispatching;

import java.nio.channels.SocketChannel;

public class Delegation
{
    private OperationType type;
    private SocketChannel channel;
    private Object attachment;

    public Delegation(SocketChannel key, OperationType opType, Object attachment)
    {
        this.channel = key;
        this.type = opType;
        this.attachment = attachment;
    }

    public OperationType getType()
    {
        return this.type;
    }

    public SocketChannel getChannel()
    {
        return this.channel;
    }

    public void setType(OperationType opType)
    {
        this.type = opType;
    }

    public Object getAttachment()
    {
        return this.attachment;
    }

    public void attach(Object attachment)
    {
        this.attachment = attachment;
    }

}
