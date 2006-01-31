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
