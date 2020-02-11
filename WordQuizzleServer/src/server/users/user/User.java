package server.users.user;

import messages.Message;
import messages.MessageType;
import messages.exceptions.UnexpectedMessageException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.users.exceptions.OpponentAlreadyEngagedException;
import server.users.exceptions.OpponentOfflineException;
import server.users.exceptions.RequestAlreadySentException;

import java.net.SocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;


public class User
{
    // User info
    private String username;
    private Password password;
    private HashSet<String> friends;
    private int score;

    // LogIn address flag
    private ReentrantLock logLock;
    private SocketAddress address;

    // Pending requests
    private Set<String> friendshipRequests;
    private AtomicReference<String> challengeRequest;

    // Backlogs
    private ConcurrentLinkedDeque<Message> requestsBackLog;
    private Message responseBackLog;

    public User(String username, char[] password)
    {
        this.username = username;
        this.password = new Password(password);
        this.friends = new HashSet<>(20);
        this.friendshipRequests = ConcurrentHashMap.newKeySet(10);
        this.challengeRequest = new AtomicReference<>(null);
        this.requestsBackLog = new ConcurrentLinkedDeque<>();
        this.logLock = new ReentrantLock();
        this.address = null;
        this.score = 0;
    }

    public User(String username, Password password, int score, HashSet<String> friends, ConcurrentLinkedDeque<Message> backLog, Set<String> friendshipRequests)
    {
        this.username = username;
        this.password = password;
        this.friends = friends;
        this.friendshipRequests = friendshipRequests;
        this.challengeRequest = new AtomicReference<>(null);
        this.requestsBackLog = backLog;
        this.logLock = new ReentrantLock();
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

    public void addPendingFriendshipRequest(String applicant) throws RequestAlreadySentException
    {
        boolean check = this.friendshipRequests.add(applicant);
        if (!check)
            throw new RequestAlreadySentException("FRIENDSHIP REQUEST FROM \"" + applicant + "\" TO \"" + username + "\" HAS ALREADY BEEN SENT");
    }

    public void removePendingFriendshipRequest(String applicant) throws UnexpectedMessageException
    {
        boolean check = this.friendshipRequests.remove(applicant);
        if (!check)
            throw new UnexpectedMessageException("RESPONSE TO THE FRIENDSHIP BETWEEN \"" + username + "\" AND \"" + applicant + "\" DO NOT CORRESPONDS TO ANY REQUEST");
    }

    public void setPendingChallengeRequest(String from) throws OpponentOfflineException, OpponentAlreadyEngagedException
    {
        this.logLock.lock();

        if (this.address != null)
        {
            boolean check = this.challengeRequest.compareAndSet(null, from);
            if (!check)
            {
                this.logLock.unlock();
                throw new OpponentAlreadyEngagedException("\"" + username + "\" IS ALREADY ENGAGED IN OTHER CHALLENGE");
            }
        }
        else
        {
            this.logLock.unlock();
            throw new OpponentOfflineException("USER \"" + username + "\" IS OFFLINE");
        }

        this.logLock.unlock();
    }

    public boolean removePendingChallengeRequest(String opponent) throws UnexpectedMessageException
    {
        this.logLock.lock();

        boolean check = this.challengeRequest.compareAndSet(opponent, null);
        if (!check)
            throw new UnexpectedMessageException("RESPONSE TO THE CHALLENGE BETWEEN \"" + username + "\" AND \"" + opponent + "\" DO NOT CORRESPONDS TO ANY REQUEST");

        this.logLock.unlock();

        return true;
    }

    public boolean storeMessage(Message message)
    {
        this.requestsBackLog.addLast(message);
        return true;
    }

    public boolean restoreMessage(Message message)
    {
        this.requestsBackLog.addFirst(message);
        return true;
    }

    public boolean storeResponse(Message message)
    {
        this.responseBackLog = message;
        return true;
    }

    public Message retrieveMessage()
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

    public boolean logIn(SocketAddress address)
    {
        this.logLock.lock();
        this.address = address;
        this.logLock.unlock();
        return true;
    }

    public boolean logOut()
    {
        this.logLock.lock();

        this.challengeRequest.set(null);
        this.address = null;

        this.logLock.unlock();
        return true;
    }

    public SocketAddress getAddress()
    {
        SocketAddress address;

        this.logLock.lock();
        address =  this.address;
        this.logLock.unlock();

        return address;
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

        requests.addAll(this.friendshipRequests);

        for (Message mex : this.requestsBackLog)
        {
            if (mex.getType() != MessageType.REQUEST_FOR_CHALLENGE)
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
