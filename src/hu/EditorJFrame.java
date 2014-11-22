package hu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.*;

import jsui.JSJDialog;
import ca.uhn.hl7v2.Version;
import ca.uhn.hl7v2.model.Message;

/** main class and top level frame */
public class EditorJFrame extends JFrame {
	
	public static final String AUTO_VERSION = "Auto";
	
	private static final EditorJFrame frame = new EditorJFrame();
	
	public static EditorJFrame getInstance() {
		return frame;
	}
	
	public static void main (String[] args) {
		System.out.println("user dir " + System.getProperty("user.dir"));
		frame.setVisible(true);
		if (args.length == 0) {
			frame.addEditor();
		} else {
			for (String a : args) {
				frame.addFileEditor(new File(a));
			}
		}
	}
	
	private final JTabbedPane tabs = new JTabbedPane();
	
	private File dir = new File(System.getProperty("user.dir"));
	private Font editorFont = new Font("Monospaced", 0, 14);
	private String messageVersion = AUTO_VERSION;
	private String js = "message.encode()";
	private String host = "localhost";
	private int port = 1000;
	
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
		
		setContentPane(tabs);
		setPreferredSize(new Dimension(800, 600));
		pack();
	}
	
	private JMenuBar createMenu () {
		JMenu fontMenu = new JMenu("Font");
		{
			JMenuItem item = new JMenuItem("Font...");
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed (ActionEvent e) {
					System.out.println("font");
					FontJDialog dialog = new FontJDialog(EditorJFrame.this, editorFont);
					dialog.setVisible(true);
					Font f = dialog.getSelectedFont();
					if (f != null) {
						setEditorFonts(f);
					}
				}
			});
			fontMenu.add(item);
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
			
			JMenuItem openItem = new JMenuItem("Open...");
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
			
			JMenuItem saveItem = new JMenuItem("Save As...");
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
				JMenuItem item = new JMenuItem("Print Structure...");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed (ActionEvent e) {
						printStructure();
					}
				});
				messageMenu.add(item);
			}
			{
				JMenuItem item = new JMenuItem("Print Locations...");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed (ActionEvent e) {
						printLocations();
					}
				});
				messageMenu.add(item);
			}
			{
				JMenuItem item = new JMenuItem("Apply JavaScript...");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed (ActionEvent e) {
						applyJs();
					}
				});
				messageMenu.add(item);
			}
			{
				JMenuItem item = new JMenuItem("Send...");
				item.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed (ActionEvent e) {
						send();
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
	
	private void send() {
		System.out.println("send");
		Component comp = tabs.getSelectedComponent();
		if (comp instanceof EditorJPanel) {
			EditorJPanel ep = (EditorJPanel) comp;
			try {
				MsgInfo info = ep.getMessageInfo();
				HostJDialog hostDialog = new HostJDialog(this, host, port);
				hostDialog.setVisible(true);
				if (hostDialog.isOk()) {
					host = hostDialog.getHost();
					port = hostDialog.getPort();
					Message response = MsgUtil.send(info.msg, host, port);
					String text = response.encode();
					TextJDialog textDialog = new TextJDialog(this, "Response", editorFont, text);
					textDialog.setVisible(true);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.toString(), "Send", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void applyJs() {
		System.out.println("apply js");
		Component comp = tabs.getSelectedComponent();
		if (comp instanceof EditorJPanel) {
			EditorJPanel ep = (EditorJPanel) comp;
			try {
				MsgInfo info = ep.getMessageInfo();
				Map<String,Object> m = new TreeMap<>();
				m.put("message", info.msg);
				m.put("terser", info.terser);
				m.put("messageStr", info.msgCr);
				JSJDialog d = new JSJDialog(this, "Apply JavaScript", editorFont, m, js);
				d.setLocationRelativeTo(frame);
				d.setVisible(true);
				js = d.getInputText();
				
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.toString(), "Apply", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void printStructure() {
		System.out.println("print structure");
		Component comp = tabs.getSelectedComponent();
		if (comp instanceof EditorJPanel) {
			EditorJPanel ep = (EditorJPanel) comp;
			try {
				String structure = ep.printStructure();
				TextJDialog dialog = new TextJDialog(EditorJFrame.this, "Structure", editorFont, structure);
				dialog.setVisible(true);
				
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, e.toString(), "Structure", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	private void printLocations() {
		System.out.println("print locations");
		Component comp = tabs.getSelectedComponent();
		if (comp instanceof EditorJPanel) {
			EditorJPanel ep = (EditorJPanel) comp;
			try {
				String structure = ep.printLocations();
				TextJDialog dialog = new TextJDialog(EditorJFrame.this, "Locations", editorFont, structure);
				dialog.setVisible(true);
				
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), e.toString(), "Locations", JOptionPane.ERROR_MESSAGE);
			}
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
				FileUtil.writeFile(file, ep.getMessage().replace('\n', MsgSep.SEGMENT));
				ep.setFile(file);
				tabs.setTitleAt(tabs.getSelectedIndex(), file.getName());
			}
		}
	}
	
	private void closeCurrentEditor () {
		System.out.println("close current editor");
		EditorJPanel ep = (EditorJPanel) tabs.getSelectedComponent();
		if (ep != null) {
			String message = ep.getMessage();
			if (message == null || message.length() == 0 || JOptionPane.showConfirmDialog(this, "Close editor?", "Close", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
				tabs.remove(ep);
			}
		}
	}
	
	private void setEditorFonts (Font font) {
		System.out.println("set editor fonts " + font);
		this.editorFont = font;
		for (Component comp : tabs.getComponents()) {
			if (comp instanceof EditorJPanel) {
				((EditorJPanel) comp).setEditorFont(font);
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
