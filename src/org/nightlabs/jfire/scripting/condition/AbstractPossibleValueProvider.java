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
package org.nightlabs.jfire.scripting.condition;

import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.id.AbstractPossibleValueProvider"
 *		detachable="true"
 *		table="JFireScripting_AbstractPossibleValueProvider"
 *
 * @jdo.inheritance strategy="subclass-table"
 * 
 * @jdo.create-objectid-class
 *
 */
public abstract class AbstractPossibleValueProvider 
implements IPossibleValueProvider 
{
//	public AbstractPossibleValueProvider(ScriptRegistryItemID scriptID) {
//		this.scriptRegistryItemID = scriptID;
//	}
	
	/**
	 * @deprecated for JDO only 
	 */
	protected AbstractPossibleValueProvider() {
		
	}
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * 						primary-key="true"
	 * @jdo.column length="100"  
	 */	
	private ScriptRegistryItemID scriptRegistryItemID;
	
	public ScriptRegistryItemID getScriptRegistryItemID() {
		return scriptRegistryItemID;
	}	

}
