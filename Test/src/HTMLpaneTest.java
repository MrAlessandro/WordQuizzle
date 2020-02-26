import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class HTMLpaneTest
{
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        JTextPane pane = new JTextPane();

        HTMLEditorKit htmlKit = new HTMLEditorKit();
        HTMLDocument document = (HTMLDocument) htmlKit.createDefaultDocument();

        pane.setEditable(false);
        pane.setContentType("text/html");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 500));
        frame.setMinimumSize(new Dimension(500, 500));
        frame.add(pane);
        frame.setVisible(true);
    }
}
