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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jdo.Extent;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.spi.PersistenceCapable;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.ScriptRegistry;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * The default PossibleValueProvider,
 * takes the resultClass of the script with the given ScriptRegistryItemID
 * and returns all instances of the same class as possible
 * values, from the datastore.
 *
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.scripting.condition.PossibleValueProvider"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */@javax.jdo.annotations.PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class DefaultPossibleValueProvider
extends PossibleValueProvider
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(DefaultPossibleValueProvider.class);

	public DefaultPossibleValueProvider(Script script) // ScriptRegistryItemID scriptID)
	{
		super(script);
	}

	@Override
	public List<Object> getPossibleValues(Map<String, Object> parameterValues, int limit)
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		ScriptRegistry scriptRegistry = ScriptRegistry.getScriptRegistry(pm);
		Script script = scriptRegistry.getScript(getScriptRegistryItemType(),
				getScriptRegistryItemID());
		List<Script> scripts = new ArrayList<Script>(1);
		scripts.add(script);
		try {
			Map<ScriptRegistryItemID, Object> results = scriptRegistry.execute(scripts, parameterValues);
			if (results != null)
			{
				ScriptRegistryItemID itemID = ScriptRegistryItemID.create(
						getOrganisationID(), getScriptRegistryItemType(), getScriptRegistryItemID());
				Object result = results.get(itemID);
				if (result instanceof PersistenceCapable) {
					// TODO: write as query to determine a limit
					Extent extent = pm.getExtent(result.getClass());
					List<Object> possibleValues = new LinkedList<Object>();
					for (Iterator it = extent.iterator(); it.hasNext(); ) {
						possibleValues.add(it.next());
//						possibleValues.add(pm.getObjectId(it.next()));
					}
					if (logger.isDebugEnabled())
						logger.debug(possibleValues.size()+" possible values for class "+result.getClass());
					return possibleValues;
				} else {
					logger.warn("resultClass of script scriptRegistryItemID "+getScriptRegistryItemID()+" is no instance of PersistenceCapable!");
				}
			}
		} catch (Exception e) {
			logger.error("getPossibleValues() produced the following error "+e);
		}
		return Collections.emptyList();
	}

}
