package client.gui;

import client.gui.constants.GuiConstants;
import client.operators.LogInOperator;
import messages.Message;

import javax.swing.*;
import java.awt.*;

public class WordQuizzleClientFrame extends JFrame
{
    public volatile Message response;
    public volatile JPlaceholderTextField usernameTextField;
    public volatile JPlaceholderPasswordField passwordField;
    public volatile JLabel warningLabel;

    public WordQuizzleClientFrame()
    {
        super("WordQuizzle");
        this.usernameTextField = new JPlaceholderTextField("Username");
        this.passwordField = new JPlaceholderPasswordField("Password");
        this.warningLabel = new JLabel(" ");
        this.warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.welcomeFrame();
        this.setResizable(false);
        this.setVisible(true);
    }

    private void welcomeFrame()
    {
        this.getContentPane().removeAll();
        JPanel panel = new JPanel();
        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(new FlowLayout());
        this.getContentPane().add(new JLabel(GuiConstants.LOGO), BorderLayout.LINE_START);
        this.getContentPane().add(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        JLabel welcomeLabel = new JLabel("Welcome to WordQuizzle!");
        welcomeLabel.setFont(new Font("", Font.PLAIN, 30));
        welcomeLabel.setForeground(GuiConstants.MAIN_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JButton logInButton = new JButton("LogIn");
        logInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logInButton.addActionListener(e -> SwingUtilities.invokeLater(this::logInProcedure));
        JButton signUpButton = new JButton("SignUp");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.addActionListener(e -> SwingUtilities.invokeLater(this::signUpProcedure));
        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(logInButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(signUpButton);
        this.getContentPane().add(panel);
        this.pack();
    }

    public void logInProcedure()
    {
        this.getContentPane().removeAll();
        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new FlowLayout());
        JButton logInButton = new JButton("LogIn");
        logInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logInButton.addActionListener(e -> SwingUtilities.invokeLater(this::loading));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(this::welcomeFrame));
        panel.add(cancelButton);
        panel.add(logInButton);
        this.getContentPane().add(this.usernameTextField);
        this.getContentPane().add(this.passwordField);
        this.getContentPane().add(this.warningLabel);
        this.getContentPane().add(panel);
        this.pack();
    }

    private void signUpProcedure()
    {
        this.getContentPane().removeAll();
        this.getContentPane().setBackground(Color.WHITE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new FlowLayout());
        JButton signUpButton = new JButton("SignUp");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.addActionListener(e -> SwingUtilities.invokeLater(this::loading));
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(this::welcomeFrame));
        panel.add(cancelButton);
        panel.add(signUpButton);
        this.getContentPane().add(this.usernameTextField);
        this.getContentPane().add(this.passwordField);
        this.getContentPane().add(this.warningLabel);
        this.getContentPane().add(panel);
        this.pack();
    }


    private void loading()
    {
        if (this.usernameTextField.getText().equals(""))
        {
            this.warningLabel.setText("Username can not be void");
            if (this.passwordField.getPassword().length == 0)
                this.warningLabel.setText("Empty fields");
            return;
        }

        if (this.passwordField.getPassword().length == 0)
        {
            this.warningLabel.setText("Password can not be void");
            return;
        }

        this.warningLabel.setText(" ");

        this.getContentPane().removeAll();
        this.getContentPane().setLayout(new FlowLayout());
        this.getContentPane().setBackground(Color.WHITE);
        JLabel loadingGif = new JLabel(GuiConstants.LOADING_GIF);
        this.getContentPane().add(loadingGif);
        LogInOperator operator = new LogInOperator(this);
        operator.execute();
        this.pack();
    }

    public void session()
    {
        this.getContentPane().removeAll();
        this.setSize(500, 500);
    }

}
