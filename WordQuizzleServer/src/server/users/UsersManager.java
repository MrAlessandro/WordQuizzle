package server.users;

import messages.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import remote.Registrable;
import server.users.exceptions.AlreadyExistingRelationshipException;
import server.users.exceptions.UnknownUserException;
import server.users.exceptions.WrongPasswordException;
import server.users.user.User;
import server.constants.ServerConstants;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.server.RemoteServer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class UsersManager extends RemoteServer implements Registrable
{
    private static final UsersManager INSTANCE = new UsersManager();
    private static final ConcurrentHashMap<String, User> USERS_ARCHIVE = new ConcurrentHashMap<>(ServerConstants.INITIAL_USERS_DATABASE_SIZE);
    private static final ConcurrentHashMap<SelectionKey, Short> WRITABLE_CONNECTIONS = new ConcurrentHashMap<>();
    private static final BiFunction<SelectionKey, Short, Short> INCREMENTER = (SelectionKey selectionKey, Short relatedCounter) -> {
        if (relatedCounter == null)
            return (short) 1;
        else
            return ++relatedCounter;
    };
    private static final BiFunction<SelectionKey, Short, Short> DECREMENTER = (SelectionKey selectionKey, Short relatedCounter) -> {
        if (relatedCounter == null)
            return null;
        else
            if (relatedCounter == 1)
                return null;
            else
                return  --relatedCounter;
    };

    private UsersManager()
    {}

    public static UsersManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public boolean registerUser(String username, char[] password)
    {
        return (USERS_ARCHIVE.putIfAbsent(username, new User(username, password))) == null;
    }

    public static boolean openSession(String username, char[] password, SelectionKey connection) throws UnknownUserException, WrongPasswordException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        if (!taken.checkPassword(password))
            throw new WrongPasswordException();

        taken.connect(connection);
        taken.prependMessage(new Message(MessageType.OK));

        if (connection != null)
            WRITABLE_CONNECTIONS.compute(connection, INCREMENTER);

        return true;
    }

    public static boolean closeSession(String username, SelectionKey connection)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("Attempt to close a not existing session");

        WRITABLE_CONNECTIONS.remove(connection);

        taken.disconnect();

        return true;
    }

    public static boolean makeFriends(String username1, String username2) throws AlreadyExistingRelationshipException, UnknownUserException
    {
        User taken1  = USERS_ARCHIVE.get(username1);
        User taken2  = USERS_ARCHIVE.get(username2);

        if (taken1 == null)
            throw new UnknownUserException("UNKNOWN USER \"" + username1 + "\"");
        if (taken2 == null)
            throw new UnknownUserException("UNKNOWN USER \"" + username2 + "\"");

        boolean check1;
        boolean check2;

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
                throw new Error("Inconsistent relationship");
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
                throw new Error("Inconsistent relationship");
            }
            else
            {
                taken1.unlock();
                taken2.unlock();
                throw new AlreadyExistingRelationshipException("\"" + username1 + "\" and \"" + username2 + "\" ARE ALREADY FRIENDS");
            }
        }

        taken1.unlock();
        taken2.unlock();

        return true;
    }

    public static boolean sendMessage(String username, Message message) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        SelectionKey connection = taken.appendMessage(message);

        if (connection != null)
            WRITABLE_CONNECTIONS.compute(connection, INCREMENTER);

        return true;
    }

    public static boolean sendResponse(String username, Message message) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        SelectionKey connection = taken.prependMessage(message);

        if (connection != null)
            WRITABLE_CONNECTIONS.compute(connection, INCREMENTER);

        return true;
    }

    public static boolean restoreUnsentMessage(String username, Message message)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("System inconsistency");

        taken.prependMessage(message);

        return true;
    }

    public static Message retrieveMessage(String username, SelectionKey connection) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);
        Message message;

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        message = taken.getMessage();

        if (message != null)
            WRITABLE_CONNECTIONS.compute(connection, DECREMENTER);

        return message;
    }

    public static boolean hasPendingMessages(SelectionKey connection)
    {
        return WRITABLE_CONNECTIONS.containsKey(connection);
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
            Files.deleteIfExists(ServerConstants.USERS_DATABASE_BACKUP_PATH);
            Files.write(ServerConstants.USERS_DATABASE_BACKUP_PATH, jsonBytes, StandardOpenOption.CREATE_NEW);
        }
        catch (IOException e)
        {
            throw new Error("Backing up server.users system");
        }
    }

    public static void restore()
    {
        if (!Files.exists(ServerConstants.USERS_DATABASE_BACKUP_PATH))
            return;

        byte[] jsonBytes;

        try
        {
            jsonBytes = Files.readAllBytes(ServerConstants.USERS_DATABASE_BACKUP_PATH);
        }
        catch (IOException e)
        {
            throw new Error("Reading server.users system back up file");
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
            throw new Error("Parsing server.users system back up file");
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
