package hu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

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
		System.out.println("user dir " + System.getProperty("user.dir"));
		getInstance().setVisible(true);
	}
	
	private final JTabbedPane tabs = new JTabbedPane();
	
	private File dir = new File(System.getProperty("user.dir"));
	private Font editorFont = new Font("monospaced", 0, 12);
	private String messageVersion = AUTO_VERSION;
	
	public EditorJFrame () {
		super("HAPI HL7|^~\\&!");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setJMenuBar(createMenu());
		setTransferHandler(new FileTransferHandler());
		
		tabs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked (MouseEvent e) {
				if (e.getClickCount() >= 2) {
					System.out.println("mouse click " + e.getClickCount());
					addEditor();
				}
			}
		});
		tabs.setTransferHandler(new FileTransferHandler());
		
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
		
		JMenuItem reopenItem = new JMenuItem("Re-open");
		reopenItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				reopenFile();
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
		fileMenu.add(reopenItem);
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
		EditorPanel ep = new EditorPanel();
		ep.setMsgVersion(messageVersion);
		addEditor("untitled", ep);
	}
	
	public void addFileEditor (File file) {
		String msgLf = FileUtil.readFile(file);
		EditorPanel ep = new EditorPanel();
		ep.setMsgVersion(messageVersion);
		ep.setFile(file);
		ep.setMessage(msgLf);
		addEditor(file.getName(), ep);
	}
	
	private void addEditor (String name, EditorPanel ep) {
		ep.setEditorFont(editorFont);
		ep.setTransferHandler(new FileTransferHandler());
		tabs.addTab(name, ep);
		tabs.setSelectedComponent(ep);
	}
	
	private void openFile () {
		System.out.println("open file in new editor " + dir);
		JFileChooser fc = new JFileChooser();
		fc.setCurrentDirectory(dir);
		fc.setMultiSelectionEnabled(false);
		if (fc.showOpenDialog(EditorJFrame.this) == JFileChooser.APPROVE_OPTION) {
			addFileEditor(fc.getSelectedFile());
			dir = fc.getCurrentDirectory();
		}
	}
	
	private void reopenFile () {
		System.out.println("reopen file in current editor");
		EditorPanel ep = (EditorPanel) tabs.getSelectedComponent();
		if (ep != null) {
			File file = ep.getFile();
			if (file != null) {
				if (JOptionPane.showConfirmDialog(this, "Re-open file?", "Re-open", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					String text = FileUtil.readFile(file);
					ep.setMessage(text);
				}
			}
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
				FileUtil.writeFile(file, ep.getMessage().replace('\n', Sep.SEGMENT));
				ep.setFile(file);
				tabs.setTitleAt(tabs.getSelectedIndex(), file.getName());
			}
		}
	}
	
	private void closeCurrentEditor () {
		System.out.println("close current editor");
		EditorPanel ep = (EditorPanel) tabs.getSelectedComponent();
		if (ep != null) {
			if (JOptionPane.showConfirmDialog(this, "Close editor?", "Close", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				tabs.remove(ep);
			}
		}
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
				((EditorPanel) comp).setMsgVersion(version);
			}
		}
	}
	
}
