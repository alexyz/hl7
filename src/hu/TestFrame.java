package hu;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.Composite;
import ca.uhn.hl7v2.model.Field;
import ca.uhn.hl7v2.model.Group;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.model.MessageVisitor;
import ca.uhn.hl7v2.model.MessageVisitorFactory;
import ca.uhn.hl7v2.model.MessageVisitorSupport;
import ca.uhn.hl7v2.model.MessageVisitors;
import ca.uhn.hl7v2.model.Primitive;
import ca.uhn.hl7v2.model.Segment;
import ca.uhn.hl7v2.model.Structure;
import ca.uhn.hl7v2.model.MessageVisitors.PopulatedVisitor;
import ca.uhn.hl7v2.model.Type;
import ca.uhn.hl7v2.model.v26.message.ADT_A01;
import ca.uhn.hl7v2.model.v26.segment.AL1;
import ca.uhn.hl7v2.model.v26.segment.MSH;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

public class TestFrame extends JFrame {
	
	public static final HapiContext context = new DefaultHapiContext();
	
	public static void main (String[] args) {
		new TestFrame().show();
	}
	
	public TestFrame () {
		super("HL7|^~\\&");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		JTabbedPane p = new JTabbedPane();
		p.addTab("editor", new Editor());
		setContentPane(p);
		setPreferredSize(new Dimension(1024, 768));
		pack();
	}
}

class Editor extends JPanel {
	private final JTextArea area = new JTextArea();
	private final JTextField status = new JTextField();
	
	public Editor () {
		super(new BorderLayout());
		area.setBorder(new TitledBorder("Area"));
		area.setFont(new Font("monospaced", 0, 14));
		area.setLineWrap(true);
		area.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate (CaretEvent e) {
				status.setText(getPath(area.getText(), Math.min(e.getMark(), e.getDot())));
			}
		});
		status.setBorder(new TitledBorder("Status"));
		JScrollPane p = new JScrollPane(area);
		add(p, BorderLayout.CENTER);
		add(status, BorderLayout.SOUTH);
	}
	
	private static String getPath (String msgstr, int i) {
		try {
			PipeParser p = TestFrame.context.getPipeParser();
			p.setValidationContext(ValidationContextFactory.noValidation());
			msgstr = msgstr.replace("\n", "\r");
			Message msg = p.parse(msgstr);
			Terser t = new Terser(msg);
			String msh1 = t.get("MSH-1");
			String msh2 = t.get("MSH-2");
			char fieldSep = msh1.charAt(0);
			char repSep = msh2.charAt(1);
			char compSep = msh2.charAt(0);
			char subSep = msh2.charAt(3);
			// count back from i
			int seg = 1;
			int field = 0;
			int rep = 1;
			int comp = 1;
			int sub = 1;
			int ch = 0;
			i--;
			while (i > 0) {
				char c = msgstr.charAt(i);
				if (c == '\r') {
					seg++;
				} else if (seg == 1) {
					if (c == fieldSep) {
						//System.out.println("field at " + msgstr.substring(i, i+10));
						field++;
					} else if (field == 0) {
						if (c == repSep) {
							rep++;
						} else if (rep == 1) {
							if (c == compSep) {
								comp++;
							} else if (comp == 1) {
								if (c == subSep) {
									sub++;
								} else if (sub == 1) {
									ch++;
								}
							}
						}
					}
				}
				
				i--;
			}
			
			if (seg == 1) {
				field++;
			}
			
			final StringBuilder segSb = new StringBuilder();
			MVA va2 = new MVA() {
				public boolean start(Segment segment, Location location) throws HL7Exception {
					segSb.append(location.toString()).append(",");
					return true;
				}
			};
			MessageVisitors.visit(msg, MessageVisitors.visitStructures(va2));
			
//			MessageVisitors.visit(msg, MessageVisitors.visitPopulatedElements(new PV()));
			
			return String.format("seg=%d field=%d rep=%d comp=%d sub=%d ch=%d segs=%s", seg, field, rep, comp, sub, ch, segSb);
			
		} catch (Exception e) {
			return e.toString();
		}
	}
	
}
