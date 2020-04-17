package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.settings.Settings;

import javax.swing.*;

public class FriendshipRequestConfirmedOperator extends Operator
{
    private final String receiver;
    public FriendshipRequestConfirmedOperator(WordQuizzleClientFrame frame, String receiver)
    {
        super(frame);
        this.receiver = receiver;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        // Show information dialog
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.frame,
                "User \"" + this.receiver + "\" accepted your friendship request", "Friendship request confirmed",
                JOptionPane.INFORMATION_MESSAGE, Settings.THUMB_UP_ICON));
        this.frame.friendsPanel.addFriend(this.receiver);

        return null;
    }
}
