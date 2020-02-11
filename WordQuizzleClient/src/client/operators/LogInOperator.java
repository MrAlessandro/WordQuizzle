package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.panels.FriendsPanel;
import client.main.WordQuizzleClient;
import messages.Message;
import messages.MessageType;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class LogInOperator implements Runnable
{
    private String username;
    private char[] password;

    public LogInOperator(String username, char[] password)
    {
        this.username = username;
        this.password = password;
    }

    @Override
    public void run()
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
        Message response = WordQuizzleClient.send(logInMessage);

        switch (response.getType())
        {
            case OK:
            {
                // Setting the global logged username
                WordQuizzleClientFrame.username = this.username;

                // Parsing friends list
                String jsonString = new String(response.getField(0));
                JSONParser parser = new JSONParser();
                JSONArray DEfriendsList;

                try
                {
                    DEfriendsList = (JSONArray) parser.parse(jsonString);
                }
                catch (ParseException e)
                {
                    throw new Error("Parsing friends list");
                }

                // Inserts deserialized friends' username in the global friends list
                for (String friend : (Iterable<String>) DEfriendsList)
                {
                    FriendsPanel.FRIENDS_LIST.addElement(friend);
                }


                SwingUtilities.invokeLater(WordQuizzleClientFrame::session);
                break;
            }
            case USERNAME_UNKNOWN:
            {
                SwingUtilities.invokeLater(() -> WordQuizzleClientFrame.logIn("Username unknown"));
                break;
            }
            case PASSWORD_WRONG:
            {
                SwingUtilities.invokeLater(() -> WordQuizzleClientFrame.logIn("Password wrong"));
                break;
            }
            default:
            {throw new Error("Unexpected message");}
        }
    }
}
