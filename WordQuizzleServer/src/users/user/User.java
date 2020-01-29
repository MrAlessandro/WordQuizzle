package users.user;

import messages.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import users.UsersManager;

import java.nio.channels.SelectionKey;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class User
{
    private String username;
    private Password password;

    private ReentrantLock userLock;
    private SelectionKey connection;
    private LinkedList<Message> backLogMessages;
    private HashSet<String> friends;
    private int score;

    public User(String username, char[] password)
    {
        this.username = username;
        this.password = new Password(password);
        this.friends = new HashSet<>(20);
        this.backLogMessages = new LinkedList<>();
        this.userLock = new ReentrantLock();
        this.connection = null;
        this.score = 0;
    }

    public User(String username, Password password, int score, HashSet<String> friends, LinkedList<Message> backLog)
    {
        this.username = username;
        this.password = password;
        this.friends = friends;
        this.backLogMessages = backLog;
        this.userLock = new ReentrantLock();
        this.connection = null;
        this.score = score;
    }

    public String getUsername()
    {
        return this.username;
    }

    public boolean checkPassword(char[] password)
    {
        return this.password.checkPassword(password);
    }

    public boolean isFriendOf(String userName)
    {
        boolean check;

        this.userLock.lock();
        check = this.friends.contains(userName);
        this.userLock.unlock();

        return check;
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

    public SelectionKey appendMessage(Message message)
    {
        SelectionKey connection;

        this.userLock.lock();
        this.backLogMessages.add(message);
        connection = this.connection;
        this.userLock.unlock();

        return connection;
    }

    public SelectionKey prependMessage(Message message)
    {
        SelectionKey connection;

        this.userLock.lock();
        this.backLogMessages.addFirst(message);
        connection = this.connection;
        this.userLock.unlock();

        return connection;
    }

    public Message getMessage()
    {
        Message message;

        this.userLock.lock();
        message = this.backLogMessages.pollFirst();
        this.userLock.unlock();

        return message;
    }

    public SelectionKey connect(SelectionKey connection)
    {
        SelectionKey returned;

        this.userLock.lock();
        if (this.connection != null)
            returned =  null;
        else
            returned = (this.connection = connection);
        this.userLock.unlock();

        return returned;
    }

    public SelectionKey disconnect()
    {
        SelectionKey connection;

        this.userLock.lock();
        connection = this.connection;
        this.connection = null;
        this.userLock.unlock();

        return connection;
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
