package client.gui.panels;

import client.gui.constants.GuiConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ChallengePanel extends JPanel
{
    public static final ChallengePanel PANEL = new ChallengePanel();
    public static boolean challengeable = false;

    private ChallengePanel()
    {
        super();

        // Initialize Challenge panel
        this.setBackground(GuiConstants.BACKGROUND_COLOR);
        this.setLayout(new BorderLayout());
        this.setBorder(new CompoundBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1), new EmptyBorder(10,10,10,10)));
        this.setMinimumSize(new Dimension(600, 400));
        this.setPreferredSize(new Dimension(600, 400));
    }

    public static void waitForChallengeRequest()
    {
        // Restore challenge flag
        challengeable = true;

        // Empty Challenge panel
        PANEL.removeAll();

        // Initializing components
        JPanel labelContainer = new JPanel();
        JLabel placeHolderLabel1 = new JLabel("No challenge in progress.");
        JLabel placeHolderLabel2 = new JLabel("Select a friend and challenge him using the left panel.");

        // Setup label container
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.Y_AXIS));

        // Setup placeholder labels
        placeHolderLabel1.setFont(new Font("", Font.PLAIN, 20));
        placeHolderLabel2.setFont(new Font("", Font.PLAIN, 15));
        placeHolderLabel1.setForeground(GuiConstants.MAIN_COLOR);
        placeHolderLabel2.setForeground(GuiConstants.MAIN_COLOR);
        placeHolderLabel1.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeHolderLabel1.setAlignmentY(Component.CENTER_ALIGNMENT);
        placeHolderLabel2.setAlignmentX(Component.CENTER_ALIGNMENT);
        placeHolderLabel2.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Adding labels to labels container
        labelContainer.add(Box.createRigidArea(new Dimension(0, 140)));
        labelContainer.add(placeHolderLabel1);
        labelContainer.add(placeHolderLabel2);

        // Add all to outer container
        PANEL.add(labelContainer, BorderLayout.CENTER);

        // Repaint
        PANEL.revalidate();
    }

    public static void waitForOpponentResponse(String opponent)
    {
        // Initializing components
        JPanel labelContainer = new JPanel();
        JLabel waitingLabel = new JLabel("Waiting for response from \"" + opponent + "\"...");
        JLabel stopwatchLabel = new JLabel(GuiConstants.STOPWATCH_SYMBOL);

        // Set the waiting flag
        challengeable = false;

        // Empty outer container
        PANEL.removeAll();

        // Disable challenge button
        FriendsPanel.challengeButton.setEnabled(false);

        // Setup label container
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.Y_AXIS));

        // Initialize waiting label
        waitingLabel.setForeground(GuiConstants.MAIN_COLOR);
        waitingLabel.setFont(new Font("", Font.PLAIN, 20));
        waitingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        waitingLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Initializing stopwatch label
        stopwatchLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        stopwatchLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Adding labels to labels container
        labelContainer.add(Box.createRigidArea(new Dimension(0, 140)));
        labelContainer.add(waitingLabel);
        labelContainer.add(stopwatchLabel);

        // Add all to outer container
        PANEL.add(labelContainer, BorderLayout.CENTER);

        // Repaint
        PANEL.revalidate();
    }

    public static void requestFailed(String message)
    {
        // Initializing components
        JPanel labelContainer = new JPanel();
        JLabel messageLabel = new JLabel(message);

        // Empty outer container
        PANEL.removeAll();

        // Setup label container
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.Y_AXIS));

        // Initialize message label
        messageLabel.setForeground(GuiConstants.MAIN_COLOR);
        messageLabel.setFont(new Font("", Font.PLAIN, 20));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        messageLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Adding labels to labels container
        labelContainer.add(Box.createRigidArea(new Dimension(0, 140)));
        labelContainer.add(messageLabel);

        // Add all to outer container
        PANEL.add(labelContainer, BorderLayout.CENTER);

        // Repaint
        PANEL.revalidate();
    }
}
