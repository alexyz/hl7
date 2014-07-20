package hu;

import java.util.*;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.Location;
import ca.uhn.hl7v2.model.*;
import ca.uhn.hl7v2.validation.*;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

public class ValidatingMessageVisitor extends MessageVisitorAdapter {
	private final ValidationContext v = ValidationContextFactory.defaultValidation();
	private final String version;
	private final String msgstr;
	private final Sep sep;
	
	private int segOrd;
	public ValidatingMessageVisitor (String msgstr, Sep sep, String version) {
		this.msgstr = msgstr;
		this.sep = sep;
		this.version = version;
	}
	@Override
	public boolean start (Segment segment, Location location) throws HL7Exception {
		segOrd++;
		return true;
	}
	@Override
	public boolean visit2 (Primitive type, Location location) throws HL7Exception {
		Collection<PrimitiveTypeRule> rules = v.getPrimitiveRules(version, type.getName(), type);
//		System.out.println(location + " rep " + location.getFieldRepetition() + " value: " + type);
		for (PrimitiveTypeRule rule : rules) {
			String v = rule.correct(type.getValue());
			ValidationException[] ves = rule.apply(v);
			for (ValidationException ve : ves) {
				System.out.println("location: " + location);
				System.out.println("  type: " + type);
				System.out.println("  rule: " + rule);
				System.out.println("  exception: " + ve);
				System.out.println("  seg ord: " + segOrd);
				System.out.println("  field ord: " + location.getField());
				System.out.println("  field rep: " + location.getFieldRepetition());
				System.out.println("  comp ord: " + location.getComponent());
				System.out.println("  sub comp ord: " + location.getSubcomponent());
				System.out.println("  message: " + rule.getDescription().replace("%s", type.getValue()));
				int[] indexes = Util.getIndex(msgstr, sep, new Pos(segOrd, location.getField(), location.getFieldRepetition(), location.getComponent(), Math.max(1, location.getSubcomponent())));
				System.out.println("  indexes=" + Arrays.toString(indexes));
				System.out.println("  string=" + msgstr.substring(indexes[0], indexes[1]));
				// TODO get the indexes of this and highlight in message
			}
		}
		return true;
	}
}
