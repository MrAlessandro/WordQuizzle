package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.dialogs.ChallengeDialog;
import client.main.WordQuizzleClient;
import messages.Message;
import messages.MessageType;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ReplyChallengeRequestOperator implements Runnable
{
    public static final ChallengeDialog CHALLENGE_DIALOG = new ChallengeDialog();
    public static final AtomicInteger CHOICE = new AtomicInteger();
    private String from;

    public ReplyChallengeRequestOperator(String from)
    {
        this.from = from;
    }

    @Override
    public void run()
    {
        CHALLENGE_DIALOG.setMessage("Received challenge request from \"" + this.from +"\"\nDo you want to accept it?");
        CHALLENGE_DIALOG.setVisible(true);
        try
        {
            synchronized (CHOICE)
            {
                CHOICE.wait();
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            throw new Error("Unexpected interruption");
        }

        // Hide the dialog
        CHALLENGE_DIALOG.setVisible(false);

        int chosen = CHOICE.get();
        if (chosen == 0)
        {// Timeout
            JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Challenge request is expired", "Request expired", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (chosen == 1)
        {
            // Confirmed

        }
        else
        {// Declined
            // Prepare message
            Message message = new Message(MessageType.CHALLENGE_DECLINED, this.from, WordQuizzleClientFrame.username);
            // Send message and get the response
            Message response = WordQuizzleClient.send(message);
            // If message is different from ok ignore it
        }

    }
}
