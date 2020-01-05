import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LogInButtonListener implements ActionListener
{

    @Override
    public void actionPerformed(ActionEvent e)
    {
        JButton button = (JButton) e.getSource();
        WelcomeFrame frame = (WelcomeFrame) SwingUtilities.getRoot(button);
        frame.loggingIn();
    }
}
