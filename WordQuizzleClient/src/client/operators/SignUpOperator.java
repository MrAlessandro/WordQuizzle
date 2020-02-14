package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
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
            JOptionPane.showMessageDialog(null, "User \"" + this.username + "\" registered!", "User registered", JOptionPane.INFORMATION_MESSAGE, GuiConstants.THUMB_UP_ICON);
            SwingUtilities.invokeLater(WordQuizzleClientFrame::welcome);
        }
        else
            SwingUtilities.invokeLater(() -> WordQuizzleClientFrame.signUp("Username already used"));
    }
}
