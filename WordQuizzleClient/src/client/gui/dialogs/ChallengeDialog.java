package client.gui.dialogs;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;

import javax.swing.*;

public class ChallengeDialog
{
    public static JOptionPane OPTION_PANE;
    public static JDialog CHALLENGE_DIALOG;

    private ChallengeDialog()
    {}

    public static void updateChallengeDialog(String from)
    {
        OPTION_PANE = new JOptionPane("Received challenge request from \"" + from + "\".\nDo you want to accept it?",
                   JOptionPane.INFORMATION_MESSAGE, JOptionPane.YES_NO_OPTION, GuiConstants.CHALLENGE_ICON);

        CHALLENGE_DIALOG = OPTION_PANE.createDialog("Received challenge request");
        CHALLENGE_DIALOG.setModal(true);
        CHALLENGE_DIALOG.setLocationRelativeTo(WordQuizzleClientFrame.FRAME);
    }
}
