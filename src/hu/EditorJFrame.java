package hu;

import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;

import javax.swing.*;

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
	private File dir = new File(System.getProperty("user.dir"));
	
	public EditorJFrame () {
		super("HAPI HL7|^~\\&!");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setJMenuBar(createMenu());
		setTransferHandler(new TH());
		
		tabs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if (e.getClickCount() >= 2) {
					System.out.println("mouse click " + e.getClickCount());
					addEditor("untitled", new EditorPanel());
				}
			}
		});
		tabs.setTransferHandler(new TH());
		
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
				System.out.println("open " + dir);
				JFileChooser fc = new JFileChooser();
				fc.setCurrentDirectory(dir);
				fc.setMultiSelectionEnabled(false);
				if (fc.showOpenDialog(EditorJFrame.this) == JFileChooser.APPROVE_OPTION) {
					addEditor(fc.getSelectedFile());
					dir = fc.getCurrentDirectory();
				}
			}
		});
		
		JMenuItem saveItem = new JMenuItem("Save As");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				System.out.println("save as " + dir);
				EditorPanel ep = (EditorPanel) tabs.getSelectedComponent();
				if (ep != null) {
					JFileChooser fc = new JFileChooser();
					fc.setMultiSelectionEnabled(false);
					if (ep.getFile() != null) {
						fc.setSelectedFile(ep.getFile());
					} else {
						fc.setCurrentDirectory(dir);
					}
					if (fc.showSaveDialog(EditorJFrame.this) == JFileChooser.APPROVE_OPTION) {
						dir = fc.getCurrentDirectory();
						// this bit might fail
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
		ep.setTransferHandler(new TH());
		tabs.addTab(name, ep);
		tabs.setSelectedComponent(ep);
	}
	
}
