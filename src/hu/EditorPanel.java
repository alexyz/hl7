package hu;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;


class EditorPanel extends JPanel {
	private static final Font MONO = new Font("monospaced", 0, 14);
	private final JTextArea textArea = new JTextArea();
	private final JTextField descField = new JTextField();
	private final JTextField pathField = new JTextField();
	private final JTextField valueField = new JTextField();
	
	public EditorPanel () {
		super(new BorderLayout());
		textArea.setBorder(new TitledBorder("Area"));
		textArea.setFont(MONO);
		textArea.setLineWrap(true);
		textArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate (CaretEvent e) {
				String[] path = Util.getTerserPath(textArea.getText(), Math.min(e.getMark(), e.getDot()));
				pathField.setText(path[0]);
				valueField.setText(path[1]);
				descField.setText(path[2]);
			}
		});
		pathField.setBorder(new TitledBorder("HAPI Terser Path"));
		pathField.setFont(MONO);
		valueField.setBorder(new TitledBorder("Value"));
		valueField.setFont(MONO);
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
	
}