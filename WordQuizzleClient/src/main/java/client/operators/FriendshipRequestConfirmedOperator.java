package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.settings.Settings;

import javax.swing.*;

public class FriendshipRequestConfirmedOperator extends Operator
{
    private final String receiver;
    private int receiverScore;

    public FriendshipRequestConfirmedOperator(WordQuizzleClientFrame frame, String receiver, int receiverScore)
    {
        super(frame);
        this.receiver = receiver;
        this.receiverScore = receiverScore;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        // Show information dialog
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.frame,
                "User \"" + this.receiver + "\" accepted your friendship request", "Friendship request confirmed",
                JOptionPane.INFORMATION_MESSAGE, Settings.THUMB_UP_ICON));
        this.frame.friendsPanel.addFriend(this.receiver, this.receiverScore);

        return null;
    }
}
