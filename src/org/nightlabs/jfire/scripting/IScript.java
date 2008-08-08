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

import java.io.Serializable;

/**
 * Base interface for all scripts which can be interpreted by the {@link ScriptRegistry}.
 * An IScript knows its resultClass ({@link #getResultClass()}, its (optional) 
 * String representation of the "script" ({@link #getText()} and its (optional)
 * IScriptParameterSet ({@link #getParameterSet()} if the script has parameters.
 * It also needs to know in which language it is written ({@link #getLanguage()}.
 * Furthermore as many results for Scripts are JDO Objects we added some convenience 
 * methods so that the script itself knows if the result needs to be detached and if so,
 * with which fetch groups and with which fetch depth.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface IScript
extends Serializable
{
	/**
	 * Accessor to the language of the script. Currently,
	 * the {@link ScriptExecutorJavaScript} supports {@link ScriptExecutorJavaScript#LANGUAGE_JAVA_SCRIPT}}
	 * ({@value ScriptExecutorJavaScript#LANGUAGE_JAVA_SCRIPT}) and
	 * you can write java classes (working with or without the script text)
	 * which should extend {@link ScriptExecutorJavaClass} and support
	 * {@link ScriptExecutorJavaClass#LANGUAGE_JAVA_CLASS}.
	 *
	 * @return Returns the language.
	 */
	public String getLanguage();
	
	/**
	 * Returns the {@link Class} for the script result.
	 * @return the {@link Class} for the script result
	 */
	public Class<?> getResultClass() throws ClassNotFoundException;
	
	/**
	 * Returns the class name of the result as String.
	 * @return the class name of the result as String
	 */
	public String getResultClassName();
	
	/**
	 * Gets the actual code (usually in java script).
	 * @return Returns the actual script code.
	 */
	public String getText();
	
	/**
	 * Return the input {@link IScriptParameterSet} for the Script.
	 * @return the input {@link IScriptParameterSet} for the Script
	 */
	public IScriptParameterSet getParameterSet();
		
	/**
	 * Determines if the script value should be detached or not.
	 * @return if the script value should be detached or not
	 */
	public boolean isNeedsDetach();
	
	/**
	 * Determines the maxFetchDepth if {@link IScript#isNeedsDetach()} returns true.
	 * @return the maxFetchDepth if {@link IScript#isNeedsDetach()} returns true
	 */
	public int getMaxFetchDepth();
	
	/**
	 * Determines the fetchGroups which should be used if the {@link IScript#isNeedsDetach()} returns true.
	 * @return the fetchGroups which should be used if the {@link IScript#isNeedsDetach()} returns true
	 */
	public String[] getFetchGroups();	
}
