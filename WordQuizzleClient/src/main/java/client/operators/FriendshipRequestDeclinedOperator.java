package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.settings.Settings;

import javax.swing.*;

public class FriendshipRequestDeclinedOperator extends Operator
{
    private final String receiver;
    public FriendshipRequestDeclinedOperator(WordQuizzleClientFrame frame, String receiver)
    {
        super(frame);
        this.receiver = receiver;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        // Show information dialog
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this.frame,
                "User \"" + receiver + "\" declined your friendship request", "Friendship request declined",
                JOptionPane.INFORMATION_MESSAGE, Settings.THUMB_DOWN_ICON));
        return null;
    }
}
