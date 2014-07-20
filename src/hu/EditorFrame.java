package hu;

import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class EditorFrame extends JFrame {
	
	public static void main (String[] args) {
		new EditorFrame().show();
	}
	
	public EditorFrame () {
		super("HAPI HL7|^~\\&!");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Logger.getRootLogger().setLevel(Level.INFO);
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
