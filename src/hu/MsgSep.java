package hu;

/** represents separator characters in a message */
public class MsgSep {

	/** segment separator, always a carriage return */
	public static final char SEGMENT = '\r';
	
	public final char field;
	public final char repetition;
	public final char component;
	public final char subcomponent;
	
	public MsgSep (String msgCr) {
		// MSH|^~\&
		if (msgCr.startsWith("MSH") && msgCr.length() >= 8) {
			field = msgCr.charAt(3);
			component = msgCr.charAt(4);
			repetition = msgCr.charAt(5);
			subcomponent = msgCr.charAt(7);
		} else {
			// guess
			field = '|';
			component = '^';
			repetition = '~';
			subcomponent = '&';
		}
	}

	@Override
	public String toString () {
		return "MsgSep [field=" + field + " repetition=" + repetition + " component=" + component + " subcomponent=" + subcomponent + "]";
	}
	
	
}
