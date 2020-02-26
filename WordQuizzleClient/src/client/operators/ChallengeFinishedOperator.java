package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
import client.gui.panels.ChallengePanel;

import javax.swing.*;

public class ChallengeFinishedOperator implements Runnable
{
    private String winner;
    private int progress;
    private int scoreGain;

    public ChallengeFinishedOperator(String winner, int progress, int scoreGain)
    {
        this.winner = winner;
        this.progress = progress;
        this.scoreGain = scoreGain;
    }

    @Override
    public void run()
    {
        boolean won = this.winner.equals(WordQuizzleClientFrame.username);
        int wordsTotalQuantity = ChallengePanel.challengeWordsQuantity;
        SwingUtilities.invokeLater(ChallengePanel::waitForChallengeRequest);

        if (won)
            JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Challenge timeout expired.\n" +
                            "You won!\n" +
                            "You translated " + this.progress + " words out of " + wordsTotalQuantity + ".\n" +
                            "You gained " + scoreGain + " points.",
                    "Challenge timeout Expired", JOptionPane.INFORMATION_MESSAGE, GuiConstants.THUMB_UP_ICON);
        else
            JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Challenge timeout expired.\n" +
                            "You lost!\n" +
                            "You translated " + this.progress + " words out of " + wordsTotalQuantity + ".\n" +
                            "You gained " + scoreGain + " points.",
                    "Challenge timeout Expired", JOptionPane.INFORMATION_MESSAGE, GuiConstants.THUMB_DOWN_ICON);
    }
}
