package messages;

import messages.exceptions.InvalidMessageFormatException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
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
        this.fields = new LinkedList<>();
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

    public static Message readMessage(SocketChannel client, ByteBuffer buffer) throws IOException, InvalidMessageFormatException
    {
        if (client == null || buffer == null)
            throw new NullPointerException();

        Message readMessage;
        int numReadBytes;

        // Read message type
        buffer.clear();
        buffer.limit(2);
        numReadBytes = client.read(buffer);
        if(numReadBytes == -1)
        {// Client closed connection
            return null;
        }
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
        while (true)
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

            if (fieldLength == 0 || fieldLength < -1)
                throw new InvalidMessageFormatException("Invalid field size");
            else if (fieldLength == -1)
                // End of message
                break;
            else
            {
                buffer.limit(fieldLength * 2);
                numReadBytes = client.read(buffer);
                if(numReadBytes < fieldLength * 2)
                {
                    buffer.clear();
                    throw new InvalidMessageFormatException("Too few bytes read");
                }
                buffer.flip();
                charView = buffer.asCharBuffer();
                char[] charBuffer = new char[fieldLength];
                charView.get(charBuffer);
                buffer.clear();

                readMessage.addField(charBuffer);
            }
        }

        return readMessage;
    }

    public static int writeMessage(SocketChannel client, ByteBuffer buffer, Message toSend) throws IOException
    {
        if (client == null || buffer == null || toSend == null)
            throw new NullPointerException();

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

        // Write end of message
        buffer.clear();
        buffer.putShort((short) -1);
        buffer.flip();
        writtenBytes += client.write(buffer);

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
