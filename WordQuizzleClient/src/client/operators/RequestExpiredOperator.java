package client.operators;

import client.gui.WordQuizzleClientFrame;

import javax.swing.*;

public class RequestExpiredOperator implements Runnable
{
    @Override
    public void run()
    {
        JOptionPane.showMessageDialog(WordQuizzleClientFrame.FRAME, "Challenge request is expired", "Request expired", JOptionPane.INFORMATION_MESSAGE);
    }
}
