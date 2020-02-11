package client.gui.panels;

import client.gui.constants.GuiConstants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class ChallengePanel extends JPanel
{
    public ChallengePanel()
    {
        super();

        // Initializing components
        JLabel stopWatchLabel = new JLabel(GuiConstants.STOPWATCH_SYMBOL);
        JPanel labelContainer = new JPanel();
        JLabel placeHolderLabel1 = new JLabel("No challenge in progress.");
        JLabel placeHolderLabel2 = new JLabel("Select a friend and challenge him within the left panel.");

        // Setup outer panel
        this.setBackground(Color.WHITE);
        this.setLayout(new BorderLayout());
        this.setBorder(new CompoundBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1), new EmptyBorder(10,10,10,10)));
        this.setMinimumSize(new Dimension(500, 400));
        this.setPreferredSize(new Dimension(500, 400));

        // Setup label container
        labelContainer.setBackground(Color.WHITE);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.PAGE_AXIS));

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
        this.add(labelContainer, BorderLayout.CENTER);
    }
}
