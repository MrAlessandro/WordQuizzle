package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;

public class LogOutOperator extends Operator
{
    public LogOutOperator(WordQuizzleClientFrame frame)
    {
        super(frame);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        // Reset the gui
        SwingUtilities.invokeLater(() -> this.frame.welcome());

        // Empty friends list
        this.frame.friendsPanel.emptyFriendsList();

        // Prepare logout message
        Message request = new Message(MessageType.LOG_OUT);
        Message response = WordQuizzleClient.require(request);

        System.out.println(response);

        return null;
    }
}
