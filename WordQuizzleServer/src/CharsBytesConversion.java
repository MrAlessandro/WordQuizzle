import messages.Field;
import messages.Message;
import messages.MessageType;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Iterator;

public class CharsBytesConversion
{
    public static void main(String[] args)
    {
        ByteBuffer buffer = ByteBuffer.allocate(128);
        Message message = new Message(MessageType.LOG_IN, "漢", "字");

        buffer.putShort(message.getType().getValue());

        Iterator<Field> iter = message.getFieldsIterator();
        while (iter.hasNext())
        {
            Field current = iter.next();
            // Write field's length
            buffer.putShort(current.size());

            CharBuffer charView = buffer.asCharBuffer();
            charView.put(current.getBody());
            buffer.position(buffer.position() + charView.position()*2);
        }

        System.out.println(message);
    }

}
