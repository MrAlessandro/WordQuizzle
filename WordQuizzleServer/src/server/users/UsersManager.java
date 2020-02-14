package server.users;

import challenges.ChallengesManager;
import messages.*;
import messages.exceptions.UnexpectedMessageException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import remote.Registrable;
import remote.VoidPasswordException;
import remote.VoidUsernameException;
import server.users.exceptions.*;
import server.users.user.User;
import server.constants.ServerConstants;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.rmi.RemoteException;
import java.rmi.server.RemoteServer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsersManager extends RemoteServer implements Registrable
{
    private static final UsersManager INSTANCE = new UsersManager();
    private static final ConcurrentHashMap<String, User> USERS_ARCHIVE = new ConcurrentHashMap<>(ServerConstants.INITIAL_USERS_DATABASE_SIZE);

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
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        if (!user.checkPassword(password))
            throw new WrongPasswordException();

        user.logIn(clientAddress);

        return true;
    }

    public static SocketAddress getUserAddress(String username)
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        return user.getAddress();
    }

    public static boolean closeSession(String username)
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new Error("Attempt to close a not existing session");

        user.logOut();

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

        friendUser.addPendingFriendshipRequest(applicant);
        friendUser.storeMessage(new Message(MessageType.REQUEST_FOR_FRIENDSHIP_CONFIRMATION, applicant, friend));

        return true;
    }

    public static boolean sendChallengeRequest(String applicant, String opponent, Selector toWake) throws UnknownUserException, OpponentAlreadyEngagedException, UnexpectedMessageException, OpponentOfflineException
    {
        User applicantUser = USERS_ARCHIVE.get(applicant);
        User opponentUser = USERS_ARCHIVE.get(opponent);

        if (applicantUser == null)
            throw new Error("UNKNOWN USER \"" + applicant + "\"");
        if (opponentUser == null)
            throw new UnknownUserException("UNKNOWN USER \"" + opponent + "\"");

        try
        {
            applicantUser.setPendingChallengeRequest(opponent);
        }
        catch (OpponentOfflineException e)
        {
            e.printStackTrace();
            throw new Error("LogIn system inconsistency");
        }
        catch (OpponentAlreadyEngagedException e)
        {
            throw new UnexpectedMessageException("USER \"" + applicant + "\" SENT A CHALLENGE REQUEST BUT IS ALREADY ENGAGED IN A CHALLENGE");
        }

        try
        {
            opponentUser.setPendingChallengeRequest(applicant);
        }
        catch (OpponentOfflineException e)
        {
            applicantUser.removePendingChallengeRequest(opponent);
            throw new OpponentOfflineException(e.getMessage());
        }

        opponentUser.storeMessage(new Message(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, applicant, opponent));

        ChallengesManager.scheduleRequestTimeOut(applicant, opponent, toWake);

        return true;
    }

    public static boolean confirmFriendshipRequest(String whoSentRequest, String whoConfirmed) throws UnexpectedMessageException
    {
        User whoSentUser  = USERS_ARCHIVE.get(whoSentRequest);
        User whoConfirmedUser  = USERS_ARCHIVE.get(whoConfirmed);

        if (whoSentUser == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");
        if (whoConfirmedUser == null)
            throw new Error("UNKNOWN USER \"" + whoConfirmed + "\"");

        whoConfirmedUser.removePendingFriendshipRequest(whoSentRequest);

        whoSentUser.addFriend(whoConfirmed);
        whoConfirmedUser.addFriend(whoSentRequest);

        return true;
    }

    public static boolean cancelFriendshipRequest(String whoSentRequest, String whoDeclined) throws UnexpectedMessageException
    {
        User whoSentUser  = USERS_ARCHIVE.get(whoSentRequest);
        User whoDeclinedUser  = USERS_ARCHIVE.get(whoDeclined);

        if (whoSentUser == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");
        if (whoDeclinedUser == null)
            throw new Error("UNKNOWN USER \"" + whoDeclined + "\"");

        whoDeclinedUser.removePendingFriendshipRequest(whoSentRequest);

        return true;
    }

    public static boolean cancelChallengeRequest(String whoSentRequest, String whoDeclined, boolean timeout) throws UnexpectedMessageException
    {
        User whoSentUser  = USERS_ARCHIVE.get(whoSentRequest);
        User whoDeclinedUser  = USERS_ARCHIVE.get(whoDeclined);

        System.out.println("canceling challenge request");

        if (whoSentUser == null)
            if (timeout)
                throw new Error("Challenge system inconsistency");
            else
                throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");
        if (whoDeclinedUser == null)
            throw new Error("UNKNOWN USER \"" + whoDeclined + "\"");

        if (timeout)
            ChallengesManager.dequeueTimeOut(whoSentRequest, whoDeclined);
        else
            ChallengesManager.quitScheduledTimeOut(whoSentRequest, whoDeclined);

        whoSentUser.removePendingChallengeRequest(whoDeclined);
        whoDeclinedUser.removePendingChallengeRequest(whoSentRequest);

        if (timeout)
        {
            System.out.println("Storing notification");
            whoSentUser.storeMessage(new Message(MessageType.OPPONENT_DID_NOT_REPLY, whoSentRequest, whoDeclined));
            whoDeclinedUser.storeMessage(new Message(MessageType.CHALLENGE_REQUEST_TIMEOUT_EXPIRED, whoSentRequest, whoDeclined));
        }
        else
            whoSentUser.storeMessage(new Message(MessageType.CHALLENGE_DECLINED, whoSentRequest, whoDeclined));

        return true;
    }

    public static String retrieveSerializedFriendList(String username)
    {
        User user = USERS_ARCHIVE.get(username);

        if (user == null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        return user.JSONserializeFriendsList();
    }

    public static boolean sendMessage(String username, Message message) throws UnknownUserException
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        user.storeMessage(message);

        return true;
    }

    public static boolean sendResponse(String username, Message message)
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        user.storeResponse(message);
        return true;
    }

    public static boolean restoreUnsentMessage(String username, Message message)
    {
        User taken  = USERS_ARCHIVE.get(username);

        if (taken ==  null)
            throw new Error("System inconsistency");

        taken.restoreMessage(message);

        return true;
    }

    public static Message retrieveMessage(String username)
    {
        User user  = USERS_ARCHIVE.get(username);
        Message message;

        if (user ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        message = user.retrieveMessage();

        return message;
    }

    public static boolean hasPendingMessages(String username)
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        return user.hasPendingMessages();
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
