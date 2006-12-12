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

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class Main {

	public Main() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		Context context = Context.enter();
		try {
			Scriptable scope = new ImporterTopLevel(context);

			String sourceName = "Script";
			Object result = context.evaluateString(
					scope,
//					"new Packages.org.nightlabs.jfire.scripting.test.Main();",
//					"new java.lang.Long(\"5983724587634867435\")",
//					"\"Blubb\"",
					"new java.util.Date()",
					sourceName, 1, null);

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
//			else
//				throw new IllegalStateException("context.evaluateString(...) returned an object of an unknown type!");

			System.out.println("AFTER CONVERSION");
			System.out.println("result=" + result);
			System.out.println("result.class=" + (result == null ? null : result.getClass().getName()));
		} finally {
			Context.exit();
		}
	}
	
//	public static void main(String[] args) {
//		Context context = Context.enter();
//		try {
//			Scriptable scope = new ImporterTopLevel(context);
//
//			StringBuffer imports = new StringBuffer();
//			for (Map.Entry<String, Object> me : getParameterValues().entrySet()) 
//			{ 
////				Object value = me.getValue();
////				String packageName = value.getClass().getPackage().getName();
////				String importString = "importPackage(Packages."+packageName+");"+"\n";
////				imports.append(importString);
//				Object js_value = Context.javaToJS(me.getValue(), scope);
//				ScriptableObject.putProperty(scope, me.getKey(), js_value);
//			}
//
//			String sourceName = "Script";
//			String scriptText = imports.toString() + getScriptText();
//			System.out.println("scriptText = "+scriptText);
//			Object result = context.evaluateString(
//					scope,
//					scriptText,
//					sourceName, 1, null);
//
//			System.out.println("result=" + result);
//			System.out.println("result.class=" + (result == null ? null : result.getClass().getName()));
//
//			if (result instanceof Undefined)
//				result = null;
//			else if (result instanceof NativeJavaObject)
//				result = ((NativeJavaObject)result).unwrap();
//			else if (result instanceof Boolean)
//				; // fine - no conversion necessary
//			else if (result instanceof Number)
//				; // fine - no conversion necessary
//			else if (result instanceof String)
//				; // fine - no conversion necessary
////			else
////				throw new IllegalStateException("context.evaluateString(...) returned an object of an unknown type!");
//
//			System.out.println("AFTER CONVERSION");
//			System.out.println("result=" + result);
//			System.out.println("result.class=" + (result == null ? null : result.getClass().getName()));
//		} finally {
//			Context.exit();
//		}
//	}
//
//	private static Map<String, Object> getParameterValues() 
//	{
//		Map<String, Object> parameter = new HashMap<String, Object>();
//		CategorySet categorySet = new CategorySet("organisationID", "categorySet1");
//		parameter.put("Category", new Category(categorySet, 0, 0));
//		return parameter;
//	}
//	
//	private static String getScriptText() 
//	{
////		return "Category.getName().getText()==1";
////		return "Category.getCategoryIndex()==1";
//		return "Category==organisationID=organisationID&categorySetID=categorySet1&categoryID=0";
//	}
}
