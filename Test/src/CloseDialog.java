import javax.swing.*;
import java.awt.*;

public class CloseDialog
{
    static JFrame frame;

    public static void main(String[] args)
    {
        frame = new JFrame("Title");
        ChallengeDialog dialog = new ChallengeDialog();
        dialog.setMessage("Message");
        dialog.setVisible(true);
    }

    public static class ChallengeDialog extends JDialog
    {
        private JLabel message = new JLabel(" ");

        public ChallengeDialog()
        {
            super(frame, "Received challenge request", true);
            this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));


            JPanel buttonsContainer = new JPanel();
            buttonsContainer.setLayout(new FlowLayout());

            this.message.setAlignmentX(Component.CENTER_ALIGNMENT);
            this.message.setAlignmentY(Component.CENTER_ALIGNMENT);

            JButton confirmButton = new JButton("Confirm");
            JButton declineButton = new JButton("Decline");

            buttonsContainer.add(confirmButton);
            buttonsContainer.add(declineButton);

            this.add(message);
            this.add(Box.createRigidArea(new Dimension(0,5)));
            this.add(buttonsContainer);


            this.pack();
        }

        public void setMessage(String message)
        {
            this.message.setText(message);
        }
    }

}
