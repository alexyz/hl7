package hu;

/** represents separator characters in a message */
public class MsgSep {

	/** segment separator, always a carriage return */
	public static final char SEGMENT = '\r';
	
	public final char field;
	public final char repetition;
	public final char component;
	public final char subcomponent;
	
	public MsgSep (String msg) {
		// MSH|^~\&
		if (msg.startsWith("MSH") && msg.length() >= 8) {
			field = msg.charAt(3);
			component = msg.charAt(4);
			repetition = msg.charAt(5);
			subcomponent = msg.charAt(7);
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
