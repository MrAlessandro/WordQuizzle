package client.operators;

import client.gui.panels.ChallengePanel;

import javax.swing.*;

public class ChallengeRequestTimeoutExpiredOperator implements Runnable
{
    private String whoMissedRequest;

    public ChallengeRequestTimeoutExpiredOperator(String whoMissedRequest)
    {
        this.whoMissedRequest = whoMissedRequest;
    }

    @Override
    public void run()
    {
        // Show information message about the expiration of the request
        JOptionPane.showMessageDialog(null, "Challenge request to \"" + whoMissedRequest + "\" is expired", "Request timeout expired", JOptionPane.INFORMATION_MESSAGE);

        // Restore challenge panel to initial configuration
        SwingUtilities.invokeLater(ChallengePanel::waitForChallengeRequest);
    }
}
