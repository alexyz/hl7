package hu;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

public class ScriptUtils {
	private final Message msg;
	public ScriptUtils (Message msg) {
		this.msg = msg;
	}
	public int replace (final String s, final String t) {
		return replace(s, t, false);
	}
	public int replaceAll (final String s, final String t) {
		return replace(s, t, true);
	}
	public int replace (final String s, final String t, final boolean regex) {
		final int[] a = new int[1];
		MessageVisitor mv = new MessageVisitorAdapter() {
			@Override
			public boolean visit2 (Primitive type, Location location) throws HL7Exception {
				String v = type.getValue();
				if (v != null) {
					String v2 = regex ? v.replaceAll(s, t) : v.replace(s, t);
					if (!v.equals(v2)) {
						type.setValue(v2);
						a[0]++;
					}
				}
				return true;
			}
		};
		try {
			MessageVisitors.visit(msg, MessageVisitors.visitPopulatedElements(mv));
		} catch (HL7Exception e) {
			e.printStackTrace();
		}
		return a[0];
	}
}
