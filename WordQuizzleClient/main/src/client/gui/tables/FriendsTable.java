package client.gui.tables;

import client.settings.Settings;

import javax.swing.*;
import java.awt.*;

public class FriendsTable extends JList<FriendRecord>
{
    private final DefaultListModel<FriendRecord> listModel;

    public FriendsTable()
    {
        super();

        this.setBackground(Settings.BACKGROUND_COLOR);
        this.setForeground(Settings.FOREGROUND_COLOR);
        this.listModel = new DefaultListModel<>();
        this.setModel(this.listModel);
        this.setCellRenderer(new CellRender());
    }

    public void addRecord(String friend, int score)
    {
        this.listModel.addElement(new FriendRecord(friend, score));
    }

    private static class CellRender extends JPanel implements ListCellRenderer<FriendRecord>
    {
        private final JLabel friendLabel;
        private final JLabel scoreLabel;


        public CellRender()
        {
            super();

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.friendLabel = new JLabel();
            this.scoreLabel = new JLabel();

            this.add(this.friendLabel);
            this.add(Box.createHorizontalGlue());
            this.add(this.scoreLabel);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends FriendRecord> list, FriendRecord value, int index, boolean isSelected, boolean cellHasFocus)
        {
            this.friendLabel.setText(value.friendName);
            this.scoreLabel.setText(String.valueOf(value.friendScore));

            if (isSelected)
            {
                this.setBackground(Settings.MAIN_COLOR);
                friendLabel.setBackground(Settings.MAIN_COLOR);
                friendLabel.setForeground(Settings.BACKGROUND_COLOR);
                scoreLabel.setBackground(Settings.MAIN_COLOR);
                scoreLabel.setForeground(Settings.BACKGROUND_COLOR);
            }
            else
            {
                this.setBackground(Settings.BACKGROUND_COLOR);
                friendLabel.setBackground(Settings.BACKGROUND_COLOR);
                friendLabel.setForeground(Settings.MAIN_COLOR);
                scoreLabel.setBackground(Settings.BACKGROUND_COLOR);
                scoreLabel.setForeground(Settings.MAIN_COLOR);
            }

            return this;
        }
    }
}
