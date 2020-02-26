package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;

import javax.swing.*;

public class FriendshipRequestDeclinedOperator implements Runnable
{
    private String to;

    public FriendshipRequestDeclinedOperator(String to)
    {
        this.to = to;
    }

    @Override
    public void run()
    {
        // Show information dialog
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "User \"" + to + "\" declined your friendship request", "Friendship request declined", JOptionPane.INFORMATION_MESSAGE, GuiConstants.THUMB_DOWN_ICON));
    }
}
