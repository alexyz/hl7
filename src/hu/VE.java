package hu;

public class VE {
	public enum Type { ERROR, INFO }
	public final Pos pos;
	public final String msg;
	public final int[] indexes;
	public final Type type;
	public VE (Pos pos, String msg, int[] indexes, Type type) {
		this.pos = pos;
		this.msg = msg;
		this.indexes = indexes;
		this.type = type;
	}
}