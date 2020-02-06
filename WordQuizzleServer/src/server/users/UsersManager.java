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
import java.net.SocketAddress;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
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
    public boolean registerUser(String username, char[] password) throws RemoteException, VoidPasswordException, VoidUsernameException
    {
        System.out.print("Registering user \"" + username + "\"... ");

        if (username == null || username.equals(""))
        {
            System.out.println("\u001B[31m" + "EMPTY USERNAME" + "\u001B[0m");
            throw new VoidUsernameException("Empty username");
        }
        if (password.length == 0)
        {
            System.out.println("\u001B[31m" + "EMPTY PASSWORD" + "\u001B[0m");
            throw new VoidPasswordException("Empty password");
        }

        boolean result = (USERS_ARCHIVE.putIfAbsent(username, new User(username, password))) == null;

        if (result)
            System.out.println("\u001B[32m" + "REGISTERED" + "\u001B[0m");
        else
            System.out.println("\u001B[31m" + "USERNAME ALREADY USED" + "\u001B[0m");

        return result;
    }

    public static boolean openSession(String username, char[] password, SocketAddress clientAddress) throws UnknownUserException, WrongPasswordException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        if (!taken.checkPassword(password))
            throw new WrongPasswordException();

        taken.logIn(clientAddress);
        taken.storeResponse(new Message(MessageType.OK));

        int backLogRequestsAmount = 1;
        backLogRequestsAmount += taken.getBackLogAmount();

        if(WRITABLE_CONNECTIONS.putIfAbsent(username, (short) backLogRequestsAmount) != null)
            throw new Error("Writable channels management inconsistency");

        return true;
    }

    public static SocketAddress getUserAddress(String username)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        return taken.getAddress();
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

    public static boolean sendFriendshipRequest(String applicant, String friend) throws UnknownUserException, AlreadyExistingRelationshipException, RequestAlreadySentException
    {
        User applicantUser = USERS_ARCHIVE.get(applicant);
        User friendUser = USERS_ARCHIVE.get(friend);
        boolean check1;
        boolean check2;

        if (applicantUser == null)
            throw new Error("UNKNOWN USER \"" + applicant + "\"");
        if (friendUser == null)
            throw new UnknownUserException("UNKNOWN USER \"" + friend + "\"");

        check1 = applicantUser.isFriendOf(friend);
        check2 = friendUser.isFriendOf(applicant);

        if (check1 != check2)
            throw new Error("Inconsistent relationship");
        else if (check1)
            throw new AlreadyExistingRelationshipException("\"" + applicant + "\" and \"" + friend + "\" ARE ALREADY FRIENDS");


        if (friendUser.hasPendingFriendshipRequest(applicant))
            throw new RequestAlreadySentException("FRIENDSHIP REQUEST FROM \"" + applicant + "\" to \"" + friend + "\" ALREADY SENT");

        friendUser.addPendingFriendshipRequest(applicant);
        friendUser.appendRequest(new Message(MessageType.REQUEST_FOR_FRIENDSHIP, applicant, friend));
        if (friendUser.isLogged())
            WRITABLE_CONNECTIONS.compute(friend, INCREMENTER);

        return true;
    }

    public static boolean confirmFriendship(String whoSentRequest, String whoConfirmed) throws UnexpectedMessageException, AlreadyExistingRelationshipException
    {
        User whoSentUser  = USERS_ARCHIVE.get(whoSentRequest);
        User whoConfirmedUser  = USERS_ARCHIVE.get(whoConfirmed);

        if (whoSentUser == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");
        if (whoConfirmedUser == null)
            throw new Error("UNKNOWN USER \"" + whoConfirmed + "\"");

        boolean check1;
        boolean check2;

        if (!whoConfirmedUser.removePendingFriendshipRequest(whoSentRequest))
            throw new UnexpectedMessageException("CONFIRMATION NOT CORRESPONDS TO ANY REQUEST");
        whoSentUser.removePendingFriendshipRequest(whoConfirmed);

        check1 = whoSentUser.isFriendOf(whoConfirmed);
        check2 = whoConfirmedUser.isFriendOf(whoSentRequest);

        if (check1 != check2)
            throw new Error("Inconsistent relationship");
        else if (check1)
            throw new AlreadyExistingRelationshipException("\"" + whoSentRequest + "\" and \"" + whoConfirmed + "\" ARE ALREADY FRIENDS");
        else
        {
            whoSentUser.addFriend(whoConfirmed);
            whoConfirmedUser.addFriend(whoSentRequest);
        }

        return true;
    }

    public static boolean cancelFriendshipRequest(String whoSentRequest, String whoConfirmed) throws UnexpectedMessageException
    {
        User whoSentUser  = USERS_ARCHIVE.get(whoSentRequest);
        User whoConfirmedUser  = USERS_ARCHIVE.get(whoConfirmed);

        if (whoSentUser == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");
        if (whoConfirmedUser == null)
            throw new Error("UNKNOWN USER \"" + whoConfirmed + "\"");


        if (!whoConfirmedUser.removePendingFriendshipRequest(whoSentRequest))
            throw new UnexpectedMessageException("DECLINE NOT CORRESPONDS TO ANY REQUEST");

        return true;
    }

    public static boolean sendMessage(String username, Message message) throws UnknownUserException
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");


        if (taken.appendRequest(message))
            WRITABLE_CONNECTIONS.compute(username, INCREMENTER);

        return true;
    }

    public static boolean sendResponse(String username, Message message)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        if (taken.storeResponse(message))
            WRITABLE_CONNECTIONS.compute(username, INCREMENTER);

        return true;
    }

    public static boolean restoreUnsentMessage(String username, Message message)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("System inconsistency");

        taken.prependRequest(message);

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
            e.printStackTrace();
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
