package client.gui.dialogs;

import client.gui.WordQuizzleClientFrame;
import client.main.WordQuizzleClient;
import client.operators.ChallengeRequestDeclineOperator;

import javax.swing.*;
import java.awt.*;
public class ChallengeDialog extends JDialog
{
    public static final ChallengeDialog CHALLENGE_DIALOG = new ChallengeDialog();
    private JLabel message = new JLabel(" ");
    private String from = "";

    private ChallengeDialog()
    {
        super(WordQuizzleClientFrame.FRAME, "Received challenge request", true);
        this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        this.getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        JPanel buttonsContainer = new JPanel();
        buttonsContainer.setLayout(new FlowLayout());

        JLabel questionLabel = new JLabel("Do you want to accept it?");
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        questionLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.message.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.message.setAlignmentY(Component.CENTER_ALIGNMENT);

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(actionEvent -> {

        });
        JButton declineButton = new JButton("Decline");
        confirmButton.addActionListener(actionEvent -> {
            SwingUtilities.invokeLater(() -> CHALLENGE_DIALOG.setVisible(false));
            WordQuizzleClient.POOL.execute(new ChallengeRequestDeclineOperator(this.from));
        });

        buttonsContainer.add(confirmButton);
        buttonsContainer.add(declineButton);

        this.add(message);
        this.add(questionLabel);
        this.add(Box.createRigidArea(new Dimension(0,5)));
        this.add(buttonsContainer);

        this.pack();
        this.setVisible(false);
    }

    public void requestFrom(String from)
    {
        this.from = from;
        this.message.setText("Received challenge request from \"" + from +"\".");
    }
}
