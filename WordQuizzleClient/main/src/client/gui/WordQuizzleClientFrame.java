package client.gui;

import client.gui.fields.JPlaceholderPasswordField;
import client.gui.fields.JPlaceholderTextField;
import client.operators.LoginOperator;
import client.operators.SignupOperator;
import client.settings.Settings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WordQuizzleClientFrame extends JFrame
{
    public JLabel logoLabel;
    public JLabel welcomeMessageLabel;
    public JLabel warningLabel;
    public JLabel loadingIconLabel;
    public JButton logInButton;
    public JButton signUpButton;
    public JButton executeLoginButton;
    public JButton executeSignupButton;
    public JButton cancelButton;
    public JPlaceholderTextField usernameTextField;
    public JPlaceholderPasswordField passwordTextField;
    public Border normalBorders;
    public Border errorBorder;
    public DocumentListener loginFieldsChecker;
    public DocumentListener signupFieldsChecker;

    public WordQuizzleClientFrame()
    {
        super("WordQuizzle");

        this.logoLabel = new JLabel(Settings.LOGO_ICON);
        this.welcomeMessageLabel = new JLabel("Welcome to WordQuizzle!");
        this.warningLabel = new JLabel();
        this.loadingIconLabel = new JLabel(Settings.LOADING_ICON);
        this.logInButton = new JButton("LogIn");
        this.signUpButton = new JButton("SignUp");
        this.executeLoginButton = new JButton("LogIn");
        this.executeSignupButton = new JButton("SignUp");
        this.cancelButton = new JButton("Cancel");
        this.usernameTextField = new JPlaceholderTextField("Username");
        this.passwordTextField = new JPlaceholderPasswordField("Password");
        this.normalBorders = new CompoundBorder(new LineBorder(Settings.BACKGROUND_COLOR, 3), new LineBorder(Settings.MAIN_COLOR, 1));
        this.errorBorder = new CompoundBorder(new LineBorder(Settings.BACKGROUND_COLOR, 3), new LineBorder(Color.RED, 1));
        this.loginFieldsChecker = new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                check();
            }

            private void check()
            {
                boolean checkUsernameField = usernameTextField.getText().equals("");
                boolean checkPasswordField = passwordTextField.getPassword().length == 0;
                if (checkUsernameField || checkPasswordField)
                {
                    if (checkUsernameField && checkPasswordField)
                    {
                        usernameTextField.setBorder(errorBorder);
                        passwordTextField.setBorder(errorBorder);
                        warningLabel.setText("Empty fields");
                    }
                    else if (checkUsernameField)
                    {
                        usernameTextField.setBorder(errorBorder);
                        warningLabel.setText("Empty username");
                    }
                    else
                    {
                        passwordTextField.setBorder(errorBorder);
                        warningLabel.setText("Empty password");
                    }

                    executeLoginButton.setEnabled(false);
                }
                else
                {
                    usernameTextField.setBorder(normalBorders);
                    passwordTextField.setBorder(normalBorders);
                    warningLabel.setText(" ");
                    executeLoginButton.setEnabled(true);
                }
            }
        };
        this.signupFieldsChecker = new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                check();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                check();
            }

            private void check()
            {
                boolean checkUsernameField = usernameTextField.getText().equals("");
                boolean checkPasswordField = passwordTextField.getPassword().length == 0;
                if (checkUsernameField || checkPasswordField)
                {
                    if (checkUsernameField && checkPasswordField)
                    {
                        usernameTextField.setBorder(errorBorder);
                        passwordTextField.setBorder(errorBorder);
                        warningLabel.setText("Empty fields");
                    }
                    else if (checkUsernameField)
                    {
                        usernameTextField.setBorder(errorBorder);
                        warningLabel.setText("Empty username");
                    }
                    else
                    {
                        passwordTextField.setBorder(errorBorder);
                        warningLabel.setText("Empty password");
                    }

                    executeSignupButton.setEnabled(false);
                }
                else
                {
                    usernameTextField.setBorder(normalBorders);
                    passwordTextField.setBorder(normalBorders);
                    warningLabel.setText(" ");
                    executeSignupButton.setEnabled(true);
                }
            }

        };

        // Initialize frame
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(Settings.BACKGROUND_COLOR);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setIconImage(Settings.LOGO_ICON.getImage());

        // Setup labels
        this.welcomeMessageLabel.setFont(new Font("", Font.PLAIN, 30));
        this.welcomeMessageLabel.setForeground(Settings.MAIN_COLOR);
        this.welcomeMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.warningLabel.setForeground(Color.RED);
        this.warningLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.warningLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        this.loadingIconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.loadingIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup fields
        this.usernameTextField.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.usernameTextField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup buttons
        this.logInButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.signUpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.logInButton.addActionListener(e -> SwingUtilities.invokeLater(() -> logIn(null)));
        this.signUpButton.addActionListener(e -> SwingUtilities.invokeLater(() -> signUp(null)));
        executeLoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        executeLoginButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                loading("Loading...");
                usernameTextField.setText("");
                passwordTextField.setText("");
            });
            (new LoginOperator(this, this.usernameTextField.getText(), this.passwordTextField.getPassword())).execute();
        });
        executeSignupButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        executeSignupButton.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                this.loading("");
                usernameTextField.setText("");
                passwordTextField.setText("");
            });
            (new SignupOperator(this, this.usernameTextField.getText(), this.passwordTextField.getPassword())).execute();
        });
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            this.welcome();
            usernameTextField.setText("");
            passwordTextField.setText("");
        }));
    }

    public void loading(String message)
    {
        JLabel messageLabel = null;

        // Empty frame
        this.getContentPane().removeAll();

        // Setup outer container
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        // Setup message if not null
        if (message != null)
        {
            messageLabel = new JLabel(message);
            messageLabel.setFont(new Font("", Font.PLAIN, 15));
            messageLabel.setForeground(Settings.MAIN_COLOR);
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            this.getContentPane().add(messageLabel);
        }

        // Setup loading animation
        JPanel loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Settings.BACKGROUND_COLOR);
        loadingPanel.setPreferredSize(new Dimension(300, 300));

        // Add loading label to loading panel
        loadingPanel.add(loadingIconLabel, BorderLayout.CENTER);

        // Add components to frame
        if (messageLabel != null)
            this.getContentPane().add(messageLabel);
        this.getContentPane().add(loadingPanel);

        // Resize frame accordingly
        this.pack();
        // Set frame visible
        this.setVisible(true);
    }

    public void welcome()
    {
        // Empty frame
        this.getContentPane().removeAll();

        // Setup outer container
        this.getContentPane().setLayout(new FlowLayout());

        // Setup inner container
        JPanel panel = new JPanel();
        panel.setBackground(Settings.BACKGROUND_COLOR);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Include inner components (with borders) to the inner container
        panel.add(this.welcomeMessageLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(this.logInButton);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(this.signUpButton);

        // Include all components to the outer container
        this.getContentPane().add(this.logoLabel, BorderLayout.LINE_START);
        this.getContentPane().add(panel);

        // Resize accordingly
        this.pack();
        // Make the frame visible
        this.setVisible(true);
    }

    public void logIn(String warningMessage)
    {
        // Empty frame
        this.getContentPane().removeAll();

        // Setup outer container
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        // Setup warning label
        if (warningMessage == null)
            this.warningLabel.setText(" ");
        else
            this.warningLabel.setText(warningMessage);

        // Setup fields
        this.usernameTextField.setBorder(this.normalBorders);
        this.passwordTextField.setBorder(this.normalBorders);
        this.usernameTextField.getDocument().addDocumentListener(this.loginFieldsChecker);
        this.passwordTextField.getDocument().addDocumentListener(this.loginFieldsChecker);

        // Setup inner container
        JPanel panel = new JPanel();
        panel.setBackground(Settings.BACKGROUND_COLOR);
        panel.setLayout(new FlowLayout());

        // Include inner components (with borders) to the inner container
        panel.add(this.executeLoginButton);
        panel.add(this.cancelButton);

        // Include all components to the outer container
        this.getContentPane().add(this.usernameTextField);
        this.getContentPane().add(this.passwordTextField);
        this.getContentPane().add(this.warningLabel);
        this.getContentPane().add(panel);

        // Resize accordingly
        this.pack();
    }

    public void signUp(String warningMessage)
    {
        // Empty frame
        this.getContentPane().removeAll();

        // Setup outer container
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        // Setup warning label
        if (warningMessage == null)
            this.warningLabel.setText(" ");
        else
            this.warningLabel.setText(warningMessage);

        // Setup fields
        this.usernameTextField.setBorder(this.normalBorders);
        this.passwordTextField.setBorder(this.normalBorders);
        this.usernameTextField.getDocument().addDocumentListener(this.loginFieldsChecker);
        this.passwordTextField.getDocument().addDocumentListener(this.loginFieldsChecker);

        // Setup inner container
        JPanel panel = new JPanel();
        panel.setBackground(Settings.BACKGROUND_COLOR);
        panel.setLayout(new FlowLayout());

        // Include inner components (with borders) to the inner container
        panel.add(this.executeSignupButton);
        panel.add(this.cancelButton);

        // Include all components to the outer container
        this.getContentPane().add(this.usernameTextField);
        this.getContentPane().add(this.passwordTextField);
        this.getContentPane().add(this.warningLabel);
        this.getContentPane().add(panel);

        // Resize accordingly
        this.pack();
    }

    public void session()
    {
        // Empty frame
        this.getContentPane().removeAll();

        // Setup outer container
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        this.getContentPane().add(new Label("LOGGED IN!"));

        // Resize accordingly
        this.pack();
    }
}
