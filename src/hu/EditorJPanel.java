package hu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.text.*;

public class EditorJPanel extends JPanel {
	private static final Color ERROR = new Color(255, 192, 192);
	
	/** message area, always represent segments separators as line feeds */
	private final JTextArea msgArea = new JTextArea();
	private final JTextArea descriptionArea = new JTextArea(3, 40);
	private final JTextField pathField = new JTextField();
	private final JTextField valueField = new JTextField();
	private final List<Object> highlights = new ArrayList<>();
	
	private File file;
	private String msgVersion;
	
	public EditorJPanel () {
		super(new BorderLayout());
		
		msgArea.setBorder(new TitledBorder("Message"));
		msgArea.setLineWrap(true);
		msgArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate (CaretEvent ce) {
				// avoid error when setting text before version
				if (EditorJPanel.this.isShowing()) {
					//update(Math.min(ce.getMark(), ce.getDot()));
					update();
				}
			}
		});
		msgArea.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed (MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup(e.getPoint());
				}
			}
			
			@Override
			public void mouseReleased (MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup(e.getPoint());
				}
			}
			
		});
		
		pathField.setBorder(new TitledBorder("HAPI Terser Path"));
		pathField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				pathUpdated();
			}
		});
		
		JButton pathButton = new JButton("Get");
		pathButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				pathUpdated();
			}
		});
		
		valueField.setBorder(new TitledBorder("Value"));
		valueField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				valueUpdated();
			}
		});
		
		JButton valueButton = new JButton("Set");
		valueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				valueUpdated();
			}
		});
		
		JScrollPane descScroller = new JScrollPane(descriptionArea);
		descScroller.setBorder(new TitledBorder("Description"));
		
		JScrollPane msgScroller = new JScrollPane(msgArea);
		
		descriptionArea.setLineWrap(true);
		descriptionArea.setEditable(false);
		
		JPanel pathPanel = new JPanel(new BorderLayout());
		pathPanel.add(pathField, BorderLayout.CENTER);
		pathPanel.add(pathButton, BorderLayout.EAST);
		
		JPanel valuePanel = new JPanel(new BorderLayout());
		valuePanel.add(valueField, BorderLayout.CENTER);
		valuePanel.add(valueButton, BorderLayout.EAST);
		
		JPanel fieldsPanelNorth = new JPanel(new GridLayout(2, 1));
		fieldsPanelNorth.add(pathPanel);
		fieldsPanelNorth.add(valuePanel);
		
		JPanel fieldsPanel = new JPanel(new BorderLayout());
		fieldsPanel.add(fieldsPanelNorth, BorderLayout.NORTH);
		fieldsPanel.add(descScroller, BorderLayout.CENTER);
		
		add(msgScroller, BorderLayout.CENTER);
		add(fieldsPanel, BorderLayout.SOUTH);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown (ComponentEvent e) {
				System.out.println("component shown");
				msgArea.requestFocusInWindow();
			}
		});
	}
	
	public File getFile () {
		return file;
	}
	
	public void setFile (File file) {
		this.file = file;
	}
	
	public String getMessage () {
		return msgArea.getText();
	}
	
	public void setMessage (String msgLf) {
		msgArea.setText(msgLf);
		msgArea.setCaretPosition(0);
		update();
	}
	
	public Font getEditorFont () {
		return msgArea.getFont();
	}
	
	public void setEditorFont (Font f) {
		System.out.println("set editor font " + f);
		for (Component comp : Arrays.asList(msgArea, pathField, valueField, descriptionArea)) {
			comp.setFont(f);
		}
	}
	
	public String getMsgVersion () {
		return msgVersion;
	}
	
	public void setMsgVersion (String messageVersion) {
		this.msgVersion = messageVersion;
		update();
	}
	
	private void update () {
		System.out.println("update");
		int i = msgArea.getCaretPosition();
		
		String msgLf = msgArea.getText();
		if (msgLf.length() == 0) {
			System.out.println("update: no message");
			return;
		}
		
		try {
			
			// clear error highlighting
			Highlighter h = msgArea.getHighlighter();
			for (Object o : highlights) {
				h.removeHighlight(o);
			}
			highlights.clear();
			
			// do error highlighting
			
			final Info info = MsgUtil.getInfo(msgLf, msgVersion);
			final Pos pos = MsgUtil.getPosition(info.msgCr, info.sep, i);
			final List<VE> errors = MsgUtil.getErrors(info.msg, info.msgCr, info.sep, msgVersion);
			System.out.println("errors: " + errors.size());
			
			String currentError = null;
			for (VE ve : errors) {
				if (ve.pos.equals(pos)) {
					currentError = ve.msg;
				}
				System.out.println("add highlight " + ve.indexes[0] + ", " + ve.indexes[1]);
				highlights.add(h.addHighlight(ve.indexes[0], ve.indexes[1], new DefaultHighlighter.DefaultHighlightPainter(ERROR)));
			}
			
			// populate the fields
			TP terserPath = MsgUtil.getTerserPath(info.msg, info.terser, pos);
			if (terserPath != null) {
				if (!pathField.getText().equals(terserPath.path)) {
					pathField.setText(terserPath.path);
				}
				if (!valueField.getText().equals(terserPath.value)) {
					valueField.setText(terserPath.value);
				}
				String desc = terserPath.description;
				if (currentError != null) {
					desc += " \u2192 " + currentError;
				}
				if (!descriptionArea.getText().equals(desc)) {
					descriptionArea.setText(desc);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			descriptionArea.setText(e.toString());
		}
	}
	
	private void pathUpdated () {
		System.out.println("path updated");
		
		try {
			Info info = MsgUtil.getInfo(msgArea.getText(), msgVersion);
			String value = info.terser.get(pathField.getText());
			valueField.setText(value);
			// TODO update description, caret (if bounded!)
			
		} catch (Exception e) {
			e.printStackTrace();
			descriptionArea.setText(e.toString());
		}
	}
	
	private void valueUpdated () {
		System.out.println("value updated");
		
		try {
			String msgLf = msgArea.getText();
			Info info = MsgUtil.getInfo(msgLf, msgVersion);
			// XXX caret might not represent value position
			Pos pos = MsgUtil.getPosition(info.msgCr, info.sep, msgArea.getCaretPosition());
			
			if (pos.segOrd == 1 && pos.fieldOrd <= 2) {
				// need to use terser for these
				System.out.println("update msh-1 or msh-2 field");
				info.terser.set(pathField.getText(), valueField.getText());
				String msgCr = info.msg.encode();
				msgArea.setText(msgCr.replace(Sep.SEGMENT, '\n'));
				
			} else {
				// simple string substitution
				System.out.println("update none msh-1 or msh-2 field");
				int[] i1 = MsgUtil.getIndex(info.msgCr, info.sep, pos);
				
				msgLf = msgLf.substring(0, i1[0]) + valueField.getText() + msgLf.substring(i1[1]);
				
				msgArea.setText(msgLf);
				
				int[] i2 = MsgUtil.getIndex(info.msgCr, info.sep, pos);
				msgArea.setCaretPosition(i2[1]);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			descriptionArea.setText(e.toString());
		}
	}
	
	private void popup (Point p) {
		JMenuItem cutItem = new JMenuItem("Cut");
		cutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				cutOrCopy(true);
			}
		});
		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				cutOrCopy(false);
			}
		});
		JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				paste();
			}
		});
		
		JPopupMenu menu = new JPopupMenu("Editor");
		menu.add(cutItem);
		menu.add(copyItem);
		menu.add(pasteItem);
		menu.show(msgArea, p.x, p.y);
	}
	
	private void cutOrCopy (boolean cut) {
		System.out.println("cut or copy " + cut);
		int s1 = msgArea.getSelectionStart();
		int s2 = msgArea.getSelectionEnd();
		if (s1 != s2) {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(new StringSelection(msgArea.getSelectedText()), null);
			if (cut) {
				String text = msgArea.getText();
				msgArea.setText(text.substring(0, s1) + text.substring(s2));
				msgArea.setCaretPosition(s1);
			}
		}
	}
	
	private void paste () {
		System.out.println("paste");
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clip.getContents(null);
		try {
			String transText = (String) t.getTransferData(DataFlavor.stringFlavor);
			if (transText != null) {
				int s1 = msgArea.getSelectionStart();
				int s2 = msgArea.getSelectionEnd();
				String text = msgArea.getText();
				msgArea.setText(text.substring(0, s1) + transText + text.substring(s2));
				msgArea.setCaretPosition(s2);
			}
		} catch (Exception e) {
			throw new RuntimeException("could not paste", e);
		}
	}
}
