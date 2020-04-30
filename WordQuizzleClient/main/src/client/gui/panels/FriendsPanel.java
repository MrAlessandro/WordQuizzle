package client.gui.panels;

import client.gui.WordQuizzleClientFrame;
import client.gui.tables.FriendsTable;
import client.operators.SendChallengeRequestOperator;
import client.operators.SendFriendshipRequestOperator;
import client.settings.Settings;
import org.json.simple.JSONObject;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collection;

public class FriendsPanel extends JPanel
{
    private final WordQuizzleClientFrame parentFrame;
    private ChallengePanel challengePanel;

    private final FriendsTable friendsTable;
    private final JPanel friendsScrollPaneHeader;
    private final JScrollPane friendsScrollPane;
    private final JLabel friendListLabel;
    private final JButton addFriendButton;
    private final JButton challengeButton;

    public FriendsPanel(WordQuizzleClientFrame parentFrame, ChallengePanel challengePanel)
    {
        super();

        // Set parent frame
        this.parentFrame = parentFrame;

        // Set relative challenge panel
        this.challengePanel = challengePanel;

        // Setup Friend panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(Settings.BACKGROUND_COLOR);
        this.setBorder(new LineBorder(Settings.MAIN_COLOR, 1));

        // Setup friend table
        this.friendsTable = new FriendsTable();
        this.friendsTable.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                if (!challengePanel.isBusy())
                    challengeButton.setEnabled(true);
            }
        });

        // Setup label
        this.friendListLabel = new JLabel("Friends list:");
        this.friendListLabel.setForeground(Settings.MAIN_COLOR);

        // Setup scroll pane
        this.friendsScrollPane = new JScrollPane(friendsTable);
        this.friendsScrollPane.setBorder(new LineBorder(Settings.MAIN_COLOR, 1));
        this.friendsScrollPane.setBackground(Settings.BACKGROUND_COLOR);
        this.friendsScrollPane.setForeground(Settings.FOREGROUND_COLOR);
        this.friendsScrollPane.setPreferredSize(new Dimension(160, 220));
        this.friendsScrollPane.setMaximumSize(new Dimension(160, 220));

        // Setup scroll pane header
        this.friendsScrollPaneHeader = new JPanel();
        this.friendsScrollPaneHeader.setBackground(Settings.BACKGROUND_COLOR);
        this.friendsScrollPaneHeader.setLayout(new BorderLayout());
        this.friendsScrollPaneHeader.setPreferredSize(new Dimension(160, 25));
        this.friendsScrollPaneHeader.setMaximumSize(new Dimension(160, 25));

        // Setup ad friend button buttons
        this.addFriendButton = new JButton("+");
        this.addFriendButton.setPreferredSize(new Dimension(20, 20));
        this.addFriendButton.addActionListener(e -> new SendFriendshipRequestOperator(this.parentFrame).execute());

        // Setup challenge button
        this.challengeButton = new JButton("Challenge");
        this.challengeButton.addActionListener(e -> {
            new SendChallengeRequestOperator(this.parentFrame, this.friendsTable.getSelectedValue().friendName).execute();
            SwingUtilities.invokeLater(() -> {
                challengeButton.setEnabled(false);
                challengePanel.waitingReply(friendsTable.getSelectedValue().friendName);
            });
        });
        this.challengeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.challengeButton.setEnabled(false); // Initially disabled

        // Add components to scroll pane header
        this.friendsScrollPaneHeader.add(this.friendListLabel, BorderLayout.WEST);
        this.friendsScrollPaneHeader.add(this.addFriendButton, BorderLayout.EAST);

        // Add components (with margins) to friends panel
        this.add(Box.createVerticalGlue());
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(this.friendsScrollPaneHeader);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(this.friendsScrollPane);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(this.challengeButton);
        this.add(Box.createVerticalGlue());

    }

    public void setEnableChallengeButton(boolean enabled)
    {
        this.challengeButton.setEnabled(enabled);
    }

    public void setFriendsTable(Collection<JSONObject> friends)
    {
        for (JSONObject item : friends)
        {
            this.friendsTable.addRecord((String) item.get("Username"), Integer.parseInt(String.valueOf(item.get("Score"))));
        }
    }

    public void addFriend(String friend, int score)
    {
        this.friendsTable.addRecord(friend, score);
    }

    public void updateFriendScore(String friend, int score)
    {
        this.friendsTable.updateRecord(friend, score);
    }

    @Override
    public Dimension getPreferredSize()
    {
        return new Dimension(Settings.FRIENDS_PANE_WIDTH, Settings.SESSION_PANE_HEIGHT);
    }

    @Override
    public Dimension getMinimumSize()
    {
        return new Dimension(Settings.FRIENDS_PANE_WIDTH, Settings.SESSION_PANE_HEIGHT);
    }
}
