package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
import client.gui.panels.ChallengePanel;

import javax.swing.*;

public class ChallengeRequestDeclinedOperator implements Runnable
{
    private String to;

    public ChallengeRequestDeclinedOperator(String to)
    {
        this.to = to;
    }

    @Override
    public void run()
    {
        SwingUtilities.invokeLater(ChallengePanel::waitForChallengeRequest);
        JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Opponent user \"" + this.to + "\" declined your request.", "Opponent declined", JOptionPane.INFORMATION_MESSAGE, GuiConstants.THUMB_DOWN_ICON);
    }
}
