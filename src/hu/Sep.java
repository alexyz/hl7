package hu;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.util.Terser;

public class Sep {

	public static final char segSep = '\r';
	public final char fieldSep;
	public final char repSep;
	public final char compSep;
	public final char subCompSep;
	
	public Sep (Terser t) throws HL7Exception {
		String msh1 = t.get("/MSH-1");
		String msh2 = t.get("/MSH-2");
		fieldSep = msh1.charAt(0);
		repSep = msh2.charAt(1);
		compSep = msh2.charAt(0);
		subCompSep = msh2.charAt(3);
	}
}