package hu;

/** a logical position in a message */
class MsgPos implements Comparable<MsgPos> {
	public final int segOrd;
	/** field ordinal - can actually be 0 (i.e. no field selected) */
	public final int fieldOrd;
	public final int fieldRep;
	public final int compOrd;
	public final int subCompOrd;
	public MsgPos (int segOrd, int fieldOrd, int fieldRep, int compOrd, int subCompOrd) {
		if (!(segOrd >= 1 || fieldOrd >= 0 || fieldRep >= 0 || compOrd >= 1 || subCompOrd >= 1)) {
			throw new RuntimeException(String.format("invalid position: seg %d >= 1 field %d >= 0 rep %d >= 0 comp %d >= 1 subcomp %d >= 1", segOrd, fieldOrd, fieldRep, compOrd, subCompOrd));
		}
		this.segOrd = segOrd;
		this.fieldOrd = fieldOrd;
		this.fieldRep = fieldRep;
		this.compOrd = compOrd;
		this.subCompOrd = subCompOrd;
	}
	@Override
	public int compareTo (MsgPos p) {
		int cmp = segOrd - p.segOrd;
		if (cmp == 0) {
			cmp = fieldOrd - p.fieldOrd;
		}
		if (cmp == 0) {
			cmp = fieldRep - p.fieldRep;
		}
		if (cmp == 0) {
			cmp = compOrd - p.compOrd;
		}
		if (cmp == 0) {
			cmp = subCompOrd - p.subCompOrd;
		}
		return cmp;
	}
	@Override
	public boolean equals (Object obj) {
		if (obj instanceof MsgPos) {
			MsgPos p = (MsgPos) obj;
			return segOrd == p.segOrd && fieldOrd == p.fieldOrd && fieldRep == p.fieldRep && compOrd == p.compOrd && subCompOrd == p.subCompOrd;
		}
		return false;
	}
	@Override
	public int hashCode () {
		return (((((((segOrd << 6) + fieldOrd) << 6) + fieldRep) << 6) + compOrd) << 6) + subCompOrd;
	}
	@Override
	public String toString () {
		return "Pos [segOrd=" + segOrd + ", fieldOrd=" + fieldOrd + ", fieldRep=" + fieldRep + ", compOrd=" + compOrd + ", subCompOrd=" + subCompOrd + "]";
	}
	
}