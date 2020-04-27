package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.settings.Settings;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;

public class SendChallengeRequestOperator extends Operator
{
    private final String opponent;

    public SendChallengeRequestOperator(WordQuizzleClientFrame frame, String opponent)
    {
        super(frame);
        this.opponent = opponent;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        // Prepare message
        Message message = new Message(MessageType.REQUEST_FOR_CHALLENGE, opponent);
        // Send the request and get the response
        Message response = WordQuizzleClient.require(message);

        // Analyze response type
        switch (response.getType())
        {
            case OK:
                // Wait for challenge reply
                break;
            case RECEIVER_ENGAGED_IN_OTHER_CHALLENGE:
                this.frame.challengePanel.unemploy();
                JOptionPane.showMessageDialog(this.frame, "User \"" + opponent +"\" is actually engaged in other challenge", "Opponent engaged", JOptionPane.ERROR_MESSAGE, Settings.THUMB_DOWN_ICON);
                break;
            case RECEIVER_ENGAGED_IN_OTHER_CHALLENGE_REQUEST:
                this.frame.challengePanel.unemploy();
                JOptionPane.showMessageDialog(this.frame, "User \"" + opponent +"\" is actually engaged in other challenge request", "Opponent engaged", JOptionPane.ERROR_MESSAGE, Settings.THUMB_DOWN_ICON);
                break;
            case RECEIVER_OFFLINE:
                this.frame.challengePanel.unemploy();
                JOptionPane.showMessageDialog(this.frame, "User \"" + opponent +"\" is actually offline", "Opponent offline", JOptionPane.ERROR_MESSAGE, Settings.LOGOUT_ICON);
                break;
            default:
                this.frame.challengePanel.unemploy();
                JOptionPane.showMessageDialog(this.frame, "Something went wrong", "RequestError", JOptionPane.ERROR_MESSAGE, Settings.WARNING_ICON);
                break;
        }

        return null;
    }
}
