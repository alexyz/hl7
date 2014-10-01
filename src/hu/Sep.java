package hu;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.util.Terser;

/** represents separator characters in a message */
public class Sep {

	/** segment separator, always a carriage return */
	public static final char SEGMENT = '\r';
	
	public final char field;
	public final char repetition;
	public final char component;
	public final char subcomponent;
	
	public Sep (final Terser t) throws HL7Exception {
		final String msh1 = t.get("/MSH-1");
		final String msh2 = t.get("/MSH-2");
		field = msh1.charAt(0);
		repetition = msh2.charAt(1);
		component = msh2.charAt(0);
		subcomponent = msh2.charAt(3);
	}
}
