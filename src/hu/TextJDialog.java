package hu;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextJDialog extends JDialog {
	public TextJDialog (JFrame frame, String title, Font font, String text) {
		super(frame, title);
		JTextArea ta = new JTextArea();
		ta.setText(text);
		ta.setFont(font);
		ta.setEditable(false);
		ta.setLineWrap(true);
		ta.setCaretPosition(0);
		JScrollPane sp = new JScrollPane(ta);
		setContentPane(sp);
		setPreferredSize(new Dimension(640, 480));
		pack();
		setLocationRelativeTo(frame);
	}
}
