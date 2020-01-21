package users;

import messages.Message;
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
    private LinkedList<Message> BackLogMessages;

    public User(String userName, Password password)
    {
        this.UserName = userName;
        this.Password = password;
        this.Score = 0;
        this.Friends = new LinkedList<>();
        this.BackLogMessages = new LinkedList<>();
    }

    public User(String userName, Password password, int score, LinkedList<String> friends, LinkedList<Message> backLog)
    {
        this.UserName = userName;
        this.Password = password;
        this.Friends = friends;
        this.BackLogMessages = backLog;
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

    protected Iterator<Message> getBackLogMessageIterator()
    {
        return this.BackLogMessages.iterator();
    }

    protected boolean isFriendOf(String userName)
    {
        return this.Friends.contains(userName);
    }

    protected boolean checkPassword(char[] password)
    {
        return this.Password.checkPassword(password);
    }

    protected LinkedList<Message> retrieveBackLog()
    {
        Message current;
        LinkedList<Message> returnList = new LinkedList<>();

        while ((current = this.BackLogMessages.poll()) != null)
        {
            returnList.addLast(current);
        }

        return returnList;
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

    protected JSONObject JSONserialize()
    {
        JSONObject retValue = new JSONObject();
        JSONArray friendList = new JSONArray();
        JSONArray backLogs = new JSONArray();

        retValue.put("UserName", this.UserName);
        retValue.put("Password", this.Password.JSONserialize());
        retValue.put("Score", this.Score);

        for (String friend : this.Friends)
        {
            friendList.add(friend);
        }

        for (Message mex : this.BackLogMessages)
        {
            backLogs.add(mex.JSONserialize());
        }

        retValue.put("Friends", friendList);

        retValue.put("BackLogsMessages", backLogs);

        return retValue;
    }

    protected static User JSONdeserialize(JSONObject serializedUser)
    {
        String currentUsername = (String) serializedUser.get("UserName");
        int currentScore = ((Long) serializedUser.get("Score")).intValue();
        JSONObject currentPassword = (JSONObject) serializedUser.get("Password");
        JSONArray currentFriendList = (JSONArray) serializedUser.get("Friends");
        JSONArray currentBackLog = (JSONArray) serializedUser.get("BackLogsMessages");

        Password DEpassword = users.Password.JSONdeserialize(currentPassword);

        LinkedList<String> DEfriendList = new LinkedList<>();
        for (String friend : (Iterable<String>) currentFriendList)
        {
            DEfriendList.addLast(friend);
        }

        LinkedList<Message> DEbackLogMessages = new LinkedList<>();
        for (JSONObject mex : (Iterable<JSONObject>) currentBackLog)
        {
            DEbackLogMessages.addLast(Message.JSONdeserialize(mex));
        }

        return new User(currentUsername, DEpassword, currentScore, DEfriendList, DEbackLogMessages);
    }
}
