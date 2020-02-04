package server.users;

import messages.*;
import messages.exceptions.UnexpectedMessageException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import remote.Registrable;
import remote.VoidPasswordException;
import remote.VoidUsernameException;
import server.users.exceptions.AlreadyExistingRelationshipException;
import server.users.exceptions.RequestAlreadySentException;
import server.users.exceptions.UnknownUserException;
import server.users.exceptions.WrongPasswordException;
import server.users.user.User;
import server.constants.ServerConstants;

import java.io.IOException;
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
    private static final ConcurrentHashMap<String, Short> WRITABLE_CONNECTIONS = new ConcurrentHashMap<>();
    private static final BiFunction<String, Short, Short> INCREMENTER = (String user, Short relatedCounter) -> {
        if (relatedCounter == null)
            return (short) 1;
        else
            return ++relatedCounter;
    };
    private static final BiFunction<String, Short, Short> DECREMENTER = (String user, Short relatedCounter) -> {
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
    public boolean registerUser(String username, char[] password) throws VoidPasswordException, VoidUsernameException
    {
        if (username == null || username.equals(""))
            throw new VoidUsernameException("Empty username");
        if (password.length == 0)
            throw new VoidPasswordException("Empty password");

        return (USERS_ARCHIVE.putIfAbsent(username, new User(username, password))) == null;
    }

    public static boolean openSession(String username, char[] password) throws UnknownUserException, WrongPasswordException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        if (!taken.checkPassword(password))
            throw new WrongPasswordException();

        taken.logIn();
        taken.prependMessage(new Message(MessageType.OK));
        WRITABLE_CONNECTIONS.compute(username, INCREMENTER);

        return true;
    }

    public static boolean closeSession(String username)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("Attempt to close a not existing session");

        WRITABLE_CONNECTIONS.remove(username);

        taken.logOut();

        return true;
    }

    public static boolean makeFriends(String applicant, String friend) throws UnexpectedMessageException
    {
        User taken1  = USERS_ARCHIVE.get(applicant);
        User taken2  = USERS_ARCHIVE.get(friend);

        if (taken1 == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + applicant + "\"");
        if (taken2 == null)
            throw new Error("UNKNOWN USER \"" + friend + "\"");

        boolean check1;
        boolean check2;

        taken1.lock();
        taken2.lock();

        check1 = taken1.removeWaitingOutcomeFriendshipRequest(friend);
        check2 = taken2.removeWaitingIncomeFriendshipRequest(applicant);

        if (check1 != check2)
            throw new Error("Inconsistent relationship");
        if (!check1)
            throw new UnexpectedMessageException("CONFIRMATION NOT CORRESPONDS TO ANY REQUEST");

        check1 = taken1.isFriendOf(friend);
        check2 = taken2.isFriendOf(applicant);

        if (check1 != check2)
            throw new Error("Inconsistent relationship");
        else if (check1)
            throw new Error("Inconsistent relationship");
        else
        {
            taken1.addFriend(friend);
            taken2.addFriend(applicant);
        }

        taken1.unlock();
        taken2.unlock();

        return true;
    }

    public static boolean cancelFriendshipRequest(String applicant, String friend) throws UnexpectedMessageException
    {
        User taken1  = USERS_ARCHIVE.get(applicant);
        User taken2  = USERS_ARCHIVE.get(friend);

        if (taken1 == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + applicant + "\"");
        if (taken2 == null)
            throw new Error("UNKNOWN USER \"" + friend + "\"");

        boolean check1;
        boolean check2;

        taken1.lock();
        taken2.lock();

        check1 = taken1.removeWaitingOutcomeFriendshipRequest(friend);
        check2 = taken2.removeWaitingIncomeFriendshipRequest(applicant);

        if (check1 != check2)
            throw new Error("Inconsistent relationship");
        if (!check1)
            throw new UnexpectedMessageException("CONFIRMATION NOT CORRESPONDS TO ANY REQUEST");

        taken1.unlock();
        taken2.unlock();

        return true;
    }

    private static boolean areFriends(String username1, String username2) throws UnknownUserException
    {
        User taken1  = USERS_ARCHIVE.get(username1);
        User taken2  = USERS_ARCHIVE.get(username2);
        boolean check1;
        boolean check2;

        if (taken1 == null)
            throw new UnknownUserException("UNKNOWN USER \"" + username1 + "\"");
        if (taken2 == null)
            throw new UnknownUserException("UNKNOWN USER \"" + username2 + "\"");

        taken1.lock();
        taken2.lock();

        check1 = taken1.isFriendOf(username2);
        check2 = taken2.isFriendOf(username1);

        if (check1 != check2)
            throw new Error("Inconsistent relationship");

        taken1.unlock();
        taken2.unlock();

        return check1;
    }

    public static boolean sendMessage(String username, Message message) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");


        if (taken.appendMessage(message))
            WRITABLE_CONNECTIONS.compute(username, INCREMENTER);

        return true;
    }

    public static boolean sendFriendshipRequest(String applicant, String friend) throws UnknownUserException, AlreadyExistingRelationshipException, RequestAlreadySentException
    {
        User taken1 = USERS_ARCHIVE.get(applicant);
        User taken2 = USERS_ARCHIVE.get(friend);
        boolean check1;
        boolean check2;

        if (taken1 == null)
            throw new Error("UNKNOWN USER \"" + applicant + "\"");
        if (taken2 == null)
            throw new UnknownUserException("UNKNOWN USER \"" + friend + "\"");
        if (areFriends(applicant, friend))
            throw new AlreadyExistingRelationshipException("\"" + applicant + "\" and \"" + friend + "\" ARE ALREADY FRIENDS");

        taken1.lock();
        taken2.lock();

        check1 = taken1.addWaitingOutcomeFriendshipRequest(friend);
        check2 = taken2.addWaitingIncomeFriendshipRequest(applicant);

        if (check1 != check2)
            throw new Error("Inconsistent relationship");
        else if (!check1)
        {
            taken1.unlock();
            taken2.unlock();
            throw new RequestAlreadySentException();
        }

        taken1.unlock();
        taken2.unlock();

        taken2.appendMessage(new Message(MessageType.REQUEST_FOR_FRIENDSHIP, applicant));

        if (taken2.appendMessage(new Message(MessageType.REQUEST_FOR_FRIENDSHIP, applicant)))
            WRITABLE_CONNECTIONS.compute(friend, INCREMENTER);

        return true;
    }

    public static boolean sendResponse(String username, Message message)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        if (taken.prependMessage(message))
            WRITABLE_CONNECTIONS.compute(username, INCREMENTER);

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

    public static Message retrieveMessage(String username)
    {
        User taken  = USERS_ARCHIVE.get(username);
        Message message;

        if (taken ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        message = taken.getMessage();

        if (message != null)
            WRITABLE_CONNECTIONS.compute(username, DECREMENTER);

        return message;
    }

    public static boolean hasPendingMessages(String username)
    {
        return WRITABLE_CONNECTIONS.containsKey(username);
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
