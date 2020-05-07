package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;

public class SetUpChallengeOperator extends Operator
{
    private String opponent;
    private int wordQuantity;
    private int challengeDurationSeconds;

    public SetUpChallengeOperator(WordQuizzleClientFrame frame, String opponent, int wordQuantity, int challengeDurationSeconds)
    {
        super(frame);
        this.opponent = opponent;
        this.wordQuantity = wordQuantity;
        this.challengeDurationSeconds = challengeDurationSeconds;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        SwingUtilities.invokeLater(() -> frame.challengePanel.challengeLoading(this.opponent));
        // Prepare first word request message
        Message request = new Message(MessageType.CHALLENGE_GET_WORD);
        Message response = WordQuizzleClient.require(request);
        String firstWord = String.valueOf(response.getFields()[0]);
        // Set up challenge panel
        SwingUtilities.invokeLater(() -> frame.challengePanel.challenge(firstWord, opponent, wordQuantity, challengeDurationSeconds));

        return null;
    }
}
