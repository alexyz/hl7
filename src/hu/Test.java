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
public class Test {

	public static void main2 (String[] args) throws Exception {
		
		Logger log = Logger.getLogger(TestFrame.class);
		HapiContext context = new DefaultHapiContext();
		// Parser p = context.getGenericParser();
		PipeParser p = context.getPipeParser();
		p.setValidationContext(ValidationContextFactory.noValidation());
		
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new FileReader("adta04.txt"))) {
			String l;
			while ((l = br.readLine()) != null) {
				sb.append(l).append("\r");
			}
		}
		String s = sb.toString();
		
		System.out.println("s=" + s);
		Message message = p.parse(sb.toString());
		ADT_A01 adt = (ADT_A01) message;
		adt.getPID().getBirthPlace().setValue("field| component^ repeat~ escape\\ subcomp&");
		
		// log.debug("structure=" + message.printStructure());
		
		log.debug("names=" + Arrays.toString(message.getNames()));
		Structure structure = message.get("PID");
		log.debug("pid=" + structure);
		
	}
	
}
