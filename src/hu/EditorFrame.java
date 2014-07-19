package hu;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import ca.uhn.hl7v2.*;

public class EditorFrame extends JFrame {
	
	public static final HapiContext context = new DefaultHapiContext();
	
	public static void main (String[] args) {
		new EditorFrame().show();
	}
	
	public EditorFrame () {
		super("HL7|^~\\&!");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JTabbedPane p = new JTabbedPane();
		p.addTab("editor", new EditorPanel());
		p.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				System.out.println("mouse click " + e.getClickCount());
			}
		});
		
		setContentPane(p);
		setPreferredSize(new Dimension(800, 600));
		pack();
	}
}
