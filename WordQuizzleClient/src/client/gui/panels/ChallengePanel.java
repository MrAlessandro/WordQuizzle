package client.gui.panels;

import client.gui.WordQuizzleClientFrame;
import client.gui.constants.GuiConstants;
import client.main.WordQuizzleClient;
import client.operators.ProvideTranslationOperator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.io.IOException;

public class ChallengePanel extends JPanel
{
    public static final JLabel NO_CHALLENGE_IN_PROGRESS_LABEL = new JLabel("No challenge in progress.");
    public static final JLabel INSTRUCTION_LABEL = new JLabel("Select a friend and challenge him using the left panel.");
    public static final JLabel WAITING_FOR_RESPONSE_LABEL = new JLabel();
    public static final JLabel STOPWATCH_ICON_LABEL = new JLabel(GuiConstants.STOPWATCH_ICON);
    public static final JLabel FAILURE_MESSAGE_LABEL = new JLabel();
    public static final JLabel TRANSLATE_HERE_LABEL = new JLabel("Insert your translation here:");
    public static final JLabel LOADING_CHALLENGE_LABEL = new JLabel("Loading challenge...");
    public static final JLabel LOADING_CHALLENGE_ICON_LABEL = new JLabel(GuiConstants.LOADING_GIF);

    private static final JTextPane CHALLENGE_PROGRESS_PANEL = new JTextPane();
    public static final JScrollPane SCROLL_CHALLENGE_PANE = new JScrollPane(CHALLENGE_PROGRESS_PANEL);
    public static final JButton SUBMIT_BUTTON = new JButton("Submit");
    public static final JTextField TRANSLATION_FIELD = new JTextField();
    public static final ChallengePanel CHALLENGE_PANEL = new ChallengePanel();
    public static final String FONT_FAMILY = CHALLENGE_PANEL.getFont().getFamily();
    public static boolean challengeable = false;
    public static volatile String applicant = null;
    public volatile static String opponent = null;
    private static int challengeTimeout;
    public static int challengeWordsQuantity;
    public static int progress;

