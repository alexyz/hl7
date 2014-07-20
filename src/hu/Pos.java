package hu;

/** a logical position in a message */
class Pos implements Comparable<Pos> {
	public final int segOrd, fieldOrd, fieldRep, compOrd, subCompOrd;
	public Pos (int segOrd, int fieldOrd, int fieldRep, int compOrd, int subCompOrd) {
		this.segOrd = segOrd;
		this.fieldOrd = fieldOrd;
		this.fieldRep = fieldRep;
		this.compOrd = compOrd;
		this.subCompOrd = subCompOrd;
	}
	@Override
	public int compareTo (Pos p) {
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
		if (obj instanceof Pos) {
			Pos p = (Pos) obj;
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