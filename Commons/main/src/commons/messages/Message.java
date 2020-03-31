package commons.messages;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;

import commons.messages.exceptions.InvalidMessageFormatException;

/**
 * This class represent the message, whose instances are exchanged between server and clients in order to send and
 * receive information.
 * The {@link Message} is composed by:
 * <li><b>A {@link MessageType}</b>: Which describes the purpose and the meaning of the message.</li>
 * <li><b>A {@link Collection} of {@link Field}</b>: Which contains the various information related to the {@link Message}.</li>
 * @author Alessandro
 * @version 1.0
 */
public class Message
{
    private MessageType type;
    private LinkedList<Field> fields;

    /**
     * Initialize a new uncategorized and empty {@link Message}.
     */
    public Message()
    {
        this.fields = null;
        this.type = null;
    }

    /**
     * Initialize a new message of the given {@link MessageType} and 0 or more {@link String}, eventually added  to the
     * new {@link Message} as fields.
     * @param type The {@link MessageType} of the new {@link Message}
     * @param fields The {@link String} list used to make the fields
     */
    public Message(MessageType type, String ...fields)
    {
        this.type = type;
        this.fields = new LinkedList<>();

        for (String current : fields)
        {
            this.fields.addLast(new Field(current.toCharArray()));
        }
    }

    /**
     * Initialize a new message of the given {@link MessageType} and 0 or more {@link String},
     * contained in a {@link Collection} eventually added  to the new {@link Message} as fields.
     * @param type The {@link MessageType} of the new {@link Message}
     * @param fields The {@link LinkedList} containing the strings used to make the fields
     */
    private Message(MessageType type, Collection<Field> fields)
    {
        this.type = type;
        this.fields = new LinkedList<>(fields);
    }

    /**
     * This method return the {@link MessageType} related to this {@link Message}.
     * @return The {@link MessageType} related to this {@link Message}
     */
    public MessageType getType()
    {
        return this.type;
    }

    /**
     * This method sets the {@link MessageType} related to this {@link Message}.
     * @param type The {@link MessageType} to assign at this {@link Message}
     */
    protected void setType(MessageType type)
    {
        this.type = type;
    }

    /**
     * This method append a new {@link Field} containing the given data (as {@code char} array),
     * to the {@link Field} collection of this message,
     * @param field The data of the {@link Field} to append at the {@link Field} list of this {@link Message}
     */
    public void addField(char[] field)
    {
        if (this.fields == null)
            this.fields = new LinkedList<>();

        this.fields.addLast(new Field(field));
    }

    /**
     * This method append the given {@link Field} to the {@link Field} list of this message.
     * @param field The {@link Field} to append at the {@link Field} collection of this {@link Message}.
     */
    public void addField(Field field)
    {
        if (this.fields == null)
            this.fields = new LinkedList<>();

        this.fields.addLast(field);
    }

    /**
     * This method gets the {@link Field} list of this {@link Message} and return it as a {@link Field} array.
     * @return The {@link Field} collection of this {@link Message} as a {@link Field} array.
     */
    public Field[] getFields()
    {
        return this.fields.toArray(new Field[0]);
    }

    /**
     * This method return the number of {@link Field} contained in the {@link Field} collection of this {@link Message}.
     * @return The number of {@link Field} contained in the {@link Field} collection of this {@link Message}.
     */
    public int amountOfFields()
    {
        return this.fields.size();
    }

    /**
     * This method return the data of the {@link Field} identified by the given index within the {@link Field} collection
     * related to this {@link Message}.
     * @param index Index of the wanted {@link Field} within the {@link Field} collection related to this {@link Message}.
     * @return The data of the wanted {@link Field} as {@code char} array.
     */
    public char[] getFieldDataAt(int index)
    {
        try
        {
            return this.fields.get(index).getData();
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }
    }

