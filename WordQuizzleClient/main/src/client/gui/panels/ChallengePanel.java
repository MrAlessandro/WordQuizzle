package client.gui.panels;

import client.gui.WordQuizzleClientFrame;
import client.settings.Settings;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChallengePanel extends JPanel
{
    private JLabel noChallengeInProgressLabel;
    private JLabel instructionLabel;
    private JLabel waitingForResponseLabel;
    private JLabel stopwatchIconLabel;
    private JLabel failureMessageLabel;
    private JLabel translateHereLabel;
    private JLabel loadingChallengeLabel;
    private JLabel loadingChallengeIconLabel;

    private JTextPane challengeProgressPanel;
    private JScrollPane scrollChallengePane;
    private JButton submitButton;
    private JTextField translationField;

    private AtomicBoolean busy;

    public String opponent;
//    public volatile static String opponent = null;
//    private static int challengeTimeout;
//    public static int challengeWordsQuantity;
//    public static int progress;

    public ChallengePanel()
    {
        super();

        // Initialize Challenge panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Settings.BACKGROUND_COLOR);
        this.setBorder(new CompoundBorder(new LineBorder(Settings.MAIN_COLOR, 1), new LineBorder(Settings.BACKGROUND_COLOR, 10)));

        // Setup labels
        this.noChallengeInProgressLabel = new JLabel("No challenge in progress.");
        this.noChallengeInProgressLabel.setFont(new Font("", Font.PLAIN, 20));
        this.noChallengeInProgressLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.noChallengeInProgressLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.noChallengeInProgressLabel.setForeground(Settings.MAIN_COLOR);

        this.instructionLabel = new JLabel("Select a friend and challenge him using the left panel.");
        this.instructionLabel.setFont(new Font("", Font.PLAIN, 15));
        this.instructionLabel.setForeground(Settings.MAIN_COLOR);
        this.instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.instructionLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.waitingForResponseLabel = new JLabel();
        this.waitingForResponseLabel.setForeground(Settings.MAIN_COLOR);
        this.waitingForResponseLabel.setFont(new Font("", Font.PLAIN, 20));
        this.waitingForResponseLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.waitingForResponseLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.stopwatchIconLabel = new JLabel(Settings.STOPWATCH_ICON);
        this.stopwatchIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.stopwatchIconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.failureMessageLabel = new JLabel();
        this.failureMessageLabel.setForeground(Settings.MAIN_COLOR);
        this.failureMessageLabel.setFont(new Font("", Font.PLAIN, 20));
        this.failureMessageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.failureMessageLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.translateHereLabel = new JLabel("Insert your translation here:");
        this.translateHereLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.translateHereLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.translateHereLabel.setForeground(Settings.MAIN_COLOR);

        this.loadingChallengeLabel = new JLabel("Loading challenge...");
        this.loadingChallengeLabel.setForeground(Settings.MAIN_COLOR);
        this.loadingChallengeLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.loadingChallengeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.loadingChallengeIconLabel = new JLabel(Settings.LOADING_ICON);
        this.loadingChallengeIconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.loadingChallengeIconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup panels
        this.challengeProgressPanel = new JTextPane();
        this.challengeProgressPanel.setContentType("text/html");
        this.challengeProgressPanel.setBackground(Settings.BACKGROUND_COLOR);
        this.challengeProgressPanel.setBorder(new LineBorder(Settings.MAIN_COLOR, 1));
        this.challengeProgressPanel.setEditable(false);

        this.scrollChallengePane = new JScrollPane(challengeProgressPanel);
        this.scrollChallengePane.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.scrollChallengePane.setAlignmentY(Component.CENTER_ALIGNMENT);
        this.scrollChallengePane.setBorder(new LineBorder(Settings.MAIN_COLOR, 1));

        // Setup fields
        this.translationField = new JTextField();
        this.translationField.setColumns(25);
        this.translationField.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.translationField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup button
        this.submitButton = new JButton("Submit");
        this.submitButton.setEnabled(true);
        this.submitButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        this.submitButton.setAlignmentY(Component.CENTER_ALIGNMENT);
        /*this.submitButton.addActionListener(e -> {
            submitButton.setEnabled(false);
            WordQuizzleClient.POOL.execute(new ProvideTranslationOperator(applicant, opponent, translationField.getText()));
            translationField.setText("");
        });*/

        // Set busy flag
        this.busy = new AtomicBoolean(false);

        // Set challenge data
        this.opponent = null;

        // Start with unemployed status
        this.unemploy();
    }

    public void unemploy()
    {
        // Empty the challenge playground
        this.challengeProgressPanel.setText("");

        // Clean challenge data
        this.opponent = null;

        // Empty panel
        this.removeAll();

        // Setup panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add components to panel
        this.add(Box.createVerticalGlue());
        this.add(this.noChallengeInProgressLabel);
        this.add(this.instructionLabel);
        this.add(Box.createVerticalGlue());

        // Enable challenge button
        //((WordQuizzleClientFrame) this.getParent()).friendsPanel.setEnableChallengeButton(true);

        // Set busy flag
        this.busy.set(false);

        // Repaint
        this.repaint();
        this.revalidate();
    }

    public boolean isBusy()
    {
        return this.busy.get();
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(Settings.CHALLENGE_PANE_WIDTH, Settings.SESSION_PANE_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(Settings.CHALLENGE_PANE_WIDTH, Settings.SESSION_PANE_HEIGHT);
    }
}
