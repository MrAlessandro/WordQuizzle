package server.users.user;

import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import commons.messages.Message;
import org.json.simple.JSONArray;
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
}
