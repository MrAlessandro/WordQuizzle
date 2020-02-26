package client.gui.panels;

import client.gui.constants.GuiConstants;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongUnaryOperator;

public class ScoresPanel extends JPanel
{
    public static final DefaultListModel<String> SCORES_LIST_MODEL = new DefaultListModel<>();
    public static final JList<String> SCORES_LIST = new JList<>(SCORES_LIST_MODEL);
    public static final JScrollPane SCORES_SCROLL_PANE = new JScrollPane(SCORES_LIST);
    public static final JLabel RANKING_LABEL = new JLabel("Ranking:");
    public static final JLabel COUNT_DOWN_LABEL = new JLabel();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("mm:ss");
    public static final AtomicLong TIME_LEFT = new AtomicLong(0);
    public static final LongUnaryOperator TIME_LEFT_UPDATE_FUNCTION = (X) -> {
        if (X > 0)
            return X - 1000;
        else
            return 0;
    };
    public static final ActionListener TICK_FUNCTION = e -> {
        long timeLeft = TIME_LEFT.updateAndGet(TIME_LEFT_UPDATE_FUNCTION);
        if (timeLeft == 0)
        {
            SwingUtilities.invokeLater(() -> COUNT_DOWN_LABEL.setText(""));
            ScoresPanel.TIMER.stop();
        }
        else
            SwingUtilities.invokeLater(() -> COUNT_DOWN_LABEL.setText("Time left: " + DATE_FORMAT.format(TIME_LEFT)));
    };
    public static final Timer TIMER = new Timer(1000, TICK_FUNCTION);
    public static final ScoresPanel PANEL = new ScoresPanel();

    private ScoresPanel()
    {
        super();

        // Setup scores panel
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBackground(GuiConstants.BACKGROUND_COLOR);
        this.setBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1));

        // Setup timer label
        COUNT_DOWN_LABEL.setFont(new Font("", Font.BOLD, 18));
        COUNT_DOWN_LABEL.setForeground(GuiConstants.MAIN_COLOR);
        COUNT_DOWN_LABEL.setAlignmentY(Component.CENTER_ALIGNMENT);
        COUNT_DOWN_LABEL.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Setup label
        RANKING_LABEL.setForeground(GuiConstants.MAIN_COLOR);

        // Setup friends list
        SCORES_LIST.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        // Setup scroll pane
        SCORES_SCROLL_PANE.setBorder(new LineBorder(GuiConstants.MAIN_COLOR, 1));
        SCORES_SCROLL_PANE.setBackground(GuiConstants.SCROLL_PANE_BACKGROUND_COLOR);
        SCORES_SCROLL_PANE.setForeground(GuiConstants.FOREGROUND_COLOR);
        SCORES_SCROLL_PANE.setPreferredSize(new Dimension(160, 220));
        SCORES_SCROLL_PANE.setMaximumSize(new Dimension(160, 220));

    }

    public static void setUp()
    {
        // Empty panel
        PANEL.removeAll();

        // Setup label container
        JPanel labelContainer = new JPanel();
        labelContainer.setBackground(GuiConstants.BACKGROUND_COLOR);
        labelContainer.setLayout(new BorderLayout());
        labelContainer.setPreferredSize(new Dimension(160, 25));
        labelContainer.setMaximumSize(new Dimension(160, 25));

        // Add ranking label to Label container
        labelContainer.add(RANKING_LABEL, BorderLayout.WEST);

        // Add elements to outer container
        PANEL.add(Box.createRigidArea(new Dimension(0, 10)));
        PANEL.add(COUNT_DOWN_LABEL);
        PANEL.add(Box.createRigidArea(new Dimension(0, 10)));
        PANEL.add(labelContainer);
        PANEL.add(Box.createRigidArea(new Dimension(0, 5)));
        PANEL.add(SCORES_SCROLL_PANE);
    }

    public static void launchTimer(int timeAmount)
    {
        TIME_LEFT.set(timeAmount * 1000);

        SwingUtilities.invokeLater(() -> COUNT_DOWN_LABEL.setText("Time left: " + DATE_FORMAT.format(TIME_LEFT)));

        TIMER.start();
    }

    public static void stopTimer()
    {
        TIMER.stop();
        SwingUtilities.invokeLater(() -> COUNT_DOWN_LABEL.setText(""));
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
