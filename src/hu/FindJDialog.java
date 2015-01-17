package hu;

import hu.mv.FindingMessageVisitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import org.apache.commons.lang3.text.WordUtils;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.MessageVisitors;

public class FindJDialog extends JDialog {
	
	private final JTextField textField = new JTextField(10);
	private final JButton findButton = new JButton("Find");
	private final JButton selectButton = new JButton("Select");
	private final JButton closeButton = new JButton("Close");
	private final JTable table = new JTable();
	private final JCheckBox ignoreCaseBox = new JCheckBox("Ignore Case");
	private final Message msg;
	
	private String findText;
	
	public FindJDialog (JFrame frame, Message msg, String findText) {
		super(frame, "Find");
		this.msg = msg;
		this.findText = findText;
		textField.setText(findText);
		loadPrefs();
		initComps();
		pack();
	}
	
	private void initComps () {
		textField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				find();
			}
		});
		
		findButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				find();
			}
		});
		
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				setVisible(false);
			}
		});
		
		JPanel p1 = new JPanel();
		p1.add(new JLabel("Text"));
		p1.add(textField);
		p1.add(ignoreCaseBox);
		p1.add(findButton);
		JScrollPane sp = new JScrollPane(table);
		JPanel p2 = new JPanel();
		p2.add(selectButton);
		p2.add(closeButton);
		JPanel p = new JPanel(new BorderLayout());
		p.add(p1, BorderLayout.NORTH);
		p.add(sp, BorderLayout.CENTER);
		p.add(p2, BorderLayout.SOUTH);
		setContentPane(p);
		setPreferredSize(new Dimension(480, 320));
	}
	
	private void loadPrefs () {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		ignoreCaseBox.setSelected(prefs.getBoolean("ignoreCase", false));
	}
	
	private void savePrefs () {
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		prefs.putBoolean("ignoreCase", ignoreCaseBox.isSelected());
		try {
			prefs.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void find () {
		System.out.println("find");
		String text = textField.getText();
		if (text.length() > 0) {
			try {
				FindingMessageVisitor fmv = new FindingMessageVisitor(text, ignoreCaseBox.isSelected());
				MessageVisitors.visit(msg, fmv);
				table.setModel(new MsgPathTableModel(fmv.getList()));
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, WordUtils.wrap(e.toString(), 80), "Find", JOptionPane.ERROR_MESSAGE);
			}
		}
		this.findText = text;
		savePrefs();
	}
	
	public String getFindText () {
		return findText;
	}
}