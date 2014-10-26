package hu;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

/** printing visitor */
public class StringMessageVisitor extends MessageVisitorAdapter {
	
	private final StringBuilder sb = new StringBuilder();
	
	@Override
	public boolean start (Group group, Location location) throws HL7Exception {
		sb.append(location + " is group " + group.getName() + "\n");
		return true;
	}
	
	@Override
	public boolean start2 (Segment segment, Location location) throws HL7Exception {
		sb.append(location + " is segment " + segment.getName() + "\n");
		return true;
	}
	
	@Override
	public boolean start2 (Field field, Location location) throws HL7Exception {
		sb.append(location + " is field\n");
		return true;
	}
	
	@Override
	public boolean start2 (Composite type, Location location) throws HL7Exception {
		sb.append(location + " is composite type " + type.getName() + "\n");
		return true;
	}
	
	@Override
	public boolean visit2 (Primitive type, Location location) throws HL7Exception {
		sb.append(location + " is primitive type " + type.getName() + " value " + type + "\n");
		return true;
	}
	
	@Override
	public String toString () {
		return sb.toString();
	}
	
}