    /**
     * This method check the validity of this {@link Message}. Specifically it checks the number of fields according
     * to the related {@link MessageType}
     * @throws InvalidMessageFormatException If this {@link Message} is malformed.
     */
    public void checkValidity() throws InvalidMessageFormatException
    {
        switch (this.getType())
        {
            case LOG_IN:
                if (this.fields.size() != 3)
                    throw new InvalidMessageFormatException("INVALID LOGIN MESSAGE");
                break;
            case REQUEST_FOR_FRIENDSHIP:
                if (this.fields.size() != 1)
                    throw new InvalidMessageFormatException("INVALID REQUEST FOR FRIENDSHIP MESSAGE");
                break;
            case CONFIRM_FRIENDSHIP_REQUEST:
                if (this.fields.size() != 1)
                    throw new InvalidMessageFormatException("INVALID CONFIRM FRIENDSHIP REQUEST MESSAGE");
                break;
            case DECLINE_FRIENDSHIP_REQUEST:
                if (this.fields.size() != 1)
                    throw new InvalidMessageFormatException("INVALID DECLINE FRIENDSHIP REQUEST MESSAGE");
                break;
            case REQUEST_FOR_CHALLENGE:
                if (this.fields.size() != 1)
                    throw new InvalidMessageFormatException("INVALID REQUEST FOR CHALLENGE MESSAGE");
                break;
            case CONFIRM_CHALLENGE_REQUEST:
                if (this.fields.size() != 1)
                    throw new InvalidMessageFormatException("INVALID CONFIRM CHALLENGE REQUEST MESSAGE");
                break;
            case DECLINE_CHALLENGE_REQUEST:
                if (this.fields.size() != 1)
                    throw new InvalidMessageFormatException("INVALID DECLINE CHALLENGE REQUEST MESSAGE");
                break;
        }
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

    /**
     * This method generate the {@code JSON} serialization of this {@link Message} represented as a {@link JSONObject}
     * @return The {@link JSONObject} representing the {@code JSON} serialization of this {@link Message}
     */
    public JSONObject serialize()
    {
        JSONObject retValue = new JSONObject();
        JSONArray fieldsList = new JSONArray();

        retValue.put("Type", this.type.getCode());

        for (Field current : this.fields)
        {
            fieldsList.add(current.serialize());
        }

        retValue.put("Fields", fieldsList);

        return retValue;
    }

    /**
     * This static method takes a {@link JSONObject} representing the {@code JSON} serialization of a {@link Message}
     * and returns the deserialized instance of the {@link Message}
     * @param serialized The {@link JSONObject} serialization of the wanted {@link Field}
     * @return The deserialized instance of {@link Field} represented by the given {@link JSONObject}
     */
    public static Message deserialize(JSONObject serialized)
    {
        // Get message type
        MessageType resType =  MessageType.valueOf((short) ((Long) serialized.get("Type")).intValue());

        // Get the list of fields
        LinkedList<Field> DEfields = new LinkedList<>();
        JSONArray messageFiledList = (JSONArray) serialized.get("Fields");
        for (JSONObject field : (Iterable<JSONObject>) messageFiledList)
        {
            // Deserialize field
            DEfields.addLast(Field.deserialize(field));
        }

        return new Message(resType, DEfields);
    }

    /**
     * Service static method for reading a {@code short} number from a given {@link SocketChannel}
     * @param sender {@link SocketChannel} on which make the reading
     * @param buffer {@link ByteBuffer} where to store the bytes read
     * @return The {@code short} read
     * @throws IOException If happens an error during the reading
     */
    private static short readShort(SocketChannel sender, ByteBuffer buffer) throws IOException
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
                throw new IOException("CLIENT CLOSED CONNECTION");
            }
        }

        buffer.flip();
        shortRead = buffer.getShort();
        buffer.clear();

        return shortRead;
    }

    /**
     * Service static method for writing a {@code short} number on a given {@link SocketChannel}
     * @param receiver {@link SocketChannel} on which make the writing
     * @param buffer {@link ByteBuffer} where to store the written bytes
     * @param toWrite {@code short} to be written
     * @return The number of written bytes ({@code short} size -> 2 bytes)
     * @throws IOException If happens an error during the writing
     */
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

    /**
     * Static method which realizes the reading of a request or response {@link Message} from a {@link SocketChannel}.
     * The reading is composed by the reading of the {@link MessageType}, then by the reading of the
     * amount of fields within the {@link Message}, then, one after the other, by the reading of the {@link Field} collection.
     * @param sender {@link SocketChannel} from which make the reading.
     * @param buffer {@link ByteBuffer} where to store the bytes read
     * @return The {@link Message} read
     * @throws InvalidMessageFormatException If has been read an invalid {@link MessageType} code or a negative fields amount
     * or a negative field size
     * @throws IOException If happens an error on the socket during the reading
     */
    public static Message readMessage(SocketChannel sender, ByteBuffer buffer) throws InvalidMessageFormatException, IOException
    {
        Message message = new Message();
        short messageType;
        short fieldsNum;

        // Read message type
        messageType = readShort(sender, buffer);

        // Check if is a valid type
        if (MessageType.valueOf(messageType) == null)
            throw new InvalidMessageFormatException("INVALID MESSAGE TYPE");

        // Set the type to the message
        message.setType(MessageType.valueOf(messageType));

        // Read fields number
        fieldsNum = readShort(sender, buffer);

        // Check if is a valid fields amount
        if (fieldsNum < 0)
            throw new InvalidMessageFormatException("INVALID FIELDS NUMBER");

        // Read the fields one after the other
        for (int i = 0; i < fieldsNum; i++)
        {
            Field field = Field.readField(sender, buffer);
            message.addField(field);
        }

        return message;
    }

    /**
     * Static method which realizes the writing of a request or response {@link Message} on a {@link SocketChannel}.
     * The writing is composed by the writing of the {@link MessageType}, then by the writing of the
     * amount of fields within the {@link Message}, then, one after the other, by the writing of the {@link Field} collection.
     * @param receiver {@link SocketChannel} on which make the writing.
     * @param buffer {@link ByteBuffer} where to store the bytes to be written
     * @param toSend {@link Message} to be written
     * @return The amount of written bytes
     * @throws IOException If happens an error on the socket during the writing
     */
    public static int writeMessage(SocketChannel receiver, ByteBuffer buffer, Message toSend) throws IOException
    {
        int writtenBytes;

        // Write the message type
        writtenBytes = writeShort(receiver, buffer, toSend.type.getCode());

        // Write the fields amount
        writtenBytes += writeShort(receiver, buffer, (short) toSend.fields.size());

        // Write the fields one after the other
        for (Field field : toSend.fields)
        {
            writtenBytes += Field.writeField(receiver, buffer, field);
        }

        return writtenBytes;
    }

    /**
     * Static method which realizes the reading of a notification {@link Message} from a {@link DatagramChannel}.
     * @param sender {@link DatagramChannel} from which make the reading.
     * @param buffer {@link ByteBuffer} where to store the bytes read
     * @return The {@link Message} read
     * @throws InvalidMessageFormatException If has been read an invalid {@link MessageType} code or a negative fields amount
     * or a negative field size
     * @throws IOException If happens an error on the socket during the reading
     */
    public static Message readNotification(DatagramChannel sender, ByteBuffer buffer) throws IOException, InvalidMessageFormatException
    {
        Message message = new Message();
        short messageType;
        short fieldsNum;

        // Reading of the entire message
        buffer.clear();
        sender.receive(buffer);

        // Get the message type
        buffer.flip();
        messageType = buffer.getShort();
        if (MessageType.valueOf(messageType) == null)
            throw new InvalidMessageFormatException("INVALID MESSAGE TYPE");

        // Set the type of the message
        message.setType(MessageType.valueOf(messageType));

        // Get the fields amount
        fieldsNum = buffer.getShort();
        if (fieldsNum < 0)
            throw new InvalidMessageFormatException("INVALID FIELDS NUMBER");

        // Get the fields one after the other
        for (int i = 0; i < fieldsNum; i++)
        {
            // Get the field size
            int length = buffer.getInt();
            if (length < 0)
                throw new InvalidMessageFormatException("INVALID FIELD LENGTH");

            // Get the field data
            char[] body = new char[length];
            for (int j = 0; j < length; j++)
            {
                body[j] = buffer.getChar();
            }

            // Append the gotten field to the message
            message.addField(body);
        }

        return message;
    }

    /**
     * Static method which realizes the writing of a notification {@link Message} on a {@link DatagramChannel}.
     * @param receiver {@link DatagramChannel} on which make the writing.
     * @param destination {@link SocketAddress} address of the receiver's {@link DatagramChannel}
     * @param buffer {@link ByteBuffer} where to store the bytes to be written
     * @param toSend {@link Message} to be written
     * @return The amount of written bytes
     * @throws IOException If happens an error on the socket during the writing
     */
    public static int writeNotification(DatagramChannel receiver, SocketAddress destination,  ByteBuffer buffer, Message toSend) throws IOException
    {
        int written;

        // Put the message type inside the buffer
        buffer.clear();
        buffer.putShort(toSend.type.getCode());

        // Put the amount of fields inside the buffer
        buffer.putShort((short) toSend.fields.size());

        // Put fields inside the buffer one after the other
        for (Field field : toSend.fields)
        {
            buffer.putInt(field.size());
            for (int i = 0; i < field.getData().length; i++)
            {
                buffer.putChar(field.getData()[i]);
            }
        }

        // Write the entire message
        buffer.flip();
        written = receiver.send(buffer, destination);

        return written;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        Message message = (Message) o;
        return getType() == message.getType() && this.getFields().equals(message.getFields());
    }
}
