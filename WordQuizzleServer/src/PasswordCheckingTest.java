import exceptions.InconsistentRelationshipException;
import exceptions.UnknownUserException;
import messages.Message;
import sessions.SessionsManager;
import users.UsersManager;

import java.util.LinkedList;

public class PasswordCheckingTest
{
    public static void main(String[] args) throws UnknownUserException
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
        Net.registerUser("Chiara", new char[]{'1','2','3','4'});

        try
        {
            Net.addFriendship("Andrea", "Giacomo");
            Net.addFriendship("Andrea", "Alessandro");
            Net.addFriendship("Andrea", "Paola");
            Net.addFriendship("Andrea", "Claudia");
            Net.addFriendship("Giacomo", "Stefano");
            Net.addFriendship("Giacomo", "Chiara");
            Net.addFriendship("Giacomo", "Beatrice");
            Net.addFriendship("Giacomo", "Alessandro");
            Net.addFriendship("Alessandro", "Claudia");
            Net.addFriendship("Alessandro", "Beatrice");
            Net.addFriendship("Alessandro", "Paola");
            Net.addFriendship("Chiara", "Claudia");
            Net.addFriendship("Chiara", "Beatrice");
        }
        catch (InconsistentRelationshipException e)
        {
            e.printStackTrace();
        }*/

        Net.restoreNet();

        LinkedList<Message> backLog = UsersManager.validatePasswordRetrieveBackLog("Andrea", new char[]{'3','3','3','3'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.validatePasswordRetrieveBackLog("Alessandro", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.validatePasswordRetrieveBackLog("Chiara", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.validatePasswordRetrieveBackLog("Giacomo", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");
        backLog = UsersManager.validatePasswordRetrieveBackLog("Paola", new char[]{'1','2','3','4'});
        if (backLog != null)
            System.out.println("Checked");
        else
            System.out.println("Wrong");



        Net.printNet();

        Net.backUpNet();
    }

}
