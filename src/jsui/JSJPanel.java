package jsui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.*;

import javax.script.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.CaretEvent;

/**
 * javascript panel
 */
public class JSJPanel extends JPanel {
	
	private static final ScriptEngineManager sem = new ScriptEngineManager();
	
	private final ScriptEngine engine;
	private final JTextArea inputArea;
	private final JTextArea outputArea;
	private final JScrollPane inputScroller;
	private final JScrollPane outputScroller;
	private final JPanel buttonPanel;
	
	private Font jsFont;
	
	public JSJPanel () {
		super(new BorderLayout());
		
		engine = sem.getEngineByMimeType("text/javascript");
		
		outputArea = new JTextArea();
		outputArea.setEditable(false);
		outputArea.setLineWrap(true);
		
		inputArea = new JTextArea();
		inputArea.setLineWrap(true);
		inputArea.setCaretPosition(0);
		inputArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped (KeyEvent e) {
				if (e.isControlDown()) {
					final char c = e.getKeyChar();
					System.out.println("ctrl " + (int)c);
					switch (c) {
						case '\n':
						case '\r':
							eval();
							break;
						case 12:
							clear();
							break;
						case ' ':
							suggest();
							break;
					}
				}
			}
		});
		
		inputScroller = new JScrollPane(inputArea);
		inputScroller.setBorder(new TitledBorder("Input (ctrl-enter to evaluate)"));
		
		outputScroller = new JScrollPane(outputArea);
		outputScroller.setBorder(new TitledBorder("Output"));
		
		JButton evalButton = new JButton("Eval");
		evalButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				eval();
			}
		});
		
		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				clear();
			}
		});
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScroller, outputScroller);
		splitPane.setResizeWeight(0.5);
		
		buttonPanel = new JPanel();
		buttonPanel.add(evalButton);
		buttonPanel.add(clearButton);
		
		add(splitPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	public void setJSText(String text) {
		inputArea.setText(text);
		inputArea.setCaretPosition(text.length());
	}
	
	public void setJSData(Map<String,Object> map) {
		for (String k : map.keySet()) {
			engine.put(k, map.get(k));
		}
		Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
		for (String k : bindings.keySet()) {
			Object v = bindings.get(k);
			Class<? extends Object> cl = v.getClass();
			if (Proxy.isProxyClass(cl)) {
				cl = cl.getInterfaces()[0];
			}
			outputArea.append(k + " is " + cl + "\n");
		}
	}
	
	public Font getJSFont() {
		return jsFont;
	}
	
	public void setJSFont(Font jsFont) {
		this.jsFont = jsFont;
		outputArea.setFont(jsFont);
		inputArea.setFont(jsFont);
		revalidate();
	}
	
	public void clear () {
		outputArea.setText("");
	}
	
	public void eval () {
		System.out.println("eval");
		String input = inputArea.getSelectedText();
		if (input != null) {
			input = input.trim();
		} else {
			input = inputArea.getText().trim();
		}
		if (input.length() > 0) {
			try {
				final Object val = engine.eval(input);
				if (val != null) {
					outputArea.append(input + " = " + val + " [" + val.getClass().getSimpleName() + "]\n");
				} else if (!input.contains("\n")) {
					outputArea.append(input + " is null\n");
				}
			} catch (Exception e) {
				outputArea.append(": " + e.toString() + "\n");
			}
			outputArea.setCaretPosition(outputArea.getText().length());
		}
	}
	
	public void suggest () {
		System.out.println("suggest");
		final String text = inputArea.getText();
		final String sep = "\n\r\t ;";
		
		final int caretPos = inputArea.getCaretPosition();
		System.out.println("caretPos=" + caretPos);
		
		final int dotPos = text.lastIndexOf(".", caretPos);
		System.out.println("dotPos=" + dotPos);
		
		if (dotPos <= 0) {
			return;
		}
		
		int expPos;
		for (expPos = dotPos; expPos >= 0; expPos--) {
			if (sep.indexOf(text.charAt(expPos)) >= 0) {
				break;
			}
		}
		expPos++;
		System.out.println("expPos=" + expPos);
		
		if (dotPos <= expPos) {
			return;
		}
		
		String expression = text.substring(expPos, dotPos);
		System.out.println("expression=" + expression);
		String member = text.substring(dotPos + 1, caretPos).toLowerCase();
		System.out.println("member=" + member);
		
		Object value;
		try {
			value = engine.eval(expression);
			System.out.println("eval=" + value);
		} catch (ScriptException e1) {
			System.out.println(e1.getMessage());
			return;
		}
		
		if (value == null) {
			return;
		}
		
		Set<String> properties = new TreeSet<>();
		if (value instanceof String) {
			properties.add("length");
			
		} else {
			final Class<?> cl = value.getClass();
			for (Method m : cl.getMethods()) {
				if (!Modifier.isStatic(m.getModifiers())) {
					if (m.getName().toLowerCase().startsWith(member)) {
						properties.add(m.getName() + "()");
					}
				}
			}
			for (Field f : cl.getFields()) {
				if (!Modifier.isStatic(f.getModifiers())) {
					if (f.getName().toLowerCase().startsWith(member)) {
						properties.add(f.getName());
					}
				}
			}
		}
		
		System.out.println("properties=" + properties);
		if (properties.size() == 0) {
			return;
		}
		
		JPopupMenu menu = new JPopupMenu();
		ActionListener al = new ActionListener() {
			@Override
			public void actionPerformed (ActionEvent e) {
				JMenuItem item = (JMenuItem) e.getSource();
				final String t1 = text.substring(0, dotPos) + "." + item.getText();
				inputArea.setText(t1 + text.substring(caretPos));
				inputArea.setCaretPosition(t1.length());
			}
		};
		
		int count = 0;
		for (String s : properties) {
			JMenuItem item = new JMenuItem(s);
			item.addActionListener(al);
			menu.add(item);
			if (count++ > 20) {
				JMenuItem item2 = new JMenuItem("...");
				item2.setEnabled(false);
				menu.add(item2);
				break;
			}
		}
		
		Point point = inputArea.getCaret().getMagicCaretPosition();
		menu.show(inputArea, point.x, point.y);
	}
	
	public String getInputText() {
		return inputArea.getText();
	}
	
	public JPanel getButtonPanel () {
		return buttonPanel;
	}
}