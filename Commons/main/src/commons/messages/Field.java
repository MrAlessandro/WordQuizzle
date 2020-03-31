package commons.messages;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Objects;

import commons.messages.exceptions.InvalidMessageFormatException;

/**
 * Class which represent field of a {@link Message}. It can contains only textual data.
 * @author Alessandro Meschi
 * @version 1.0
 */
public class Field
{
    private int size;
    private char[] data;

    /**
     * Initialize a new empty {@link Field}
     */
    protected Field()
    {
        this.size = 0;
        this.data = null;
    }

    /**
     * Initialize a new {@link Field} containing the data contained in {@code content}
     * @param content {@code char} array containing the data tu put in this {@link Field}
     */
    protected Field(char[] content)
    {
        this.data = content;
        this.size = (short) content.length;
    }

    /**
     * Gets he size of the content of this {@link Field}
     * @return The size of the content of this {@link Field}
     */
    public int size()
    {
        return this.size;
    }

    /**
     * Get the textual data contained in this {@link Field}
     * @return The textual data contained in this {@link Field} as {@code char} array
     */
    public char[] getData()
    {
        return this.data;
    }

    /**
     * Sets the this {@link Field} content with the data contained in {@code body}
     */
    private void setData(char[] data)
    {
        this.data = data;
    }

    /**
     * Static method which realizes the reading of a {@link Field} from a {@link SocketChannel}.
     * The reading is composed by the reading of the {@link Field}'s size ({@code int} of 4 bytes), and,
     * accordingly to the size just read, by the {@link Field}'s data are read.
     * @param sender {@link SocketChannel} on which make the reading.
     * @param buffer {@link ByteBuffer} where to store the bytes read
     * @return The {@link Field} read
     * @throws InvalidMessageFormatException If has been read a negative field size
     * @throws IOException If happens an error on the socket during the reading
     */
    protected static Field readField(SocketChannel sender, ByteBuffer buffer) throws IOException, InvalidMessageFormatException
    {
        if (sender == null || buffer == null)
            throw new NullPointerException();

        Field field = new Field();
        CharBuffer charView;
        int numReadBytes;
        int length;

        // Reading the field's length in the buffer
        buffer.clear();
        buffer.limit(4);
        while (buffer.hasRemaining())
        {
            numReadBytes = sender.read(buffer);
            if(numReadBytes == -1)
                throw new IOException("CLIENT CLOSED CONNECTION");
        }

        // Get the length from the buffer
        buffer.flip();
        length = buffer.getInt();
        buffer.clear();

        // Check if is a valid length (0 sized fields are valid)
        if (length < 0)
            throw new InvalidMessageFormatException("INVALID FIELD LENGTH");

        // Set the field's length
        field.size = length;

        // Read the field's data in the buffer
        buffer.limit(field.size * 2);
        while (buffer.hasRemaining())
        {
            numReadBytes = sender.read(buffer);
            if(numReadBytes == -1)
            {
                buffer.clear();
                throw new IOException("CLIENT CLOSED CONNECTION");
            }
        }

        // Get the data from the buffer
        buffer.flip();
        charView = buffer.asCharBuffer();
        char[] data = new char[field.size];
        charView.get(data);
        buffer.clear();

        // Set the field's data with the read data
        field.setData(data);

        return field;
    }

    /**
     * Static method which realizes the writing of a {@link Field} to a {@link SocketChannel}.
     * The writing is composed by the writing of the {@link Field}'s size ({@code int} of 4 bytes),
     * by the writing of the {@link Field}'s data
     * @param receiver {@link SocketChannel} on which make the writing.
     * @param buffer {@link ByteBuffer} where to store the bytes to write
     * @param field {@link Field} to write
     * @return The amount of written bytes
     * @throws IOException If happens an error on the socket during the writing
     */
    protected static int writeField(SocketChannel receiver, ByteBuffer buffer, Field field) throws IOException
    {
        if (receiver == null || buffer == null || field == null)
            throw new NullPointerException();

        CharBuffer charView;
        int writtenBytes = 0;

        // Put the field's size into the buffer
        buffer.clear();
        buffer.putInt(field.size);

        // Write the size on the socket
        buffer.flip();
        while (buffer.hasRemaining())
            writtenBytes = receiver.write(buffer);

        //Put the field's data
        buffer.clear();
        charView = buffer.asCharBuffer();
        charView.put(field.getData());
        buffer.position(buffer.position() + charView.position()*2);

        // Write field's data on the socket
        buffer.flip();
        while (buffer.hasRemaining())
            writtenBytes += receiver.write(buffer);

        return writtenBytes;
    }

    public String toString()
    {
        return String.valueOf(this.data);
    }

    /**
     * This method generate the {@code JSON} serialization of this {@link Field} represented as a {@link JSONObject}
     * @return The {@link JSONObject} representing the {@code JSON} serialization of this {@link Field}
     */
    protected JSONObject serialize()
    {
        JSONObject serializedThis = new JSONObject();
        serializedThis.put("Size", size);
        serializedThis.put("Body", String.valueOf(this.data));
        return serializedThis;
    }

    /**
     * This static method takes a {@link JSONObject} representing the {@code JSON} serialization of a {@link Field}
     * and returns the deserialized instance of the {@link Field}
     * @param serialized The {@link JSONObject} serialization of the wanted {@link Field}
     * @return The deserialized instance of {@link Field} represented by the given {@link JSONObject}
     */
    protected static Field deserialize(JSONObject serialized)
    {
        String gotBody = (String) serialized.get("Body");
        return new Field(gotBody.toCharArray());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        Field field = (Field) o;
        return this.size == field.size && Arrays.equals(this.getData(), field.getData());
    }
}
