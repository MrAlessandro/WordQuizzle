package server.users;

import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import server.settings.Settings;
import server.users.user.User;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;

public class UsersManager implements commons.remote.Registrable
{
    private ConcurrentHashMap<String, User> usersArchive;

    public UsersManager()
    {
        this.usersArchive = new ConcurrentHashMap<>(Settings.USERS_ARCHIVE_INITIAL_SIZE);
    }

    public UsersManager(JSONArray serializedUsersArchive)
    {
        this.usersArchive = new ConcurrentHashMap<>(Settings.USERS_ARCHIVE_INITIAL_SIZE);

        for (JSONObject serializedUser : (Iterable<JSONObject>) serializedUsersArchive)
        {
            // Deserialize user and add to users archive
            User user = new User(serializedUser);
            this.usersArchive.put(user.getUsername(), user);
        }
    }

    @Override
    public void registerUser(String username, char[] password) throws VoidPasswordException, VoidUsernameException, UsernameAlreadyUsedException, RemoteException
    {
        if (username == null || username.equals(""))
            throw new VoidUsernameException("EMPTY USERNAME");
        if (password.length == 0)
            throw new VoidPasswordException("EMPTY PASSWORD");

        if ((this.usersArchive.putIfAbsent(username, new User(username, password))) != null)
            throw new UsernameAlreadyUsedException("USERNAME " + username + " IS ALREADY USED");
    }

    public User getUser(String username)
    {
        return this.usersArchive.get(username);
    }

    public void makeFriends(User user1, User user2)
    {
        user1.getFriends().add(user2.getUsername());
        user2.getFriends().add(user1.getUsername());
    }

    public boolean areFriends(User user1, User user2)
    {
        return user1.getFriends().contains(user2.getUsername()) && user2.getFriends().contains(user1.getUsername());
    }

    public JSONArray getSerializedFriendsList(String username)
    {
        User user = getUser(username);
        return user.serializeFriends();
    }

    public JSONArray getSerializedFriendsListWithScores(String username)
    {
        User user = getUser(username);
        Set<String> friends = user.getFriends();

        JSONArray serializeList = new JSONArray();

        for (String friend : friends)
        {
            JSONObject friendEntry = new JSONObject();
            User friendUser = getUser(friend);
            friendEntry.put("Username", friendUser.getUsername());
            friendEntry.put("Score", friendUser.getScore());
            serializeList.add(friendEntry);
        }

        return serializeList;
    }

    public int updateUserScore(String username, int gain)
    {
        User user = getUser(username);
        return user.updateScore(gain);
    }

    public int getUsersScore(String username)
    {
        User user = getUser(username);
        return user.getScore();
    }

    public JSONArray serialize()
    {
        JSONArray users = new JSONArray();
        Collection<User> collectedUsers = this.usersArchive.values();

        for (User user : collectedUsers)
        {
            users.add(user.serialize());
        }

        return users;
    }
}
