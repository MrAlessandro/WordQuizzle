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

    private Message()
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
                buffer.clear();
                return Short.MIN_VALUE;
            }
        }

        buffer.flip();
        shortRead = buffer.getShort();
        buffer.clear();

        return shortRead;
    }

    protected static int writeShort(SocketChannel receiver, ByteBuffer buffer, short toWrite) throws IOException
    {
        if (receiver == null || buffer == null)
            throw new NullPointerException();

        int writtenBytes = 0;

        buffer.clear();
        buffer.putShort(toWrite);
        buffer.flip();

        while (buffer.hasRemaining())
            writtenBytes = receiver.write(buffer);

        buffer.clear();

        return writtenBytes;
    }

    public static Message readMessage(SocketChannel sender, ByteBuffer buffer) throws IOException, InvalidMessageFormatException
    {
        if (sender == null || buffer == null)
            throw new NullPointerException();

        Message message = new Message();
        short messageType;
        short fieldsNum;

        // Read message type
        messageType = readShort(sender, buffer);
        if (messageType == Short.MIN_VALUE)
            return null;
        if (MessageType.valueOf(messageType) == null)
            throw new InvalidMessageFormatException("Invalid message type");

        message.setType(MessageType.valueOf(messageType));

        // Read fields number
        fieldsNum = readShort(sender, buffer);
        if (fieldsNum == Short.MIN_VALUE)
            return null;
        if (fieldsNum < 0)
            throw new InvalidMessageFormatException("Invalid fields number");


        for (int i = 0; i < fieldsNum; i++)
        {
            // Read field length
            Field field = Field.readField(sender, buffer);
            if (field == null)
                return null;

            message.addField(field);
        }

        return message;
    }

    public static int writeMessage(SocketChannel client, ByteBuffer buffer, Message toSend) throws IOException
    {
        if (client == null || buffer == null || toSend == null)
            throw new NullPointerException();

        int writtenBytes = writeShort(client, buffer, toSend.type.getValue());
        writtenBytes += writeShort(client, buffer, (short) toSend.fields.size());

        for (Field field : toSend.fields)
        {
            writtenBytes += Field.writeField(client, buffer, field);
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
