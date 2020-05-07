package client.main;

import client.gui.WordQuizzleClientFrame;
import client.settings.Settings;

import javax.swing.*;
import java.io.IOException;

public class ChallengeViewShortcut
{
    public static void main(String[] args)
    {
        // Load client properties
        try
        {
            Settings.loadProperties();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
        }

        WordQuizzleClientFrame frame = new WordQuizzleClientFrame();
        SwingUtilities.invokeLater(() -> frame.session("Pippo", 10));
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> frame.challengePanel.challenge("PrimaParola", "Ernesto", 10 , 60));
        SwingUtilities.invokeLater(() -> frame.challengePanel.appendTranslationOutcome("PrimaTraduzione", true));
        for (int i = 0; i < 20; i++)
        {
            SwingUtilities.invokeLater(() -> frame.challengePanel.appendTranslationTask("SecondaParola"));
            SwingUtilities.invokeLater(() -> frame.challengePanel.appendTranslationOutcome("SecondaTraduzione", false));
        }

        SwingUtilities.invokeLater(() -> frame.challengePanel.appendWaitMessage());
        return;
    }
}
