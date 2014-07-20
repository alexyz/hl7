package hu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import ca.uhn.hl7v2.HL7Exception;

class EditorPanel extends JPanel {
	private static final Font MONO = new Font("monospaced", 0, 14);
	private static final Color ERROR = new Color(255, 192, 192);
	
	private final JTextArea textArea = new JTextArea();
	private final JTextField descField = new JTextField();
	private final JTextField pathField = new JTextField();
	private final JTextField valueField = new JTextField();
	private final TreeMap<Pos,Object> objs = new TreeMap<>();
	
	private Info info;
	
	public EditorPanel () {
		super(new BorderLayout());
		textArea.setBorder(new TitledBorder("Area"));
		textArea.setFont(MONO);
		textArea.setLineWrap(true);
		textArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate (CaretEvent ce) {
				caretUpdated(Math.min(ce.getMark(), ce.getDot()));
			}
		});
		pathField.setBorder(new TitledBorder("Terser Path"));
		pathField.setFont(MONO);
		pathField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				pathUpdated();
			}
		});
		valueField.setBorder(new TitledBorder("Value"));
		valueField.setFont(MONO);
		valueField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				valueUpdated();
			}
		});
		JPanel fieldsPanel = new JPanel(new GridLayout(3, 1));
		fieldsPanel.add(pathField);
		fieldsPanel.add(valueField);
		fieldsPanel.add(descField);
		descField.setBorder(new TitledBorder("Description"));
		descField.setFont(MONO);
		JScrollPane textAreaScroller = new JScrollPane(textArea);
		add(textAreaScroller, BorderLayout.CENTER);
		add(fieldsPanel, BorderLayout.SOUTH);
	}
	
	private void caretUpdated (int i) {
		System.out.println("caret updated");
		info = Util.getInfo(textArea.getText(), i);
		
		String currentError = null;

		if (info.errors != null) {
			Highlighter h = textArea.getHighlighter();
			
			h.removeAllHighlights();
			for (VE ve : info.errors) {
				if (ve.pos.equals(info.pos)) {
					currentError = ve.msg;
				}
				try {
					System.out.println("add highlight " + ve.indexes[0] + ", " + ve.indexes[1]);
					h.addHighlight(ve.indexes[0],ve.indexes[1],new DefaultHighlighter.DefaultHighlightPainter(ERROR));
				} catch (BadLocationException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
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
		if (info != null && info.msg != null) {
			System.out.println("update value");
			String value = "";
			try {
				value = info.t.get(pathField.getText());
			} catch (HL7Exception e1) {
				value = e1.getMessage();
			}
			valueField.setText(value);
		}
	}
	
	private void valueUpdated () {
		System.out.println("value updated");
	}
	
}
