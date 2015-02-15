package hu.mv;

import hu.*;

import java.util.Arrays;

public class Comment {
	public enum Type { ERROR, SEGMENT, VALUE }
	public final MsgPos pos;
	public final String msg;
	/** start and end index of validation message, can be null if the message does not have a real location */
	public final int[] indexes;
	public final Type type;
	public Comment (MsgPos pos, String msg, int[] indexes, Type type) {
		this.pos = pos;
		this.msg = msg;
		this.indexes = indexes;
		this.type = type;
	}
	@Override
	public String toString () {
		return "ValidationMessage [pos=" + pos + ", msg=" + msg + ", indexes=" + Arrays.toString(indexes) + ", type=" + type + "]";
	}
	
}
