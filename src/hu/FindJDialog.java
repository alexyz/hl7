package hu;

import hu.mv.FindingMessageVisitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.*;
import java.util.prefs.Preferences;

import javax.swing.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import ca.uhn.hl7v2.model.*;

public class FindJDialog extends JDialog {
	
	private final JTextField textField = new JTextField(10);
	private final JButton findButton = new JButton("Find");
	private final JButton selectButton = new JButton("Select");
	private final JButton closeButton = new JButton("Close");
	private final JTable table = new JTable();
	private final JCheckBox ignoreCaseBox = new JCheckBox("Ignore Case");
	private final Message msg;
	
	private String findText;
	private String terserPath;
	
	public FindJDialog (Window window, Message msg, String findText) {
		super(window, "Find");
		this.msg = msg;
		this.findText = findText;
		textField.setText(findText);
		loadPrefs();
		initComps();
		pack();
		if (StringUtils.isNotBlank(findText)) {
			find();
		}
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
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if (e.getClickCount() == 2) {
					select(table.rowAtPoint(e.getPoint()));
				}
			}
		});
		
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				select(table.getSelectedRow());
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
				showException("Title", e);
			}
		}
		this.findText = text;
		savePrefs();
	}

	private void showException (String title, Exception e) {
		JOptionPane.showMessageDialog(this, WordUtils.wrap(e.toString(), 80), title, JOptionPane.ERROR_MESSAGE);
	}
	
	public String getFindText () {
		return findText;
	}
	
	public String getTerserPath () {
		return terserPath;
	}

	private void select (int viewRow) {
		if (viewRow >= 0) {
			int row = table.convertRowIndexToModel(viewRow);
			terserPath = (String) ((MsgPathTableModel)table.getModel()).getValueAt(row, 0);
			setVisible(false);
		}
	}
}