package client.gui.panels;

import client.gui.constants.GuiConstants;
import client.main.WordQuizzleClient;
import client.operators.SendFriendshipRequestOperator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class FriendsPanel extends JPanel
{
    public static final DefaultListModel<String> FRIENDS_LIST = new DefaultListModel<>();

    public FriendsPanel()
    {
        super();

        // Initialize components
        JPanel friendsPanelHeader = new JPanel();
        JLabel friendListLabel = new JLabel("Friends list:");
        JButton addFriendButton = new JButton("+");
        JList<String> jList = new JList<>(FRIENDS_LIST);
        JScrollPane friendsScrollPane = new JScrollPane(jList);
        JButton challengeButton = new JButton();

        // Setup outer container
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Color.WHITE);
        this.setBorder(new CompoundBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1), new EmptyBorder(10,10,10,10)));

        // Setup friends panel header
        friendsPanelHeader.setBackground(Color.WHITE);
        friendsPanelHeader.setLayout(new BorderLayout());
        friendsPanelHeader.setPreferredSize(new Dimension(160, 25));
        friendsPanelHeader.setMaximumSize(new Dimension(160, 25));

        // Setup add friend button
        addFriendButton.setPreferredSize(new Dimension(20, 20));
        addFriendButton.addActionListener(e -> WordQuizzleClient.POOL.execute(new SendFriendshipRequestOperator()));

        // Setup friend list
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.addListSelectionListener(e -> challengeButton.setEnabled(true));

        // Setup friends scroll pane
        friendsScrollPane.setPreferredSize(new Dimension(160, 220));
        friendsScrollPane.setMaximumSize(new Dimension(160, 220));

        // Setup challenge button
        challengeButton.setText("Challenge");
        challengeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        challengeButton.setEnabled(false);

        // Add components to friends panel header
        friendsPanelHeader.add(friendListLabel, BorderLayout.WEST);
        friendsPanelHeader.add(addFriendButton, BorderLayout.EAST);

        // Add components (with margins) to friends panel
        this.add(friendsPanelHeader);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(friendsScrollPane);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(challengeButton);

    }
}
