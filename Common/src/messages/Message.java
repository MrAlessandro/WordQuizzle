package messages;

import messages.exceptions.InvalidMessageFormatException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

public class Message
{
    protected MessageType type;
    private LinkedList<Field> fields;

    public Message()
    {
        this.fields = null;
        this.type = null;
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

    protected void setType(MessageType type)
    {
        this.type = type;
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

    public void addField(Field field)
    {
        if (this.fields == null)
            this.fields = new LinkedList<>();

        this.fields.addLast(field);
    }

    public Iterator<Field> getFieldsIterator()
    {
        return this.fields.iterator();
    }

    public char[] getField(int index)
    {
        return this.fields.get(index).getBody();
    }

    protected static short readShort(SocketChannel sender, ByteBuffer buffer) throws IOException
    {
        if (sender == null || buffer == null)
            throw new NullPointerException();

        short shortRead;
        int numReadBytes;

        buffer.clear();
        buffer.limit(2);
        while (buffer.hasRemaining())
        {
            numReadBytes = sender.read(buffer);
            if(numReadBytes == -1)
            {
                throw new IOException();
            }
        }

        buffer.flip();
        shortRead = buffer.getShort();
        buffer.clear();

        return shortRead;
    }

    protected static int writeShort(SocketChannel receiver, ByteBuffer buffer, short toWrite) throws IOException
    {
        int writtenBytes = 0;

        buffer.clear();
        buffer.putShort(toWrite);
        buffer.flip();

        while (buffer.hasRemaining())
            writtenBytes = receiver.write(buffer);

        buffer.clear();

        return writtenBytes;
    }

    public static Message readMessage(SocketChannel sender, ByteBuffer buffer)
    {
        Message message = new Message();
        short messageType;
        short fieldsNum;

        // Read message type
        try
        {
            messageType = readShort(sender, buffer);
        }
        catch (IOException e)
        {
            return null;
        }

        if (MessageType.valueOf(messageType) == null)
            return null;

        message.setType(MessageType.valueOf(messageType));

        // Read fields number
        try
        {
            fieldsNum = readShort(sender, buffer);
        }
        catch (IOException e)
        {
            return null;
        }

        if (fieldsNum < 0)
            return null;


        for (int i = 0; i < fieldsNum; i++)
        {
            Field field;

            try
            {
                field = Field.readField(sender, buffer);
            }
            catch (IOException e)
            {
                return null;
            }

            if (field == null)
                return null;

            message.addField(field);
        }

        return message;
    }

    public static int writeMessage(SocketChannel client, ByteBuffer buffer, Message toSend)
    {
        int writtenBytes;

        try
        {
            writtenBytes = writeShort(client, buffer, toSend.type.getValue());
            writtenBytes += writeShort(client, buffer, (short) toSend.fields.size());

            for (Field field : toSend.fields)
            {
                writtenBytes += Field.writeField(client, buffer, field);
            }

        }
        catch (IOException e)
        {
            return -1;
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

        builder.append("Type: ").append(type.toString()).append(";");

        if (fields != null && fields.size() > 0)
        {
            int i = 1;
            for (Field field : this.fields)
            {
                builder.append(" Field").append(i).append(": ").append(field);
                if (i != fields.size())
                    builder.append(",");
                i++;
            }
        }

        return builder.toString();
    }
}
