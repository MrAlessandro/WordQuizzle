package client.operators;

import client.gui.panels.ChallengePanel;

import javax.swing.*;

public class OpponentDidNotReplyOperator implements Runnable
{
    private String whoMissedRequest;

    public OpponentDidNotReplyOperator(String whoMissedRequest)
    {
        this.whoMissedRequest = whoMissedRequest;
    }

    @Override
    public void run()
    {
        // Show information message about the expiration of the request
        JOptionPane.showMessageDialog(null, "Opponent \"" + whoMissedRequest + "\" did not reply in time", "Request expired", JOptionPane.INFORMATION_MESSAGE);

        // Restore challenge panel to initial configuration
        SwingUtilities.invokeLater(ChallengePanel::waitForChallengeRequest);
    }
}
