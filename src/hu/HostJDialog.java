package hu;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class HostJDialog extends JDialog {

	public static void main(String[] args) {
		new HostJDialog(null, "localhost", 123).setVisible(true);
		System.exit(0);
	}
	
	private final JTextField hostField = new JTextField(10);
	private final JSpinner portSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 65535, 1));
	private boolean ok;
	
	public HostJDialog(Frame frame, String host, int port) {
		super(frame, "Send To");
		
		hostField.setText(host);
		portSpinner.setValue(port);
		
		JPanel p = new JPanel();
		p.add(new JLabel("Host"));
		p.add(hostField);
		p.add(new JLabel("Port"));
		p.add(portSpinner);
		
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ok = true;
				setVisible(false);
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		
		JPanel p2 = new JPanel();
		p2.add(okButton);
		p2.add(cancelButton);
		
		JPanel p3 = new JPanel(new BorderLayout());
		p3.add(p, BorderLayout.CENTER);
		p3.add(p2, BorderLayout.SOUTH);
		
		setContentPane(p3);
		setModal(true);
		pack();
		setLocationRelativeTo(frame);
	}
	
	public String getHost() {
		return hostField.getText();
	}
	
	public int getPort() {
		return (int) portSpinner.getValue();
	}

	public boolean isOk() {
		return ok;
	}
}
