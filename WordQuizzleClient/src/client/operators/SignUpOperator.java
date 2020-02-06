package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;

import javax.swing.*;

public class SignUpOperator extends SwingWorker <Boolean, Void>
{
    private WordQuizzleClientFrame frame;
    private String username;
    private char[] password;
    private boolean result;

    public SignUpOperator(WordQuizzleClientFrame frame)
    {
        this.frame = frame;
        this.username = frame.usernameTextField.getText();
        this.password = frame.passwordField.getPassword();
    }

    @Override
    protected Boolean doInBackground() throws Exception
    {
        this.result = WordQuizzleClient.register(this.username, this.password);
        return this.result;
    }

    @Override
    protected void done()
    {
        if (this.result)
            SwingUtilities.invokeLater(() -> frame.session());
        else
        {
            this.frame.warningLabel.setText("Username already used");
            SwingUtilities.invokeLater(() -> frame.signUpProcedure());
        }
    }
}
