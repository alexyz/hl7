package hu.mv;

import java.util.*;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

/** message visitor adaptor */
public class MessageVisitorAdapter implements MessageVisitor {
	
	/**
	 * continue visiting structures (true by default)
	 */
	protected boolean continue_ = true;
	protected Segment currentSegment;
	protected Field currentField;
	protected Group currentGroup;
	protected Message currentMessage;
	protected Composite currentComposite;
	protected String[] currentSegmentNames;
	
	private final Map<String, Integer> fieldReps = new TreeMap<>();
	
	@Override
	public final boolean start (Message message) throws HL7Exception {
		currentMessage = message;
		currentGroup = message;
		return start2((AbstractMessage) message);
	}
	
	public boolean start2 (AbstractMessage message) throws HL7Exception {
		return continue_;
	} 
	
	@Override
	public final boolean end (Message message) throws HL7Exception {
		currentMessage = null;
		currentGroup = null;
		return end2((AbstractMessage) message);
	}
	
	public boolean end2 (AbstractMessage message) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean start (Group group, Location location) throws HL7Exception {
		currentGroup = group;
		return start2((AbstractGroup) group, location);
	}
	
	public boolean start2 (AbstractGroup group, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean end (Group group, Location location) throws HL7Exception {
		currentGroup = null;
		return end2((AbstractGroup) group, location);
	}
	
	public boolean end2 (AbstractGroup group, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean start (Segment segment, Location location) throws HL7Exception {
		currentSegment = segment;
		currentSegmentNames = segment.getNames();
		fieldReps.clear();
		Group parent = segment.getParent();
		if (parent instanceof AbstractGroup) {
			AbstractGroup ag = (AbstractGroup) parent;
			loop: for (String name : ag.getNames()) {
				for (Structure st : ag.getAll(name)) {
					if (st == segment) {
						// did hapi get the path wrong?
						if (!location.getSegmentName().equals(name)) {
							location.withSegmentName(name);
						}
						break loop;
					}
				}
			}
		}
		return start2((AbstractSegment)segment, location);
	}
	
	public boolean start2 (AbstractSegment segment, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean end (Segment segment, Location location) throws HL7Exception {
		currentSegment = null;
		return end2((AbstractSegment)segment,location);
	}
	
	public boolean end2 (AbstractSegment segment, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean start (Field field, Location location) throws HL7Exception {
		currentField = field;
		fieldReps.clear();
		return start2(field, location);
	}
	
	public boolean start2 (Field field, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean end (Field field, Location location) throws HL7Exception {
		currentField = null;
		return end2(field,location);
	}
	
	public boolean end2 (Field field, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean start (Composite type, Location location) throws HL7Exception {
		currentComposite = type;
		updateFieldRepetition(location);
		return start2(type, location);
	}
	
	public boolean start2 (Composite type, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean end (Composite type, Location location) throws HL7Exception {
		return end2(type,location);
	}
	
	public boolean end2 (Composite type, Location location) throws HL7Exception {
		return continue_;
	}
	
	@Override
	public final boolean visit (Primitive type, Location location) throws HL7Exception {
		updateFieldRepetition(location);
		return visit2(type, location);
	}
	
	/** visit with fixed field repetition count */
	public boolean visit2 (Primitive type, Location location) throws HL7Exception {
		return continue_;
	}
	
	/** fix the repetition count being 0 */
	private void updateFieldRepetition (Location location) {
		String path = location.toString();
		Integer i = fieldReps.get(path);
		if (i == null) {
			i = 0;
		} else {
			i++;
		}
		fieldReps.put(path, i);
		if (i > 0) {
			location.withFieldRepetition(i);
		}
	}
	
}
