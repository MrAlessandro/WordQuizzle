package client.operators;

import client.gui.panels.ChallengePanel;
import client.main.WordQuizzleClient;
import messages.Message;
import messages.MessageType;

import javax.swing.*;

public class ProvideTranslationOperator implements Runnable
{
    private String from;
    private String to;
    private String translation;

    public ProvideTranslationOperator(String from, String to, String translation)
    {
        this.from = from;
        this.to = to;
        this.translation = translation;
    }

    @Override
    public void run()
    {
        // Prepare message
        Message message = new Message(MessageType.PROVIDE_TRANSLATION, this.from, this.to, this.translation);
        boolean correct;

        // Send message and get response
        Message response = WordQuizzleClient.send(message);
        correct = response.getType() == MessageType.OK;

        SwingUtilities.invokeLater(() -> {
            ChallengePanel.appendTranslation(translation, correct);
            ChallengePanel.SCROLL_CHALLENGE_PANE.getVerticalScrollBar().setValue(ChallengePanel.SCROLL_CHALLENGE_PANE.getVerticalScrollBar().getMaximum());
        });


        if (response.fieldsQuantity() == 3)
        {// Intermediate message
            ChallengePanel.progress++;
            SwingUtilities.invokeLater(() ->{
                ChallengePanel.appendTranslationTask(String.valueOf(response.getField(2)));
                ChallengePanel.SUBMIT_BUTTON.setEnabled(true);
            });
        }
        else
        {// Last message of challenge
            SwingUtilities.invokeLater(ChallengePanel::appendWaitMessage);
        }
    }
}
