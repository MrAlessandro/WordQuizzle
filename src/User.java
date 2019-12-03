import java.util.HashSet;

public class User
{
    private String UserName;
    private char[] Password;
    private int Score;
    private HashSet<String> Friends;

    public User(String userName, char[] password)
    {
        this.UserName = userName;
        this.Password = password;
        this.Friends = new HashSet<String>();
        this.Score = 0;
    }
}
