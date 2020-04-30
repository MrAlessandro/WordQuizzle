package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.settings.Settings;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;
import java.awt.*;

public class ReplyChallengeRequestOperator extends Operator
{
    public static JOptionPane optionPane;
    private final String applicant;

    public ReplyChallengeRequestOperator(WordQuizzleClientFrame frame, String applicant)
    {
        super(frame);
        this.applicant = applicant;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        optionPane = new JOptionPane("Received challenge request from \"" + applicant + "\".\nDo you want to accept it?",
                JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION, Settings.CHALLENGE_ICON);
        JDialog dialog = optionPane.createDialog("Received challenge request");
        dialog.setLocationRelativeTo(this.frame);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setModal(true);
        dialog.setVisible(true);

        int result = (int) optionPane.getValue();
        dialog.setVisible(false);
        switch (result)
        {
            case JOptionPane.YES_OPTION:
            {
                // Prepare message
                Message message = new Message(MessageType.CONFIRM_CHALLENGE_REQUEST, this.applicant);
                // Send confirmation message
                Message response = WordQuizzleClient.require(message);
                if (response.getType() != MessageType.OK)
                    throw new Error("COMMUNICATION INCONSISTENCY");
                // Extract challenge data
                String opponent = String.valueOf(response.getFields()[0].getBody());
                int wordsQuantity = Integer.parseInt(String.valueOf(response.getFields()[2].getBody()));
                int challengeDuration = Integer.parseInt(String.valueOf(response.getFields()[1].getBody()));

                // Rend request for first word
                message = new Message(MessageType.CHALLENGE_GET_WORD);
                response = WordQuizzleClient.require(message);
                if (response.getType() != MessageType.OK)
                    throw new Error("COMMUNICATION INCONSISTENCY");

                // Extract firs word from response message
                String firstWord = String.valueOf(response.getFields()[0].getBody());

                // Set gui for challenge
                SwingUtilities.invokeLater(() -> frame.challengePanel.challenge(firstWord, opponent, wordsQuantity, challengeDuration));

                break;
            }
            case JOptionPane.NO_OPTION:
            {
                // Prepare message
                Message message = new Message(MessageType.DECLINE_CHALLENGE_REQUEST, this.applicant);
                // Send declination message
                WordQuizzleClient.require(message);
                break;
            }
            case Settings.CHALLENGE_REQUEST_TIMER_EXPIRED_NOTIFICATION_VALUE:
            {
                JOptionPane.showMessageDialog(this.frame, "Challenge request is expired", "Request expired",
                        JOptionPane.INFORMATION_MESSAGE, Settings.TIMEOUT_ICON);
                break;
            }
            case Settings.CHALLENGE_REQUEST_APPLICANT_OFFLINE_NOTIFICATION_VALUE:
            {
                JOptionPane.showMessageDialog(this.frame, "Applicant user \"" + this.applicant +
                                "\" logged out in the meantime", "Applicant logged out",
                        JOptionPane.INFORMATION_MESSAGE, Settings.LOGOUT_ICON);
                break;
            }
        }

        return null;
    }
}
