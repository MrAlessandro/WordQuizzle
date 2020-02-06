package messages;

import messages.exceptions.InvalidMessageFormatException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.DatagramChannel;
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

    public char[] getField(int index)
    {
        return this.fields.get(index).getBody();
    }

    protected static short readShort(SocketChannel sender, ByteBuffer buffer) throws IOException
    {
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

    public static Message readMessage(SocketChannel sender, ByteBuffer buffer) throws InvalidMessageFormatException, IOException
    {
        Message message = new Message();
        short messageType;
        short fieldsNum;

        // Read message type
        messageType = readShort(sender, buffer);

        if (MessageType.valueOf(messageType) == null)
            throw new InvalidMessageFormatException("INVALID MESSAGE TYPE");

        message.setType(MessageType.valueOf(messageType));

        // Read fields number
        fieldsNum = readShort(sender, buffer);

        if (fieldsNum < 0)
            throw new InvalidMessageFormatException("INVALID FIELDS NUMBER");


        for (int i = 0; i < fieldsNum; i++)
        {
            Field field = Field.readField(sender, buffer);
            message.addField(field);
        }

        return message;
    }

    public static int writeMessage(SocketChannel client, ByteBuffer buffer, Message toSend) throws IOException
    {
        int writtenBytes = writeShort(client, buffer, toSend.type.getValue());
        writtenBytes += writeShort(client, buffer, (short) toSend.fields.size());

        for (Field field : toSend.fields)
        {
            writtenBytes += Field.writeField(client, buffer, field);
        }

        return writtenBytes;
    }

    // Client use only
    public static Message readNotification(DatagramChannel UDPchannel, ByteBuffer buffer) throws IOException, InvalidMessageFormatException
    {
        Message message = new Message();
        short messageType;
        short fieldsNum;

        buffer.clear();

        UDPchannel.receive(buffer);

        buffer.flip();

        messageType = buffer.getShort();
        if (MessageType.valueOf(messageType) == null)
            throw new InvalidMessageFormatException("INVALID MESSAGE TYPE");

        message.setType(MessageType.valueOf(messageType));

        fieldsNum = buffer.getShort();
        if (fieldsNum < 0)
            throw new InvalidMessageFormatException("INVALID FIELDS NUMBER");

        for (int i = 0; i < fieldsNum; i++)
        {
            short length = buffer.getShort();
            if (length <= 0)
                throw new InvalidMessageFormatException("INVALID FIELD LENGTH");

            char[] body = new char[length];
            for (int j = 0; j < length; j++)
            {
                body[j] = buffer.getChar();
            }

            message.addField(body);
        }

        return message;
    }

    // Server use only
    public static int writeNotification(DatagramChannel UDPchannel, SocketAddress destination,  ByteBuffer buffer, Message message) throws IOException
    {
        int written;

        buffer.clear();
        buffer.putShort(message.type.getValue());
        buffer.putShort((short) message.fields.size());

        for (Field field : message.fields)
        {
            buffer.putShort(field.size());
            for (int i = 0; i < field.getBody().length; i++)
            {
                buffer.putChar(field.getBody()[i]);
            }
        }

        buffer.flip();

        written = UDPchannel.send(buffer, destination);

        buffer.clear();

        return written;
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
        MessageType resType =  MessageType.valueOf((short) ((Long) serializedMessage.get("Type")).intValue());
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
