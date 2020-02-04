package server.main;

import messages.exceptions.UnexpectedMessageException;
import org.json.simple.parser.ParseException;
import server.users.exceptions.AlreadyExistingRelationshipException;
import server.users.exceptions.UnknownUserException;
import server.users.UsersManager;

import java.io.IOException;

public class PasswordCheckingTest
{
    public static void main(String[] args) throws UnknownUserException, IOException, ParseException
    {
        UsersManager Net = UsersManager.getInstance();

        /*Net.registerUser("Alfredo", new char[]{'1','2','3','4'});
        Net.registerUser("Andrea", new char[]{'3','3','3','3'});
        Net.registerUser("Giacomo", new char[]{'1','2','3','4'});
        Net.registerUser("Alessandro", new char[]{'1','2','3','4'});
        Net.registerUser("Paola", new char[]{'1','2','3','4'});
        Net.registerUser("Claudia", new char[]{'1','2','3','4'});
        Net.registerUser("Stefano", new char[]{'1','2','3','4'});
        Net.registerUser("Beatrice", new char[]{'1','2','3','4'});
        Net.registerUser("Chiara", new char[]{'1','2','3','4'});*/

        /*try
        {
            UsersManager.makeFriends("Andrea", "Giacomo");
            UsersManager.makeFriends("Andrea", "Alessandro");
            UsersManager.makeFriends("Andrea", "Paola");
            UsersManager.makeFriends("Andrea", "Claudia");
            UsersManager.makeFriends("Giacomo", "Stefano");
            UsersManager.makeFriends("Giacomo", "Chiara");
            UsersManager.makeFriends("Giacomo", "Beatrice");
            UsersManager.makeFriends("Giacomo", "Alessandro");
            UsersManager.makeFriends("Alessandro", "Claudia");
            UsersManager.makeFriends("Alessandro", "Beatrice");
            UsersManager.makeFriends("Alessandro", "Paola");
            UsersManager.makeFriends("Chiara", "Claudia");
            UsersManager.makeFriends("Chiara", "Beatrice");
        }
        catch (UnexpectedMessageException e)
        {
            e.printStackTrace();
        }*/

        /*
        UsersManager.restore();

        Collection<Message> backLog = UsersManager.grantAccess("Andrea", new char[]{'3','3','3','3'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.grantAccess("Alessandro", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.grantAccess("Chiara", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.grantAccess("Giacomo", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.grantAccess("Paola", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        */


        UsersManager.backUp();
        UsersManager.print();


    }

}
