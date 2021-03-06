package hu.ui;

import hu.*;
import hu.mv.Comment;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.text.*;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.text.WordUtils;

public class EditorJPanel extends JPanel {
	
	private static final String DESC_SEP = " \u25aa ";
	private static final Color ERROR_COL = new Color(255, 192, 192);
	private static final Color SEGMENT_COL = new Color(192, 255, 192);
	private static final Color VALUE_COL = new Color(255, 255, 192);
	
	private static Color getColor (Comment.Type type) {
		switch (type) {
			case ERROR:
				return ERROR_COL;
			case SEGMENT:
				return SEGMENT_COL;
			case VALUE:
				return VALUE_COL;
			default:
				throw new RuntimeException("unknown type " + type);
		}
	}
	
	/** message area, always represent segments separators as line feeds */
	private final JTextArea msgArea = new JTextArea();
	private final JTextArea descriptionArea = new JTextArea(3, 40);
	private final JTextField pathField = new JTextField();
	private final JTextField valueField = new JTextField();
	private final List<Object> highlights = new ArrayList<>();
	
	private File file;
	private String msgVersion;
	private String findText = "";
	
	public EditorJPanel () {
		super(new BorderLayout());
		initComps();
	}

	private void initComps () {
		((AbstractDocument) msgArea.getDocument()).setDocumentFilter(new DocumentFilter() {
			@Override
			public void insertString (FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
				System.out.println("document filter insert string");
				super.insertString(fb, offset, string.replace(MsgSep.SEGMENT, '\n'), attr);
			}
			
			@Override
			public void replace (FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
				System.out.println("document filter replace");
				super.replace(fb, offset, length, text.replace(MsgSep.SEGMENT, '\n'), attrs);
			}
		});
		
		msgArea.setBorder(new TitledBorder("Message"));
		msgArea.setLineWrap(true);
		
		msgArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed (KeyEvent e) {
				if ((e.isControlDown() || e.isMetaDown()) && e.getKeyChar() == 'f') {
					find();
				}
			}
		});
		
		msgArea.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate (CaretEvent ce) {
				// avoid error when setting text before version
				if (EditorJPanel.this.isShowing()) {
					// update(Math.min(ce.getMark(), ce.getDot()));
					update();
				}
			}
		});
		
		msgArea.addMouseListener(new MouseAdapter() {
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
		
		pathField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				pathUpdated();
			}
		});
		
		JButton pathButton = new JButton("Get");
		pathButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				pathUpdated();
			}
		});
		
		valueField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				valueAction();
			}
		});
		
		JButton valueButton = new JButton("Set");
		valueButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				valueAction();
			}
		});
		
		JScrollPane descScroller = new JScrollPane(descriptionArea);
		descScroller.setBorder(new TitledBorder("Description"));
		
		JScrollPane msgScroller = new JScrollPane(msgArea);
		
		descriptionArea.setLineWrap(true);
		descriptionArea.setEditable(false);
		
		JPanel pathPanel = new JPanel(new BorderLayout());
		pathPanel.setBorder(new TitledBorder("HAPI Terser Path"));
		pathPanel.add(pathField, BorderLayout.CENTER);
		pathPanel.add(pathButton, BorderLayout.EAST);
		
		JPanel valuePanel = new JPanel(new BorderLayout());
		valuePanel.setBorder(new TitledBorder("Java String Value"));
		valuePanel.add(valueField, BorderLayout.CENTER);
		valuePanel.add(valueButton, BorderLayout.EAST);
		
		JPanel fieldsPanelNorth = new JPanel(new BorderLayout());
		fieldsPanelNorth.add(pathPanel, BorderLayout.NORTH);
		fieldsPanelNorth.add(valuePanel, BorderLayout.CENTER);
		
		JPanel fieldsPanel = new JPanel(new BorderLayout());
		fieldsPanel.add(fieldsPanelNorth, BorderLayout.NORTH);
		fieldsPanel.add(descScroller, BorderLayout.CENTER);
		
		add(msgScroller, BorderLayout.CENTER);
		add(fieldsPanel, BorderLayout.SOUTH);
		
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown (ComponentEvent e) {
				System.out.println("component shown");
				msgArea.requestFocusInWindow();
			}
		});
	}
	
	public File getFile () {
		return file;
	}
	
	public void setFile (File file) {
		this.file = file;
	}
	
	/** return message with segments separated with carriage returns */
	public String getMsgCr () {
		// guess where the segment separators should be
		String text = msgArea.getText();
		MsgSep sep = new MsgSep(text);
		Pattern segPat = Pattern.compile("[A-Z0-9]{3}" + Pattern.quote(String.valueOf(sep.field)));
		StringBuilder sb = new StringBuilder(text);
		int i = 0;
		while ((i = sb.indexOf("\n", i + 1)) > 0) {
			if (i < sb.length() - 4) {
				String seg = sb.substring(i + 1, i + 5);
				if (segPat.matcher(seg).matches()) {
					sb.replace(i, i + 1, "\r");
				}
			} else {
				sb.replace(i, i + 1, "\r");
			}
		}
		return sb.toString();
	}
	
	public void setMsg (String msg) {
		msgArea.setText(msg.replace(MsgSep.SEGMENT, '\n'));
		msgArea.setCaretPosition(0);
		update();
	}
	
	public Font getEditorFont () {
		return msgArea.getFont();
	}
	
	public void setEditorFont (Font f) {
		System.out.println("set editor font " + f);
		for (Component comp : Arrays.asList(msgArea, pathField, valueField, descriptionArea)) {
			comp.setFont(f);
		}
		update();
	}
	
	public String getMsgVersion () {
		return msgVersion;
	}
	
	public void setMsgVersion (String messageVersion) {
		this.msgVersion = messageVersion;
		update();
	}
	
	private void update () {
		System.out.println("update");
		final int caretIndex = msgArea.getCaretPosition();
		final int selStart = msgArea.getSelectionStart();
		final int selEnd = msgArea.getSelectionEnd();
		final String msgLf = msgArea.getText();
		
		if (msgLf.length() == 0) {
			System.out.println("update: no message");
			return;
		}
		
		try {
			// clear error highlighting
			Highlighter h = msgArea.getHighlighter();
			for (Object o : highlights) {
				h.removeHighlight(o);
			}
			highlights.clear();
			
			// do error highlighting
			
			String selectedValue = null;
			if (selStart > 0 && selEnd > selStart) {
				selectedValue = msgLf.substring(selStart, selEnd);
			}
			
			final MsgInfo info = getMsgInfo();
			final MsgPos pos = MsgUtil.getPosition(info.msgCr, info.sep, caretIndex);
			final List<Comment> errors = MsgUtil.getErrors(info.msg, info.msgCr, info.sep, msgVersion, selectedValue);
			System.out.println("errors: " + errors.size());
			
			String currentError = null;
			for (Comment ve : errors) {
				//System.out.println("highlight " + ve);
				if (ve.pos.equals(pos) && ve.type == Comment.Type.ERROR) {
					currentError = ve.msg;
				}
				
				final int[] i = ve.indexes;
				if (i != null) {
					if (i[0] != msgArea.getSelectionStart() || i[1] != msgArea.getSelectionEnd()) {
						highlights.add(h.addHighlight(i[0], i[1], new DefaultHighlighter.DefaultHighlightPainter(getColor(ve.type))));
					}
					
				} else {
					System.out.println("could not highlight " + ve);
				}
			}
			
			// populate the fields
			MsgPath terserPath = MsgUtil.getTerserPath(info.msg, info.terser, pos, DESC_SEP);
			if (terserPath != null) {
				if (!pathField.getText().equals(terserPath.path)) {
					pathField.setText(terserPath.path);
				}
				String javaValue = StringEscapeUtils.escapeJava(terserPath.value);
				if (!valueField.getText().equals(javaValue)) {
					valueField.setText(javaValue);
				}
				String desc = terserPath.description;
				if (currentError != null) {
					desc += " \u2192 " + currentError;
				}
				if (!descriptionArea.getText().equals(desc)) {
					descriptionArea.setText(desc);
				}
			}
			
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				showException("Update", e);
			}
			System.out.println("could not update: " + e);
			e.printStackTrace(System.out);
			pathField.setText("");
			valueField.setText("");
			descriptionArea.setText(e.toString());
		}
	}
	
	private void pathUpdated () {
		System.out.println("path updated");
		try {
			MsgInfo info = getMsgInfo();
			String path = pathField.getText();
			String value = info.terser.get(path);
			valueField.setText(StringEscapeUtils.escapeJava(value));
			
			MsgPos pos = MsgUtil.getPosition(info.terser, path);
			String desc = MsgUtil.getDescription(info.msg, pos, DESC_SEP);
			descriptionArea.setText(desc);
			
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				e.printStackTrace();
			}
			System.out.println("could not update path: " + e);
			descriptionArea.setText(e.toString());
		}
	}
	
	private void valueAction () {
		System.out.println("value updated");
		
		try {
			String path = pathField.getText();
			MsgInfo info = getMsgInfo();
			MsgPos pos = MsgUtil.getPosition(info.terser, path);
			
			// set the new value
			
			info.terser.set(path, StringEscapeUtils.unescapeJava(valueField.getText()));
			
			// re-encode the whole message...
			
			String msgCr = info.msg.encode();
			msgArea.setText(msgCr.replace(MsgSep.SEGMENT, '\n'));
			
			// move the caret (doesn't work for msh-1)
			
			int[] i = MsgUtil.getIndexes(msgCr, new MsgSep(msgCr), pos);
			if (i != null) {
				System.out.println("new index is " + i[0] + ", " + i[1]);
				msgArea.setCaretPosition(i[1]);
				
			} else {
				System.out.println("could not get index of " + pos);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			descriptionArea.setText(e.toString());
		}
	}
	
	private void popup (Point p) {
		JMenuItem cutItem = new JMenuItem("Cut");
		cutItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				cutOrCopy(true);
			}
		});
		JMenuItem copyItem = new JMenuItem("Copy");
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				cutOrCopy(false);
			}
		});
		JMenuItem pasteItem = new JMenuItem("Paste");
		pasteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent ae) {
				paste();
			}
		});
		
		JPopupMenu menu = new JPopupMenu("Editor");
		menu.add(cutItem);
		menu.add(copyItem);
		menu.add(pasteItem);
		menu.show(msgArea, p.x, p.y);
	}
	
	private void cutOrCopy (boolean cut) {
		System.out.println("cut or copy " + cut);
		int s1 = msgArea.getSelectionStart();
		int s2 = msgArea.getSelectionEnd();
		if (s1 != s2) {
			Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
			clip.setContents(new StringSelection(msgArea.getSelectedText()), null);
			if (cut) {
				String text = msgArea.getText();
				msgArea.setText(text.substring(0, s1) + text.substring(s2));
				msgArea.setCaretPosition(s1);
			}
		}
	}
	
	private void paste () {
		System.out.println("paste");
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = clip.getContents(null);
		try {
			String transText = (String) t.getTransferData(DataFlavor.stringFlavor);
			if (transText != null) {
				int s1 = msgArea.getSelectionStart();
				int s2 = msgArea.getSelectionEnd();
				String text = msgArea.getText();
				msgArea.setText(text.substring(0, s1) + transText + text.substring(s2));
				msgArea.setCaretPosition(s2);
			}
		} catch (Exception e) {
			e.printStackTrace();
			showException("Paste", e);
		}
	}
	
	public MsgInfo getMsgInfo () throws Exception {
		return MsgUtil.getInfo(getMsgCr(), msgVersion);
	}

	private void showException (String title, Exception e) {
		JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), WordUtils.wrap(e.toString(), 80), 
				title, JOptionPane.ERROR_MESSAGE);
	}

	public void find () {
		try {
			MsgInfo info = getMsgInfo();
			FindJDialog dialog = new FindJDialog(SwingUtilities.getWindowAncestor(this), info.msg, findText);
			dialog.setLocationRelativeTo(this);
			dialog.setModal(true);
			dialog.setVisible(true);
			findText = dialog.getFindText();
			String terserPath = dialog.getTerserPath();
			if (terserPath != null) {
				MsgPos pos = MsgUtil.getPosition(info.terser, terserPath);
				int[] i = MsgUtil.getIndexes(info.msgCr, info.sep, pos);
				if (i != null) {
					msgArea.setSelectionStart(i[0]);
					msgArea.setSelectionEnd(i[1]);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			showException("Send", e);
		}
	}
	
}
