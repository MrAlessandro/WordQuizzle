package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
import client.gui.panels.ChallengePanel;
import client.gui.panels.ScoresPanel;

import javax.swing.*;

public class ChallengeTimeoutExpiredOperator implements Runnable
{
    private String winner;
    private int progress;
    private int scoreGain;

    public ChallengeTimeoutExpiredOperator(String winner, int progress, int scoreGain)
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

        ScoresPanel.stopTimer();
        SwingUtilities.invokeLater(ChallengePanel::waitForChallengeRequest);

        if (won)
            JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Challenge timeout expired.\n" +
                            "You won!\n" +
                            "You translated " + this.progress + " words out of " + wordsTotalQuantity + ".\n" +
                            "You gained " + scoreGain + " points.",
                            "Challenge timeout Expired", JOptionPane.INFORMATION_MESSAGE, GuiConstants.TIMEOUT_ICON);
        else
            JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Challenge timeout expired.\n" +
                            "You lost!\n" +
                            "You translated " + this.progress + " words out of " + wordsTotalQuantity + ".\n" +
                            "You gained " + scoreGain + " points.",
                    "Challenge timeout Expired", JOptionPane.INFORMATION_MESSAGE, GuiConstants.TIMEOUT_ICON);

    }
}
