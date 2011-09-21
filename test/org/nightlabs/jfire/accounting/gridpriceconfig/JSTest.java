package org.nightlabs.jfire.accounting.gridpriceconfig;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

public class JSTest {

	public static class TestClass {
		
		public boolean test(Object object) {
			return test(new Object[] {object});
		}
		
		public boolean test(Object...objects) {
			if (objects.length == 2) {
				return true;
			}
			return false;
		}
		
		public boolean test2(String...objects) {
			if (objects.length == 2) {
				return true;
			}
			return false;
		}
	}
	
	
	private ScriptEngine createJavaScriptEngine() {
		ScriptEngineManager mgr = new ScriptEngineManager();
		ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
		return jsEngine;
	}
	
	@Test
	public void testBuiltInVarargInvokation1() throws ScriptException {
		execScript("test.test(new Packages.java.util.Date())");
	}
	
	@Test
	public void testBuiltInVarargInvokation2() throws ScriptException {
		execScript("test.test([new Packages.java.util.Date(), new Packages.java.util.Date()])");
	}

	@Test
	public void testBuiltInVarargInvokation3() throws ScriptException {
		execScript("test.test2([\"1\", \"2\"])");
	}
	
	private Object execScript(String script) throws ScriptException {
		ScriptEngine scriptEngine = createJavaScriptEngine();
		ScriptContext context = new SimpleScriptContext();
		Bindings scope = context.getBindings(ScriptContext.ENGINE_SCOPE);
		scope.put("test", new TestClass());
		return scriptEngine.eval(script, scope);
	}

	@Test
	public void testRhinoVarargInvokation1() throws ScriptException {
		execRhinoScript("test.test(new Packages.java.util.Date())");
	}
	
	@Test
	public void testRhinoVarargInvokation2() throws ScriptException {
		execRhinoScript("test.test(new Packages.java.util.Date(), new Packages.java.util.Date())");
	}

	@Test
	public void testRhinoVarargInvokation3() throws ScriptException {
		execRhinoScript("test.test2(\"1\", \"2\")");
	}
	
	private Object execRhinoScript(String script) {
		Context context = Context.enter();
		Scriptable scope = new ImporterTopLevel(context);
		TestClass testClass = new TestClass();
		final Object js_testClass = Context.javaToJS(testClass, scope);
		ScriptableObject.putProperty(scope, "test", js_testClass);
		return context.evaluateString(scope, script, "", 1, null);
	}
	
}
