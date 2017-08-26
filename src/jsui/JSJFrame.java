package jsui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.util.*;
import java.util.prefs.Preferences;

import javax.swing.*;

/**
 * javascript jframe and main method
 */
public class JSJFrame extends JFrame {
	
	public static void main (String[] args) {
		JSJFrame ui = new JSJFrame();
		ui.show();
	}
	
	private final JSJPanel jsPanel = new JSJPanel();
	
	public JSJFrame () {
		super("JSUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initComps();
		loadPrefs();
		
		TreeMap<String,Object> m = new TreeMap<>();
		m.put("frame", this);
		jsPanel.setJSData(m);
	}

	private void initComps () {
		JButton fontButton = new JButton("Font...");
		fontButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				font();
			}
		});
		jsPanel.getButtonPanel().add(fontButton);
		setPreferredSize(new Dimension(640, 480));
		setContentPane(jsPanel);
		pack();
	}
	
	private void loadPrefs() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		String name = prefs.get("fontname", "monospaced");
		int size = prefs.getInt("fontsize", 14);
		int style = prefs.getInt("fontstyle", 0);
		final Font font = new Font(name, style, size);
		jsPanel.setJSFont(font);
	}
	
	private void savePrefs() {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		Font font = jsPanel.getJSFont();
		prefs.put("fontname", font.getName());
		prefs.putInt("fontsize", font.getSize());
		prefs.putInt("fontstyle", font.getStyle());
		try {
			prefs.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void font() {
		FontJDialog dialog = new FontJDialog(this, jsPanel.getJSFont());
		dialog.setVisible(true);
		Font font = dialog.getSelectedFont();
		if (font != null) {
			jsPanel.setJSFont(font);
			savePrefs();
		}
	}
}
