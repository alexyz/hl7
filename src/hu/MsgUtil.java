package hu;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.*;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

/** utilities for hl7 messages */
public class MsgUtil {
	
	/** get info about the message and the index */
	public static Info getInfo (final String msgstrLf, final String version, final int index) {
		// parse the message
		final String msgstr = msgstrLf.replace("\n", "\r");
		HapiContext context = new DefaultHapiContext();
		
		if (!version.equals(EditorJFrame.AUTO_VERSION)) {
			CanonicalModelClassFactory mcf = new CanonicalModelClassFactory(version);
			context.setModelClassFactory(mcf);
		}
		
		Message msg;
		try {
			PipeParser p = context.getPipeParser();
			p.setValidationContext(ValidationContextFactory.noValidation());
			msg = p.parse(msgstr);
		} catch (Exception e) {
			System.out.println("could not parse message: " + e.toString());
			//e.printStackTrace(System.out);
			return new Info(e.getMessage());
		}
		
		Terser t = new Terser(msg);
		
		// do the validations
		Sep sep;
		try {
			sep = new Sep(t);
		} catch (HL7Exception e1) {
			return new Info("could not get seperators");
		}
		
		List<VE> errors = null;
		try {
			ValidatingMessageVisitor vmv = new ValidatingMessageVisitor(msgstr, sep, "2.5");
			MessageVisitors.visit(msg, vmv);
			errors = vmv.getErrors();
		} catch (Exception e) {
			System.out.println("could not validate: " + e);
		}
		
		// get the terser path for the index
		Pos pos = getPosition(msgstr, sep, index);
		TP tp = getTerserPath(msg, t, pos);
		return new Info(msg, t, sep, pos, tp, errors);
	}
	
	/** get the terser path for the message position */
	public static TP getTerserPath (final Message msg, final Terser t, final Pos pos) {
		SL sl = getSegmentLocation(msg, pos.segOrd);
		String path = "";
		String desc = "";
		String value = "";
		
		if (sl != null) {
			path = sl.location.toString() + "-" + pos.fieldOrd + (pos.fieldRep > 0 ? "(" + pos.fieldRep + ")" : "") + (pos.compOrd > 1 ? "-" + pos.compOrd : "") + (pos.subCompOrd > 1 ? "-" + pos.subCompOrd : "");
			desc = getDescription(sl.segment, pos);
			try {
				value = t.get(path);
			} catch (Exception e) {
				System.out.println("could not get terser path value: " + e);
			}
		}
		
		return new TP(path, value, desc);
	}
	
	/** get segment and segment location from a segment ordinal */
	public static SL getSegmentLocation (final Message msg, final int segmentOrd) {
		final SL[] sl = new SL[1];
		MessageVisitorAdapter mv = new MessageVisitorAdapter() {
			int s = 1;
			@Override
			public boolean start (Segment segment, Location location) throws HL7Exception {
				if (s == segmentOrd) {
					sl[0] = new SL(segment, location);
				}
				s++;
				return sl[0] == null;
			}
		};
		try {
			MessageVisitors.visit(msg, MessageVisitors.visitStructures(mv));
		} catch (HL7Exception e) {
			System.out.println("could not get segment name: " + e);
		}
		return sl[0];
	}
	
	/** get description of hl7 field, component and subcomponent */
	public static String getDescription (final Segment segment, final Pos pos) {
		String desc = "desc";
		Object[] fd = getDescription2(segment.getClass(), pos.fieldOrd);
		if (fd != null) {
			desc = "" + fd[0];
			if (fd[1] != null) {
				Object[] cd = getDescription2((Class<?>) fd[1], pos.compOrd);
				if (cd != null) {
					desc += " " + cd[0];
					if (cd[1] != null) {
						Object[] scd = getDescription2((Class<?>) cd[1], pos.subCompOrd);
						if (scd != null) {
							desc += " " + scd[0];
						}
					}
				}
			}
		}
		return desc;
	}
	
	/** get description of element from class */
	private static Object[] getDescription2 (Class<?> cl, int ord) {
		Pattern methodPat = Pattern.compile("get\\w{" + cl.getSimpleName().length() + "}(\\d+)_(\\w+)");
		for (Method m : cl.getMethods()) {
			Class<?> rt = m.getReturnType();
			if (rt != null && !rt.isPrimitive()) {
				// getObr5_PriorityOBR()
				Matcher mat = methodPat.matcher(m.getName());
				if (mat.matches()) {
					int f = Integer.parseInt(mat.group(1));
					String fn = mat.group(2);
					if (f == ord) {
						if (Object[].class.isAssignableFrom(rt)) {
							rt = rt.getComponentType();
						}
						fn += " [" + rt.getSimpleName() + "]";
						if (!AbstractType.class.isAssignableFrom(rt)) {
							rt = null;
						}
						if (rt != null && AbstractPrimitive.class.isAssignableFrom(rt)) {
							rt = null;
						}
						return new Object[] { fn, rt };
					}
				}
			}
		}
		return null;
	}
	
	/** get the segment, field etc for the character index in the message */
	public static Pos getPosition (final String msgstrCr, final Sep sep, final int index) {
		// start field at 1 for MSH, 0 for others
		int s = 1, f = 1, fr = 0, c = 1, sc = 1;
		for (int i = 0; i < index; i++) {
			char ch = msgstrCr.charAt(i);
			if (ch == Sep.segSep) {
				s++;
				f = 0;
				fr = 0;
				c = 1;
				sc = 1;
			} else if (ch == sep.fieldSep) {
				f++;
				fr = 0;
				c = 1;
				sc = 1;
			} else if (ch == sep.repSep) {
				fr++;
				c = 1;
				sc = 1;
			} else if (ch == sep.compSep) {
				c++;
				sc = 1;
			} else if (ch == sep.subCompSep) {
				sc++;
			}
		}
		return new Pos(s, f, fr, c, sc);
	}
	
	/** get the character indexes of the given primitive */
	public static int[] getIndex (final String msgstrCr, final Sep sep, final Pos pos) {
		System.out.println("get indexes " + pos);
		int[] indexes = new int[2];
		// start field at 1 for MSH, 0 for others
		int s = 1, f = 1, fr = 0, c = 1, sc = 1, l = 0;
		for (int i = 0; i < msgstrCr.length(); i++) {
			char ch = msgstrCr.charAt(i);
			if (ch == Sep.segSep) {
				s++;
				f = 0;
				fr = 0;
				c = 1;
				sc = 1;
				l = 0;
			} else if (ch == sep.fieldSep) {
				f++;
				fr = 0;
				c = 1;
				sc = 1;
				l = 0;
			} else if (ch == sep.repSep) {
				fr++;
				c = 1;
				sc = 1;
				l = 0;
			} else if (ch == sep.compSep) {
				c++;
				sc = 1;
				l = 0;
			} else if (ch == sep.subCompSep) {
				sc++;
				l = 0;
			} else {
				l++;
			}
			//System.out.println(String.format("s %d f %d fr %d c %d sc %d l %d", s, f, fr, c, sc, l));
			if (s == pos.segOrd && f == pos.fieldOrd && fr == pos.fieldRep && c == pos.compOrd && sc == pos.subCompOrd) {
				//System.out.println("char: " + ch);
				if (indexes[0] == 0) {
					indexes[0] = i + 1;
				}
				indexes[1] = indexes[0] + l;
			}
		}
		return indexes;
	}
	
}
