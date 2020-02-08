package server.users.user;

import messages.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;


public class User
{
    private String username;
    private Password password;
    private SocketAddress address;
    private HashSet<String> friends;
    private Set<String> friendshipRequest;
    private ConcurrentLinkedDeque<Message> requestsBackLog;
    private Message responseBackLog;
    private int score;

    public User(String username, char[] password)
    {
        this.username = username;
        this.password = new Password(password);
        this.friends = new HashSet<>(20);
        this.friendshipRequest = ConcurrentHashMap.newKeySet(10);
        this.requestsBackLog = new ConcurrentLinkedDeque<>();
        this.address = null;
        this.score = 0;
    }

    public User(String username, Password password, int score, HashSet<String> friends, ConcurrentLinkedDeque<Message> backLog, Set<String> friendshipRequest)
    {
        this.username = username;
        this.password = password;
        this.friends = friends;
        this.friendshipRequest = friendshipRequest;
        this.requestsBackLog = backLog;
        this.address = null;
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
        return this.friends.contains(userName);
    }

    public boolean addFriend(String userName)
    {
        return this.friends.add(userName);
    }

    public boolean hasPendingFriendshipRequest(String friend)
    {
        return this.friendshipRequest.contains(friend);
    }

    public boolean removePendingFriendshipRequest(String friend)
    {
        return this.friendshipRequest.remove(friend);
    }

    public boolean addPendingFriendshipRequest(String friend)
    {
        return this.friendshipRequest.add(friend);
    }

    public boolean appendRequest(Message message)
    {
        this.requestsBackLog.addLast(message);
        return true;
    }

    public boolean prependRequest(Message message)
    {
        this.requestsBackLog.addFirst(message);
        return true;
    }

    public boolean storeResponse(Message message)
    {
        this.responseBackLog = message;
        return true;
    }

    public Message getMessage()
    {
        Message message;

        if (this.responseBackLog == null)
            message = this.requestsBackLog.pollFirst();
        else
        {
            message = this.responseBackLog;
            this.responseBackLog = null;
        }

        return message;
    }

    public boolean hasPendingMessages()
    {
        return (this.responseBackLog != null) || !(this.requestsBackLog.isEmpty());
    }

    public int getBackLogAmount()
    {
        return this.requestsBackLog.size();
    }

    public boolean logIn(SocketAddress address)
    {
        this.address = address;
        return true;
    }

    public boolean logOut()
    {
        if (this.address == null)
            return false;

        this.address = null;

        return true;
    }

    public boolean isLogged()
    {
        return this.address != null;
    }

    public SocketAddress getAddress()
    {
        return this.address;
    }

    public String JSONserializeFriendsList()
    {
        JSONArray friendsList = new JSONArray();

        friendsList.addAll(this.friends);

        return friendsList.toJSONString();
    }

    public JSONObject JSONserialize()
    {
        JSONObject retValue = new JSONObject();
        JSONArray friendList = new JSONArray();
        JSONArray requests = new JSONArray();
        JSONArray backLogs = new JSONArray();

        retValue.put("UserName", this.username);
        retValue.put("Password", this.password.JSONserialize());
        retValue.put("Score", this.score);

        friendList.addAll(this.friends);

        requests.addAll(this.friendshipRequest);

        for (Message mex : this.requestsBackLog)
        {
            backLogs.add(mex.JSONserialize());
        }

        retValue.put("Friends", friendList);

        retValue.put("FriendshipRequests", requests);

        retValue.put("BackLogsRequests", backLogs);

        return retValue;
    }

    public static User JSONdeserialize(JSONObject serializedUser)
    {
        String currentUsername = (String) serializedUser.get("UserName");
        int currentScore = ((Long) serializedUser.get("Score")).intValue();
        JSONObject currentPassword = (JSONObject) serializedUser.get("Password");
        JSONArray currentFriendList = (JSONArray) serializedUser.get("Friends");
        JSONArray currentRequests = (JSONArray) serializedUser.get("FriendshipRequests");
        JSONArray currentBackLog = (JSONArray) serializedUser.get("BackLogsRequests");

        Password DEpassword = server.users.user.Password.JSONdeserialize(currentPassword);

        HashSet<String> DEfriendList = new HashSet<>(20);
        for (String friend : (Iterable<String>) currentFriendList)
        {
            DEfriendList.add(friend);
        }

        Set<String> DErequests = ConcurrentHashMap.newKeySet(10);
        for (String request : (Iterable<String>) currentRequests)
        {
            DErequests.add(request);
        }

        ConcurrentLinkedDeque<Message> DEbackLogMessages = new ConcurrentLinkedDeque<>();
        for (JSONObject mex : (Iterable<JSONObject>) currentBackLog)
        {
            DEbackLogMessages.addLast(Message.JSONdeserialize(mex));
        }

        return new User(currentUsername, DEpassword, currentScore, DEfriendList, DEbackLogMessages, DErequests);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("    Username: ").append(this.username).append(System.lineSeparator());
        builder.append("    Password: ").append(this.password).append(System.lineSeparator());
        builder.append("       Score: ").append(this.score).append(System.lineSeparator());

        if (this.friends.size() > 0)
        {
            boolean first = true;
            builder.append("      Fiends: ");
            for (String friend : this.friends)
            {
                if (first) {
                    builder.append(friend).append(System.lineSeparator());
                    first = false;
                } else
                    builder.append("              ").append(friend).append(System.lineSeparator());
            }
        }

        if (this.requestsBackLog.size() > 0)
        {
            builder.append("      Backlog messages:");
            for (Message message : this.requestsBackLog)
            {
                builder.append("                        ").append(message).append(System.lineSeparator());
            }
        }

        return builder.toString();
    }
}
