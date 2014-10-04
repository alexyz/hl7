package hu;

import java.util.*;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

/** message visitor adaptor */
public class MessageVisitorAdapter implements MessageVisitor {
	
	/**
	 * continue visiting structures (true by default)
	 */
	protected boolean continue_ = true;
	
	private final Map<String,Integer> repIndexes = new TreeMap<>();
	
	@Override
	public boolean start (Message message) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean end (Message message) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean start (Group group, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean end (Group group, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean start (Segment segment, Location location) throws HL7Exception {
		repIndexes.clear();
		return start2(segment, location);
	}
	
	public boolean start2 (Segment segment, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean end (Segment segment, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean start (Field field, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean end (Field field, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean start (Composite type, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean end (Composite type, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public boolean visit (Primitive type, Location location) throws HL7Exception {
		// fix the repetition count being 0
		String l = location.toString();
		Integer repIndex = repIndexes.get(l);
		if (repIndex == null) {
			repIndex = 0;
		} else {
			repIndex++;
		}
		repIndexes.put(l, repIndex);
		if (repIndex > 0) {
			location.withFieldRepetition(repIndex);
		}
		return visit2 (type, location);
	}
	
	/** visit with fixed field repetition count */
	public boolean visit2 (Primitive type, Location location) throws HL7Exception {
		return continue_;
	}
}
