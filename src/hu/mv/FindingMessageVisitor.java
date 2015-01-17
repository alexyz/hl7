package hu.mv;

import hu.MsgPath;

import java.util.*;

import ca.uhn.hl7v2.*;
import ca.uhn.hl7v2.model.*;

/** finding visitor */
public class FindingMessageVisitor extends MessageVisitorAdapter {
	
	private final List<MsgPath> list = new ArrayList<>();
	private final String text;
	private final boolean ignoreCase;
	
	public FindingMessageVisitor (String text, boolean ignoreCase) {
		this.text = ignoreCase ? text.toUpperCase() : text;
		this.ignoreCase = ignoreCase;
	}
	
	@Override
	public boolean start (Group group, Location location) throws HL7Exception {
		test(location, group.getName());
		return true;
	}
	
	@Override
	public boolean start2 (Segment segment, Location location) throws HL7Exception {
		test(location, segment.getName());
		return true;
	}
	
	@Override
	public boolean start2 (Composite type, Location location) throws HL7Exception {
		test(location, type.getName());
		return true;
	}
	
	@Override
	public boolean visit2 (Primitive type, Location location) throws HL7Exception {
		test(location, type.getValue());
		return true;
	}
	
	public List<MsgPath> getList () {
		return list;
	}
	
	private void test (Location location, String value) {
		if (value != null && (ignoreCase ? value.toUpperCase() : value).contains(text)) {
			list.add(new MsgPath(location.toString(), value));
		}
	}
	
}
