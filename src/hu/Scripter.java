package hu;

import java.io.File;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.CanonicalModelClassFactory;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.validation.impl.ValidationContextFactory;

public class Scripter {
	public static void main (String[] args) {
		try {
			ScriptEngineManager sem = new ScriptEngineManager();
			ScriptEngine e = sem.getEngineByMimeType("text/javascript");
			System.out.println("function=" + e.eval("function() { }"));
			e.put("scripter", new Scripter());
			e.eval("function load(f,v) { return scripter.load(f,v); }");
			e.eval("m = load('adt.txt');");
			e.eval("print('m=' + m);");
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public ScriptMessage load (String file) throws Exception {
		return load(file, null);
	}
	
	public ScriptMessage load (String filename, String version) throws Exception {
		System.out.println("load " + filename + ", " + version);
		File f = new File(filename);
		if (!f.exists()) {
			throw new Exception("file " + f.getAbsolutePath() + " not found");
		}
		String msgstrLf = FileUtil.readFile(f);
		String msgstr = msgstrLf.replace('\n', Sep.SEGMENT);
		HapiContext context = new DefaultHapiContext();
		if (!version.equals("undefined")) {
			CanonicalModelClassFactory mcf = new CanonicalModelClassFactory(version);
			context.setModelClassFactory(mcf);
		}
		PipeParser p = context.getPipeParser();
		p.setValidationContext(ValidationContextFactory.noValidation());
		Message msg = p.parse(msgstr);
		return new ScriptMessage(msg);
	}
}