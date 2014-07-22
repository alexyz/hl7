package hu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import ca.uhn.hl7v2.Version;

/** main class and top level frame */
public class EditorJFrame extends JFrame {
	
	public static final String AUTO_VERSION = "Auto";
	
	private static EditorJFrame frame;
	
	public static synchronized EditorJFrame getInstance() {
		if (frame == null) {
			frame = new EditorJFrame();
		}
		return frame;
	}
	
	public static void main (String[] args) {
		// stop hapi spamming the console
		//Logger.getRootLogger().setLevel(Level.INFO);
		System.out.println(System.getProperty("user.dir"));
		getInstance().setVisible(true);
	}
	
	private final JTabbedPane tabs = new JTabbedPane();
	private File dir = new File(System.getProperty("user.dir"));
	private Font editorFont = new Font("monospaced", 0, 14);
	private String messageVersion = AUTO_VERSION;
	
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
				openFile();
			}
		});
		
		JMenuItem saveItem = new JMenuItem("Save As");
		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				saveCurrentEditor();
			}
		});
		
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				closeCurrentEditor();
			}
		});
		
		JRadioButtonMenuItem[] sizeItems = new JRadioButtonMenuItem[9];
		ButtonGroup sizeGroup = new ButtonGroup();
		for (int n = 0; n < sizeItems.length; n++) {
			final int size = n + 10;
			JRadioButtonMenuItem item = new JRadioButtonMenuItem("Size " + size);
			sizeGroup.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					System.out.println("size " + size);
					setFontSizes(size);
				}
			});
			if (size == editorFont.getSize()) {
				item.setSelected(true);
			}
			sizeItems[n] = item;
		}
		
		List<String> versions = new ArrayList<>();
		versions.add(AUTO_VERSION);
		for (Version v : Version.availableVersions()) {
			versions.add(v.getVersion());
		}
		
		JRadioButtonMenuItem[] versionItems = new JRadioButtonMenuItem[9];
		ButtonGroup versionGroup = new ButtonGroup();
		for (int n = 0; n < versionItems.length; n++) {
			final String version = versions.get(n);
			JRadioButtonMenuItem item = new JRadioButtonMenuItem("HL7 " + version);
			versionGroup.add(item);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					setMessageVersions(version);
				}
			});
			if (version.equals(messageVersion)) {
				item.setSelected(true);
			}
			versionItems[n] = item;
		}
		
		JMenu fileMenu = new JMenu("File");
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(closeItem);
		
		JMenu fontMenu = new JMenu("Font");
		for (JMenuItem item : sizeItems) {
			fontMenu.add(item);
		}
		
		JMenu versionMenu = new JMenu("Version");
		for (JMenuItem item : versionItems) {
			versionMenu.add(item);
		}
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(fontMenu);
		menuBar.add(versionMenu);
		return menuBar;
	}
	
	public void addEditor () {
		addEditor("untitled", new EditorPanel());
	}
	
	public void addFileEditor (File file) {
		String text = FileUtil.readFile(file);
		EditorPanel ep = new EditorPanel();
		ep.setFile(file);
		ep.setText(text);
		addEditor(file.getName(), ep);
	}
	
	private void addEditor (String name, EditorPanel ep) {
		ep.setTransferHandler(new TH());
		ep.setEditorFont(editorFont);
		ep.setMessageVersion(messageVersion);
		tabs.addTab(name, ep);
		tabs.setSelectedComponent(ep);
	}
	
	private void openFile () {
		System.out.println("open file in editor " + dir);
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(dir);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(EditorJFrame.this) == JFileChooser.APPROVE_OPTION) {
			addFileEditor(fc.getSelectedFile());
			dir = fc.getCurrentDirectory();
		}
	}
	
	private void saveCurrentEditor () {
		System.out.println("save current editor " + dir);
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
	
	private void closeCurrentEditor () {
		System.out.println("close current editor");
		tabs.remove(tabs.getSelectedComponent());
	}
	
	private void setFontSizes (int size) {
		System.out.println("set editor font size " + size);
		editorFont = new Font("monospaced", 0, size);
		for (Component comp : tabs.getComponents()) {
			if (comp instanceof EditorPanel) {
				((EditorPanel) comp).setEditorFont(editorFont);
			}
		}
	}
	
	private void setMessageVersions(String version) {
		System.out.println("set editor version " + version);
		messageVersion = version;
		for (Component comp : tabs.getComponents()) {
			if (comp instanceof EditorPanel) {
				((EditorPanel) comp).setMessageVersion(version);
			}
		}
	}
	
}
