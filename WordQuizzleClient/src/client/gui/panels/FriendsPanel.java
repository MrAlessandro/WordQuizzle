package client.gui.panels;

import client.gui.constants.GuiConstants;
import client.main.WordQuizzleClient;
import client.operators.SendChallengeRequestOperator;
import client.operators.SendFriendshipRequestOperator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class FriendsPanel extends JPanel
{
    public static final FriendsPanel PANEL = new FriendsPanel();
    public static final DefaultListModel<String> FRIENDS_LIST = new DefaultListModel<>();
    public static JButton challengeButton;

    private FriendsPanel()
    {
        super();

        // Setup Friend panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(GuiConstants.BACKGROUND_COLOR);
        this.setBorder(new CompoundBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1), new EmptyBorder(10,10,10,10)));
    }

    public static void setUp()
    {
        // Empty panel
        PANEL.removeAll();

        // Initialize components
        JPanel friendsPanelHeader = new JPanel();
        JLabel friendListLabel = new JLabel("Friends list:");
        JButton addFriendButton = new JButton("+");
        JList<String> jList = new JList<>(FRIENDS_LIST);
        JScrollPane friendsScrollPane = new JScrollPane(jList);
        challengeButton = new JButton();

        // Setup friends panel header
        friendsPanelHeader.setBackground(GuiConstants.BACKGROUND_COLOR);
        friendsPanelHeader.setLayout(new BorderLayout());
        friendsPanelHeader.setPreferredSize(new Dimension(160, 25));
        friendsPanelHeader.setMaximumSize(new Dimension(160, 25));

        // Setup the friends list label
        friendListLabel.setForeground(GuiConstants.MAIN_COLOR);

        // Setup add friend button
        addFriendButton.setPreferredSize(new Dimension(20, 20));
        addFriendButton.addActionListener(e -> WordQuizzleClient.POOL.execute(new SendFriendshipRequestOperator()));

        // Setup friend list
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.addListSelectionListener(listSelectionEvent -> {
            if (ChallengePanel.challengeable)
                challengeButton.setEnabled(true);
        });

        // Setup friends scroll pane
        friendsScrollPane.setBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1));
        friendsScrollPane.setBackground(GuiConstants.SCROLL_PANE_BACKGROUND_COLOR);
        friendsScrollPane.setForeground(GuiConstants.FOREGROUND_COLOR);
        friendsScrollPane.setPreferredSize(new Dimension(160, 220));
        friendsScrollPane.setMaximumSize(new Dimension(160, 220));

        // Setup challenge button
        challengeButton.setText("Challenge");
        challengeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        challengeButton.addActionListener(e -> WordQuizzleClient.POOL.execute(new SendChallengeRequestOperator(jList.getSelectedValue())));
        challengeButton.setEnabled(false);

        // Add components to friends panel header
        friendsPanelHeader.add(friendListLabel, BorderLayout.WEST);
        friendsPanelHeader.add(addFriendButton, BorderLayout.EAST);

        // Add components (with margins) to friends panel
        PANEL.add(friendsPanelHeader);
        PANEL.add(Box.createRigidArea(new Dimension(0, 5)));
        PANEL.add(friendsScrollPane);
        PANEL.add(Box.createRigidArea(new Dimension(0, 5)));
        PANEL.add(challengeButton);
    }
}
