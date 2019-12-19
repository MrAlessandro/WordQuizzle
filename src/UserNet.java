import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

public class UserNet
{
    private final static UserNet Net = new UserNet();
    private final static UserMap Map = new UserMap();

    private UserNet()
    {}

    protected boolean RegisterUser(String userName, char[] passwd)
    {
        boolean test;

        test = Map.put(userName, passwd);

        return test;
    }

    protected boolean AddFriendship(String userName1, String userName2) throws InconsistentRelationshipException
    {
        boolean test;

        test = Map.addFriendship(userName1, userName2);

        return test;
    }

}
