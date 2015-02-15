package hu.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class FontJDialog extends JDialog implements ItemListener, ChangeListener, ActionListener {
	
	private final JLabel fontLabel = new JLabel("Abc Def");
	private final JComboBox<String> fontCombo = new JComboBox<>(new String[] { "Monospaced", "SansSerif", "Serif" });
	private final JCheckBox boldBox = new JCheckBox("Bold");
	private final JCheckBox italicBox = new JCheckBox("Italic");
	private final JSpinner sizeSpinner = new JSpinner(new SpinnerNumberModel(14, 1, 99, 1));
	
	private Font font;
	
	public FontJDialog (JFrame frame, Font font) {
		super(frame, "Font", ModalityType.DOCUMENT_MODAL);
		fontCombo.setSelectedItem(font.getName());
		sizeSpinner.setValue(font.getSize());
		boldBox.setSelected((font.getStyle() & Font.BOLD) != 0);
		italicBox.setSelected((font.getStyle() & Font.ITALIC) != 0);
		
		fontCombo.addItemListener(this);
		sizeSpinner.addChangeListener(this);
		italicBox.addItemListener(this);
		boldBox.addItemListener(this);
		fontLabel.setHorizontalAlignment(SwingConstants.CENTER);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(this);
		
		update();
		
		JPanel controlsPanel = new JPanel();
		controlsPanel.add(fontCombo);
		controlsPanel.add(sizeSpinner);
		controlsPanel.add(boldBox);
		controlsPanel.add(italicBox);
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		
		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(controlsPanel, BorderLayout.NORTH);
		contentPanel.add(fontLabel, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		setContentPane(contentPanel);
		pack();
		setLocationRelativeTo(frame);
	}
	
	@Override
	public void itemStateChanged (ItemEvent e) {
		update();
	}
	
	@Override
	public void stateChanged (ChangeEvent e) {
		update();
	}
	
	@Override
	public void actionPerformed (ActionEvent e) {
		font = fontLabel.getFont();
		setVisible(false);
	}
	
	private void update() {
		Font f = new Font((String) fontCombo.getSelectedItem(), (boldBox.isSelected() ? Font.BOLD : 0) + (italicBox.isSelected() ? Font.ITALIC : 0), (int) sizeSpinner.getValue());
		fontLabel.setFont(f);
		pack();
	}
	
	public Font getSelectedFont() {
		return font;
	}
}
