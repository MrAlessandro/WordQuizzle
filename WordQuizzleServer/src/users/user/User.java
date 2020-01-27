package users.user;

import messages.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class User
{
    private String username;
    private Password password;
    private int score;
    private HashSet<String> friends;
    private LinkedList<Message> backLogMessages;
    private ReentrantLock userLock;

    public User(String username, char[] password)
    {
        this.username = username;
        this.password = new Password(password);
        this.score = 0;
        this.friends = new HashSet<>(20);
        this.backLogMessages = new LinkedList<>();
        this.userLock = new ReentrantLock();
    }

    public User(String username, Password password, int score, HashSet<String> friends, LinkedList<Message> backLog)
    {
        this.username = username;
        this.password = password;
        this.friends = friends;
        this.backLogMessages = backLog;
        this.score = score;
        this.userLock = new ReentrantLock();
    }

    public String getUsername()
    {
        return this.username;
    }

    public String getPassword()
    {
        return this.password.toString();
    }

    public boolean checkPassword(char[] password)
    {
        return this.password.checkPassword(password);
    }

    protected String[] getFriendsList()
    {
        String[] friendsList;

        this.userLock.lock();
        if (this.friends.isEmpty())
            friendsList = null;
        else
        {
            friendsList = new String[this.friends.size()];
            int i = 0;
            for (String friend : this.friends)
            {
                friendsList[i++] = friend;
            }
        }
        this.userLock.unlock();

        return friendsList;
    }

    public boolean isFriendOf(String userName)
    {
        boolean check;

        this.userLock.lock();
        check = this.friends.contains(userName);
        this.userLock.unlock();

        return check;
    }

    public Collection<Message> retrieveBackLog()
    {
        Message current;
        ArrayList<Message> backLog = new ArrayList<>();

        this.userLock.lock();
        if (this.backLogMessages.size() != 0)
        {
            backLog = new ArrayList<>(this.backLogMessages.size());
            while ((current = this.backLogMessages.poll()) != null)
            {
                backLog.add(current);
            }
        }
        this.userLock.unlock();

        return backLog;
    }

    public boolean addFriend(String userName)
    {
        boolean retValue;

        this.userLock.lock();
        retValue = this.friends.add(userName);
        this.userLock.unlock();

        return retValue;
    }

    public boolean removeFriend(String username)
    {
        boolean retValue;

        this.userLock.lock();
        retValue = this.friends.remove(username);
        this.userLock.unlock();

        return retValue;
    }

    public JSONObject JSONserialize()
    {
        JSONObject retValue = new JSONObject();
        JSONArray friendList = new JSONArray();
        JSONArray backLogs = new JSONArray();

        retValue.put("UserName", this.username);
        retValue.put("Password", this.password.JSONserialize());
        retValue.put("Score", this.score);

        friendList.addAll(this.friends);

        for (Message mex : this.backLogMessages)
        {
            backLogs.add(mex.JSONserialize());
        }

        retValue.put("Friends", friendList);

        retValue.put("BackLogsMessages", backLogs);

        return retValue;
    }

    public static User JSONdeserialize(JSONObject serializedUser)
    {
        String currentUsername = (String) serializedUser.get("UserName");
        int currentScore = ((Long) serializedUser.get("Score")).intValue();
        JSONObject currentPassword = (JSONObject) serializedUser.get("Password");
        JSONArray currentFriendList = (JSONArray) serializedUser.get("Friends");
        JSONArray currentBackLog = (JSONArray) serializedUser.get("BackLogsMessages");

        Password DEpassword = users.user.Password.JSONdeserialize(currentPassword);

        HashSet<String> DEfriendList = new HashSet<>(20);
        for (String friend : (Iterable<String>) currentFriendList)
        {
            DEfriendList.add(friend);
        }

        LinkedList<Message> DEbackLogMessages = new LinkedList<>();
        for (JSONObject mex : (Iterable<JSONObject>) currentBackLog)
        {
            DEbackLogMessages.addLast(Message.JSONdeserialize(mex));
        }

        return new User(currentUsername, DEpassword, currentScore, DEfriendList, DEbackLogMessages);
    }

    public void lock()
    {
        this.userLock.lock();
    }

    public void unlock()
    {
        this.userLock.unlock();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        String[] friends;
        Message[] backLog;

        builder.append("    Username: ").append(this.username).append(System.lineSeparator());
        builder.append("    Password: ").append(this.password).append(System.lineSeparator());
        builder.append("       Score: ").append(this.score).append(System.lineSeparator());

        this.userLock.lock();

        if (this.friends != null && this.friends.size() > 0)
        {
            boolean first = true;
            builder.append("      Fiends: ");
            for (String friend : this.friends)
            {
                if (first)
                {
                    builder.append(friend).append(System.lineSeparator());
                    first = false;
                }
                else
                    builder.append("              ").append(friend).append(System.lineSeparator());
            }
        }

        if (this.backLogMessages != null && this.backLogMessages.size() > 0)
        {
            builder.append("      Backlog messages:");
            for (Message message : this.backLogMessages)
            {
                builder.append("                        ").append(message).append(System.lineSeparator());
            }
        }
        this.userLock.unlock();

        return builder.toString();
    }
}
