package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.settings.Settings;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;

public class ReplyFriendshipRequestOperator extends Operator
{
    private final String applicant;

    public ReplyFriendshipRequestOperator(WordQuizzleClientFrame frame, String applicant)
    {
        super(frame);
        this.applicant = applicant;
    }

    @Override
    protected Void doInBackground()
    {
        // Show confirmation dialog and get the choice
        int choice = JOptionPane.showConfirmDialog(this.frame, "User \"" + this.applicant + "\" sent you a friendship request \n Do you want to accept it?", "Friendship request", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, Settings.HANDSHAKE_ICON);
        if (choice == 0)
        {// Yes
            // Prepare message
            Message message = new Message(MessageType.CONFIRM_FRIENDSHIP_REQUEST, this.applicant);
            // Send message and get the response
            Message response = WordQuizzleClient.require(message);
            if (response.getType() == MessageType.OK)
            {
                frame.friendsPanel.addFriend(this.applicant);
            }
        }
        else
        {// No
            // Prepare message
            Message message = new Message(MessageType.DECLINE_FRIENDSHIP_REQUEST, this.applicant);
            // Send message
            WordQuizzleClient.require(message);
        }

        return null;
    }
}
