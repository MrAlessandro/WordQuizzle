package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.panels.HeaderPanel;
import client.main.WordQuizzleClient;
import client.settings.Settings;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;

public class ChallengeTerminationOperator extends Operator
{
    private final MessageType messageType;
    private final int winStatus;
    private final int progress;
    private final int scoreGain;

    private String dialogMessage;
    private String dialogTitle;
    private ImageIcon dialogIcon;

    public ChallengeTerminationOperator(WordQuizzleClientFrame frame, MessageType messageType, int winStatus, int progress, int scoreGain)
    {
        super(frame);
        this.messageType = messageType;
        this.winStatus = winStatus;
        this.progress = progress;
        this.scoreGain = scoreGain;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        SwingUtilities.invokeLater(() -> this.frame.challengePanel.waitingReport());
        if (messageType == MessageType.CHALLENGE_EXPIRED)
        {
            this.dialogMessage = "Challenge timeout expired.";
            this.dialogTitle = "Challenge timeout Expired";
            this.dialogIcon = Settings.TIMEOUT_ICON;
        }
        else if (messageType == MessageType.CHALLENGE_OPPONENT_LOGGED_OUT)
        {
            this.dialogMessage = "User \"" + this.frame.challengePanel.opponent + "\" logged out during challenge.";
            this.dialogTitle = "Opponent left the challenge";
            this.dialogIcon = Settings.LOGOUT_ICON;
        }
        else if (messageType == MessageType.CHALLENGE_REPORT)
        {
            this.dialogMessage = "Challenge completed!";
            this.dialogTitle = "Challenge completed";
            if (this.winStatus >= 0)
                this.dialogIcon = Settings.THUMB_UP_ICON;
            else
                this.dialogIcon = Settings.THUMB_DOWN_ICON;
        }

        String winStatusMessage;
        if (this.winStatus > 0)
            winStatusMessage = "You won!";
        else if (this.winStatus == 0)
            winStatusMessage = "It's a tie!";
        else
            winStatusMessage = "You lost!";


        JOptionPane.showMessageDialog(this.frame, this.dialogMessage + "\n" +
                winStatusMessage + "\n" +
                "You translated " + this.progress + " words out of " + this.frame.challengePanel.challengeWordsQuantity + ".\n" +
                "You gained " + this.scoreGain + " points.", this.dialogTitle, JOptionPane.INFORMATION_MESSAGE, this.dialogIcon);

        // Request fro the new score
        Message scoreRequest = new Message(MessageType.REQUEST_FOR_SCORE_AMOUNT);
        Message scoreResponse = WordQuizzleClient.require(scoreRequest);

        SwingUtilities.invokeLater(() ->
        {
            this.frame.challengePanel.setScore(Integer.parseInt(String.valueOf(scoreResponse.getFields()[0].getBody())));
            this.frame.challengePanel.unemploy();
        });
        return null;
    }
}
