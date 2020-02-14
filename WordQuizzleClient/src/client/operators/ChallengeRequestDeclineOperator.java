package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.dialogs.ChallengeDialog;
import client.main.WordQuizzleClient;
import messages.Message;
import messages.MessageType;

public class ChallengeRequestDeclineOperator implements Runnable
{
    private String from;

    public ChallengeRequestDeclineOperator(String from)
    {
        this.from = from;
    }

    @Override
    public void run()
    {
        // Prepare message
        Message message = new Message(MessageType.CHALLENGE_DECLINED, this.from, WordQuizzleClientFrame.username);
        // Send message and get the response
        Message response = WordQuizzleClient.send(message);
        // If message is different from ok ignore it
    }
}
