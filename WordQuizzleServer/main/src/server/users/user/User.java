package server.users.user;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;

import commons.messages.Message;
import commons.messages.MessageType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import server.users.exceptions.WrongPasswordException;

public class User
{
    // User info
    private String username;
    private Password password;
    private Set<String> friends;
    private AtomicInteger score;

    // Arrears demands
    private Deque<Message> backlog;

    public User(String username, char[] password)
    {
        // User info
        this.username = username;
        this.password = new Password(password);
        this.friends = ConcurrentHashMap.newKeySet(32);
        this.score = new AtomicInteger(0);

        // Arrears demands
        this.backlog = new ConcurrentLinkedDeque<>();
    }

    public User(JSONObject serializedUser)
    {
        this.username = (String) serializedUser.get("Username");
        this.password = new Password((JSONObject) serializedUser.get("Password"));
        this.score = new AtomicInteger(((Long) serializedUser.get("Score")).intValue());
        this.friends = ConcurrentHashMap.newKeySet(32);
        this.backlog = new ConcurrentLinkedDeque<>();

        JSONArray serializedFriends = (JSONArray) serializedUser.get("Friends");
        for (String friend : (Iterable<String>) serializedFriends)
        {
            this.friends.add(friend);
        }

        JSONArray serializedBacklog = (JSONArray) serializedUser.get("Backlog");
        for (JSONObject message : (Iterable<JSONObject>) serializedBacklog)
        {
            this.backlog.add(new Message(message));
        }
    }

    public User(String username, char[] password, int score, Collection<String> friends, Collection<Message> backlog)
    {
        // User info
        this.username = username;
        this.password = new Password(password);
        this.friends = new HashSet<>(friends);
        this.score = new AtomicInteger(score);

        // Arrears demands
        this.backlog = new ConcurrentLinkedDeque<>(backlog);
    }

    public String getUsername()
    {
        return this.username;
    }

    public Set<String> getFriends()
    {
        return this.friends;
    }

    public Deque<Message> getBacklog()
    {
        return this.backlog;
    }

    public int getScore()
    {
        return this.score.get();
    }

    public int updateScore(int gain)
    {
        return this.score.updateAndGet((x) -> x + gain);
    }

    public void checkPassword(char[] password) throws WrongPasswordException
    {
        if (!this.password.checkPassword(password))
            throw new WrongPasswordException();
    }

    public JSONArray serializeFriends()
    {
        JSONArray friendsList = new JSONArray();

        friendsList.addAll(this.friends);

        return friendsList;
    }

    public JSONObject serialize()
    {
        JSONObject user = new JSONObject();
        JSONArray backLog = new JSONArray();
        JSONArray friends;

        user.put("Username", this.username);
        user.put("Password", this.password.serialize());
        user.put("Score", this.score);
        friends = serializeFriends();

        for (Message message : this.backlog)
        {
            if (message.getType() == MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION)
                backLog.add(message.serialize());
        }

        user.put("Friends", friends);
        user.put("Backlog", backLog);

        return user;
    }
}
