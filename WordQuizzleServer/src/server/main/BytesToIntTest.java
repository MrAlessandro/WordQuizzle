package server.main;

import java.nio.ByteBuffer;
import java.util.LinkedList;

public class BytesToIntTest
{
    public static void main(String[] args)
    {
/*        byte[] bytes = ByteBuffer.allocate(4).putInt(6).array();;
        int val = 0;

        if(bytes.length>4)
            throw new RuntimeException("Too big to fit in int");

        for (int i = 0; i < bytes.length; i++)
        {
            val=val<<8;
            val=val|(bytes[i] & 0xFF);
        }

        System.out.println(val);*/

/*        messages.Message toSend = new messages.Message(messages.MessageType.OK, "Field1", "Field2", "Field3");

        byte[] bytesToWrite;
        int length = 6; // sizeof(messages.MessageType)=4 + sizeof(\0)=1 + .... + sizeof(\0)=1
        Iterator<char[]> iter = toSend.getFieldsIterator();
        while (iter.hasNext())
        {
            char[] current = iter.next();
            length += current.length + 1;
        }

        bytesToWrite = new byte[length];

        System.arraycopy(constants.Constants.intToByteArray(toSend.type.getValue()), 0, bytesToWrite, 0, 4);
        bytesToWrite[4] = '\0';

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

        bytesToWrite[position] = '\0';*/

        LinkedList<String> list = new LinkedList<>();

        list.addLast("Prima");
        list.addLast("Seconda");
        list.addLast("Terza");

        System.out.println(list.get(0));
        System.out.println(list.get(1));
        System.out.println(list.get(2));

    }
}
