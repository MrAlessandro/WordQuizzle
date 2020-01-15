package UsersNetwork;

import Exceptions.*;
import Utility.AnsiColors;
import Utility.Constants;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.server.RemoteServer;

public class UserNet extends RemoteServer implements Registrable
{
    private final static UserNet Net = new UserNet();
    private final static UserMap Map = new UserMap();

    private UserNet() {}

    public static UserNet getNet()
    {
        return Net;
    }

    public boolean registerUser(String userName, char[] passwd)
    {
        System.out.print("Registering user \"" + userName + "\" at the service... ");

        try
        {
            Map.insert(userName, new Password(passwd));
        }
        catch (NameNotUniqueException e)
        {
            AnsiColors.printlnRed("FAILED");
            AnsiColors.printlnRed(e.getMessage());
            return false;
        }

        AnsiColors.printlnGreen("REGISTERED");
        return true;
    }

    public static boolean checkUserPassword(String userName, char[] password) throws UnknownUserException
    {
        boolean checked = Map.checkPassword(userName, password);

        return checked;
    }

    public static boolean addFriendship(String userName1, String userName2) throws InconsistentRelationshipException
    {
        System.out.print("Making \"" + userName1 + "\" and \"" + userName2 + "\" friends...");

        try
        {
            Map.addFriendship(userName1, userName2);
        }
        catch (UnknownFirstUserException | UnknownSecondUserException | AlreadyExistingRelationshipException e)
        {
            AnsiColors.printlnRed("FAILED");
            AnsiColors.printlnRed(e.getMessage());
            return false;
        }
        catch (InconsistentRelationshipException e)
        {
            AnsiColors.printlnRed("FAILED");
            e.printStackTrace();
            System.exit(1);
        }

        AnsiColors.printlnGreen("MADE");
        return true;
    }

    public static void backUpNet()
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

    public static void restoreNet()
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

    public static void printNet()
    {
        Map.print();
    }
}
