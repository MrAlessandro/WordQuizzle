package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
import client.gui.dialogs.ChallengeDialog;
import client.gui.panels.ChallengePanel;

import javax.swing.*;

public class OpponentWentOfflineOperator implements Runnable
{
    private String to;

    public OpponentWentOfflineOperator(String to)
    {
        this.to = to;
    }

    @Override
    public void run()
    {
        ChallengeDialog.CHALLENGE_DIALOG.setVisible(false);
        SwingUtilities.invokeLater(ChallengePanel::waitForChallengeRequest);
        JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Opponent user \"" + this.to + "\" logged out in the meantime", "Opponent logged out", JOptionPane.INFORMATION_MESSAGE, GuiConstants.WARNING_ICON);
    }
}
