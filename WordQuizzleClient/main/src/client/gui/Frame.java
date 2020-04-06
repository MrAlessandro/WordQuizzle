package client.gui;

import client.gui.fields.JPlaceholderPasswordField;
import client.gui.fields.JPlaceholderTextField;
import client.settings.Settings;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class Frame extends JFrame
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
    public DocumentListener loginFieldsChecker = new DocumentListener() {
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
    public DocumentListener signupFieldsChecker = new DocumentListener() {
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


    public Frame()
    {
        super("WordQuizzle");

        this.logoLabel = new JLabel(Settings.LOGO);
        this.welcomeMessageLabel = new JLabel("Welcome to WordQuizzle!");
        this.warningLabel = new JLabel();
        this.loadingIconLabel = new JLabel(Settings.LOADING_GIF);
        this.logInButton = new JButton("LogIn");
        this.signUpButton = new JButton("SignUp");
        this.executeLoginButton = new JButton("LogIn");
        this.executeSignupButton = new JButton("SignUp");
        this.cancelButton = new JButton("Cancel");
        this.usernameTextField = new JPlaceholderTextField("Username");
        this.passwordTextField = new JPlaceholderPasswordField("Password");
        this.normalBorders = new CompoundBorder(new LineBorder(Settings.BACKGROUND_COLOR, 3), new LineBorder(Settings.MAIN_COLOR, 1));
        this.errorBorder = new CompoundBorder(new LineBorder(Settings.BACKGROUND_COLOR, 3), new LineBorder(Color.RED, 1));
    }
}
