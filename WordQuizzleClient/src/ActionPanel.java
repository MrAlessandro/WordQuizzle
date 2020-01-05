import javax.swing.*;
import java.awt.*;

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
        this.UsernameLabel = new JLabel("Username: ");
        this.PasswordTextField = new JPlaceholderPasswordField("Password");
        this.PasswordLabel = new JLabel("Password: ");
        this.WarningLabel = new JLabel("<html><u><font color='red'>Username already in use, change it!</font></u></html>");
        this.WarningLabel.setForeground(Color.red);
        if (action == ActionLogInPanel)
            this.ActionButton = new JButton("LogIn");
        else if (action == ActionSignUpPanel)
            this.ActionButton = new JButton("SignUp");
        this.CancelButton = new JButton("Cancel");

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
}
