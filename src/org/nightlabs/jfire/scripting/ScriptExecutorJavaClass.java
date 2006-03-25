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

import javax.jdo.JDOHelper;

/**
 * 
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class ScriptExecutorJavaClass
		extends ScriptExecutor
{
	public static final String LANGUAGE_JAVA_CLASS = "JavaClass";
	public static final String FILE_EXTENSION_JAVA_CLASS = "javaclass";

	private ScriptExecutorJavaClassDelegate delegate = null;

	protected ScriptExecutorJavaClassDelegate getDelegate()
	throws ScriptException
	{
		String delegateClassName = getScript().getText();

		if (delegate != null && delegate.getClass().getName().equals(delegateClassName))
			return delegate;

		try {
			Class delegateClass = Class.forName(delegateClassName);
			Object delegateInstance = delegateClass.newInstance();
			if (!(delegateInstance instanceof ScriptExecutorJavaClassDelegate))
				throw new ClassCastException(
						"Delegate " + delegateClassName + " defined for script " + JDOHelper.getObjectId(getScript())
						+	" does not implement interface " + ScriptExecutorJavaClassDelegate.class.getName());

			delegate = (ScriptExecutorJavaClassDelegate)delegateInstance;
			delegate.setScriptExecutorJavaClass(this);
			return delegate;
		} catch (ClassNotFoundException e) {
			throw new ScriptException(e);
		} catch (InstantiationException e) {
			throw new ScriptException(e);
		} catch (IllegalAccessException e) {
			throw new ScriptException(e);
		}
	}

	@Override
	protected void doPrepare()
			throws ScriptException
	{
		super.doPrepare();
		getDelegate().doPrepare();
	}

	@Override
	protected Object doExecute()
			throws ScriptException
	{
		return getDelegate().doExecute();
	}

	@Override
	public String getLanguage() {
		return LANGUAGE_JAVA_CLASS;
	}

	@Override
	public String[] getFileExtensions() {
		return new String[] { FILE_EXTENSION_JAVA_CLASS };
	}

}
