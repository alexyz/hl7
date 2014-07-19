package hu;

import java.lang.reflect.Method;
import java.util.regex.*;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.Location;
import ca.uhn.hl7v2.model.*;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

public class Util {

	public static String[] getTerserPath (String msgstr, int i) {
		msgstr = msgstr.replace("\n", "\r");
		Message msg;
		Terser t;
		String msh1, msh2;
		try {
			PipeParser p = EditorFrame.context.getPipeParser();
			p.setValidationContext(ValidationContextFactory.noValidation());
			msg = p.parse(msgstr);
			t = new Terser(msg);
			msh1 = t.get("MSH-1");
			msh2 = t.get("MSH-2");
		} catch (Exception e) {
			System.out.println("could not parse message: " + e.toString());
			return new String[] { "", "", "" };
		}
		
//		try {
//			MessageVisitors.visit(msg, MessageVisitors.visitPopulatedElements(new PrintingMessageVisitor()));
//		} catch (Exception e) {
//			System.out.println("could not print values: " + e);
//		}
		
		final char fieldSep = msh1.charAt(0);
		final char repSep = msh2.charAt(1);
		final char compSep = msh2.charAt(0);
		final char subCompSep = msh2.charAt(3);
		
		int segOrd = 1;
		int fieldOrd = 0;
		int repIndex = 0;
		int compOrd = 1;
		int subCompOrd = 1;
		int ch = 0;
		
		// count back from i
		i--;
		while (i > 0) {
			char c = msgstr.charAt(i);
			if (c == '\r') {
				segOrd++;
			} else if (segOrd == 1) {
				if (c == fieldSep) {
					// System.out.println("field at " + msgstr.substring(i,
					// i+10));
					fieldOrd++;
				} else if (fieldOrd == 0) {
					if (c == repSep) {
						repIndex++;
					} else if (repIndex == 0) {
						if (c == compSep) {
							compOrd++;
						} else if (compOrd == 1) {
							if (c == subCompSep) {
								subCompOrd++;
							} else if (subCompOrd == 1) {
								ch++;
							}
						}
					}
				}
			}
			
			i--;
		}
		
		if (segOrd == 1) {
			fieldOrd++;
		}
		
		// ["/"]   (group_spec ["(" rep ")"] "/")*   segment_spec   ["(" rep ")"]    "-"    field    ["(" rep ")"] ["-" component ["-" subcomponent]] 
		SL sl = getSegmentLocation(msg, segOrd);
		String path = "";
		String desc = "";
		if (sl != null) {
			path = sl.location.toString() + "-" + fieldOrd + (repIndex > 0 ? "(" + repIndex + ")" : "") + (compOrd > 1 ? "-" + compOrd : "") + (subCompOrd > 1 ? "-" + subCompOrd : "");
			desc = getDesc(sl.segment, fieldOrd, compOrd, subCompOrd);
		}
		
		String value = null;
		try {
			value = t.get(path);
		} catch (Exception e) {
			System.out.println("could not get path value: " + e);
		}
		// String.format("seg=%d field=%d rep=%d comp=%d sub=%d ch=%d desc=%s", segOrd, fieldOrd, repIndex, compOrd, subCompOrd, ch,
		return new String[] { path, value, desc };
		
	}
	
	public static SL getSegmentLocation (final Message msg, final int segmentOrd) {
		final SL[] sl = new SL[1];
		MessageVisitorAdapter va2 = new MessageVisitorAdapter() {
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
			MessageVisitors.visit(msg, MessageVisitors.visitStructures(va2));
		} catch (HL7Exception e) {
			System.out.println("could not get segment name: " + e);
		}
		return sl[0];
	}
	
	/** get description of hl7 field, component and subcomponent */
	public static String getDesc (final Segment segment, final int fieldOrd, final int compOrd, final int subCompOrd) {
		String desc = "desc";
		Object[] fd = getDesc(segment.getClass(), fieldOrd);
		if (fd != null) {
			desc = "" + fd[0];
			if (fd[1] != null) {
				Object[] cd = getDesc((Class<?>) fd[1], compOrd);
				if (cd != null) {
					desc += " " + cd[0];
					if (cd[1] != null) {
						Object[] scd = getDesc((Class<?>) cd[1], subCompOrd);
						if (scd != null) {
							desc += " " + scd[0];
						}
					}
				}
			}
		}
		return desc;
	}
	
	private static Object[] getDesc (Class<?> cl, int ord) {
		Pattern methodPat = Pattern.compile("get\\w{" + cl.getSimpleName().length() + "}(\\d+)_(\\w+)");
		for (Method m : cl.getMethods()) {
			Class<?> rt = m.getReturnType();
			if (!rt.isPrimitive()) {
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
						if (AbstractPrimitive.class.isAssignableFrom(rt)) {
							rt = null;
						}
						return new Object[] { fn, rt };
					}
				}
			}
		}
		return null;
	}
	

	private static class SL {
		final Segment segment;
		final Location location;
		public SL (Segment segment, Location location) {
			this.segment = segment;
			this.location = location;
		}
	}
	
}
