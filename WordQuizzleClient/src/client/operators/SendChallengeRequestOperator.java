package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.panels.ChallengePanel;
import client.main.WordQuizzleClient;
import messages.Message;
import messages.MessageType;

import javax.swing.*;

public class SendChallengeRequestOperator implements Runnable
{
    private String opponent;

    public SendChallengeRequestOperator(String opponent)
    {
        this.opponent = opponent;
    }

    @Override
    public void run()
    {
        // Prepare message
        Message message = new Message(MessageType.REQUEST_FOR_CHALLENGE, WordQuizzleClientFrame.username, opponent);

        // Send message and get the response
        Message response = WordQuizzleClient.send(message);

        if (response.getType() == MessageType.OK)
            SwingUtilities.invokeLater(() -> ChallengePanel.waitForOpponentResponse(opponent));
        else if (response.getType() == MessageType.OPPONENT_ALREADY_ENGAGED)
            SwingUtilities.invokeLater(() -> ChallengePanel.requestFailed("\"" + opponent + "\" is already engaged in other challenge."));
        else if (response.getType() == MessageType.OPPONENT_OFFLINE)
            SwingUtilities.invokeLater(() -> ChallengePanel.requestFailed("\"" + opponent + "\" is offline."));
    }
}
