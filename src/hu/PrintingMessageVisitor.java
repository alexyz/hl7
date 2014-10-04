package hu;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

/** printing visitor */
public class PrintingMessageVisitor extends MessageVisitorAdapter {
	
	@Override
	public boolean start (Message message) throws HL7Exception {
		System.out.println("start message");
		return true;
	}
	
	@Override
	public boolean start (Group group, Location location) throws HL7Exception {
		System.out.println("  " + location + " group " + group);
		return true;
	}
	
	@Override
	public boolean start2 (Segment segment, Location location) throws HL7Exception {
		System.out.println("    " + location + " segment " + segment);
		return true;
	}
	
	@Override
	public boolean start (Field field, Location location) throws HL7Exception {
		System.out.println("      " + location + " field " + field);
		return true;
	}
	
	@Override
	public boolean start (Composite type, Location location) throws HL7Exception {
		System.out.println("        " + location + " composite " + type);
		return true;
	}
	
	@Override
	public boolean visit (Primitive type, Location location) throws HL7Exception {
		System.out.println("          " + location + " primitive " + type);
		return true;
	}
	
}
