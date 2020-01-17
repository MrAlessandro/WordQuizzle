package Messages;

import Exceptions.InvalidMessageFormatException;
import Utility.Constants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class Message
{
    private static final ByteBuffer buffer = ByteBuffer.allocateDirect(2048);

    public MessageType type;
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

    private void addField(char[] field)
    {
        if (this.fields == null)
            this.fields = new LinkedList<>();

        this.fields.addLast(new Field(field));
    }

    private Iterator<Field> getFieldsIterator()
    {
        return this.fields.iterator();
    }


    private static Message readMessage(SocketChannel client) throws InvalidMessageFormatException, IOException
    {
        Message readMessage;
        int numReadBytes;

        buffer.position(buffer.capacity()-2-1);
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

        readMessage = new Message(MessageType.valueOf(type));

        while (numReadBytes > 0)
        {
            buffer.position(buffer.capacity()-2-1);
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
                throw new InvalidMessageFormatException("Invalid message type");

            buffer.position(buffer.capacity()-fieldLength-1);
            numReadBytes = client.read(buffer);
            if(numReadBytes < fieldLength)
            {
                buffer.clear();
                throw new InvalidMessageFormatException("Too few bytes read");
            }

            buffer.flip();
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

    /*TODO*/
    private static int writeMessage(SocketChannel client, Message toSend)
    {
        byte[] bytesToWrite;
        int length = 6; // sizeof(MessageType)=4 + sizeof(\0)=1 + .... + sizeof(\0)=1
        // Calculate length for every field
        Iterator<char[]> iter = toSend.getFieldsIterator();
        while (iter.hasNext())
        {
            char[] current = iter.next();
            length += current.length + 1;
        }

        // Allocate array to write
        bytesToWrite = new byte[length];

        // Set message type
        System.arraycopy(Constants.intToByteArray(toSend.type.getValue()), 0, bytesToWrite, 0, 4);
        bytesToWrite[4] = '\0';

        // Write down field within the array
        int position = 5;
        iter = toSend.getFieldsIterator();
        while (iter.hasNext())
        {
            char[] current = iter.next();
            for (int i = 0; i < current.length; i++)
            {
                bytesToWrite[position] = (byte) current[i];
                position++;
            }

            bytesToWrite[position] = '\0';
            position++;
        }

        //
        bytesToWrite[position] = '\0';

        /*TODO
        *  Bytes array to write is ready, writing part missing
        *       Volendo, aggiustare la traduzione dei char in bytes e viceversa (2 bytes per char piuttosto che 1) */

        return 0;
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
