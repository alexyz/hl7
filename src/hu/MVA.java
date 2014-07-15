package hu;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

/** message visitor adaptor */
public class MVA implements MessageVisitor {
	
	@Override
	public boolean start (Message message) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean end (Message message) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean start (Group group, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean end (Group group, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean start (Segment segment, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean end (Segment segment, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean start (Field field, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean end (Field field, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean start (Composite type, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean end (Composite type, Location location) throws HL7Exception {
		return true;
	}
	
	@Override
	public boolean visit (Primitive type, Location location) throws HL7Exception {
		return true;
	}
	
}
