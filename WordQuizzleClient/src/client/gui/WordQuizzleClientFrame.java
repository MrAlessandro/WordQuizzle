package client.gui;

import client.gui.constants.GuiConstants;
import client.gui.fields.JPlaceholderPasswordField;
import client.gui.fields.JPlaceholderTextField;
import client.gui.panels.ChallengePanel;
import client.gui.panels.FriendsPanel;
import client.gui.panels.ScoresPanel;
import client.main.WordQuizzleClient;
import client.operators.LogInOperator;
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
    public static final JLabel LOGO_LABEL = new JLabel(GuiConstants.LOGO);
    public static final JLabel WELCOME_MESSAGE_LABEL = new JLabel("Welcome to WordQuizzle!");
    public static final JLabel WARNING_LABEL = new JLabel();
    public static final JLabel LOADING_ICON_LABEL = new JLabel(GuiConstants.LOADING_GIF);
    public static final JButton LOG_IN_BUTTON = new JButton("LogIn");
    public static final JButton SIGN_UP_BUTTON = new JButton("SignUp");
    public static final JButton EXECUTE_LOGIN_BUTTON = new JButton("LogIn");
    public static final JButton EXECUTE_SIGNUP_BUTTON = new JButton("SignUp");
    public static final JButton CANCEL_BUTTON = new JButton("Cancel");
    public static final JPlaceholderTextField USERNAME_TEXT_FIELD = new JPlaceholderTextField("Username");
    public static final JPlaceholderPasswordField PASSWORD_TEXT_FIELD = new JPlaceholderPasswordField("Password");
    public static final Border NORMAL_BORDERS = new CompoundBorder(new LineBorder(GuiConstants.BACKGROUND_COLOR, 3), new LineBorder(GuiConstants.MAIN_COLOR, 1));
    public static final Border ERROR_BORDER = new CompoundBorder(new LineBorder(GuiConstants.BACKGROUND_COLOR, 3), new LineBorder(Color.RED, 1));
    public static final DocumentListener LOGIN_FIELDS_CHECKER = new DocumentListener() {
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
            boolean checkUsernameField = USERNAME_TEXT_FIELD.getText().equals("");
            boolean checkPasswordField = PASSWORD_TEXT_FIELD.getPassword().length == 0;
            if (checkUsernameField || checkPasswordField)
            {
                if (checkUsernameField && checkPasswordField)
                {
                    USERNAME_TEXT_FIELD.setBorder(ERROR_BORDER);
                    PASSWORD_TEXT_FIELD.setBorder(ERROR_BORDER);
                    WARNING_LABEL.setText("Empty fields");
                }
                else if (checkUsernameField)
                {
                    USERNAME_TEXT_FIELD.setBorder(ERROR_BORDER);
                    WARNING_LABEL.setText("Empty username");
                }
                else
                {
                    PASSWORD_TEXT_FIELD.setBorder(ERROR_BORDER);
                    WARNING_LABEL.setText("Empty password");
                }

                EXECUTE_LOGIN_BUTTON.setEnabled(false);
            }
            else
            {
                USERNAME_TEXT_FIELD.setBorder(NORMAL_BORDERS);
                PASSWORD_TEXT_FIELD.setBorder(NORMAL_BORDERS);
                WARNING_LABEL.setText(" ");
                EXECUTE_LOGIN_BUTTON.setEnabled(true);
            }
        }

    };
    public static final DocumentListener SIGNUP_FIELDS_CHECKER = new DocumentListener() {
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
            boolean checkUsernameField = USERNAME_TEXT_FIELD.getText().equals("");
            boolean checkPasswordField = PASSWORD_TEXT_FIELD.getPassword().length == 0;
            if (checkUsernameField || checkPasswordField)
            {
                if (checkUsernameField && checkPasswordField)
                {
                    USERNAME_TEXT_FIELD.setBorder(ERROR_BORDER);
                    PASSWORD_TEXT_FIELD.setBorder(ERROR_BORDER);
                    WARNING_LABEL.setText("Empty fields");
                }
                else if (checkUsernameField)
                {
                    USERNAME_TEXT_FIELD.setBorder(ERROR_BORDER);
                    WARNING_LABEL.setText("Empty username");
                }
                else
                {
                    PASSWORD_TEXT_FIELD.setBorder(ERROR_BORDER);
                    WARNING_LABEL.setText("Empty password");
                }

                EXECUTE_SIGNUP_BUTTON.setEnabled(false);
            }
            else
            {
                USERNAME_TEXT_FIELD.setBorder(NORMAL_BORDERS);
                PASSWORD_TEXT_FIELD.setBorder(NORMAL_BORDERS);
                WARNING_LABEL.setText(" ");
                EXECUTE_SIGNUP_BUTTON.setEnabled(true);
            }
        }

    };
    public static final WordQuizzleClientFrame FRAME = new WordQuizzleClientFrame();
    public static String username = null;

    //

    private WordQuizzleClientFrame()
    {
        super("WordQuizzle");

        // Initialize frame
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(GuiConstants.BACKGROUND_COLOR);
        this.setResizable(false);
        this.setLocationRelativeTo(null);

        // Setup labels
        WELCOME_MESSAGE_LABEL.setFont(new Font("", Font.PLAIN, 30));
        WELCOME_MESSAGE_LABEL.setForeground(GuiConstants.MAIN_COLOR);
        WELCOME_MESSAGE_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);

        WARNING_LABEL.setForeground(Color.RED);
        WARNING_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);
        WARNING_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup fields
        USERNAME_TEXT_FIELD.setAlignmentX(Component.CENTER_ALIGNMENT);


        PASSWORD_TEXT_FIELD.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup buttons
        LOG_IN_BUTTON.setAlignmentX(Component.CENTER_ALIGNMENT);
        LOG_IN_BUTTON.addActionListener(e -> SwingUtilities.invokeLater(() -> logIn(null)));

        SIGN_UP_BUTTON.setAlignmentX(Component.CENTER_ALIGNMENT);
        SIGN_UP_BUTTON.addActionListener(e -> SwingUtilities.invokeLater(() -> signUp(null)));

        EXECUTE_LOGIN_BUTTON.setAlignmentX(Component.CENTER_ALIGNMENT);
        EXECUTE_LOGIN_BUTTON.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                WordQuizzleClientFrame.loading();
                USERNAME_TEXT_FIELD.setText("");
                PASSWORD_TEXT_FIELD.setText("");
            });
            WordQuizzleClient.POOL.execute(new LogInOperator(USERNAME_TEXT_FIELD.getText(), PASSWORD_TEXT_FIELD.getPassword()));
        });

        EXECUTE_SIGNUP_BUTTON.setAlignmentX(Component.CENTER_ALIGNMENT);
        EXECUTE_SIGNUP_BUTTON.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                WordQuizzleClientFrame.loading();
                USERNAME_TEXT_FIELD.setText("");
                PASSWORD_TEXT_FIELD.setText("");
            });
            WordQuizzleClient.POOL.execute(new SignUpOperator(USERNAME_TEXT_FIELD.getText(), PASSWORD_TEXT_FIELD.getPassword()));
        });

        CANCEL_BUTTON.setAlignmentX(Component.CENTER_ALIGNMENT);
        CANCEL_BUTTON.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            WordQuizzleClientFrame.welcome();
            USERNAME_TEXT_FIELD.setText("");
            PASSWORD_TEXT_FIELD.setText("");
        }));
    }

    public static void welcome()
    {
        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setLayout(new FlowLayout());

        // Setup inner container
        JPanel panel = new JPanel();
        panel.setBackground(GuiConstants.BACKGROUND_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


        // Include inner components (with borders) to the inner container
        panel.add(WELCOME_MESSAGE_LABEL);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(LOG_IN_BUTTON);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(SIGN_UP_BUTTON);

        // Include all components to the outer container
        FRAME.getContentPane().add(LOGO_LABEL, BorderLayout.LINE_START);
        FRAME.getContentPane().add(panel);

        // Resize accordingly
        FRAME.pack();
        // Make the frame visible
        FRAME.setVisible(true);
    }

    public static void logIn(String warningMessage)
    {
        // Initialize components


        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setLayout(new BoxLayout(FRAME.getContentPane(), BoxLayout.Y_AXIS));

        // Setup warning label
        if (warningMessage == null)
            WARNING_LABEL.setText(" ");
        else
            WARNING_LABEL.setText(warningMessage);

        // Setup fields
        USERNAME_TEXT_FIELD.setBorder(NORMAL_BORDERS);
        PASSWORD_TEXT_FIELD.setBorder(NORMAL_BORDERS);
        USERNAME_TEXT_FIELD.getDocument().addDocumentListener(LOGIN_FIELDS_CHECKER);
        PASSWORD_TEXT_FIELD.getDocument().addDocumentListener(LOGIN_FIELDS_CHECKER);

        // Setup inner container
        JPanel panel = new JPanel();
        panel.setBackground(GuiConstants.BACKGROUND_COLOR);
        panel.setLayout(new FlowLayout());

        // Include inner components (with borders) to the inner container
        panel.add(EXECUTE_LOGIN_BUTTON);
        panel.add(CANCEL_BUTTON);

        // Include all components to the outer container
        FRAME.getContentPane().add(USERNAME_TEXT_FIELD);
        FRAME.getContentPane().add(PASSWORD_TEXT_FIELD);
        FRAME.getContentPane().add(WARNING_LABEL);
        FRAME.getContentPane().add(panel);

        // Resize accordingly
        FRAME.pack();
    }

    public static void signUp(String warningMessage)
    {
        // Initialize components
        JPanel buttonsPanel = new JPanel();

        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setLayout(new BoxLayout(FRAME.getContentPane(), BoxLayout.Y_AXIS));


        // Setup fields
        USERNAME_TEXT_FIELD.setBorder(NORMAL_BORDERS);
        PASSWORD_TEXT_FIELD.setBorder(NORMAL_BORDERS);
        USERNAME_TEXT_FIELD.getDocument().addDocumentListener(SIGNUP_FIELDS_CHECKER);
        PASSWORD_TEXT_FIELD.getDocument().addDocumentListener(SIGNUP_FIELDS_CHECKER);

        // Setup warning label
        if (warningMessage == null)
            WARNING_LABEL.setText(" ");
        else
            WARNING_LABEL.setText(warningMessage);

        // Setup inner container
        buttonsPanel.setBackground(GuiConstants.BACKGROUND_COLOR);
        buttonsPanel.setLayout(new FlowLayout());


        // Include inner components (with borders) to the inner container
        buttonsPanel.add(EXECUTE_SIGNUP_BUTTON);
        buttonsPanel.add(CANCEL_BUTTON);

        // Include all components to the outer container
        FRAME.getContentPane().add(USERNAME_TEXT_FIELD);
        FRAME.getContentPane().add(PASSWORD_TEXT_FIELD);
        FRAME.getContentPane().add(WARNING_LABEL);
        FRAME.getContentPane().add(buttonsPanel);

        // Resize accordingly
        FRAME.pack();
    }

    private static void loading()
    {

        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setLayout(new FlowLayout());

        // Setup loading animation
        FRAME.getContentPane().add(LOADING_ICON_LABEL);

    }

    public static void session()
    {

        // Empty frame
        FRAME.getContentPane().removeAll();

        // Setup outer container
        FRAME.getContentPane().setLayout(new BorderLayout());


        // Add all components to outer container
        FRAME.getContentPane().add(FriendsPanel.PANEL, BorderLayout.WEST);
        FRAME.getContentPane().add(ChallengePanel.CHALLENGE_PANEL, BorderLayout.CENTER);
        FRAME.getContentPane().add(ScoresPanel.PANEL, BorderLayout.EAST);

        // Delegate Friends panel setup
        FriendsPanel.setUp();

        // Delegate Scores panel setup
        ScoresPanel.setUp();

        // Delegate Challenge panel setup
        ChallengePanel.waitForChallengeRequest();

        // Resize accordingly
        FRAME.pack();
    }

}