    private ChallengePanel()
    {
        super();

        // Initialize Challenge panel
        this.setBackground(GuiConstants.BACKGROUND_COLOR);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(new CompoundBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1), new LineBorder(GuiConstants.BACKGROUND_COLOR, 10)));

        // Setup labels
        NO_CHALLENGE_IN_PROGRESS_LABEL.setFont(new Font("", Font.PLAIN, 20));
        INSTRUCTION_LABEL.setFont(new Font("", Font.PLAIN, 15));
        NO_CHALLENGE_IN_PROGRESS_LABEL.setForeground(GuiConstants.MAIN_COLOR);
        INSTRUCTION_LABEL.setForeground(GuiConstants.MAIN_COLOR);
        NO_CHALLENGE_IN_PROGRESS_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);
        NO_CHALLENGE_IN_PROGRESS_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);
        INSTRUCTION_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);
        INSTRUCTION_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);

        WAITING_FOR_RESPONSE_LABEL.setForeground(GuiConstants.MAIN_COLOR);
        WAITING_FOR_RESPONSE_LABEL.setFont(new Font("", Font.PLAIN, 20));
        WAITING_FOR_RESPONSE_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);
        WAITING_FOR_RESPONSE_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);

        STOPWATCH_ICON_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);
        STOPWATCH_ICON_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);

        FAILURE_MESSAGE_LABEL.setForeground(GuiConstants.MAIN_COLOR);
        FAILURE_MESSAGE_LABEL.setFont(new Font("", Font.PLAIN, 20));
        FAILURE_MESSAGE_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);
        FAILURE_MESSAGE_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);

        TRANSLATE_HERE_LABEL.setAlignmentX(Component.LEFT_ALIGNMENT);
        TRANSLATE_HERE_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);
        TRANSLATE_HERE_LABEL.setForeground(GuiConstants.MAIN_COLOR);

        LOADING_CHALLENGE_LABEL.setForeground(GuiConstants.MAIN_COLOR);
        LOADING_CHALLENGE_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);
        LOADING_CHALLENGE_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);

        LOADING_CHALLENGE_ICON_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);
        LOADING_CHALLENGE_ICON_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup panels
        CHALLENGE_PROGRESS_PANEL.setContentType("text/html");
        CHALLENGE_PROGRESS_PANEL.setBackground(GuiConstants.BACKGROUND_COLOR);
        CHALLENGE_PROGRESS_PANEL.setBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1));
        CHALLENGE_PROGRESS_PANEL.setEditable(false);

        SCROLL_CHALLENGE_PANE.setAlignmentX(Component.CENTER_ALIGNMENT);
        SCROLL_CHALLENGE_PANE.setAlignmentY(Component.CENTER_ALIGNMENT);
        SCROLL_CHALLENGE_PANE.setBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1));
        //SCROLL_CHALLENGE_PANE.setPreferredSize(new Dimension(480, 280));
        //SCROLL_CHALLENGE_PANE.setMinimumSize(new Dimension(480, 290));
        //SCROLL_CHALLENGE_PANE.setMaximumSize(new Dimension(475, 300));
        //SCROLL_CHALLENGE_PANE.setSize(new Dimension(475, 350));

        // Setup fields
        TRANSLATION_FIELD.setColumns(25);
        TRANSLATION_FIELD.setAlignmentX(Component.LEFT_ALIGNMENT);
        TRANSLATION_FIELD.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup buttons
        SUBMIT_BUTTON.setEnabled(true);
        SUBMIT_BUTTON.setAlignmentX(Component.RIGHT_ALIGNMENT);
        SUBMIT_BUTTON.setAlignmentY(Component.CENTER_ALIGNMENT);
        SUBMIT_BUTTON.addActionListener(e -> {
            SUBMIT_BUTTON.setEnabled(false);
            WordQuizzleClient.POOL.execute(new ProvideTranslationOperator(applicant, opponent, TRANSLATION_FIELD.getText()));
            TRANSLATION_FIELD.setText("");
        });

    }

    public static void waitForChallengeRequest()
    {
        // Restore challenge data
        CHALLENGE_PROGRESS_PANEL.setText("");
        challengeable = true;
        applicant = null;
        opponent = null;
        progress = 0;

        if (FriendsPanel.FRIENDS_LIST.getSelectedValue() != null)
            FriendsPanel.CHALLENGE_BUTTON.setEnabled(true);

        // Empty Challenge panel
        CHALLENGE_PANEL.removeAll();

        // Initializing label container
        JPanel labelContainer = new JPanel();

        // Setup label container
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.Y_AXIS));

        // Adding labels to labels container
        labelContainer.add(Box.createRigidArea(new Dimension(0, 130)));
        labelContainer.add(NO_CHALLENGE_IN_PROGRESS_LABEL);
        labelContainer.add(INSTRUCTION_LABEL);

        // Add all to outer container
        CHALLENGE_PANEL.add(labelContainer, BorderLayout.CENTER);

        // Repaint
        CHALLENGE_PANEL.repaint();
        CHALLENGE_PANEL.revalidate();
    }

    public static void waitForOpponentResponse(String opponent)
    {
        // Set the waiting flag
        challengeable = false;

        // Empty outer container
        CHALLENGE_PANEL.removeAll();

        // Disable challenge button
        FriendsPanel.CHALLENGE_BUTTON.setEnabled(false);

        // Setup label container
        JPanel labelContainer = new JPanel();
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.Y_AXIS));

        // SetUp waiting label
        WAITING_FOR_RESPONSE_LABEL.setText("Waiting for response from \"" + opponent + "\"...");

        // Adding labels to labels container
        labelContainer.add(Box.createRigidArea(new Dimension(0, 100)));
        labelContainer.add(WAITING_FOR_RESPONSE_LABEL);
        labelContainer.add(STOPWATCH_ICON_LABEL);

        // Add all to outer container
        CHALLENGE_PANEL.add(labelContainer, BorderLayout.CENTER);

        // Repaint
        CHALLENGE_PANEL.repaint();
        CHALLENGE_PANEL.revalidate();
    }

    public static void requestFailed(String message)
    {
        // Empty outer container
        CHALLENGE_PANEL.removeAll();

        // Setup label container
        JPanel labelContainer = new JPanel();
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.Y_AXIS));

        // SetUp message label
        FAILURE_MESSAGE_LABEL.setText(message);

        // Adding labels to labels container
        labelContainer.add(Box.createRigidArea(new Dimension(0, 130)));
        labelContainer.add(FAILURE_MESSAGE_LABEL);

        // Add all to outer container
        CHALLENGE_PANEL.add(labelContainer, BorderLayout.CENTER);

        // Repaint
        CHALLENGE_PANEL.repaint();
        CHALLENGE_PANEL.revalidate();
    }

    public static void challenge(int timeout, int wordsQuantity, String firstWord)
    {
        // Setup values
        challengeTimeout = timeout;
        challengeWordsQuantity = wordsQuantity;
        progress = 1;

        // Initialize progress panel


        // Append first word to challenge progress panel
        appendTranslationTask(firstWord);

        // Empty outer container
        CHALLENGE_PANEL.removeAll();

        // Disable challenge button
        FriendsPanel.CHALLENGE_BUTTON.setEnabled(false);

        // Setup label panel
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.X_AXIS));
        labelPanel.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelPanel.setPreferredSize(new Dimension(480, 25));
        labelPanel.setMaximumSize(new Dimension(480, 25));

        // Setup translation panel
        JPanel translationPanel = new JPanel();
        translationPanel.setLayout(new BoxLayout(translationPanel, BoxLayout.X_AXIS));
        translationPanel.setBackground(GuiConstants.BACKGROUND_COLOR);
        translationPanel.setPreferredSize(new Dimension(480, 25));
        translationPanel.setMaximumSize(new Dimension(480, 25));

        // Add label to label container
        labelPanel.add(TRANSLATE_HERE_LABEL);
        labelPanel.add(Box.createHorizontalGlue());

        // Add components to translation panel
        translationPanel.add(TRANSLATION_FIELD);
        translationPanel.add(Box.createHorizontalGlue());
        translationPanel.add(SUBMIT_BUTTON);

        // Add elements to outer container
        CHALLENGE_PANEL.add(SCROLL_CHALLENGE_PANE);
        CHALLENGE_PANEL.add(labelPanel);
        CHALLENGE_PANEL.add(translationPanel);

        // Repaint
        CHALLENGE_PANEL.repaint();
        CHALLENGE_PANEL.revalidate();
    }

    public static void loading()
    {
        // Set the waiting flag
        challengeable = false;

        // Empty outer container
        CHALLENGE_PANEL.removeAll();

        // Disable challenge button
        FriendsPanel.CHALLENGE_BUTTON.setEnabled(false);

        // Setup label container
        JPanel labelContainer = new JPanel();
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BoxLayout(labelContainer, BoxLayout.Y_AXIS));

        // Add components to labels container
        labelContainer.add(Box.createRigidArea(new Dimension(0, 140)));
        labelContainer.add(LOADING_CHALLENGE_LABEL);
        labelContainer.add(Box.createRigidArea(new Dimension(0, 10)));
        labelContainer.add(LOADING_CHALLENGE_ICON_LABEL);

        // Add all to outer container
        CHALLENGE_PANEL.add(labelContainer, BorderLayout.CENTER);

        // Repaint
        CHALLENGE_PANEL.repaint();
        CHALLENGE_PANEL.revalidate();
    }

    public static void appendTranslationTask(String word)
    {
        HTMLDocument document = (HTMLDocument) CHALLENGE_PROGRESS_PANEL.getStyledDocument();

        try
        {
            document.insertAfterEnd(document.getCharacterElement(document.getLength()),
                    "<p color=\"#33ace0\" align=\"left\"><font face=\"" + FONT_FAMILY + "\">Word to translate (" + progress + "\\" + challengeWordsQuantity + "): \"<em>" + word + "</em>\"</font></p>");
        }
        catch (BadLocationException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void appendTranslation(String translation, boolean correct)
    {
        HTMLDocument document = (HTMLDocument) CHALLENGE_PROGRESS_PANEL.getStyledDocument();

        try
        {
            if (correct)
                document.insertAfterEnd(document.getCharacterElement(document.getLength()),
                        "<br><p color=\"#33ace0\" align=\"right\"><font face=\"" + FONT_FAMILY + "\">Translation provided: \"<em>" + translation + "</em>\". ⟶ <b><span style=\"color: #32CD32\">RIGHT</span></font></b></p>");
            else
                document.insertAfterEnd(document.getCharacterElement(document.getLength()),
                        "<br><p color=\"#33ace0\" align=\"right\"><font face=\"" + FONT_FAMILY + "\">Translation provided: \"<em>" + translation + "</em>\". ⟶ <b><span style=\"color: #FF0000\">WRONG</span></font></b></p>");
        }
        catch (BadLocationException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void appendWaitMessage()
    {
        HTMLDocument document = (HTMLDocument) CHALLENGE_PROGRESS_PANEL.getStyledDocument();
        String otherPlayer;

        if (opponent.equals(WordQuizzleClientFrame.username))
            otherPlayer = applicant;
        else
            otherPlayer = opponent;

        try
        {
            document.insertAfterEnd(document.getCharacterElement(document.getLength()),
                    "<p align=\"center\">________________________________</p>" +
                            "<h2 align=\"center\">Translation terminated!</h2>" +
                            "<h3 align=\"center\"><br>Wait for \"" + otherPlayer + "\" to terminate...</h3>");
        }
        catch (BadLocationException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(500, 400);
    }

    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(500, 400);
    }
}
