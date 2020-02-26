package server.users;

import server.challenges.ChallengesManager;
import messages.*;
import messages.exceptions.UnexpectedMessageException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import remote.Registrable;
import remote.VoidPasswordException;
import remote.VoidUsernameException;
import server.challenges.challenge.ChallengeReport;
import server.users.exceptions.*;
import server.users.user.User;
import server.constants.ServerConstants;

import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.SelectionKey;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    public static boolean openSession(String username, char[] password, SocketAddress clientAddress, SelectionKey key) throws UnknownUserException, WrongPasswordException
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        if (!user.checkPassword(password))
            throw new WrongPasswordException();

        user.logIn(clientAddress, key);

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
        User user = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new Error("Attempt to close a not existing session");

        String challengeRequest = user.removePendingChallengeRequest();
        if (challengeRequest != null)
        {
            String[] split = challengeRequest.split(":");
            MessageType messageType;
            String applicant = split[0];
            String opponent = split[1];


            ChallengesManager.quitScheduledRequestTimeOut(applicant, opponent);

            User otherPlayer;
            if (applicant.equals(username))
            {
                otherPlayer = USERS_ARCHIVE.get(opponent);
                messageType = MessageType.APPLICANT_WENT_OFFLINE_DURING_REQUEST;
            }
            else
            {
                otherPlayer = USERS_ARCHIVE.get(applicant);
                messageType = MessageType.OPPONENT_WENT_OFFLINE_DURING_REQUEST;
            }

            otherPlayer.removePendingChallengeRequest();
            otherPlayer.storeMessage(new Message(messageType, applicant, opponent));
            otherPlayer.wakeUpDeputy();

        }

        String eventualOpponent = user.removeOpponent();
        if (eventualOpponent != null)
        {
            ChallengeReport report = null;
            Message message;
            String applicant;
            String opponent;

            report = ChallengesManager.abortChallenge(username, eventualOpponent);
            if (report != null)
            {// Disconnecting user is applicant
                message = new Message(MessageType.APPLICANT_WENT_OFFLINE_DURING_CHALLENGE, username, eventualOpponent,
                report.winner, String.valueOf(report.opponentProgress), String.valueOf(report.opponentScore));
                applicant = username;
                opponent = eventualOpponent;
            }
            else
            {
                report = ChallengesManager.abortChallenge(eventualOpponent, username);
                if (report == null)
                    throw new Error("Challenge system inconsistency");

                message = new Message(MessageType.OPPONENT_WENT_OFFLINE_DURING_CHALLENGE, username, eventualOpponent,
                        report.winner, String.valueOf(report.applicantProgress), String.valueOf(report.applicantScore));
                applicant = eventualOpponent;
                opponent = username;
            }

            User otherPlayer = USERS_ARCHIVE.get(eventualOpponent);
            otherPlayer.removeOpponent();
            otherPlayer.storeMessage(message);

            UsersManager.updateUserScore(applicant, report.applicantScore);
            UsersManager.updateUserScore(opponent, report.opponentScore);

            otherPlayer.wakeUpDeputy();
        }

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
        friendUser.wakeUpDeputy();

        return true;
    }

    public static boolean sendChallengeRequest(String applicant, String opponent) throws UnknownUserException, OpponentAlreadyEngagedException, UnexpectedMessageException, OpponentOfflineException, OpponentNotFriendException
    {
        User applicantUser = USERS_ARCHIVE.get(applicant);
        User opponentUser = USERS_ARCHIVE.get(opponent);

        if (applicantUser == null)
            throw new Error("UNKNOWN USER \"" + applicant + "\"");
        if (opponentUser == null)
            throw new UnknownUserException("UNKNOWN USER \"" + opponent + "\"");

        if (!applicantUser.isFriendOf(opponent))
            throw new OpponentNotFriendException("USER \"" + applicant + "\" AND USER \"" + opponent + "\" ARE NOT FRIENDS");

        try
        {
            applicantUser.setPendingChallengeRequest(applicant, opponent);
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
            opponentUser.setPendingChallengeRequest(applicant, opponent);
        }
        catch (OpponentOfflineException e)
        {
            applicantUser.removePendingChallengeRequest();
            throw new OpponentOfflineException(e.getMessage());
        }


        opponentUser.storeMessage(new Message(MessageType.REQUEST_FOR_CHALLENGE_CONFIRMATION, applicant, opponent));
        opponentUser.wakeUpDeputy();

        ChallengesManager.scheduleRequestTimeOut(applicant, opponent);

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

    public static String confirmChallengeRequest(String applicant, String opponent) throws UnexpectedMessageException
    {
        User applicantUser  = USERS_ARCHIVE.get(applicant);
        User opponentUser  = USERS_ARCHIVE.get(opponent);

        if (applicantUser == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + applicant + "\"");
        if (opponentUser == null)
            throw new Error("UNKNOWN USER \"" + opponent + "\"");

        ChallengesManager.quitScheduledRequestTimeOut(applicant, opponent);
        String firstWord = ChallengesManager.registerChallenge(applicant, opponent);

        applicantUser.removePendingChallengeRequest();
        opponentUser.removePendingChallengeRequest();
        
        applicantUser.setOpponent(opponent);
        opponentUser.setOpponent(applicant);

        return firstWord;
    }

    public static boolean cancelFriendshipRequest(String whoSentRequest, String whoDeclined) throws UnexpectedMessageException
    {
        User whoSentUser  = USERS_ARCHIVE.get(whoSentRequest);
        User whoDeclinedUser  = USERS_ARCHIVE.get(whoDeclined);

        if (whoSentUser == null)
            throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");
        if (whoDeclinedUser == null)
            throw new Error("UNKNOWN USER \"" + whoDeclined + "\"");

        ChallengesManager.quitScheduledRequestTimeOut(whoSentRequest, whoDeclined);

        whoDeclinedUser.removePendingFriendshipRequest(whoSentRequest);

        return true;
    }

    public static boolean cancelChallengeRequest(String whoSentRequest, String whoDeclined, boolean timeout) throws UnexpectedMessageException
    {
        User whoSentUser  = USERS_ARCHIVE.get(whoSentRequest);
        User whoDeclinedUser  = USERS_ARCHIVE.get(whoDeclined);
        boolean check;

        if (whoSentUser == null)
            if (timeout)
                throw new Error("Challenge system inconsistency");
            else
                throw new UnexpectedMessageException("UNKNOWN USER \"" + whoSentRequest + "\"");
        if (whoDeclinedUser == null)
            throw new Error("UNKNOWN USER \"" + whoDeclined + "\"");

        if (timeout)
            check = ChallengesManager.dequeueScheduledRequestTimeOut(whoSentRequest, whoDeclined);
        else
            check = ChallengesManager.quitScheduledRequestTimeOut(whoSentRequest, whoDeclined);

        if (!check)
            throw new Error("Timers storing inconsistency");

        if (whoSentUser.removePendingChallengeRequest() == null)
            throw new UnexpectedMessageException("CANCELING CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" AND \"" + whoDeclined + "\" DOES NOT CORRESPOND TO ANY REQUEST");
        if (whoDeclinedUser.removePendingChallengeRequest() == null)
            throw new UnexpectedMessageException("CANCELING CHALLENGE REQUEST BETWEEN \"" + whoSentRequest + "\" AND \"" + whoDeclined + "\" DOES NOT CORRESPOND TO ANY REQUEST");

        if (timeout)
        {
            whoSentUser.storeMessage(new Message(MessageType.OPPONENT_DID_NOT_REPLY, whoSentRequest, whoDeclined));
            whoDeclinedUser.storeMessage(new Message(MessageType.CHALLENGE_REQUEST_TIMEOUT_EXPIRED, whoSentRequest, whoDeclined));
            whoDeclinedUser.wakeUpDeputy();
        }
        else
            whoSentUser.storeMessage(new Message(MessageType.CHALLENGE_DECLINED, whoSentRequest, whoDeclined));

        whoSentUser.wakeUpDeputy();

        return true;
    }

    public static boolean quitChallenge(String username)
    {
        User user = USERS_ARCHIVE.get(username);
        if (user ==  null)
            throw new Error("Users database inconsistency");

        user.removeOpponent();

        return true;
    }

    public static String retrieveSerializedFriendList(String username)
    {
        User user = USERS_ARCHIVE.get(username);

        if (user == null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        return user.JSONserializeFriendsList();
    }

    public static String retrieveSerializedFriendListAndScores(String username)
    {
        User user = USERS_ARCHIVE.get(username);
        JSONArray friendsAndScoresArray = new JSONArray();

        if (user == null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        String[] friendsList = user.getFriendsList();
        for (String friend : friendsList)
        {
            User friendUser = USERS_ARCHIVE.get(friend);

            if (friendUser == null)
                throw new Error("UNKNOWN USER \"" + username + "\"");

            friendsAndScoresArray.add(friendUser.JSONserializeUsernameAndScore());
        }

        return friendsAndScoresArray.toJSONString();
    }

    public static boolean sendMessage(String username, Message message) throws UnknownUserException
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new UnknownUserException("UNKNOWN USER \"" + username + "\"");

        user.storeMessage(message);
        user.wakeUpDeputy();

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

    public static void updateUserScore(String username, int gain)
    {
        User user  = USERS_ARCHIVE.get(username);

        if (user ==  null)
            throw new Error("UNKNOWN USER \"" + username + "\"");

        user.updateScore(gain);

        String[] friendsList = user.getFriendsList();
        for (String friend : friendsList)
        {
            Message updateMessage = new Message(MessageType.SCORE_UPDATE, username, String.valueOf(user.getScore()));
            User friendUser = USERS_ARCHIVE.get(friend);

            if (friendUser == null)
                throw new Error("UNKNOWN USER \"" + username + "\"");

            if (friendUser.getAddress() != null)
                friendUser.storeMessage(updateMessage);
        }
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
            Files.deleteIfExists(Paths.get(ServerConstants.USERS_DATABASE_BACKUP_PATH.getPath()));
            Files.write(Paths.get(ServerConstants.USERS_DATABASE_BACKUP_PATH.getPath()), jsonBytes, StandardOpenOption.CREATE_NEW);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Backing up server.users system");
        }
    }

    public static void restore()
    {
        try
        {
            File backUpFile = Paths.get(ServerConstants.USERS_DATABASE_BACKUP_PATH.toURI()).toFile();

            if (!backUpFile.exists())
                return;

            String jsonString = new String(Files.readAllBytes(backUpFile.toPath()));

            JSONParser parser = new JSONParser();
            Map<String, User> DEusersArchive = new HashMap<>();
            JSONArray DEusersArray;

            DEusersArray = (JSONArray) parser.parse(jsonString);


            for (JSONObject currentUser : (Iterable<JSONObject>) DEusersArray)
            {
                // Deserialize user and add to users archive
                User DEuser = User.JSONdeserialize(currentUser);
                DEusersArchive.put(DEuser.getUsername(), DEuser);
            }

            USERS_ARCHIVE.putAll(DEusersArchive);

        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Reading server.users system back up file");
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
            throw new Error("URI generation error");
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw new Error("Parsing server.users system back up file");
        }
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
