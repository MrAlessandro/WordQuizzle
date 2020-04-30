package client.gui.panels;

import client.gui.WordQuizzleClientFrame;
import client.operators.ProvideTranslationOperator;
import client.settings.Settings;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ChallengePanel extends JPanel
{
    private WordQuizzleClientFrame parentFrame;

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
//  public volatile static String opponent = null;
//    private static int challengeTimeout;
    public int challengeWordsQuantity;
    public AtomicInteger progress;

    public ChallengePanel(WordQuizzleClientFrame parentFrame)
    {
        super();

        // Set parent frame
        this.parentFrame = parentFrame;

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

        // Setup challenge playground panel
        this.challengeProgressPanel = new JTextPane();
        this.challengeProgressPanel.setContentType("text/html");
        this.challengeProgressPanel.setBackground(Settings.BACKGROUND_COLOR);
        this.challengeProgressPanel.setBorder(new LineBorder(Settings.MAIN_COLOR, 1));
        this.challengeProgressPanel.setEditable(false);
        this.challengeProgressPanel.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        ((HTMLDocument) this.challengeProgressPanel.getStyledDocument()).getStyleSheet()
                .addRule("#challenge-playground{margin-top: -20; padding: 0 5px; text-align: left; font-size: 14pt; color: " +
                        String.format("#%02x%02x%02x", Settings.MAIN_COLOR.getRed(), Settings.MAIN_COLOR.getGreen(), Settings.MAIN_COLOR.getBlue()) + " }");
        ((HTMLDocument) this.challengeProgressPanel.getStyledDocument()).getStyleSheet()
                .addRule(".word-entry{}");
        ((HTMLDocument) this.challengeProgressPanel.getStyledDocument()).getStyleSheet()
                .addRule(".word{font-style: italic;}");
        ((HTMLDocument) this.challengeProgressPanel.getStyledDocument()).getStyleSheet()
                .addRule(".translation-entry{text-align: right}");
        ((HTMLDocument) this.challengeProgressPanel.getStyledDocument()).getStyleSheet()
                .addRule(".translation{font-style: italic;}");
        ((HTMLDocument) this.challengeProgressPanel.getStyledDocument()).getStyleSheet()
                .addRule(".correct-outcome{text-transform: uppercase; font-weight: bold; color: green}");
        ((HTMLDocument) this.challengeProgressPanel.getStyledDocument()).getStyleSheet()
                .addRule(".wrong-outcome{text-transform: uppercase; font-weight: bold; color: red}");


        // Setup scroll pane
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
        this.submitButton.setEnabled(false);
        this.submitButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        this.submitButton.setAlignmentY(Component.CENTER_ALIGNMENT);

        // Set translation validation mechanism
        this.translationField.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void insertUpdate(DocumentEvent e)
            {
                submitButton.setEnabled(!translationField.getText().trim().isEmpty() && progress.get() <= challengeWordsQuantity);
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                submitButton.setEnabled(!translationField.getText().trim().isEmpty() && progress.get() <= challengeWordsQuantity);
            }

            @Override
            public void changedUpdate(DocumentEvent e)
            {
                submitButton.setEnabled(!translationField.getText().trim().isEmpty() && progress.get() <= challengeWordsQuantity);
            }
        });
        this.submitButton.addActionListener(e -> {
            (new ProvideTranslationOperator(parentFrame, translationField.getText().trim(), progress.get(), challengeWordsQuantity)).execute();
            progress.incrementAndGet();
            translationField.setText("");
        });

        // Initialize progress
        this.progress = new AtomicInteger(1);

        // Set busy flag
        this.busy = new AtomicBoolean(false);

        // Set challenge data
        this.opponent = null;

        // Start with unemployed status
        this.unemploy();
    }

    public void waitingReply(String receiver)
    {
        // Empty panel
        this.removeAll();

        // Setup panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Set waiting label
        this.waitingForResponseLabel.setText("Waiting reply from \"" + receiver + "\"...");

        // Add components to panel
        this.add(Box.createVerticalGlue());
        this.add(this.waitingForResponseLabel);
        this.add(this.loadingChallengeIconLabel);
        this.add(Box.createVerticalGlue());

        // Set busy flag
        this.busy.set(true);

        // Repaint
        this.repaint();
        this.revalidate();
    }

    public void waitingReport()
    {
        // Empty panel
        this.removeAll();

        // Setup panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Add components to panel
        this.add(Box.createVerticalGlue());
        this.add(this.loadingChallengeIconLabel);
        this.add(Box.createVerticalGlue());

        // Set busy flag
        this.busy.set(true);

        // Repaint
        this.repaint();
        this.revalidate();
    }

    public void unemploy()
    {
        // Reset challenge data
        this.opponent = null;
        this.progress.set(1);
        this.challengeWordsQuantity = 0;

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

        // Set busy flag
        this.busy.set(false);

        // Repaint
        this.repaint();
        this.revalidate();
    }

    public void challengeLoading(String opponent)
    {
        // Empty panel
        this.removeAll();

        // Setup panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // Set waiting label
        this.loadingChallengeLabel.setText("Waiting reply from \"" + opponent + "\"...");

        // Add components to panel
        this.add(Box.createVerticalGlue());
        this.add(this.loadingChallengeLabel);
        this.add(this.loadingChallengeIconLabel);
        this.add(Box.createVerticalGlue());

        // Set busy flag
        this.busy.set(true);

        // Repaint
        this.repaint();
        this.revalidate();
    }

    public void challenge(String firstWord, String opponent, int wordQuantity, int timeout)
    {
        // Empty panel
        this.removeAll();

        // Set challenge details
        this.opponent = opponent;
        this.challengeWordsQuantity = wordQuantity;
        this.progress.set(1);

        // Setup challenge playground text area
        this.challengeProgressPanel.setText(Settings.CHALLENGE_PLAYGROUND_MODEL);
        this.appendTranslationTask(firstWord);

        // Set proper layout
        this.setLayout(new BorderLayout());

        // DownsidePanel
        JPanel downsidePanel = new JPanel();
        downsidePanel.setLayout(new BoxLayout(downsidePanel, BoxLayout.Y_AXIS));
        downsidePanel.setBackground(Settings.BACKGROUND_COLOR);

        // Setup label panel
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.setBackground(Settings.BACKGROUND_COLOR);

        // Setup translation panel
        JPanel translationPanel = new JPanel();
        translationPanel.setLayout(new BoxLayout(translationPanel, BoxLayout.X_AXIS));
        translationPanel.setBackground(Settings.BACKGROUND_COLOR);

        // Add label to label container
        labelPanel.add(this.translateHereLabel);
        labelPanel.add(Box.createHorizontalGlue());

        // Add components to translation panel
        translationPanel.add(this.translationField);
        translationPanel.add(Box.createHorizontalGlue());
        translationPanel.add(this.submitButton);


        // Add components to downside panel
        downsidePanel.add(labelPanel);
        downsidePanel.add(translationPanel);

        this.add(scrollChallengePane, BorderLayout.CENTER);
        this.add(downsidePanel, BorderLayout.SOUTH);

        // Repaint
        this.repaint();
        this.revalidate();
    }

    public void appendTranslationTask(String word)
    {
        HTMLDocument document = (HTMLDocument) this.challengeProgressPanel.getStyledDocument();
        Element challengePlaygroundDiv = document.getElement("challenge-playground");

        JScrollBar sb = this.scrollChallengePane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                sb.removeAdjustmentListener(this);
            }
        };
        sb.addAdjustmentListener(downScroller);

        try
        {
            document.insertBeforeEnd(challengePlaygroundDiv,
                    "<p class=\"word-entry\">Word to translate (" + progress + "\\" + challengeWordsQuantity + "): &quot;<span class=\"word\">" + word + "</span>&quot;</p>");
        }
        catch (BadLocationException | IOException e) {
            throw new Error("PARSING HTML INTERNAL DOCUMENT");
        }


    }

    public void appendTranslationOutcome(String translation, boolean correct)
    {
        HTMLDocument document = (HTMLDocument) this.challengeProgressPanel.getStyledDocument();
        Element challengePlaygroundDiv = document.getElement("challenge-playground");

        JScrollBar sb = this.scrollChallengePane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                sb.removeAdjustmentListener(this);
            }
        };
        sb.addAdjustmentListener(downScroller);

        try
        {
            if (correct)
                document.insertBeforeEnd(challengePlaygroundDiv,
                        "<p class=\"translation-entry\">Translation provided: &quot;<span class=\"translation\">" + translation + "</span>&quot; ⟶ <span class=\"correct-outcome\">RIGHT</span></p>");
            else
                document.insertBeforeEnd(challengePlaygroundDiv,
                        "<p class=\"translation-entry\">Translation provided: &quot;<span class=\"translation\">" + translation + "</span>&quot; ⟶ <span class=\"wrong-outcome\">WRONG</span></p>");
        }
        catch (BadLocationException | IOException e) {
            throw new Error("PARSING HTML INTERNAL DOCUMENT");
        }
    }

    public void appendWaitMessage()
    {
        HTMLDocument document = (HTMLDocument) this.challengeProgressPanel.getStyledDocument();
        Element challengePlaygroundDiv = document.getElement("challenge-playground");

        JScrollBar sb = this.scrollChallengePane.getVerticalScrollBar();
        AdjustmentListener downScroller = new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                Adjustable adjustable = e.getAdjustable();
                adjustable.setValue(adjustable.getMaximum());
                sb.removeAdjustmentListener(this);
            }
        };
        sb.addAdjustmentListener(downScroller);

        try
        {
            document.insertBeforeEnd(challengePlaygroundDiv, "<br><hr>");
            document.insertBeforeEnd(challengePlaygroundDiv, "<h2 align=\"center\">Challenge terminated!</h2>");
            document.insertBeforeEnd(challengePlaygroundDiv, "<h3 align=\"center\"><br>Wait for \"" + this.opponent + "\" to terminate...</h3>");
        }
        catch (BadLocationException | IOException e) {
            throw new Error("PARSING HTML INTERNAL DOCUMENT");
        }

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
