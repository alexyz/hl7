package jsui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;

/**
 * modal javascript dialog
 */
public class JSJDialog extends JDialog implements ActionListener {
	
	private static Font monoFont = new Font("monospaced", 0, 14);
	
	public static void main (String[] args) {
		new JSJDialog(null, "JSDialog", monoFont, null, "3+4").setVisible(true);
		System.out.println("end");
		System.exit(0);
	}
	
	private final JSJPanel jsPanel = new JSJPanel();
	
	public JSJDialog (Frame frame, String title, Font font, Map<String,Object> m, String text) {
		super(frame, title);
		setModal(true);
		setPreferredSize(new Dimension(640, 480));
		jsPanel.setJSFont(font);
		jsPanel.setJSData(m);
		jsPanel.setJSText(text);
		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this);
		jsPanel.getButtonPanel().add(closeButton);
		
		setContentPane(jsPanel);
		pack();
	}
	
	@Override
	public void actionPerformed (ActionEvent e) {
		setVisible(false);
	}
	
	public String getInputText() {
		return jsPanel.getInputText();
	}
}
