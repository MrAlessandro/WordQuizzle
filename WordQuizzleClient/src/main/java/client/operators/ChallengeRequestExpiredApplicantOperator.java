package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.settings.Settings;

import javax.swing.*;

public class ChallengeRequestExpiredApplicantOperator extends Operator
{
    public ChallengeRequestExpiredApplicantOperator(WordQuizzleClientFrame frame)
    {
        super(frame);
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        SwingUtilities.invokeLater(() -> frame.challengePanel.unemploy());
        JOptionPane.showMessageDialog(this.frame, "Your challenge request has expired...",
                "Challenge request expired", JOptionPane.INFORMATION_MESSAGE, Settings.TIMEOUT_ICON);
        this.frame.friendsPanel.setEnableChallengeButton(true);
        return null;
    }
}
