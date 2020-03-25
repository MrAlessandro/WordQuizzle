package server.users;

import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;
import server.settings.ServerConstants;
import server.users.user.User;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;

public class UsersManager implements commons.remote.Registrable
{
    private static final UsersManager USERS_MANAGER = new UsersManager();

    private static ConcurrentHashMap<String, User> usersArchive;

    public static UsersManager getUsersManager()
    {
        return USERS_MANAGER;
    }

    public static void setUp()
    {
        usersArchive = new ConcurrentHashMap<>(ServerConstants.USERS_ARCHIVE_INITIAL_SIZE);
    }

    @Override
    public void registerUser(String username, char[] password) throws VoidPasswordException, VoidUsernameException, UsernameAlreadyUsedException, RemoteException
    {
        if (username == null || username.equals(""))
            throw new VoidUsernameException("EMPTY USERNAME");
        if (password.length == 0)
            throw new VoidPasswordException("EMPTY PASSWORD");

        if ((usersArchive.putIfAbsent(username, new User(username, password))) != null)
            throw new UsernameAlreadyUsedException("USERNAME " + username + " IS ALREADY USED");
    }

    public static User getUser(String username)
    {
        return usersArchive.get(username);
    }

    public static void makeFriends(User user1, User user2)
    {
        user1.getFriends().add(user2.getUsername());
        user2.getFriends().add(user1.getUsername());
    }

    public static boolean areFriends(User user1, User user2)
    {
        return user1.getFriends().contains(user2.getUsername()) && user2.getFriends().contains(user1.getUsername());
    }

    /*TODO*/
    public static JSONArray getSerializedFriendsList(String username)
    {
        User user = getUser(username);
        return user.serializeFriends();
    }
}
