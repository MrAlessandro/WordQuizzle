package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import commons.messages.Message;
import commons.messages.MessageType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class LoginOperator extends Operator
{
    String username;
    char[] password;

    public LoginOperator(WordQuizzleClientFrame frame, String username, char[] password)
    {
        super(frame);
        this.username = username;
        this.password = password;
    }

    @Override
    protected Void doInBackground()
    {
        // Prepare login message
        Message logInMessage = new Message(MessageType.LOG_IN);
        logInMessage.addField(this.username.toCharArray());
        logInMessage.addField(this.password);

        try
        {// Add notification channel port to the message
            logInMessage.addField(String.valueOf(((InetSocketAddress) WordQuizzleClient.notificationChannel.getLocalAddress()).getPort()).toCharArray());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Sequence inconsistency");
        }

        // Send login message and get the response
        Message response = WordQuizzleClient.require(logInMessage);

        // Analyze response
        switch (response.getType())
        {
            case OK:
            {
                // Setting the global logged username
                WordQuizzleClient.SESSION_USERNAME.set(this.username);

                // Retrieve friends list
                Message friendsListRequest = new Message(MessageType.REQUEST_FOR_FRIENDS_LIST_WITH_SCORES);
                Message friendsListResponse = WordQuizzleClient.require(friendsListRequest);

                try
                {
                    // Parse friends list
                    JSONParser parser = new JSONParser();
                    JSONArray serializedFriendsList = (JSONArray) parser.parse(String.valueOf(friendsListResponse.getFields()[0].getBody()));
                    List<JSONObject> friends = new LinkedList<>((Collection<JSONObject>) serializedFriendsList);
                    // Add friends to friends panel
                    this.frame.friendsPanel.setFriendsTable(friends);
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                    System.exit(1);
                }

                // Retrieve score
                Message scoreRequest = new Message(MessageType.REQUEST_FOR_SCORE_AMOUNT);
                Message scoreResponse = WordQuizzleClient.require(scoreRequest);

                // Access at sessioned gui
                SwingUtilities.invokeLater(() -> this.frame.session(this.username, Integer.parseInt(String.valueOf(scoreResponse.getFields()[0]))));
                break;
            }
            case USERNAME_UNKNOWN:
            {
                SwingUtilities.invokeLater(() -> this.frame.logIn("Username unknown"));
                break;
            }
            case PASSWORD_WRONG:
            {
                SwingUtilities.invokeLater(() -> this.frame.logIn("Password wrong"));
                break;
            }
            case USER_ALREADY_LOGGED:
            {
                SwingUtilities.invokeLater(() -> this.frame.logIn("User \"" + this.username + "\" is already logged in."));
                break;
            }
            default:
            {throw new Error("Unexpected message");}
        }

        return null;
    }
}
