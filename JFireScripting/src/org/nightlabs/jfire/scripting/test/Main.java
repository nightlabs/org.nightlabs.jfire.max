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
package org.nightlabs.jfire.scripting.test;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.nightlabs.jfire.scripting.condition.VisibleScope;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class Main 
{
	public static final String LINE_BREAK = "\n";
	
	public static void main(String[] args)
	{
		Context context = Context.enter();
		try {
			Scriptable scope = new ImporterTopLevel(context);
			VisibleScope visibleScope= new VisibleScope();
			String varNameVisible = VisibleScope.VARIABLE_NAME;
			Object js_pm = Context.javaToJS(visibleScope, scope);
			ScriptableObject.putProperty(scope, varNameVisible, js_pm);
			runJavaScript(context, scope, getScriptText(true), getParameterValues(true, visibleScope));
			runJavaScript(context, scope, getScriptText(true), getParameterValues(true, visibleScope));
		} finally {
			Context.exit();
		}
	}

	
//	private static void runJavaScript(String text, Map<String, Object> parameter)
//	{
//		Context context = Context.enter();
//		try {
//			Scriptable scope = new ImporterTopLevel(context);
//
////			boolean visible = false;
////			String varNameVisible = "visible";
////			Object js_pm = Context.javaToJS(visible, scope);
////			ScriptableObject.putProperty(scope, varNameVisible, js_pm);			
//
//			runJavaScript(context, scope, text, parameter);
//		} finally {
//			Context.exit();
//		}
//	}

	private static void runJavaScript(Context context, Scriptable scope, String text, 
			Map<String, Object> parameter) 
	{
		context = Context.enter();
		for (Map.Entry<String, Object> me : parameter.entrySet())
		{
			Object js_value = Context.javaToJS(me.getValue(), scope);
			ScriptableObject.putProperty(scope, me.getKey(), js_value);
		}

		String sourceName = "Script";
		String scriptText = text;
		Object result = context.evaluateString(
				scope,
				scriptText,
				sourceName, 1, null);

		System.out.println("");
		System.out.println("result=" + result);
		System.out.println("result.class=" + (result == null ? null : result.getClass().getName()));

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
		context.exit();
	}
	
	private static Map<String, Object> getParameterValues(boolean value, VisibleScope visibleScope)
	{
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put("CustomerIsAnonymous", value);
		parameter.put(VisibleScope.VARIABLE_NAME, visibleScope);
		return parameter;
	}

	private static String getScriptText(boolean value)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("expression = eval(CustomerIsAnonymous=="+value+");");
		sb.append(LINE_BREAK);
		sb.append("if (visibleScope.isVisible() == false) {");
		sb.append(LINE_BREAK);
		sb.append("	if (expression == true) {");
		sb.append(LINE_BREAK);
		sb.append("		visibleScope.setVisible(true)");
		sb.append(LINE_BREAK);
		sb.append("	}");
		sb.append(LINE_BREAK);
		sb.append("}");
		sb.append(LINE_BREAK);
		sb.append("else {");
		sb.append(LINE_BREAK);
		sb.append("	expression = false");
		sb.append(LINE_BREAK);
		sb.append("}");
		sb.append(LINE_BREAK);
		sb.append("expression");
		String script = sb.toString();
		System.out.println(script);
		return script;
	}
	
}
