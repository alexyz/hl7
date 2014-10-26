package hu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
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
				 if (!e.isPopupTrigger() && e.getClickCount() >= 2) {
					System.out.println("mouse click " + e.getClickCount());
					addEditor();
				}
			}
			@Override
			public void mousePressed (MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup(e.getPoint());
				}
			}
			@Override
			public void mouseReleased (MouseEvent e) {
				if (e.isPopupTrigger()) {
					popup(e.getPoint());
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
		JMenu fontMenu = new JMenu("Font");
		{
			ButtonGroup sizeGroup = new ButtonGroup();
			for (int n = 10; n <= 18; n++) {
				final int nn = n;
				JRadioButtonMenuItem item = new JRadioButtonMenuItem("Size " + n);
				sizeGroup.add(item);
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed (ActionEvent e) {
						System.out.println("size " + nn);
						setFontSizes(nn);
					}
				});
				if (n == editorFont.getSize()) {
					item.setSelected(true);
				}
				fontMenu.add(item);
			}
		}
		
		JMenu fileMenu = new JMenu("File");
		{
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
			fileMenu.add(newItem);
			fileMenu.add(openItem);
			fileMenu.add(reopenItem);
			fileMenu.add(saveItem);
			fileMenu.add(closeItem);
		}
		
		JMenu versionMenu = new JMenu("Version");
		{
			List<String> versions = new ArrayList<>();
			versions.add(AUTO_VERSION);
			for (Version v : Version.availableVersions()) {
				versions.add(v.getVersion());
			}
			ButtonGroup versionGroup = new ButtonGroup();
			for (final String version : versions) {
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
				versionMenu.add(item);
			}
		}
		
		JMenu messageMenu = new JMenu("Message");
		{
			{
				JMenuItem item = new JMenuItem("Print Structure");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed (ActionEvent e) {
						printStructure();
					}
				});
				messageMenu.add(item);
			}
			{
				JMenuItem item = new JMenuItem("Print Locations");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed (ActionEvent e) {
						printLocations();
					}
				});
				messageMenu.add(item);
			}
		}
		
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		menuBar.add(fontMenu);
		menuBar.add(versionMenu);
		menuBar.add(messageMenu);
		return menuBar;
	}
	
	private void printStructure() {
		System.out.println("print structure");
		Component comp = tabs.getSelectedComponent();
		if (comp instanceof EditorJPanel) {
			String structure = ((EditorJPanel) comp).printStructure();
			TextJDialog dialog = new TextJDialog(EditorJFrame.this, "Structure", editorFont, structure);
			dialog.setVisible(true);
		}
	}
	
	private void printLocations() {
		System.out.println("print locations");
		Component comp = tabs.getSelectedComponent();
		if (comp instanceof EditorJPanel) {
			String structure = ((EditorJPanel) comp).printLocations();
			TextJDialog dialog = new TextJDialog(EditorJFrame.this, "Locations", editorFont, structure);
			dialog.setVisible(true);
		}
	}
	
	private void popup (Point p) {
		JMenuItem reopenItem = new JMenuItem("Re-open");
		reopenItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				reopenFile();
			}
		});
		JMenuItem closeItem = new JMenuItem("Close");
		closeItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				closeCurrentEditor();
			}
		});
		
		JPopupMenu menu = new JPopupMenu("Editor");
		menu.add(reopenItem);
		menu.add(closeItem);
		menu.show(tabs, p.x, p.y);
	}
	
	public void addEditor () {
		EditorJPanel ep = new EditorJPanel();
		ep.setMsgVersion(messageVersion);
		addEditor("untitled", ep);
	}
	
	public void addFileEditor (File file) {
		String msgLf = FileUtil.readFile(file);
		EditorJPanel ep = new EditorJPanel();
		ep.setMsgVersion(messageVersion);
		ep.setFile(file);
		ep.setMessage(msgLf);
		addEditor(file.getName(), ep);
	}
	
	private void addEditor (String name, EditorJPanel ep) {
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
		EditorJPanel ep = (EditorJPanel) tabs.getSelectedComponent();
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
		EditorJPanel ep = (EditorJPanel) tabs.getSelectedComponent();
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
		EditorJPanel ep = (EditorJPanel) tabs.getSelectedComponent();
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
			if (comp instanceof EditorJPanel) {
				((EditorJPanel) comp).setEditorFont(editorFont);
			}
		}
	}
	
	private void setMessageVersions(String version) {
		System.out.println("set editor version " + version);
		messageVersion = version;
		for (Component comp : tabs.getComponents()) {
			if (comp instanceof EditorJPanel) {
				((EditorJPanel) comp).setMsgVersion(version);
			}
		}
	}
	
}
