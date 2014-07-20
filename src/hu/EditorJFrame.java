package hu;

import java.awt.Dimension;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTabbedPane;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/** main class and top level frame */
public class EditorJFrame extends JFrame {
	
	public static EditorJFrame frame = new EditorJFrame();
	
	public static void main (String[] args) {
		// stop hapi spamming the console
		Logger.getRootLogger().setLevel(Level.INFO);
		System.out.println(System.getProperty("user.dir"));
		frame.setVisible(true);
	}
	
	private final JTabbedPane tabs = new JTabbedPane();
	private final DT dropTarget = new DT();
	
	public EditorJFrame () {
		super("HAPI HL7|^~\\&!");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setDropTarget(dropTarget);
		setJMenuBar(createMenu());
		
		tabs.setDropTarget(dropTarget);
		tabs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if (e.getClickCount() >= 2) {
					System.out.println("mouse click " + e.getClickCount());
					addEditor("untitled", new EditorPanel());
				}
			}
		});
		
		addEditor();
		
		setContentPane(tabs);
		setPreferredSize(new Dimension(800, 600));
		pack();
	}

	private JMenuBar createMenu () {
		JMenuItem newItem = new JMenuItem("New");
		newItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				System.out.println("new");
				addEditor();
			}
		});
		
		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				System.out.println("open");
				JFileChooser fc = new JFileChooser();
				fc.setSelectedFile(new File(System.getProperty("user.dir")));
				fc.setMultiSelectionEnabled(false);
				if (fc.showOpenDialog(EditorJFrame.this) == JFileChooser.APPROVE_OPTION) {
					addEditor(fc.getSelectedFile());
				}
			}
		});
		
		JMenuItem saveItem = new JMenuItem("Save As");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				System.out.println("save as");
				EditorPanel ep = (EditorPanel) tabs.getSelectedComponent();
				if (ep != null) {
					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(false);
					if (ep.getFile() != null) {
						fc.setSelectedFile(ep.getFile());
					}
					if (fc.showSaveDialog(EditorJFrame.this) == JFileChooser.APPROVE_OPTION) {
						File file = fc.getSelectedFile();
						FileUtil.writeFile(file, ep.getText());
						ep.setFile(file);
						tabs.setTitleAt(tabs.getSelectedIndex(), file.getName());
					}
				}
			}
		});
		
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				System.out.println("close");
				tabs.remove(tabs.getSelectedComponent());
			}
		});
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		return menuBar;
	}
	
	public void addEditor () {
		addEditor("untitled", new EditorPanel());
	}
	
	public void addEditor (File file) {
		String text = FileUtil.readFile(file);
		EditorPanel ep = new EditorPanel();
		ep.setFile(file);
		ep.setText(text);
		addEditor(file.getName(), ep);
	}

	private void addEditor (String name, EditorPanel ep) {
		ep.setDropTarget(dropTarget);
		tabs.addTab(name, ep);
		tabs.setSelectedComponent(ep);
	}
	
}
