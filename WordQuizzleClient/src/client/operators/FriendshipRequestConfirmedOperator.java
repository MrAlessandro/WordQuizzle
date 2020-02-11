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

public class FriendshipRequestConfirmedOperator implements Runnable
{
    private String to;

    public FriendshipRequestConfirmedOperator(String to)
    {
        this.to = to;
    }

    @Override
    public void run()
    {
        // Show information dialog
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "User \"" + to + "\" accepted your friendship request", "Friendship request confirmed", JOptionPane.INFORMATION_MESSAGE));

        // Get the updated friends list
        // Prepare request message
        Message message = new Message(MessageType.REQUEST_FOR_FRIENDS_LIST);

        // Send message and get response
        Message response = WordQuizzleClient.send(message);

        // Parse friend list
        String jsonString = new String(message.getField(0));
        JSONParser parser = new JSONParser();
        JSONArray DEfriendsList;

        try
        {
            DEfriendsList = (JSONArray) parser.parse(jsonString);
        }
        catch (ParseException e)
        {
            throw new Error("Parsing server.users system back up file");
        }

        // Remove all friends from the global friends list
        FriendsPanel.FRIENDS_LIST.removeAllElements();

        // Inserts deserialized friends' username in the global friends list
        for (String friend : (Iterable<String>) DEfriendsList)
        {
            FriendsPanel.FRIENDS_LIST.addElement(friend);
        }
    }
}
