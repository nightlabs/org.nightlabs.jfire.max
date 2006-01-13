/**
 * 
 */
package org.nightlabs.jfire.scripting;

import java.util.Map;

import javax.jdo.JDOHelper;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * This implementation of {@link ScriptExecutor} supports JavaScript
 * and executes it using Rhino.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ScriptExecutorJavaScript
		extends ScriptExecutor
{
	public static final String LANGUAGE_JAVA_SCRIPT = "JavaScript";

	@Override
	protected Object doExecute()
			throws ScriptException
	{
		Context context = Context.enter();
		try {
//		 Scriptable scope = context.initStandardObjects();
			Scriptable scope = new ImporterTopLevel(context);

//			String importPackages = getImportPackages();
//			if (importPackages != null)
//				formula = "importPackage(" + importPackages + ");\n" + formula;
//
//			String importClasses = getImportClasses();
//			if (importClasses != null)
//				formula = "importClass(" + importClasses + ");\n" + formula;

			for (Map.Entry<String, Object> me : getParameterValues().entrySet()) {
				Object js_value = Context.javaToJS(me.getValue(), scope);
				ScriptableObject.putProperty(scope, me.getKey(), js_value);
			}

			Script script = getScript();
			Object result = context.evaluateString(
					scope, script.getText(), JDOHelper.getObjectId(script).toString(), 1, null);

			return result;
		} finally {
			Context.exit();
		}
	}

}
