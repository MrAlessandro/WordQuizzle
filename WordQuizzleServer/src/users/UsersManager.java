package users;

import messages.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import users.exceptions.AlreadyExistingRelationshipException;
import users.exceptions.InconsistentRelationshipException;
import users.exceptions.UnknownUserException;
import users.user.User;
import util.Constants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UsersManager extends RemoteServer implements Registrable
{
    private static final UsersManager INSTANCE = new UsersManager();
    private static final ConcurrentHashMap<String, User> USERS_ARCHIVE = new ConcurrentHashMap<>(Constants.UserMapSize);

    private UsersManager()
    {}

    public static UsersManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public boolean registerUser(String username, char[] password) throws RemoteException
    {
        return (USERS_ARCHIVE.putIfAbsent(username, new User(username, password))) == null;
    }

    public static Collection<Message> grantAccess(String username, char[] password) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("Attempt to access to a not existing user");
        else if (taken.checkPassword(password))
            return taken.retrieveBackLog();
        else
            return null;
    }

    public static boolean makeFriends(String username1, String username2) throws AlreadyExistingRelationshipException, InconsistentRelationshipException
    {
        User taken1  = USERS_ARCHIVE.get(username1);
        User taken2  = USERS_ARCHIVE.get(username2);
        boolean check1;
        boolean check2;
        boolean retValue;

        taken1.lock();
        taken2.lock();

        check1 = taken1.addFriend(username2);
        if(check1)
        {
            check2 = taken2.addFriend(username1);
            if (!check2)
            {
                taken1.removeFriend(username2);
                taken2.removeFriend(username1);
                taken1.unlock();
                taken2.unlock();
                throw new InconsistentRelationshipException("Inconsistent relationship");
            }
        }
        else
        {
            check2 = taken2.isFriendOf(username1);
            if (check2)
            {
                taken2.removeFriend(username1);
                taken1.unlock();
                taken2.unlock();
                throw new InconsistentRelationshipException("Inconsistent relationship");
            }
            else
            {
                taken1.unlock();
                taken2.unlock();
                throw new AlreadyExistingRelationshipException("\"" + username1 + "\" and \"" + username2 + "\" are already friends");
            }
        }

        taken1.unlock();
        taken2.unlock();

        return true;
    }

    public static void backUp() throws IOException
    {
        byte[] jsonBytes;
        JSONArray SEusersArray = new JSONArray();
        Collection<User> collectedUsers = USERS_ARCHIVE.values();

        for (User user : collectedUsers)
        {
            SEusersArray.add(user.JSONserialize());
        }

        jsonBytes = SEusersArray.toJSONString().getBytes();

        Files.deleteIfExists(Constants.UserNetBackUpPath);
        Files.write(Constants.UserNetBackUpPath, jsonBytes, StandardOpenOption.CREATE_NEW);
    }

    public static void restore() throws IOException, ParseException
    {
        if (!Files.exists(Constants.UserNetBackUpPath))
            return;

        byte[] jsonBytes = Files.readAllBytes(Constants.UserNetBackUpPath);
        String jsonString = new String(jsonBytes);

        JSONParser parser = new JSONParser();
        JSONArray DEusersArray = (JSONArray) parser.parse(jsonString);
        Map<String, User> DEusersArchive = new HashMap<>();

        for (JSONObject currentUser : (Iterable<JSONObject>) DEusersArray)
        {
            User DEuser = User.JSONdeserialize(currentUser);
            DEusersArchive.put(DEuser.getUsername(), DEuser);
        }

        USERS_ARCHIVE.putAll(DEusersArchive);
    }

    public static void print()
    {
        Collection<User> collectedUsers = USERS_ARCHIVE.values();
        int counter = 0;

        for (User user : collectedUsers)
        {
            System.out.println("UsersNetwork.User N° " + counter++);
            System.out.println(user);
        }
    }
}
