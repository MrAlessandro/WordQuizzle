package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import commons.messages.Message;
import commons.messages.MessageType;

import javax.swing.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class LoginOperator extends Operator
{
    String username;
    char[] password;

    public LoginOperator(WordQuizzleClientFrame frame, String username, char[] password)
    {
        super(frame);
        this.username = username;
        this.password = password;
    }

    @Override
    protected Void doInBackground()
    {
        // Prepare login message
        Message logInMessage = new Message(MessageType.LOG_IN);
        logInMessage.addField(this.username.toCharArray());
        logInMessage.addField(this.password);

        try
        {// Add notification channel port to the message
            logInMessage.addField(String.valueOf(((InetSocketAddress) WordQuizzleClient.notificationChannel.getLocalAddress()).getPort()).toCharArray());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Error("Sequence inconsistency");
        }

        // Send login message and get the response
        Message response = WordQuizzleClient.require(logInMessage);

        // Analyze response
        switch (response.getType())
        {
            case OK:
            {
                // Setting the global logged username
                WordQuizzleClient.SESSION_USERNAME.set(this.username);
                SwingUtilities.invokeLater(this.frame::session);
                break;
            }
            case USERNAME_UNKNOWN:
            {
                SwingUtilities.invokeLater(() -> this.frame.logIn("Username unknown"));
                break;
            }
            case PASSWORD_WRONG:
            {
                SwingUtilities.invokeLater(() -> this.frame.logIn("Password wrong"));
                break;
            }
            case USER_ALREADY_LOGGED:
            {
                SwingUtilities.invokeLater(() -> this.frame.logIn("User \"" + this.username + "\" is already logged in."));
                break;
            }
            default:
            {throw new Error("Unexpected message");}
        }

        return null;
    }
}
