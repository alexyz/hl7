package hu;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.Terser;

public class ScriptMessage {
	public final Message message;
	public final Terser terser;
	public ScriptMessage (Message msg) {
		this.message = msg;
		this.terser = new Terser(msg);
	}
}