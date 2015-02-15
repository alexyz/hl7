package hu;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

/**
 * represents message and terser
 */
public class MsgInfo {
	public final Message msg;
	public final Terser terser;
	public final MsgSep sep;
	public final String msgCr;

	public MsgInfo (Message msg, Terser t, MsgSep sep, String msgCr) {
		this.msg = msg;
		this.terser = t;
		this.sep = sep;
		this.msgCr = msgCr;
	}
}