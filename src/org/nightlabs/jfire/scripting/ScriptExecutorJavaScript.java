/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://www.gnu.org/copyleft/lesser.html                                 *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.scripting;

import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

/**
 * This implementation of {@link ScriptExecutor} supports JavaScript
 * and executes it using Rhino.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ScriptExecutorJavaScript
		extends ScriptExecutor
{
	private static final Logger logger = Logger.getLogger(ScriptExecutorJavaScript.class);
	public static final String LANGUAGE_JAVA_SCRIPT = "JavaScript";
	public static final String FILE_EXTENSION_JAVA_SCRIPT = "js";

	/**
	 * Every JavaScript that is executed by this ScriptExecutor has access to a
	 * {@link PersistenceManager} via this variable name ({@value #VARIABLE_PERSISTENCE_MANAGER}).
	 */
	public static final String VARIABLE_PERSISTENCE_MANAGER = "persistenceManager";

	@Override
	protected Object doExecute()
			throws ScriptException
	{
		Context context = Context.enter();
		try {
			Scriptable scope = new ImporterTopLevel(context);

			if (getPersistenceManager() != null) {
				Object js_pm = Context.javaToJS(getPersistenceManager(), scope);
				ScriptableObject.putProperty(scope, VARIABLE_PERSISTENCE_MANAGER, js_pm);
			}

			for (Map.Entry<String, Object> me : getParameterValues().entrySet()) {
				Object js_value = Context.javaToJS(me.getValue(), scope);
				ScriptableObject.putProperty(scope, me.getKey(), js_value);
			}

			IScript script = getScript();
			String sourceName = "Script";
			if (script instanceof PersistenceCapable) {
				sourceName = JDOHelper.getObjectId(script).toString();
			}
			
			logger.warn("script.getText() = "+script.getText());
			
			Object result = context.evaluateString(
					scope, script.getText(), sourceName, 1, null);
			
			if (result instanceof Undefined)
				result = null;
			else if (result instanceof NativeJavaObject)
				result = ((NativeJavaObject)result).unwrap();
			else if (result instanceof Boolean)
				; // fine - no conversion necessary
			else if (result instanceof Number)
				; // fine - no conversion necessary
			else if (result instanceof String)
				; // fine - no conversion necessary
			else
				throw new IllegalStateException("context.evaluateString(...) returned an object of an unknown type: " + (result == null ? null : result.getClass().getName()));

			return result;
		} finally {
			Context.exit();
		}
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { FILE_EXTENSION_JAVA_SCRIPT };
	}

	@Override
	public String getLanguage() {
		return LANGUAGE_JAVA_SCRIPT;
	}
	
}
