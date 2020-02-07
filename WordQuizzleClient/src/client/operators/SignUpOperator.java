package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;

import javax.swing.*;

public class SignUpOperator implements Runnable
{
    private String username;
    private char[] password;

    public SignUpOperator(String username, char[] password)
    {
        this.username = username;
        this.password = password;
    }

    @Override
    public void run()
    {
        boolean result = WordQuizzleClient.register(this.username, this.password);

        if (result)
        {
            JOptionPane.showMessageDialog(null, "User \"" + this.username + "\" registered!", "User registered", JOptionPane.INFORMATION_MESSAGE);
            SwingUtilities.invokeLater(WordQuizzleClientFrame::welcomeFrame);
        }
        else
            SwingUtilities.invokeLater(() -> WordQuizzleClientFrame.signUpProcedure("Username already used"));
    }
}
