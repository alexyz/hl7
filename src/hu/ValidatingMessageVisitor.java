package hu;

import java.util.*;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.Location;
import ca.uhn.hl7v2.model.*;
import ca.uhn.hl7v2.validation.*;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

/**
 * visit every primitive and validate the data type
 */
public class ValidatingMessageVisitor extends MessageVisitorAdapter {
	
	private final ValidationContext vc = ValidationContextFactory.defaultValidation();
	private final String version;
	private final String msgstr;
	private final Sep sep;
	private final List<VE> errors = new ArrayList<>();
	
	private int segOrd;
	
	/**
	 * msgCr - reference for getting index of errors
	 */
	public ValidatingMessageVisitor (String msgCr, Sep sep, String version) {
		this.msgstr = msgCr;
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
		Collection<PrimitiveTypeRule> rules = vc.getPrimitiveRules(version, type.getName(), type);
//		System.out.println(location + " rep " + location.getFieldRepetition() + " value: " + type);
		for (PrimitiveTypeRule rule : rules) {
			String v = rule.correct(type.getValue());
			ValidationException[] ves = rule.apply(v);
			for (ValidationException ve : ves) {
//				System.out.println("location: " + location);
//				System.out.println("  type: " + type);
//				System.out.println("  rule: " + rule);
//				System.out.println("  exception: " + ve);
//				System.out.println("  seg ord: " + segOrd);
//				System.out.println("  field ord: " + location.getField());
//				System.out.println("  field rep: " + location.getFieldRepetition());
//				System.out.println("  comp ord: " + location.getComponent());
//				System.out.println("  sub comp ord: " + location.getSubcomponent());
				String errorMsg = rule.getDescription().replace("%s", type.getValue());
				System.out.println(location + " -> " + errorMsg);
				Pos pos = new Pos(segOrd, location.getField(), Math.max(0, location.getFieldRepetition()), location.getComponent(), Math.max(1, location.getSubcomponent()));
//				int[] indexes = Util.getIndex(msgstr, sep, pos);
//				System.out.println("  indexes=" + Arrays.toString(indexes));
//				System.out.println("  string=" + msgstr.substring(indexes[0], indexes[1]));
				errors.add(new VE(pos, errorMsg, MsgUtil.getIndex(msgstr, sep, pos)));
				break;
			}
		}
		return true;
	}
	
	/** get the list of errors, maybe empty, never null */
	public List<VE> getErrors () {
		return errors;
	}
}
