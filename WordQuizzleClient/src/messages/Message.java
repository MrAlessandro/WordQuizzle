package messages;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class Message
{
    protected MessageType type;
    private LinkedList<Field> fields;

    private Message(MessageType type)
    {
        this.type = type;
        this.fields = null;
    }

    public Message(MessageType type, String ...argsList)
    {
        this.type = type;
        this.fields = new LinkedList<>();

        for (String current : argsList)
        {
            this.fields.addLast(new Field(current.toCharArray()));
        }
    }

    private Message(MessageType type, LinkedList<Field> fields)
    {
        this.type = type;
        this.fields = fields;
    }

    public MessageType getType()
    {
        return this.type;
    }

    public void addField(char[] field)
    {
        if (this.fields == null)
            this.fields = new LinkedList<>();

        this.fields.addLast(new Field(field));
    }

    public Iterator<Field> getFieldsIterator()
    {
        return this.fields.iterator();
    }

    public char[] getField(int index)
    {
        return this.fields.get(index).getBody();
    }

    public static Message readMessage(SocketChannel client, ByteBuffer buffer) throws InvalidMessageFormatException, IOException
    {
        Message readMessage;
        int numReadBytes;

        // Read message type
        buffer.clear();
        buffer.limit(2);
        numReadBytes = client.read(buffer);
        if(numReadBytes < 2)
        {
            buffer.clear();
            throw new InvalidMessageFormatException("Too few bytes read");
        }

        buffer.flip();
        short type = buffer.getShort();
        buffer.clear();
        if (MessageType.valueOf(type) == null)
            throw new InvalidMessageFormatException("Invalid message type");

        // Initialize message
        readMessage = new Message(MessageType.valueOf(type));
        CharBuffer charView;

        // Read fields
        while (numReadBytes > 0)
        {
            buffer.limit(2);
            numReadBytes = client.read(buffer);
            if (numReadBytes == 0)
                break;
            if(numReadBytes < 2)
            {
                buffer.clear();
                throw new InvalidMessageFormatException("Too few bytes read");
            }

            buffer.flip();
            short fieldLength = buffer.getShort();
            buffer.clear();
            if (fieldLength < 0)
                throw new InvalidMessageFormatException("Invalid field size");

            buffer.limit(fieldLength * 2);
            numReadBytes = client.read(buffer);
            if(numReadBytes < fieldLength)
            {
                buffer.clear();
                throw new InvalidMessageFormatException("Too few bytes read");
            }

            buffer.flip();
            charView = buffer.asCharBuffer();
            char[] charBuffer = new char[fieldLength/2];
            for (int i = 0; i < charBuffer.length; i++)
            {
                try
                {
                    charBuffer[i] = charView.get();
                }
                catch (BufferOverflowException e)
                {
                    buffer.clear();
                    throw new InvalidMessageFormatException("Too few bytes read");
                }
            }
            buffer.clear();

            readMessage.addField(charBuffer);
        }

        return readMessage;
    }

    public static int writeMessage(SocketChannel client, ByteBuffer buffer, Message toSend) throws IOException
    {
        int writtenBytes = 0;

        buffer.clear();
        buffer.putShort(toSend.type.getValue());
        buffer.flip();

        writtenBytes += client.write(buffer);

        for (Field field : toSend.fields)
        {
            // Write field's length
            buffer.clear();
            buffer.putShort(field.size());
            buffer.flip();
            writtenBytes += client.write(buffer);

            // Write field's body
            buffer.clear();
            CharBuffer charView = buffer.asCharBuffer();
            charView.put(field.getBody());
            buffer.position(buffer.position() + charView.position()*2);
            buffer.flip();
            writtenBytes += client.write(buffer);
        }

        return writtenBytes;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("Type: ").append(type.toString()).append("; Fields: ");

        for (Field current : this.fields)
        {
            builder.append("'").append(String.valueOf(current.getBody())).append("'").append(", ");
        }

        return builder.toString();
    }
}
