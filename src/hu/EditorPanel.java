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

import ca.uhn.hl7v2.HL7Exception;

class EditorPanel extends JPanel {
	private static final Color ERROR = new Color(255, 192, 192);
	
	/** message area, always represent segments separators as line feeds */
	private final JTextArea messageArea = new JTextArea();
	private final JTextArea descriptionArea = new JTextArea(3, 40);
	private final JTextField pathField = new JTextField();
	private final JTextField valueField = new JTextField();
	private final List<Object> highlights = new ArrayList<>();
	
	private Info info;
	private File file;
	private String messageVersion;
	
	public EditorPanel () {
		super(new BorderLayout());
		
		messageArea.setBorder(new TitledBorder("Message"));
		messageArea.setLineWrap(true);
		messageArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate (CaretEvent ce) {
				// avoid error when setting text before version
				if (EditorPanel.this.isShowing()) {
					messageAreaCaretUpdated(Math.min(ce.getMark(), ce.getDot()));
				}
			}
		});
		messageArea.addMouseListener(new MouseAdapter() {
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
		
		pathField.setBorder(new TitledBorder("Terser Path"));
		pathField.addActionListener(new ActionListener() {
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
		
		JScrollPane descScroller = new JScrollPane(descriptionArea);
		descScroller.setBorder(new TitledBorder("Description"));
		
		JScrollPane textAreaScroller = new JScrollPane(messageArea);
		
		descriptionArea.setLineWrap(true);
		descriptionArea.setEditable(false);
		
		JPanel fieldsPanelNorth = new JPanel(new GridLayout(2, 1));
		fieldsPanelNorth.add(pathField);
		fieldsPanelNorth.add(valueField);
		
		JPanel fieldsPanel = new JPanel(new BorderLayout());
		fieldsPanel.add(fieldsPanelNorth, BorderLayout.NORTH);
		fieldsPanel.add(descScroller, BorderLayout.CENTER);
		
		add(textAreaScroller, BorderLayout.CENTER);
		add(fieldsPanel, BorderLayout.SOUTH);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown (ComponentEvent e) {
				System.out.println("component shown");
				messageArea.requestFocusInWindow();
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
		return messageArea.getText();
	}
	
	public void setMessage (String msgLf) {
		messageArea.setText(msgLf);
		messageArea.setCaretPosition(0);
		messageAreaCaretUpdated(0);
	}
	
	public Font getEditorFont () {
		return messageArea.getFont();
	}
	
	public void setEditorFont (Font f) {
		System.out.println("set editor font " + f);
		for (Component comp : Arrays.asList(messageArea, pathField, valueField, descriptionArea)) {
			comp.setFont(f);
		}
	}
	
	public String getMessageVersion () {
		return messageVersion;
	}
	
	public void setMessageVersion (String messageVersion) {
		this.messageVersion = messageVersion;
		messageAreaCaretUpdated(messageArea.getCaretPosition());
	}
	
	private void messageAreaCaretUpdated (int i) {
		System.out.println("caret updated " + i);
		String text = messageArea.getText();
		if (text.length() == 0) {
			return;
		}
		
		info = MsgUtil.getInfo(text, messageVersion, i);
		if (info.pos != null) {
			System.out.println("caret pos " + info.pos);
		}
		
		// do the error highlighting
		String currentError = null;
		if (info.errors != null) {
			Highlighter h = messageArea.getHighlighter();
			for (Object o : highlights) {
				h.removeHighlight(o);
			}
			highlights.clear();
			for (VE ve : info.errors) {
				if (ve.pos.equals(info.pos)) {
					currentError = ve.msg;
				}
				try {
					System.out.println("add highlight " + ve.indexes[0] + ", " + ve.indexes[1]);
					highlights.add(h.addHighlight(ve.indexes[0], ve.indexes[1], new DefaultHighlighter.DefaultHighlightPainter(ERROR)));
				} catch (BadLocationException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		// populate the fields
		if (info.tp != null) {
			if (!pathField.getText().equals(info.tp.path)) {
				pathField.setText(info.tp.path);
			}
			if (!valueField.getText().equals(info.tp.value)) {
				valueField.setText(info.tp.value);
			}
			String desc = info.tp.description;
			if (currentError != null) {
				desc += " -> " + currentError;
			}
			if (!descriptionArea.getText().equals(desc)) {
				descriptionArea.setText(desc);
			}
		}
		
	}
	
	private void pathUpdated () {
		System.out.println("path updated");
		if (info != null && info.t != null) {
			try {
				String value = info.t.get(pathField.getText());
				valueField.setText(value);
			} catch (HL7Exception e1) {
				descriptionArea.setText(e1.getMessage());
			}
		}
	}
	
	private void valueUpdated () {
		System.out.println("value updated");
		
		if (info != null) {
			String msgCr = messageArea.getText().replace('\n', Sep.SEGMENT);
			int[] i1 = MsgUtil.getIndex(msgCr, info.sep, info.pos);
			msgCr = msgCr.substring(0, i1[0]) + valueField.getText() + msgCr.substring(i1[1]);
			
			Pos pos = info.pos;
			messageArea.setText(msgCr.replace(Sep.SEGMENT, '\n'));
			
			int[] i2 = MsgUtil.getIndex(msgCr, info.sep, pos);
			System.out.println("caret to " + i2[1]);
			messageArea.setCaretPosition(i2[1]);
		}
		
		// FIXME should use terser if changing MSH-1 or MSH-2 only
		// if (info != null && info.t != null) {
		// try {
		// info.t.set(pathField.getText(), valueField.getText());
		// String msgstr = info.msg.encode();
		// Pos pos = info.pos;
		// // this will update info
		// msgArea.setText(msgstr.replace(Sep.segSep, '\n'));
		// int[] index = MsgUtil.getIndex(msgstr, info.sep, pos);
		// System.out.println("caret to " + index[1]);
		// msgArea.setCaretPosition(index[1]);
		//
		// } catch (HL7Exception e1) {
		// descArea.setText(e1.getMessage());
		// }
		// }
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
		menu.show(messageArea, p.x, p.y);
	}
	
	private void cutOrCopy (boolean cut) {
		System.out.println("cut or copy " + cut);
		int s1 = messageArea.getSelectionStart();
		int s2 = messageArea.getSelectionEnd();
		if (s1 != s2) {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(new StringSelection(messageArea.getSelectedText()), null);
			if (cut) {
				String text = messageArea.getText();
				messageArea.setText(text.substring(0, s1) + text.substring(s2));
				messageArea.setCaretPosition(s1);
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
				int s1 = messageArea.getSelectionStart();
				int s2 = messageArea.getSelectionEnd();
				String text = messageArea.getText();
				messageArea.setText(text.substring(0, s1) + transText + text.substring(s2));
				messageArea.setCaretPosition(s2);
			}
		} catch (Exception e) {
			throw new RuntimeException("could not paste", e);
		}
	}
}
