package client.operators;

import client.constants.ClientConstants;
import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
import client.gui.dialogs.ChallengeDialog;
import client.gui.panels.ChallengePanel;
import client.gui.panels.ScoresPanel;
import client.main.WordQuizzleClient;
import messages.Message;
import messages.MessageType;

import javax.swing.*;

public class ReplyChallengeRequestOperator implements Runnable
{
    private String from;

    public ReplyChallengeRequestOperator(String from)
    {
        this.from = from;
    }

    @Override
    public void run()
    {
        ChallengeDialog.updateChallengeDialog(from);
        ChallengeDialog.CHALLENGE_DIALOG.setVisible(true);

        Integer choice = (Integer) ChallengeDialog.OPTION_PANE.getValue();
        switch (choice)
        {
            case JOptionPane.YES_OPTION:
            {
                // Show loading message on challenge panel
                SwingUtilities.invokeLater(ChallengePanel::loading);

                // Prepare confirmation message
                Message message = new Message(MessageType.CONFIRM_CHALLENGE, this.from, WordQuizzleClientFrame.username);

                // Send confirmation and get response
                Message response = WordQuizzleClient.send(message);

                if (response.getType() == MessageType.OK)
                {
                    int challengeTimeOut = Integer.parseInt(String.valueOf(response.getField(2)));
                    int challengeWordQuantity = Integer.parseInt(String.valueOf(response.getField(3)));
                    String firstWord = String.valueOf(response.getField(4));
                    ChallengePanel.applicant = this.from;
                    ChallengePanel.opponent = String.valueOf(response.getField(1));

                    SwingUtilities.invokeLater(() -> ChallengePanel.challenge(challengeTimeOut, challengeWordQuantity, firstWord));
                    ScoresPanel.launchTimer(challengeTimeOut);
                }
                break;
            }
            case JOptionPane.NO_OPTION:
            {
                // Prepare message
                Message message = new Message(MessageType.DECLINE_CHALLENGE, this.from, WordQuizzleClientFrame.username);
                // Send message and get the response
                Message response = WordQuizzleClient.send(message);
                // If message is different from ok ignore it
                break;
            }
            case ClientConstants.TIMER_EXPIRED_NOTIFICATION_VALUE:
            {
                ChallengeDialog.CHALLENGE_DIALOG.setVisible(false);
                JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Challenge request is expired", "Request expired", JOptionPane.INFORMATION_MESSAGE, GuiConstants.WARNING_ICON);
            }
            case ClientConstants.APPLICANT_WENT_OFFLINE_NOTIFICATION_VALUE:
            {
                SwingUtilities.invokeLater(() -> ChallengeDialog.CHALLENGE_DIALOG.setVisible(false));
                JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Applicant user \"" + this.from + "\" logged out in the meantime", "Applicant logged out", JOptionPane.INFORMATION_MESSAGE, GuiConstants.WARNING_ICON);
            }
        }
    }
}
