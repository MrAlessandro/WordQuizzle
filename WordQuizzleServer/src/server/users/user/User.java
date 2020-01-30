package server.users.user;

import messages.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;


public class User
{
    private String username;
    private Password password;
    private HashSet<String> friends;
    private HashSet<String> waitingOutcomingFriendshipRequests;
    private HashSet<String> waitingIncomingFriendshipRequests;

    private ReentrantLock userLock;
    private boolean logged;
    private LinkedList<Message> backLogMessages;
    private int score;

    public User(String username, char[] password)
    {
        this.username = username;
        this.password = new Password(password);
        this.friends = new HashSet<>(20);
        this.backLogMessages = new LinkedList<>();
        this.userLock = new ReentrantLock();
        this.logged = false;
        this.waitingOutcomingFriendshipRequests = new HashSet<>(10);
        this.waitingIncomingFriendshipRequests = new HashSet<>(10);
        this.score = 0;
    }

    public User(String username, Password password, int score, HashSet<String> friends, LinkedList<Message> backLog, HashSet<String> waitingIncomingFriendshipRequests, HashSet<String> waitingOutcomingFriendshipRequests)
    {
        this.username = username;
        this.password = password;
        this.friends = friends;
        this.backLogMessages = backLog;
        this.userLock = new ReentrantLock();
        this.logged = false;
        this.waitingOutcomingFriendshipRequests = waitingOutcomingFriendshipRequests;
        this.waitingIncomingFriendshipRequests = waitingIncomingFriendshipRequests;
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

    public boolean addWaitingOutcomeFriendshipRequest(String wantedFriend)
    {
        return this.waitingOutcomingFriendshipRequests.add(wantedFriend);
    }

    public boolean removeWaitingOutcomeFriendshipRequest(String wantedFriend)
    {
        return this.waitingOutcomingFriendshipRequests.add(wantedFriend);
    }

    public boolean addWaitingIncomeFriendshipRequest(String applicant)
    {
        return this.waitingIncomingFriendshipRequests.add(applicant);
    }

    public boolean removeWaitingIncomeFriendshipRequest(String applicant)
    {
        return this.waitingIncomingFriendshipRequests.add(applicant);
    }

    public boolean removeFriend(String username)
    {
        return this.friends.remove(username);
    }

    public boolean appendMessage(Message message)
    {
        boolean logged;

        this.userLock.lock();
        this.backLogMessages.add(message);
        logged = this.logged;
        this.userLock.unlock();

        return logged;
    }

    public boolean prependMessage(Message message)
    {
        boolean logged;

        this.userLock.lock();
        this.backLogMessages.addFirst(message);
        logged = this.logged;
        this.userLock.unlock();

        return logged;
    }

    public Message getMessage()
    {
        Message message;

        this.userLock.lock();
        message = this.backLogMessages.pollFirst();
        this.userLock.unlock();

        return message;
    }

    public boolean logIn()
    {
        boolean returned = false;

        this.userLock.lock();
        if (!this.logged)
            returned  = (this.logged = true);
        this.userLock.unlock();

        return returned;
    }

    public boolean logOut()
    {
        boolean returned = false;

        this.userLock.lock();
        if (this.logged)
        {
            returned = true;
            this.logged = false;
        }
        this.userLock.unlock();

        return returned;
    }

    public JSONObject JSONserialize()
    {
        JSONObject retValue = new JSONObject();
        JSONArray friendList = new JSONArray();
        JSONArray outcomeFriendships = new JSONArray();
        JSONArray incomeFriendships = new JSONArray();
        JSONArray backLogs = new JSONArray();

        retValue.put("UserName", this.username);
        retValue.put("Password", this.password.JSONserialize());
        retValue.put("Score", this.score);

        friendList.addAll(this.friends);

        outcomeFriendships.addAll(this.waitingOutcomingFriendshipRequests);

        incomeFriendships.addAll(this.waitingIncomingFriendshipRequests);

        for (Message mex : this.backLogMessages)
        {
            backLogs.add(mex.JSONserialize());
        }

        retValue.put("Friends", friendList);

        retValue.put("OutcomeFriendships", outcomeFriendships);

        retValue.put("IncomeFriendships", incomeFriendships);

        retValue.put("BackLogsMessages", backLogs);

        return retValue;
    }

    public static User JSONdeserialize(JSONObject serializedUser)
    {
        String currentUsername = (String) serializedUser.get("UserName");
        int currentScore = ((Long) serializedUser.get("Score")).intValue();
        JSONObject currentPassword = (JSONObject) serializedUser.get("Password");
        JSONArray currentFriendList = (JSONArray) serializedUser.get("Friends");
        JSONArray currentOutcomeFriendships = (JSONArray) serializedUser.get("OutcomeFriendships");
        JSONArray currentIncomeFriendships = (JSONArray) serializedUser.get("IncomeFriendships");
        JSONArray currentBackLog = (JSONArray) serializedUser.get("BackLogsMessages");

        Password DEpassword = server.users.user.Password.JSONdeserialize(currentPassword);

        HashSet<String> DEfriendList = new HashSet<>(20);
        for (String friend : (Iterable<String>) currentFriendList)
        {
            DEfriendList.add(friend);
        }

        HashSet<String> DEoutcomeFriendships = new HashSet<>();
        for (String unFriend: (Iterable<String>) currentOutcomeFriendships)
        {
            DEoutcomeFriendships.add(unFriend) ;
        }

        HashSet<String> DEincomeFriendships = new HashSet<>();
        for (String unFriend: (Iterable<String>) currentIncomeFriendships)
        {
            DEincomeFriendships.add(unFriend);
        }


        LinkedList<Message> DEbackLogMessages = new LinkedList<>();
        for (JSONObject mex : (Iterable<JSONObject>) currentBackLog)
        {
            DEbackLogMessages.addLast(Message.JSONdeserialize(mex));
        }

        return new User(currentUsername, DEpassword, currentScore, DEfriendList, DEbackLogMessages, DEincomeFriendships, DEoutcomeFriendships);
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
