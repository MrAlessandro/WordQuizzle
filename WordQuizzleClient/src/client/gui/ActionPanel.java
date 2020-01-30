package client.gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ActionPanel extends JPanel
{
    protected static final int ActionLogInPanel = 100;
    protected static final int ActionSignUpPanel = 200;
    private JPlaceholderTextField UsernameTextField;
    private JLabel UsernameLabel;
    private JPlaceholderPasswordField PasswordTextField;
    private JLabel PasswordLabel;
    private JLabel WarningLabel;
    private JButton ActionButton;
    private JButton CancelButton;

    public ActionPanel(int action)
    {
        super();
        this.setBackground(Constants.ForegroundColor);
        this.setLocation(248, 140);
        this.setSize(241, 115);
        this.setLayout(new GridBagLayout());
        this.setOpaque(true);
        this.setBorder(BorderFactory.createLineBorder(Constants.MainColor));
        this.setVisible(false);

        this.UsernameTextField = new JPlaceholderTextField("Username");
        this.UsernameTextField.getDocument().addDocumentListener(new UsernameTextFieldListener());
        this.UsernameTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        this.UsernameLabel = new JLabel("Username: ");
        this.PasswordTextField = new JPlaceholderPasswordField("Password");
        this.PasswordTextField.getDocument().addDocumentListener(new PasswordTextFieldListener());
        this.PasswordTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        this.PasswordLabel = new JLabel("Password: ");
        this.WarningLabel = new JLabel("<html><u><font color='red'>Username already in use, change it!</font></u></html>");
        this.WarningLabel.setForeground(Color.red);

        if (action == ActionLogInPanel)
        {
            this.ActionButton = new JButton("LogIn");
            this.ActionButton.addActionListener(new LogInButtonListener());
        }
        else if (action == ActionSignUpPanel)
        {
            this.ActionButton = new JButton("SignUp");
            this.ActionButton.addActionListener(new SignUpButtonListener());
        }

        this.CancelButton = new JButton("Cancel");
        this.CancelButton.addActionListener(new CancelButtonListener());

        this.UsernameTextField.setColumns(10);
        this.PasswordTextField.setColumns(10);

        GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        this.add(this.UsernameLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        this.add(this.UsernameTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        this.add(this.PasswordLabel, constraints);

        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        this.add(this.PasswordTextField, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.insets = new Insets(5, 0,0,0);
        this.WarningLabel.setVisible(false);
        this.add(this.WarningLabel, constraints);

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 0,0,0);
        this.add(this.CancelButton, constraints);

        constraints.gridx = 1;
        constraints.gridy = 3;
        constraints.gridwidth = 1;
        constraints.insets = new Insets(5, 60,0,0);
        this.add(this.ActionButton, constraints);

    }

    private static class LogInButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            ActionPanel panel = (ActionPanel) ((JButton) e.getSource()).getParent();

            if(panel.UsernameTextField.getText().equals("") && panel.PasswordTextField.getPassword().length == 0)
            {
                panel.UsernameTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.PasswordTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.WarningLabel.setText("<html><u><font color='red'>Empty fields!</font></u></html>");
                panel.WarningLabel.setVisible(true);
            }
            else if(panel.PasswordTextField.getPassword().length == 0 && !(panel.UsernameTextField.getText().equals("")))
            {
                panel.PasswordTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.WarningLabel.setText("<html><u><font color='red'>Password field is empty!</font></u></html>");
                panel.WarningLabel.setVisible(true);
            }
            else if(!(panel.PasswordTextField.getPassword().length == 0) && (panel.UsernameTextField.getText().equals("")))
            {
                panel.UsernameTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.WarningLabel.setText("<html><u><font color='red'>Username field is empty!</font></u></html>");
                panel.WarningLabel.setVisible(true);
            }
            else if(!(panel.PasswordTextField.getPassword().length == 0) && !(panel.UsernameTextField.getText().equals("")))
            {
                panel.removeAll();

                panel.setBackground(Color.white);

                ImageIcon loadingGIF = new ImageIcon(Constants.LoadingGifPAth.toString());
                JLabel gifLabel = new JLabel(loadingGIF);

                panel.add(gifLabel);

                panel.revalidate();
                panel.repaint();


            }
        }
    }

    private static class SignUpButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            ActionPanel panel = (ActionPanel) ((JButton) e.getSource()).getParent();

            if(panel.UsernameTextField.getText().equals("") && panel.PasswordTextField.getPassword().length == 0)
            {
                panel.UsernameTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.PasswordTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.WarningLabel.setText("<html><u><font color='red'>Empty fields!</font></u></html>");
                panel.WarningLabel.setVisible(true);
            }
            else if(panel.PasswordTextField.getPassword().length == 0 && !(panel.UsernameTextField.getText().equals("")))
            {
                panel.PasswordTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.WarningLabel.setText("<html><u><font color='red'>Password field is empty!</font></u></html>");
                panel.WarningLabel.setVisible(true);
            }
            else if(!(panel.PasswordTextField.getPassword().length == 0) && (panel.UsernameTextField.getText().equals("")))
            {
                panel.UsernameTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
                panel.WarningLabel.setText("<html><u><font color='red'>Username field is empty!</font></u></html>");
                panel.WarningLabel.setVisible(true);
            }
            else if(!(panel.PasswordTextField.getPassword().length == 0) && !(panel.PasswordTextField.getPassword().length == 0))
            {
                panel.removeAll();

                panel.setBackground(Color.white);

                ImageIcon loadingGIF = new ImageIcon(Constants.LoadingGifPAth.toString());
                JLabel gifLabel = new JLabel(loadingGIF);

                panel.add(gifLabel);

                panel.revalidate();
                panel.repaint();

                boolean registered = WordQuizzleClient.register(panel.UsernameTextField.getText().trim(), panel.PasswordTextField.getPassword());

                panel.removeAll();
                panel.setBackground(Constants.BackgroundColor);
                JLabel doneLabel = new JLabel();
                doneLabel.setForeground(Constants.MainColor);

                if(registered)
                {
                    doneLabel.setText("Correctly registered!");
                    panel.add(doneLabel);
                }
                else
                {
                    doneLabel.setText("Username not unique.");
                    JLabel more = new JLabel("Try another one!");
                    more.setForeground(Constants.MainColor);

                    GridBagConstraints constraints = new GridBagConstraints();

                    constraints.gridx = 0;
                    constraints.gridy = 0;
                    panel.add(doneLabel, constraints);

                    constraints.gridx = 0;
                    constraints.gridy = 1;
                    panel .add(more, constraints);

                    constraints.gridx = 0;
                    constraints.gridy = 2;
                    constraints.insets = new Insets(5, 0,0,0);
                    panel.add(panel.CancelButton, constraints);
                }

                panel.revalidate();
                panel.repaint();

            }
        }
    }

    private static class CancelButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JButton button = (JButton) e.getSource();
            WordQuizzleClientFrame frame = (WordQuizzleClientFrame) SwingUtilities.getRoot(button);
            frame.reset();
        }
    }

    private class UsernameTextFieldListener implements DocumentListener
    {

        @Override
        public void insertUpdate(DocumentEvent documentEvent)
        {
            UsernameTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent)
        {
            if(UsernameTextField.getText().equals(""))
            {
                UsernameTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
            }
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent)
        {}
    }

    private class PasswordTextFieldListener implements DocumentListener
    {

        @Override
        public void insertUpdate(DocumentEvent documentEvent)
        {
            PasswordTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.black));
        }

        @Override
        public void removeUpdate(DocumentEvent documentEvent)
        {
            if(PasswordTextField.getPassword().length == 0)
            {
                PasswordTextField.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.red));
            }
        }

        @Override
        public void changedUpdate(DocumentEvent documentEvent)
        {}
    }
}
