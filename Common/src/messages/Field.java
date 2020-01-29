package messages;

import messages.exceptions.InvalidMessageFormatException;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;

public class Field
{
    private short length;
    private char[] body;

    protected Field()
    {
        this.length = 0;
        this.body = null;
    }

    protected Field(char[] content)
    {
        this.body = content;
        this.length = (short) content.length;
    }

    public char[] getBody()
    {
        return this.body;
    }

    private void setBody(char[] body)
    {
        this.body = body;
    }

    public short size()
    {
        return this.length;
    }

    protected static Field readField(SocketChannel sender, ByteBuffer buffer) throws IOException, InvalidMessageFormatException
    {
        Field field = new Field();
        CharBuffer charView;
        int numReadBytes;
        short length;

        buffer.clear();
        buffer.limit(2);
        while (buffer.hasRemaining())
        {
            numReadBytes = sender.read(buffer);
            if(numReadBytes == -1)
                throw new IOException();
        }

        buffer.flip();
        length = buffer.getShort();
        buffer.clear();

        if (length <= 0)
            throw new InvalidMessageFormatException("INVALID FIELD LENGTH");

        field.length = length;

        buffer.limit(field.length * 2);
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
        charView = buffer.asCharBuffer();
        char[] body = new char[field.length];
        charView.get(body);
        buffer.clear();

        field.setBody(body);

        return field;
    }

    protected static int writeField(SocketChannel receiver, ByteBuffer buffer, Field field) throws IOException
    {
        if (receiver == null || buffer == null || field == null)
            throw new NullPointerException();

        CharBuffer charView;
        int writtenBytes = 0;

        buffer.clear();
        buffer.putShort(field.length);
        buffer.flip();

        while (buffer.hasRemaining())
            writtenBytes = receiver.write(buffer);

        buffer.clear();

        charView = buffer.asCharBuffer();
        charView.put(field.getBody());
        buffer.position(buffer.position() + charView.position()*2);
        buffer.flip();
        writtenBytes += receiver.write(buffer);
        buffer.clear();

        return writtenBytes;
    }

    protected JSONObject JSONserialize()
    {
        JSONObject serializedThis = new JSONObject();
        serializedThis.put("Length", length);
        serializedThis.put("Body", String.valueOf(this.body));
        return serializedThis;
    }

    protected static Field JSONdeserialize(JSONObject serialized)
    {
        String gotBody = (String) serialized.get("Body");
        return new Field(gotBody.toCharArray());
    }

    public String toString()
    {
        return String.valueOf(this.body);
    }
}
