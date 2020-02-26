package client.gui.panels;

import client.gui.constants.GuiConstants;
import client.main.WordQuizzleClient;
import client.operators.SendChallengeRequestOperator;
import client.operators.SendFriendshipRequestOperator;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class FriendsPanel extends JPanel
{
    public static final DefaultListModel<String> FRIENDS_LIST_MODEL = new DefaultListModel<>();
    public static final JList<String> FRIENDS_LIST = new JList<>(FRIENDS_LIST_MODEL);
    public static final JScrollPane FRIENDS_SCROLL_PANE = new JScrollPane(FRIENDS_LIST);
    public static final JLabel FRIEND_LIST_LABEL = new JLabel("Friends list:");
    public static final JButton ADD_FRIEND_BUTTON = new JButton("+");
    public static final JButton CHALLENGE_BUTTON = new JButton("Challenge");
    public static final FriendsPanel PANEL = new FriendsPanel();

    private FriendsPanel()
    {
        super();

        // Setup Friend panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(GuiConstants.BACKGROUND_COLOR);
        this.setBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1));

        // Setup label
        FRIEND_LIST_LABEL.setForeground(GuiConstants.MAIN_COLOR);

        // Setup friends list
        FRIENDS_LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        FRIENDS_LIST.addListSelectionListener(listSelectionEvent -> {
            if (ChallengePanel.challengeable)
                CHALLENGE_BUTTON.setEnabled(true);
        });

        // Setup scroll pane
        FRIENDS_SCROLL_PANE.setBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1));
        FRIENDS_SCROLL_PANE.setBackground(GuiConstants.SCROLL_PANE_BACKGROUND_COLOR);
        FRIENDS_SCROLL_PANE.setForeground(GuiConstants.FOREGROUND_COLOR);
        FRIENDS_SCROLL_PANE.setPreferredSize(new Dimension(160, 220));
        FRIENDS_SCROLL_PANE.setMaximumSize(new Dimension(160, 220));

        // Setup buttons
        ADD_FRIEND_BUTTON.setPreferredSize(new Dimension(20, 20));
        ADD_FRIEND_BUTTON.addActionListener(e -> WordQuizzleClient.POOL.execute(new SendFriendshipRequestOperator()));

        CHALLENGE_BUTTON.addActionListener(e -> WordQuizzleClient.POOL.execute(new SendChallengeRequestOperator(FRIENDS_LIST.getSelectedValue())));
        CHALLENGE_BUTTON.setAlignmentX(Component.CENTER_ALIGNMENT);
    }

    public static void setUp()
    {
        // Empty panel
        PANEL.removeAll();

        // Initialize container
        JPanel friendsPanelHeader = new JPanel();

        // Setup friends panel header
        friendsPanelHeader.setBackground(GuiConstants.BACKGROUND_COLOR);
        friendsPanelHeader.setLayout(new BorderLayout());
        friendsPanelHeader.setPreferredSize(new Dimension(160, 25));
        friendsPanelHeader.setMaximumSize(new Dimension(160, 25));

        // Setup challenge button
        CHALLENGE_BUTTON.setEnabled(false);

        // Add components to friends panel header
        friendsPanelHeader.add(FRIEND_LIST_LABEL, BorderLayout.WEST);
        friendsPanelHeader.add(ADD_FRIEND_BUTTON, BorderLayout.EAST);

        // Add components (with margins) to friends panel
        PANEL.add(Box.createRigidArea(new Dimension(0, 10)));
        PANEL.add(friendsPanelHeader);
        PANEL.add(Box.createRigidArea(new Dimension(0, 5)));
        PANEL.add(FRIENDS_SCROLL_PANE);
        PANEL.add(Box.createRigidArea(new Dimension(0, 10)));
        PANEL.add(CHALLENGE_BUTTON);
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(180, 400);
    }

    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(180, 400);
    }
}
