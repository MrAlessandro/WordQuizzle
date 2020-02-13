package client.gui.dialogs;

import client.gui.WordQuizzleClientFrame;
import client.operators.ReplyChallengeRequestOperator;

import javax.swing.*;
import java.awt.*;
public class ChallengeDialog extends JDialog
{
    private JLabel message = new JLabel(" ");

    public ChallengeDialog()
    {
        super(WordQuizzleClientFrame.FRAME, "Received challenge request", true);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        JPanel buttonsContainer = new JPanel();
        buttonsContainer.setLayout(new FlowLayout());

        this.message.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.message.setAlignmentY(Component.CENTER_ALIGNMENT);

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(actionEvent -> {
            ReplyChallengeRequestOperator.CHOICE.set(1);

            synchronized (ReplyChallengeRequestOperator.CHOICE)
            {
                ReplyChallengeRequestOperator.CHOICE.notifyAll();
            }
        });
        JButton declineButton = new JButton("Decline");
        confirmButton.addActionListener(actionEvent -> {
            ReplyChallengeRequestOperator.CHOICE.set(2);

            synchronized (ReplyChallengeRequestOperator.CHOICE)
            {
                ReplyChallengeRequestOperator.CHOICE.notifyAll();
            }
        });

        buttonsContainer.add(confirmButton);
        buttonsContainer.add(declineButton);

        this.add(message);
        this.add(Box.createRigidArea(new Dimension(0,5)));
        this.add(buttonsContainer);

        this.pack();
        this.setVisible(false);
    }

    public void setMessage(String message)
    {
        this.message.setText(message);
    }
}
