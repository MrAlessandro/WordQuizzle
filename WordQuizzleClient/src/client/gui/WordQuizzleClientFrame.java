package client.gui;

import client.gui.constants.GuiConstants;
import client.main.WordQuizzleClient;
import client.operators.LogInOperator;
import client.operators.SendFriendshipRequestOperator;
import client.operators.SignUpOperator;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class WordQuizzleClientFrame extends JFrame
{
    public static final WordQuizzleClientFrame FRAME = new WordQuizzleClientFrame();
    public static final DefaultListModel<String> FRIENDS_LIST = new DefaultListModel<>();
    public static String username = null;

    private WordQuizzleClientFrame()
    {
        super("WordQuizzle");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setResizable(false);

        // Locate in the center of the screen
        this.setLocationRelativeTo(null);
    }

    public static void welcomeFrame()
    {
        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setBackground(Color.WHITE);
        FRAME.getContentPane().setLayout(new FlowLayout());

        // Setup Logo
        JLabel logo = new JLabel(GuiConstants.LOGO);

        // Setup inner container
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Setup welcome message
        JLabel welcomeLabel = new JLabel("Welcome to WordQuizzle!");
        welcomeLabel.setFont(new Font("", Font.PLAIN, 30));
        welcomeLabel.setForeground(GuiConstants.MAIN_COLOR);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup login button
        JButton logInButton = new JButton("LogIn");
        logInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logInButton.addActionListener(e -> SwingUtilities.invokeLater(() -> logInProcedure(null)));

        // Setup signup button
        JButton signUpButton = new JButton("SignUp");
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.addActionListener(e -> SwingUtilities.invokeLater(() -> signUpProcedure(null)));

        // Include inner components (with borders) to the inner container
        panel.add(welcomeLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(logInButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(signUpButton);

        // Include all components to the outer container
        FRAME.getContentPane().add(logo, BorderLayout.LINE_START);
        FRAME.getContentPane().add(panel);

        // Resize accordingly
        FRAME.pack();
        // Make the frame visible
        FRAME.setVisible(true);
    }

    public static void logInProcedure(String warningMessage)
    {
        // Initialize components
        JPlaceholderTextField usernameField = new JPlaceholderTextField("Username");
        JPlaceholderPasswordField passwordField = new JPlaceholderPasswordField("Password");
        JLabel warningLabel = new JLabel();
        JPanel panel = new JPanel();
        JButton logInButton = new JButton("LogIn");
        JButton cancelButton = new JButton("Cancel");

        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setBackground(Color.WHITE);
        FRAME.getContentPane().setLayout(new BoxLayout(FRAME.getContentPane(), BoxLayout.Y_AXIS));

        DocumentListener fieldsChecker = new DocumentListener() {
            private Border defaultBorders = usernameField.getBorder();

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }

            private void check()
            {
                boolean checkUsernameField = usernameField.getText().equals("");
                boolean checkPasswordField = passwordField.getPassword().length == 0;
                if (checkUsernameField || checkPasswordField)
                {
                    if (checkUsernameField && checkPasswordField)
                    {
                        usernameField.setBorder(new LineBorder(Color.RED, 1));
                        passwordField.setBorder(new LineBorder(Color.RED, 1));
                        warningLabel.setText("Empty fields");
                    }
                    else if (checkUsernameField)
                    {
                        usernameField.setBorder(new LineBorder(Color.RED, 1));
                        warningLabel.setText("Empty username");
                    }
                    else
                    {
                        passwordField.setBorder(new LineBorder(Color.RED, 1));
                        warningLabel.setText("Empty password");
                    }

                    logInButton.setEnabled(false);
                }
                else
                {
                    usernameField.setBorder(defaultBorders);
                    passwordField.setBorder(defaultBorders);
                    warningLabel.setText(" ");
                    logInButton.setEnabled(true);
                }
            }

        };

        // Setup username field
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.getDocument().addDocumentListener(fieldsChecker);

        // Setup password field
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.getDocument().addDocumentListener(fieldsChecker);

        // Setup warning label
        warningLabel.setForeground(Color.RED);
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (warningMessage == null)
            warningLabel.setText(" ");
        else
            warningLabel.setText(warningMessage);

        // Setup inner container
        panel.setBackground(Color.WHITE);
        panel.setLayout(new FlowLayout());

        // Setup login button
        logInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        logInButton.addActionListener(e -> {
            SwingUtilities.invokeLater(WordQuizzleClientFrame::loading);
            WordQuizzleClient.POOL.execute(new LogInOperator(usernameField.getText(), passwordField.getPassword()));
        });

        // Setup cancel button
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(WordQuizzleClientFrame::welcomeFrame));

        // Include inner components (with borders) to the inner container
        panel.add(logInButton);
        panel.add(cancelButton);

        // Include all components to the outer container
        FRAME.getContentPane().add(usernameField);
        FRAME.getContentPane().add(passwordField);
        FRAME.getContentPane().add(warningLabel);
        FRAME.getContentPane().add(panel);

        // Resize accordingly
        FRAME.pack();
    }

    public static void signUpProcedure(String warningMessage)
    {
        // Initialize components
        JPlaceholderTextField usernameField = new JPlaceholderTextField("Username");
        JPlaceholderPasswordField passwordField = new JPlaceholderPasswordField("Password");
        JLabel warningLabel = new JLabel();
        JPanel panel = new JPanel();
        JButton signUpButton = new JButton("SignUp");
        JButton cancelButton = new JButton("Cancel");

        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setBackground(Color.WHITE);
        FRAME.getContentPane().setLayout(new BoxLayout(FRAME.getContentPane(), BoxLayout.Y_AXIS));

        DocumentListener fieldsChecker = new DocumentListener() {
            private Border defaultBorders = usernameField.getBorder();

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }

            private void check()
            {
                boolean checkUsernameField = usernameField.getText().equals("");
                boolean checkPasswordField = passwordField.getPassword().length == 0;
                if (checkUsernameField || checkPasswordField)
                {
                    if (checkUsernameField && checkPasswordField)
                    {
                        usernameField.setBorder(new LineBorder(Color.RED, 1));
                        passwordField.setBorder(new LineBorder(Color.RED, 1));
                        warningLabel.setText("Empty fields");
                    }
                    else if (checkUsernameField)
                    {
                        usernameField.setBorder(new LineBorder(Color.RED, 1));
                        warningLabel.setText("Empty username");
                    }
                    else
                    {
                        passwordField.setBorder(new LineBorder(Color.RED, 1));
                        warningLabel.setText("Empty password");
                    }

                    signUpButton.setEnabled(false);
                }
                else
                {
                    usernameField.setBorder(defaultBorders);
                    passwordField.setBorder(defaultBorders);
                    warningLabel.setText(" ");
                    signUpButton.setEnabled(true);
                }
            }

        };

        // Setup username field
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.getDocument().addDocumentListener(fieldsChecker);

        // Setup password field
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        passwordField.getDocument().addDocumentListener(fieldsChecker);

        // Setup warning label
        warningLabel.setForeground(Color.RED);
        warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (warningMessage == null)
            warningLabel.setText(" ");
        else
            warningLabel.setText(warningMessage);

        // Setup inner container
        panel.setBackground(Color.WHITE);
        panel.setLayout(new FlowLayout());

        // Setup signup button
        signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        signUpButton.addActionListener(e -> {
            SwingUtilities.invokeLater(WordQuizzleClientFrame::loading);
            WordQuizzleClient.POOL.execute(new SignUpOperator(usernameField.getText(), passwordField.getPassword()));
        });

        // Setup cancel button
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(WordQuizzleClientFrame::welcomeFrame));

        // Include inner components (with borders) to the inner container
        panel.add(signUpButton);
        panel.add(cancelButton);

        // Include all components to the outer container
        FRAME.getContentPane().add(usernameField);
        FRAME.getContentPane().add(passwordField);
        FRAME.getContentPane().add(warningLabel);
        FRAME.getContentPane().add(panel);

        // Resize accordingly
        FRAME.pack();
    }

    private static void loading()
    {
        // Initialize components
        JLabel loadingGif = new JLabel(GuiConstants.LOADING_GIF);

        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setBackground(Color.WHITE);
        FRAME.getContentPane().setLayout(new FlowLayout());

        // Setup loading animation
        FRAME.getContentPane().add(loadingGif);

        // Resize accordingly
        FRAME.pack();
    }

    public static void session()
    {
        // Initialize components
        JPanel friendsPanel = new JPanel();
        JPanel friendsPanelHeader = new JPanel();
        JLabel friendListLabel = new JLabel("Friends list:");
        JButton addFriendButton = new JButton("+");
        JList<String> jList = new JList<>(FRIENDS_LIST);
        JScrollPane friendsScrollPane = new JScrollPane(jList);
        JButton challengeButton = new JButton();

        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setBackground(Color.WHITE);
        FRAME.getContentPane().setLayout(new BorderLayout());

        // Setup friends panel
        friendsPanel.setLayout(new BoxLayout(friendsPanel, BoxLayout.Y_AXIS));
        friendsPanel.setBackground(Color.WHITE);
        friendsPanel.setBorder(new CompoundBorder(new LineBorder(GuiConstants.BACKGROUND_COLOR, 1), new EmptyBorder(10,10,10,10)));

        // Setup friends panel header
        friendsPanelHeader.setBackground(Color.WHITE);
        friendsPanelHeader.setLayout(new BorderLayout());
        friendsPanelHeader.setPreferredSize(new Dimension(160, 25));
        friendsPanelHeader.setMaximumSize(new Dimension(160, 25));

        // Setup add friend button
        addFriendButton.setPreferredSize(new Dimension(20, 20));
        addFriendButton.addActionListener(e -> WordQuizzleClient.POOL.execute(new SendFriendshipRequestOperator()));

        // Setup friend list
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.addListSelectionListener(e -> challengeButton.setEnabled(true));

        // Setup friends scroll pane
        friendsScrollPane.setPreferredSize(new Dimension(160, 220));
        friendsScrollPane.setMaximumSize(new Dimension(160, 220));

        // Setup challenge button
        challengeButton.setText("Challenge");
        challengeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        challengeButton.setEnabled(false);

        // Add components to friends panel header
        friendsPanelHeader.add(friendListLabel, BorderLayout.WEST);
        friendsPanelHeader.add(addFriendButton, BorderLayout.EAST);

        // Add components (with margins) to friends panel
        friendsPanel.add(friendsPanelHeader);
        friendsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        friendsPanel.add(friendsScrollPane);
        friendsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        friendsPanel.add(challengeButton);

        // Add all components to outer container
        FRAME.getContentPane().add(friendsPanel, BorderLayout.WEST);

        // Resize accordingly
        FRAME.pack();
    }

}
