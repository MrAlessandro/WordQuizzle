import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

class UserNet
{
    private final static UserNet Net = new UserNet();
    private final static UserMap Map = new UserMap();

    private UserNet() {}

    protected static UserNet getNet()
    {
        return Net;
    }

    protected static boolean registerUser(String userName, String passwd)
    {
        boolean test;

        test = Map.insert(userName, new Password(passwd));

        return test;
    }

    protected static boolean addFriendship(String userName1, String userName2) throws InconsistentRelationshipException
    {
        boolean test;

        test = Map.addFriendship(userName1, userName2);

        return test;
    }

    protected static void backUpNet()
    {
        byte[] jsonBytes = Map.JSONserialize().toJSONString().getBytes();

        try
        {
            Files.write(Constants.UserNetBackUpPath, jsonBytes, StandardOpenOption.CREATE);
        }
        catch (IOException e)
        {
            System.err.println("Error writing the backup file");
            e.printStackTrace();
        }
    }

    protected static void restoreNet()
    {
        byte[] read = null;
        try
        {
             read = Files.readAllBytes(Constants.UserNetBackUpPath);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Warning. No back up file found.");
            return;
        } catch (IOException e)
        {
            System.err.println("Error reading server's users database.");
            e.printStackTrace();
        }

        String json = new String(read, Charset.defaultCharset());

        try
        {
            Map.JSONdeserialize(json);
        }
        catch (ParseException e)
        {
            System.err.println("Error parsing server's users database.");
            e.printStackTrace();
        }

    }

    protected static void printNet()
    {
        Map.print();
    }
}
