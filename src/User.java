import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;


class User
{
    private String UserName;
    private Password Password;
    private int Score;
    private LinkedList<String> Friends;

    public User(String userName, Password password)
    {
        this.UserName = userName;
        this.Password = password;
        this.Friends = new LinkedList<>();
        this.Score = 0;
    }

    public User(String userName, Password password, int score, LinkedList<String> friends)
    {
        this.UserName = userName;
        this.Password = password;
        this.Friends = friends;
        this.Score = score;
    }

    protected String getUserName()
    {
        return this.UserName;
    }

    protected String getPassword()
    {
        return this.Password.getEncodedPassword();
    }

    protected Iterator<String> getFriendListIterator()
    {
        return this.Friends.iterator();
    }

    protected boolean isFriendOf(String userName)
    {
        return this.Friends.contains(userName);
    }


    protected boolean addFriend(String userName)
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

    protected JSONObject JSONserilize()
    {
        JSONObject retValue = new JSONObject();
        JSONArray friendList = new JSONArray();

        retValue.put("UserName", this.UserName);
        retValue.put("Password", this.Password.JSONserialize());
        retValue.put("Score", this.Score);

        for (String friend : this.Friends)
        {
            friendList.add(friend);
        }

        retValue.put("Friends", friendList);

        return retValue;
    }
}
