package hu;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;

/** represents separator characters in a message */
public class MsgSep {

	/** segment separator, always a carriage return */
	public static final char SEGMENT = '\r';
	
	public final char field;
	public final char repetition;
	public final char component;
	public final char subcomponent;
	
	public MsgSep (final Message m) throws HL7Exception {
		field = m.getFieldSeparatorValue();
		final String msh2 = m.getEncodingCharactersValue();
		repetition = msh2.charAt(1);
		component = msh2.charAt(0);
		subcomponent = msh2.charAt(3);
	}
}
