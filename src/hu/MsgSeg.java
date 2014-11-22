package hu;

import ca.uhn.hl7v2.Location;
import ca.uhn.hl7v2.model.Segment;

class MsgSeg {
	final Segment segment;
	final Location location;
	public MsgSeg (Segment segment, Location location) {
		this.segment = segment;
		this.location = location;
	}
}