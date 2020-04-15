package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.settings.Settings;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;

public class SendFriendshipRequestOperator extends Operator
{

    public SendFriendshipRequestOperator(WordQuizzleClientFrame frame)
    {
        super(frame);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        // Show add friend dialog
        String friend = (String) JOptionPane.showInputDialog(this.frame, "Send a friendship request to:", "Send a friendship request", JOptionPane.PLAIN_MESSAGE, Settings.HANDSHAKE_ICON, null, "");
        if (friend == null || friend.equals(""))
            // If nothing have been inserted do nothing
            return null;

        // Prepare message
        Message message = new Message(MessageType.REQUEST_FOR_FRIENDSHIP, friend);
        // Send the request and get the response
        Message response = WordQuizzleClient.require(message);

        if (response.getType() == MessageType.OK)
            JOptionPane.showMessageDialog(this.frame, "Request sent", "Request sent", JOptionPane.INFORMATION_MESSAGE, Settings.THUMB_UP_ICON);
        else if (response.getType() == MessageType.USERNAME_UNKNOWN)
            JOptionPane.showMessageDialog(this.frame, "User \"" + friend +"\" does not exist", "Request not sent", JOptionPane.ERROR_MESSAGE, Settings.WARNING_ICON);
        else if (response.getType() == MessageType.ALREADY_FRIENDS)
            JOptionPane.showMessageDialog(this.frame, "You are already friend with \"" + friend +"\"", "Request not sent", JOptionPane.ERROR_MESSAGE, Settings.WARNING_ICON);

        return null;
    }
}
