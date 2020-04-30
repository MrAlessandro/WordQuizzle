package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;

public class ProvideTranslationOperator extends Operator
{
    private final String translation;
    private final int progress;
    private final int wordQuantity;

    public ProvideTranslationOperator(WordQuizzleClientFrame frame, String translation, int progress, int wordQuantity)
    {
        super(frame);
        this.translation = translation;
        this.progress = progress;
        this.wordQuantity = wordQuantity;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        // Prepare message
        Message message = new Message(MessageType.CHALLENGE_PROVIDE_TRANSLATION, this.translation);
        // Send request and get response
        Message response = WordQuizzleClient.require(message);

        // Set response inside the gui
        frame.challengePanel.appendTranslationOutcome(this.translation, response.getType() == MessageType.TRANSLATION_CORRECT);

        // Retrieve next word if this was not the last step
        if (this.progress < this.wordQuantity)
        {
            // Prepare message
            message = new Message(MessageType.CHALLENGE_GET_WORD);
            // Send request and get the response
            response = WordQuizzleClient.require(message);

            // Extract firs word from response message
            String word = String.valueOf(response.getFields()[0].getBody());

            // Append the new word to challenge playground
            SwingUtilities.invokeLater(() -> frame.challengePanel.appendTranslationTask(word));
        }
        else if (this.progress == this.wordQuantity)
        {
            // Append the wait message to challenge playground
            SwingUtilities.invokeLater(() -> frame.challengePanel.appendWaitMessage());
        }

        return null;
    }
}
