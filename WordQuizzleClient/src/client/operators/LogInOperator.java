package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import messages.Message;

import javax.swing.*;

public class LogInOperator extends SwingWorker<Message, Void>
{
    private WordQuizzleClientFrame frame;
    private String username;
    private char[] password;
    private Message result = null;

    public LogInOperator(WordQuizzleClientFrame frame)
    {
        this.frame = frame;
        this.username = frame.usernameTextField.getText();
        this.password = frame.passwordField.getPassword();
    }

    @Override
    protected Message doInBackground() throws Exception
    {
        this.result = WordQuizzleClient.logIn(this.username, this.password);

        return this.result;
    }

    @Override
    protected void done()
    {
        Message message = this.result;

        switch (message.getType())
        {
            case OK:
            {
                SwingUtilities.invokeLater(() -> frame.session());
                break;
            }
            case USERNAME_UNKNOWN:
            {
                frame.warningLabel.setText("Username unknown");
                SwingUtilities.invokeLater(() -> frame.logInProcedure());
                break;
            }
            case PASSWORD_WRONG:
            {
                frame.warningLabel.setText("Password wrong");
                SwingUtilities.invokeLater(() -> frame.logInProcedure());
                break;
            }
            default:
            {throw new Error("Unexpected message");}
        }
    }
}
