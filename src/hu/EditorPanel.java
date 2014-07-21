package hu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
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
	
	private final JTextArea textArea = new JTextArea();
	private final JTextField descField = new JTextField();
	private final JTextField pathField = new JTextField();
	private final JTextField valueField = new JTextField();
	private final List<Object> highlights = new ArrayList<>();
	
	private Info info;
	private File file;
	private String messageVersion;
	
	public EditorPanel () {
		super(new BorderLayout());
		
		textArea.setBorder(new TitledBorder("Area"));
		textArea.setLineWrap(true);
		textArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate (CaretEvent ce) {
				// avoid error when setting text before version
				if (EditorPanel.this.isShowing()) {
					caretUpdated(Math.min(ce.getMark(), ce.getDot()));
				}
			}
		});
		textArea.setTransferHandler(new TH());
		
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
		
		descField.setBorder(new TitledBorder("Description"));
		
		JScrollPane textAreaScroller = new JScrollPane(textArea);
		
		JPanel fieldsPanel = new JPanel(new GridLayout(3, 1));
		fieldsPanel.add(pathField);
		fieldsPanel.add(valueField);
		fieldsPanel.add(descField);
		
		add(textAreaScroller, BorderLayout.CENTER);
		add(fieldsPanel, BorderLayout.SOUTH);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown (ComponentEvent e) {
				System.out.println("component shown");
				textArea.setCaretPosition(0);
				textArea.requestFocusInWindow();
			}
		});
	}
	
	public File getFile () {
		return file;
	}
	
	public void setFile (File file) {
		this.file = file;
	}
	
	public String getText() {
		return textArea.getText();
	}
	
	public void setText(String text) {
		textArea.setText(text);
	}
	
	public Font getEditorFont (Font f) {
		return textArea.getFont();
	}

	public void setEditorFont (Font f) {
		System.out.println("set editor font " + f);
		for (Component comp : Arrays.asList(textArea, pathField, valueField, descField)) {
			comp.setFont(f);
		}
	}
	
	public String getMessageVersion () {
		return messageVersion;
	}

	public void setMessageVersion (String version) {
		this.messageVersion = version;
		caretUpdated(textArea.getCaretPosition());
	}

	private void caretUpdated (int i) {
		System.out.println("caret updated " + i);
		String text = textArea.getText();
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
			Highlighter h = textArea.getHighlighter();
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
					highlights.add(h.addHighlight(ve.indexes[0],ve.indexes[1],new DefaultHighlighter.DefaultHighlightPainter(ERROR)));
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
			if (!descField.getText().equals(desc)) {
				descField.setText(desc);
			}
		}
		
	}
	
	private void pathUpdated() {
		System.out.println("path updated");
		if (info != null && info.t != null) {
			try {
				String value = info.t.get(pathField.getText());
				valueField.setText(value);
			} catch (HL7Exception e1) {
				descField.setText(e1.getMessage());
			}
		}
	}
	
	private void valueUpdated () {
		System.out.println("value updated");
		if (info != null && info.t != null) {
			try {
				info.t.set(pathField.getText(), valueField.getText());
				String msgstr = info.msg.encode();
				Pos pos = info.pos;
				// this will update info
				textArea.setText(msgstr.replace(Sep.segSep, '\n'));
				int[] index = MsgUtil.getIndex(msgstr, info.sep, pos);
				System.out.println("caret to " + index[1]);
				textArea.setCaretPosition(index[1]);
			} catch (HL7Exception e1) {
				descField.setText(e1.getMessage());
			}
		}
	}

}
