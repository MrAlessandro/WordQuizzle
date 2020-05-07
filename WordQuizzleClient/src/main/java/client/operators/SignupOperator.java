package client.operators;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.settings.Settings;
import commons.remote.exceptions.UsernameAlreadyUsedException;
import commons.remote.exceptions.VoidPasswordException;
import commons.remote.exceptions.VoidUsernameException;

import javax.swing.*;

public class SignupOperator extends Operator
{
    String username;
    char[] password;

    public SignupOperator(WordQuizzleClientFrame frame, String username, char[] password)
    {
        super(frame);
        this.username = username;
        this.password = password;
    }

    @Override
    protected Void doInBackground()
    {
        // Call register method through RMI
        try
        {
            WordQuizzleClient.register(this.username, this.password);
            SwingUtilities.invokeLater(this.frame::welcome);
            JOptionPane.showMessageDialog(this.frame, "User \"" + this.username + "\" correctly registered!", "User registered", JOptionPane.INFORMATION_MESSAGE, Settings.THUMB_UP_ICON);
        }
        catch (VoidUsernameException e)
        {
            SwingUtilities.invokeLater(() -> this.frame.signUp("Void username"));
        }
        catch (VoidPasswordException e)
        {
            SwingUtilities.invokeLater(() -> this.frame.signUp("Void password"));
        }
        catch (UsernameAlreadyUsedException e)
        {
            SwingUtilities.invokeLater(() -> this.frame.signUp("Username already used. Choice another one!"));
        }

        return null;
    }
}
