import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPasswordField;


public class JPlaceholderPasswordField extends JPasswordField
{

    private String ph;

    public JPlaceholderPasswordField(String ph) {
        this.ph = ph;
    }

    public JPlaceholderPasswordField() {
        this.ph = null;
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        char[] pass = super.getPassword();

        if (pass.length > 0 || ph == null)
            return;

        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(super.getDisabledTextColor());
        g2.drawString(ph, getInsets().left, g.getFontMetrics().getMaxAscent() + getInsets().top);
    }
}