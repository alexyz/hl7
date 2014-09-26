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
	private final String msgCr;
	private final Sep sep;
	private final List<VE> errors = new ArrayList<>();
	
	private int segOrd;
	
	/**
	 * msgCr - reference for getting index of errors
	 */
	public ValidatingMessageVisitor (String msgCr, Sep sep, String version) {
		this.msgCr = msgCr;
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
		for (PrimitiveTypeRule rule : rules) {
			String v = rule.correct(type.getValue());
			ValidationException[] ves = rule.apply(v);
			if (ves.length > 0) {
				String errorMsg = rule.getDescription().replace("%s", type.getValue());
				System.out.println("vmv visit2: " + location + " -> " + errorMsg);
				int rep = Math.max(location.getFieldRepetition(), 0);
				int comp = Math.max(location.getComponent(), 1);
				int subcomp = Math.max(location.getSubcomponent(), 1);
				Pos pos = new Pos(segOrd, location.getField(), rep, comp, subcomp);
				errors.add(new VE(pos, errorMsg, MsgUtil.getIndex(msgCr, sep, pos)));
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
