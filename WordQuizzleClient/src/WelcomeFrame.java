import javax.swing.*;

class WelcomeFrame extends JFrame
{
    private JLayeredPane LayeredPane;
    private ImageIcon BackgroundImage;
    private JLabel BackgroundLabel;
    //private JPanel MessageZone;
    private ActionPanel MessageZone;
    private JButton LogInButton;
    private JButton SignUpButton;
    private JPlaceholderTextField UsernameTextField;
    private JLabel UsernameLabel;
    private JPlaceholderTextField PasswordTextField;
    private JLabel PasswordLabel;
    private JButton ActionButton;

    protected WelcomeFrame()
    {
        // Frame settings
        super();
        this.setTitle("WordQuizzle");
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.getContentPane().setBackground(Constants.BackgroundColor);
        this.setLocationByPlatform(true);
        this.setResizable(false);

        // Image loading
        this.BackgroundImage = new ImageIcon(Constants.WordQuizzleLogoPath.toString());

        // Setting panel
        this.LayeredPane = new JLayeredPane();

        // Setting background
        this.BackgroundLabel = new JLabel(this.BackgroundImage);
        this.BackgroundLabel.setLocation(0,0);
        this.BackgroundLabel.setSize(700, 440);
        this.LayeredPane.add(this.BackgroundLabel, JLayeredPane.DEFAULT_LAYER);

        // Setting message zone
        this.MessageZone = new ActionPanel(ActionPanel.ActionLogInPanel);
        /*this.MessageZone = new JPanel();
        this.MessageZone.setBackground(Constants.ForegroundColor);
        this.MessageZone.setLocation(248, 140);
        this.MessageZone.setSize(241, 115);
        this.MessageZone.setLayout(null);
        this.MessageZone.setOpaque(true);
        this.MessageZone.setBorder(BorderFactory.createLineBorder(Constants.MainColor));
        this.MessageZone.setVisible(false);*/
        this.LayeredPane.add(this.MessageZone, JLayeredPane.PALETTE_LAYER);

        // Setting message zone components
        // Text fields
        this.UsernameTextField = new JPlaceholderTextField("Username");
        this.UsernameTextField.setColumns(10);
        this.UsernameTextField.setToolTipText("Username");
        this.PasswordTextField = new JPlaceholderTextField("Password");
        this.PasswordTextField.setColumns(10);
        this.PasswordTextField.setToolTipText("Password");
        // Label fields
        this.UsernameLabel = new JLabel();
        this.UsernameLabel.setText("Username: ");
        this.UsernameLabel.setLocation(10,10);
        this.PasswordLabel = new JLabel();
        this.PasswordLabel.setText("Password: ");

        // Setting logIn button
        this.LogInButton = new JButton("Log In");
        this.LogInButton.setSize(this.LogInButton.getPreferredSize());
        this.LogInButton.setLocation(10, 10);
        this.LogInButton.setVisible(true);
        this.LogInButton.addActionListener(new LogInButtonListener());
        this.LayeredPane.add(this.LogInButton, JLayeredPane.MODAL_LAYER);

        // Setting singUp button
        this.SignUpButton = new JButton("Sign Up");
        this.SignUpButton.setSize(this.LogInButton.getPreferredSize());
        this.SignUpButton.setLocation(this.BackgroundImage.getIconWidth() - this.SignUpButton.getWidth() - 10 , 10);
        this.SignUpButton.setVisible(true);
        this.LayeredPane.add(this.SignUpButton, JLayeredPane.MODAL_LAYER);

        // Adding panel at the frame
        this.add(this.LayeredPane);

        // Adjusting size of frame
        this.setSize(this.BackgroundImage.getIconWidth(), this.BackgroundImage.getIconHeight() + 20);

        // Making frame visible
        this.setVisible(true);
    }

    protected void loggingIn()
    {
        /*this.MessageZone.add(this.UsernameLabel);
        this.MessageZone.add(this.UsernameTextField);
        this.MessageZone.add(this.PasswordLabel);
        this.MessageZone.add(this.PasswordTextField);*/
        this.MessageZone.setVisible(true);
    }
}