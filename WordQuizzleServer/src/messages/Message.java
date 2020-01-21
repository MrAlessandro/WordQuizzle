package messages;

import exceptions.InvalidMessageFormatException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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

    private void addField(char[] field)
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
        buffer.position(buffer.capacity()-2);
        numReadBytes = client.read(buffer);
        if(numReadBytes < 2)
        {
            buffer.clear();
            throw new InvalidMessageFormatException("Too few bytes read");
        }

        buffer.flip();
        buffer.limit(buffer.capacity());
        buffer.position(buffer.capacity()-2);
        short type = buffer.getShort();
        buffer.clear();
        if (MessageType.valueOf(type) == null)
            throw new InvalidMessageFormatException("Invalid message type");

        // Initialize message
        readMessage = new Message(MessageType.valueOf(type));

        // Read fields
        while (numReadBytes > 0)
        {
            buffer.position(buffer.capacity()-2);
            numReadBytes = client.read(buffer);
            if (numReadBytes == 0)
                break;
            if(numReadBytes < 2)
            {
                buffer.clear();
                throw new InvalidMessageFormatException("Too few bytes read");
            }

            buffer.flip();
            buffer.limit(buffer.capacity());
            buffer.position(buffer.capacity()-2);
            short fieldLength = buffer.getShort();
            buffer.clear();
            if (fieldLength < 0)
                throw new InvalidMessageFormatException("Invalid field size");

            buffer.position(buffer.capacity()-fieldLength);
            numReadBytes = client.read(buffer);
            if(numReadBytes < fieldLength)
            {
                buffer.clear();
                throw new InvalidMessageFormatException("Too few bytes read");
            }

            buffer.flip();
            buffer.limit(buffer.capacity());
            buffer.position(buffer.capacity()-fieldLength);
            char[] charBuffer = new char[fieldLength/2];
            for (int i = 0; i < charBuffer.length; i++)
            {
                try
                {
                    charBuffer[i] = buffer.getChar();
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
        CharBuffer

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
            for (int i = 0; i < field.getBody().length; i++)
            {
                buffer.putChar(field.getBody()[i]);
            }
            buffer.flip();
            writtenBytes += client.write(buffer);
        }

        return writtenBytes;
    }

    public JSONObject JSONserialize()
    {
        JSONObject retValue = new JSONObject();
        JSONArray fieldsList = new JSONArray();

        retValue.put("Type", this.type.getValue());

        for (Field current : this.fields)
        {
            fieldsList.add(current.JSONserialize());
        }

        retValue.put("Fields", fieldsList);

        return retValue;
    }

    public static Message JSONdeserialize(JSONObject serializedMessage)
    {
        MessageType resType =  MessageType.valueOf((short) serializedMessage.get("Type"));
        LinkedList<Field> DEfields = new LinkedList<>();

        JSONArray messageFiledList = (JSONArray) serializedMessage.get("Fields");
        for (JSONObject field : (Iterable<JSONObject>) messageFiledList)
        {
            DEfields.addLast(Field.JSONdeserialize(field));
        }

        return new Message(resType, DEfields);
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
