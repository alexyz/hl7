package hu;

class Pos {
	public final int segOrd, fieldOrd, fieldRep, compOrd, subCompOrd;
	public Pos (int segOrd, int fieldOrd, int fieldRep, int compOrd, int subCompOrd) {
		this.segOrd = segOrd;
		this.fieldOrd = fieldOrd;
		this.fieldRep = fieldRep;
		this.compOrd = compOrd;
		this.subCompOrd = subCompOrd;
	}
	@Override
	public String toString () {
		return "Pos [segOrd=" + segOrd + ", fieldOrd=" + fieldOrd + ", fieldRep=" + fieldRep + ", compOrd=" + compOrd + ", subCompOrd=" + subCompOrd + "]";
	}
	
}