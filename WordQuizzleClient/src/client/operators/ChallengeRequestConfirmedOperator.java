package client.operators;

import client.gui.panels.ChallengePanel;
import client.gui.panels.ScoresPanel;

import javax.swing.*;

public class ChallengeRequestConfirmedOperator implements Runnable
{
    private String from;
    private String to;
    private int timeout;
    private int challengeWordsQuantity;
    private String firstWord;

    public ChallengeRequestConfirmedOperator(String from, String to, String timeout, String challengeWordsQuantity, String firstWord)
    {
        this.from = from;
        this.to = to;
        this.timeout = Integer.parseInt(timeout);
        this.challengeWordsQuantity = Integer.parseInt(challengeWordsQuantity);
        this.firstWord = firstWord;
    }

    @Override
    public void run()
    {
        ChallengePanel.applicant = this.from;
        ChallengePanel.opponent = this.to;
        SwingUtilities.invokeLater(() -> {
            ScoresPanel.launchTimer(this.timeout);
            ChallengePanel.challenge(this.timeout, this.challengeWordsQuantity, this.firstWord);
        });
    }
}
