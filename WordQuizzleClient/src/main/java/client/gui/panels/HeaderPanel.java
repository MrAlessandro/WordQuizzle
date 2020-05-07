package client.gui.panels;

import client.settings.Settings;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class HeaderPanel extends JPanel
{
    private String username;
    private int score;
    private String timer;

    private JLabel greetingLabel;
    private JLabel timerLabel;
    private JLabel scoreLabel;

    public HeaderPanel()
    {
        super();

        // Set panel's data
        this.username = "";
        this.score = 0;
        this.timer = "";

        //Setup labels
        this.greetingLabel = new JLabel("");
        this.greetingLabel.setForeground(Settings.MAIN_COLOR);
        this.greetingLabel.setFont(new Font("", Font.PLAIN, 20));
        this.greetingLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.greetingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.timerLabel = new JLabel(this.timer);
        this.timerLabel.setForeground(Settings.MAIN_COLOR);
        this.timerLabel.setFont(new Font("", Font.PLAIN, 20));
        this.timerLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.scoreLabel = new JLabel("");
        this.scoreLabel.setForeground(Settings.MAIN_COLOR);
        this.scoreLabel.setFont(new Font("", Font.PLAIN, 20));
        this.scoreLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup panel
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(new LineBorder(Settings.MAIN_COLOR, 1));
        this.setBackground(Settings.BACKGROUND_COLOR);

        // Add labels to this panel
        this.add(this.greetingLabel);
        this.add(Box.createHorizontalGlue());
        this.add(this.timerLabel);
        this.add(Box.createHorizontalGlue());
        this.add(this.scoreLabel);
    }

    public void setUsername(String username)
    {
        this.username = username;
        this.greetingLabel.setText("Welcome " + username + "!");
        this.greetingLabel.revalidate();
        this.greetingLabel.repaint();
    }

    public void setScore(int score)
    {
        this.score = score;
        this.scoreLabel.setText("Your score is: " + this.score);
        this.scoreLabel.revalidate();
        this.scoreLabel.repaint();
    }
}
