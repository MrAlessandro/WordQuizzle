import java.util.HashSet;
import java.util.LinkedList;

public class User
{
    final private String UserName;
    private char[] Password;
    private int Score;
    private LinkedList<String> Friends;

    public User(String userName, char[] password)
    {
        this.UserName = userName;
        this.Password = password;
        this.Friends = new LinkedList<>();
        this.Score = 0;
    }

    public String getUserName()
    {
        return this.UserName;
    }

    public boolean isFriendOf(String userName)
    {
        return this.Friends.contains(userName);
    }

    public boolean addFriend(String userName)
    {
        boolean retValue = true;

        for (String s : this.Friends)
        {
            if (s.equals(userName))
            {
                retValue = false;
                break;
            }
        }

        if (retValue)
            this.Friends.addFirst(userName);

        return retValue;
    }
}
