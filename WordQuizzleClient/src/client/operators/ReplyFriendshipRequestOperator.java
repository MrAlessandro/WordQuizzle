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

public class ReplyFriendshipRequestOperator implements Runnable
{
    private String from;

    public ReplyFriendshipRequestOperator(String from)
    {
        this.from = from;
    }

    @Override
    public void run()
    {
        // Show confirmation dialog and get the choice
        int choice = JOptionPane.showConfirmDialog(null, "User \"" + this.from + "\" sent you a friendship request \n Do you want to accept it?", "Friendship request", JOptionPane.YES_NO_OPTION);
        if (choice == 0)
        {// Yes
            // Prepare message
            Message message = new Message(MessageType.CONFIRM_FRIENDSHIP, this.from, WordQuizzleClientFrame.username);

            // Send message and get the response
            Message response = WordQuizzleClient.send(message);
            if (response.getType() != MessageType.OK)
                // If message is different from ok ignore it
                return;

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

            // Remove all friends from the global friends list
            FriendsPanel.FRIENDS_LIST.removeAllElements();

            // Inserts deserialized friends' username in the global friends list
            for (String friend : (Iterable<String>) DEfriendsList)
            {
                FriendsPanel.FRIENDS_LIST.addElement(friend);
            }
        }
        else
        {// No
            // Prepare message
            Message message = new Message(MessageType.DECLINE_FRIENDSHIP, this.from, WordQuizzleClientFrame.username);
            // Send message and get the response
            Message response = WordQuizzleClient.send(message);
            if (response.getType() != MessageType.OK)
                // If message is different from ok ignore it
                return;
        }
    }
}
