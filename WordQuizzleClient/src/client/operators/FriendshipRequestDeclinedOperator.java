package client.operators;

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
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, "User \"" + to + "\" declined your friendship request", "Friendship request declined", JOptionPane.INFORMATION_MESSAGE));
    }
}
