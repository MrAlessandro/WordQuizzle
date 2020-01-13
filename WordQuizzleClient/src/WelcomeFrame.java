import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class WelcomeFrame extends JFrame implements Runnable
{
    private JLayeredPane LayeredPane;
    private ImageIcon BackgroundImage;
    private JLabel BackgroundLabel;
    private ActionPanel MessageZone;
    private JButton LogInButton;
    private JButton SignUpButton;

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

        // Setting logIn button
        this.LogInButton = new JButton("Log In");
        this.LogInButton.setSize(this.LogInButton.getPreferredSize());
        this.LogInButton.setLocation(10, 10);
        this.LogInButton.setVisible(true);
        this.LogInButton.addActionListener(new LogInButtonListener());
        this.LayeredPane.add(this.LogInButton, JLayeredPane.MODAL_LAYER);

        // Setting singUp button
        this.SignUpButton = new JButton("Sign Up");
        this.SignUpButton.setSize(this.SignUpButton.getPreferredSize());
        this.SignUpButton.setLocation(this.BackgroundImage.getIconWidth() - this.SignUpButton.getWidth() - 10 , 10);
        this.SignUpButton.setVisible(true);
        this.SignUpButton.addActionListener(new SignUpButtonListener());
        this.LayeredPane.add(this.SignUpButton, JLayeredPane.MODAL_LAYER);

        // Adding panel at the frame
        this.add(this.LayeredPane);

        // Adjusting size of frame
        this.setSize(this.BackgroundImage.getIconWidth(), this.BackgroundImage.getIconHeight() + 20);
    }

    @Override
    public void run()
    {
        // Making frame visible
        this.setVisible(true);
    }

    protected void setUpLogIn()
    {
        if (this.LayeredPane.isAncestorOf(this.MessageZone))
            this.LayeredPane.remove(this.MessageZone);
        this.SignUpButton.setVisible(false);
        this.LogInButton.setVisible(false);
        this.MessageZone = new ActionPanel(ActionPanel.ActionLogInPanel);
        this.LayeredPane.add(this.MessageZone, JLayeredPane.PALETTE_LAYER);
        this.MessageZone.setVisible(true);
    }

    protected void setUpSignUp()
    {
        if (this.LayeredPane.isAncestorOf(this.MessageZone))
            this.LayeredPane.remove(this.MessageZone);
        this.SignUpButton.setVisible(false);
        this.LogInButton.setVisible(false);
        this.MessageZone = new ActionPanel(ActionPanel.ActionSignUpPanel);
        this.LayeredPane.add(this.MessageZone, JLayeredPane.PALETTE_LAYER);
        this.MessageZone.setVisible(true);
    }

    protected void reset()
    {
        if (this.LayeredPane.isAncestorOf(this.MessageZone))
            this.LayeredPane.remove(this.MessageZone);
        this.SignUpButton.setVisible(true);
        this.LogInButton.setVisible(true);
        this.repaint();
    }

    private static class SignUpButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            JButton button = (JButton) e.getSource();
            WelcomeFrame frame = (WelcomeFrame) SwingUtilities.getRoot(button);
            frame.setUpSignUp();
        }
    }

    private static class LogInButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent e)
        {
            JButton button = (JButton) e.getSource();
            WelcomeFrame frame = (WelcomeFrame) SwingUtilities.getRoot(button);
            frame.setUpLogIn();
        }
    }
}