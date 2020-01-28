package users;

import jdk.nashorn.internal.ir.ReturnNode;
import messages.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import returns.ReturnValue;
import status.Status;
import users.exceptions.AlreadyExistingRelationshipException;
import users.exceptions.InconsistentRelationshipException;
import users.exceptions.UnknownUserException;
import users.user.User;
import util.Constants;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsersManager extends RemoteServer implements Registrable
{
    private static final UsersManager INSTANCE = new UsersManager();
    private static final ConcurrentHashMap<String, User> USERS_ARCHIVE = new ConcurrentHashMap<>(Constants.UserMapSize);
    public static final Set<SocketChannel> WRITABLE_CONNECTIONS = Collections.newSetFromMap(new ConcurrentHashMap<>());

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

    public static ReturnValue openSession(String username, char[] password, SocketChannel connection)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            return new ReturnValue(Status.USER_UNKNOWN, Status.USER_UNKNOWN.toString());
        else if (taken.checkPassword(password))
        {
            taken.connect(connection);
            taken.prependMessage(new Message(MessageType.OK));
            return new ReturnValue(Status.SUCCESS, Status.SUCCESS.toString());
        }
        else
            return new ReturnValue(Status.WRONG_PASSWORD, true);
    }

    public static ReturnValue closeSession(String username)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            return new ReturnValue(Status.USER_UNKNOWN, Status.USER_UNKNOWN.toString());

        taken.disconnect();

        return new ReturnValue(Status.SUCCESS, true);
    }

    public static ReturnValue makeFriends(String username1, String username2) throws AlreadyExistingRelationshipException, InconsistentRelationshipException, UnknownUserException
    {
        User taken1  = USERS_ARCHIVE.get(username1);
        User taken2  = USERS_ARCHIVE.get(username2);

        if (taken1 == null)
            return new ReturnValue(Status.USER1_UNKNOWN, "USER \"" +  username1 + "\" UNKNOWN");
        if (taken2 == null)
            return new ReturnValue(Status.USER1_UNKNOWN, "USER \"" +  username1 + "\" UNKNOWN");

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
                throw new RuntimeException("Inconsistent relationship");
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
                throw new RuntimeException("Inconsistent relationship");
            }
            else
            {
                taken1.unlock();
                taken2.unlock();
                return new ReturnValue(Status.FRIENDSHIP_ALREADY_EXISTS, "\"" + username1 + "\" AND \"" + username2 + "\" ARE FRIENDS ALREADY");
            }
        }

        taken1.unlock();
        taken2.unlock();

        return new ReturnValue(Status.SUCCESS, true);
    }

    public static ReturnValue sendMessage(String username, Message message) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            return new ReturnValue(Status.USER_UNKNOWN, Status.USER_UNKNOWN.toString());

        taken.appendMessage(message);

        return new ReturnValue(Status.SUCCESS, true);
    }

    public static ReturnValue sendResponse(String username, Message message) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            return new ReturnValue(Status.USER_UNKNOWN, Status.USER_UNKNOWN.toString());

        return new ReturnValue(Status.SUCCESS, true);
    }

    public static ReturnValue retrieveMessage(String username)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            return new ReturnValue(Status.USER_UNKNOWN, Status.USER_UNKNOWN.toString());

        return new ReturnValue(Status.SUCCESS, taken.getMessage());
    }

    public static void backUp()
    {
        byte[] jsonBytes;
        JSONArray SEusersArray = new JSONArray();
        Collection<User> collectedUsers = USERS_ARCHIVE.values();

        for (User user : collectedUsers)
        {
            SEusersArray.add(user.JSONserialize());
        }

        jsonBytes = SEusersArray.toJSONString().getBytes();

        try
        {
            Files.deleteIfExists(Constants.UserNetBackUpPath);
            Files.write(Constants.UserNetBackUpPath, jsonBytes, StandardOpenOption.CREATE_NEW);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Backing up users system");
        }
    }

    public static void restore()
    {
        if (!Files.exists(Constants.UserNetBackUpPath))
            return;

        byte[] jsonBytes;

        try
        {
            jsonBytes = Files.readAllBytes(Constants.UserNetBackUpPath);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Reading users system back up file");
        }

        String jsonString = new String(jsonBytes);

        JSONParser parser = new JSONParser();
        Map<String, User> DEusersArchive = new HashMap<>();
        JSONArray DEusersArray;

        try
        {
            DEusersArray = (JSONArray) parser.parse(jsonString);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Parsing users system back up file");
        }

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
