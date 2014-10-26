package hu;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

class Info {
	public final Message msg;
	public final Terser terser;
	public final Sep sep;
	public final String msgCr;

	public Info (Message msg, Terser t, Sep sep, String msgCr) {
		this.msg = msg;
		this.terser = t;
		this.sep = sep;
		this.msgCr = msgCr;
	}
}