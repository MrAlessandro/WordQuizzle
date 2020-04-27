package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.settings.Settings;

import javax.swing.*;

public class OpponentLoggedOutOperator extends Operator
{
    private String opponent;
    public OpponentLoggedOutOperator(WordQuizzleClientFrame frame, String opponent)
    {
        super(frame);
        this.opponent = opponent;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        SwingUtilities.invokeLater(() -> this.frame.challengePanel.unemploy());
        JOptionPane.showMessageDialog(this.frame, "\"" + opponent + "\" logged out during challenge request",
                "Opponent logged out", JOptionPane.INFORMATION_MESSAGE, Settings.LOGOUT_ICON);
        return null;
    }
}
