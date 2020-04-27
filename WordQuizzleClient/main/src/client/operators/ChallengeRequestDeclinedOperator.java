package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.settings.Settings;

import javax.swing.*;

public class ChallengeRequestDeclinedOperator extends Operator
{
    private String receiver;

    public ChallengeRequestDeclinedOperator(WordQuizzleClientFrame frame, String receiver)
    {
        super(frame);
        this.receiver = receiver;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        SwingUtilities.invokeLater(() -> this.frame.challengePanel.unemploy());
        JOptionPane.showMessageDialog(this.frame, "\"" + receiver + "\" declined your challenge request",
                "Challenge request declined", JOptionPane.INFORMATION_MESSAGE, Settings.THUMB_DOWN_ICON);
        return null;
    }
}
