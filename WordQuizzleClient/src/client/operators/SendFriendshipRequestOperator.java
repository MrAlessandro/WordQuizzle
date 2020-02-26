package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
import client.main.WordQuizzleClient;
import messages.Message;
import messages.MessageType;

import javax.swing.*;

public class SendFriendshipRequestOperator implements Runnable
{
    @Override
    public void run()
    {
        // Show add friend dialog
        String friend = (String) JOptionPane.showInputDialog(WordQuizzleClientFrame.FRAME, "Send a friendship request to:", "Send a friendship request", JOptionPane.PLAIN_MESSAGE, GuiConstants.HANDSHAKE_ICON, null, "");
        if (friend == null || friend.equals(""))
            // If nothing have benn inserted do nothing
            return;

        // Prepare message
        Message message = new Message(MessageType.REQUEST_FOR_FRIENDSHIP, WordQuizzleClientFrame.username, friend);

        // Send the request and get the response
        Message response = WordQuizzleClient.send(message);

        if (response.getType() == MessageType.OK)
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Request sent", "Request sent", JOptionPane.INFORMATION_MESSAGE, GuiConstants.THUMB_UP_ICON));
        else if (response.getType() == MessageType.USERNAME_UNKNOWN)
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "User \"" + friend +"\" does not exist", "Request not sent", JOptionPane.ERROR_MESSAGE, GuiConstants.WARNING_ICON));
        else if (response.getType() == MessageType.ALREADY_FRIENDS)
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "You are already friend with \"" + friend +"\"", "Request not sent", JOptionPane.ERROR_MESSAGE, GuiConstants.WARNING_ICON));
    }
}
