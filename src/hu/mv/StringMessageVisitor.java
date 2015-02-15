package hu.mv;

import java.util.Set;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

/** printing visitor */
public class StringMessageVisitor extends MessageVisitorAdapter {
	
	private final StringBuilder sb = new StringBuilder();
	
	@Override
	public boolean start2 (AbstractMessage message) throws HL7Exception {
		sb.append("abstract message " + message.getName());
		Set<String> ns = message.getNonStandardNames();
		if (ns.size() > 0) {
			sb.append(" (non standard structures " + ns + ")");
		}
		sb.append("\n");
		return true;
	}
	
	@Override
	public boolean start2 (AbstractGroup group, Location location) throws HL7Exception {
		sb.append(location + " is abstract group " + group.getName());
		boolean ce = group.getParent().isChoiceElement(group.getName());
		if (ce) {
			sb.append(" (choice element)");
		}
		boolean rep = group.getParent().isRepeating(group.getName());
		if (rep) {
			sb.append(" (repeating)");
		}
		boolean req = group.getParent().isRequired(group.getName());
		if (req) {
			sb.append(" (required)");
		}
		Set<String> ns = group.getNonStandardNames();
		if (ns.size() > 0) {
			sb.append(" (non standard structures " + ns + ")");
		}
		sb.append("\n");
		return true;
	}
	
	@Override
	public boolean start2 (AbstractSegment segment, Location location) throws HL7Exception {
		sb.append(location + " is abstract segment " + segment.getName());
		int nf = segment.numFields();
		sb.append(" (" + nf + " fields)");
		sb.append("\n");
		return true;
	}
	
	@Override
	public boolean start2 (Field field, Location location) throws HL7Exception {
		sb.append(location + " is field");
		if (currentSegmentNames != null && currentSegmentNames.length > location.getField()) {
			sb.append(" " + currentSegmentNames[location.getField()]);
		}
		boolean req = currentSegment.isRequired(location.getField());
		if (req) {
			sb.append(" (required)");
		}
		int card = currentSegment.getMaxCardinality(location.getField());
		if (card == 0) {
			sb.append(" (repeating)");
		} else if (card > 1) {
			sb.append(" (repeating, max " + card + ")");
		}
		int len = currentSegment.getLength(location.getField());
		if (len > 0) {
			sb.append(" (" + len + " chars)");
		}
		sb.append("\n");
		return true;
	}
	
	@Override
	public boolean start2 (Composite type, Location location) throws HL7Exception {
		ExtraComponents ec = type.getExtraComponents();
		int ecnum = ec.numComponents();
		sb.append(location + " is composite type " + type.getName());
		if (ecnum > 0) {
			sb.append(" containing " + ecnum + " non standard components");
		}
		sb.append("\n");
		return true;
	}
	
	@Override
	public boolean visit2 (Primitive type, Location location) throws HL7Exception {
		sb.append(location + " is primitive type " + type.getName());
		if (type.getValue() != null) {
			sb.append(" value " + type);
		}
		sb.append("\n");
		return true;
	}
	
	@Override
	public String toString () {
		return sb.toString();
	}
	
}
