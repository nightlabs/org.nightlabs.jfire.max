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

import java.util.Collection;
import java.util.Collections;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.id.ScriptRegistryItemID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.condition.id.PossibleValueProviderID"
 *		detachable="true"
 *		table="JFireScripting_PossibleValueProvider"
 *
 * @jdo.inheritance strategy="subclass-table"
 * 
 * @jdo.create-objectid-class field-order="organisationID, scriptRegistryItemType, scriptRegistryItemID"
 *
 * @jdo.query
 *		name="getPossibleValueProviderByScriptRegistryItemID"
 *		query="SELECT
 *			WHERE
 *				this.organisationID == pOrganisationID &&
 *				this.scriptRegistryItemType == pScriptRegistryItemType &&
 *				this.scriptRegistryItemID == pScriptRegistryItemID
 *
 *			PARAMETERS pOrganisationID, pScriptRegistryItemType, pScriptRegistryItemID 
 *			import java.lang.String"
 */
public abstract class PossibleValueProvider
{
	private static final Logger logger = Logger.getLogger(PossibleValueProvider.class);

	public static PossibleValueProvider getPossibleValueProvider(PersistenceManager pm, String organisationID,
			String scriptRegistryItemType, String scriptRegistryItemID) 
	{
		Query q = pm.newNamedQuery(Script.class, "getPossibleValuesByScriptRegistryItemID");
		Collection providers = (Collection) q.execute(organisationID, scriptRegistryItemType, scriptRegistryItemID);
		if (providers != null && !providers.isEmpty()) {
			PossibleValueProvider provider = (PossibleValueProvider) providers.iterator().next();
			if (providers.size() > 1)
				logger.warn("There exist more than one PossibleValueProvider for " +
						"organisationID "+organisationID+", "+
						"scriptRegistryItemType "+scriptRegistryItemType+" and "+
						"scriptRegistryItemID "+scriptRegistryItemID);
			return provider;
		}
		return null;
	}

	public static PossibleValueProvider getPossibleValueProvider(PersistenceManager pm, 
			ScriptRegistryItemID scriptRegistryItemID)
	{
		return getPossibleValueProvider(pm, scriptRegistryItemID.organisationID, 
				scriptRegistryItemID.scriptRegistryItemType, scriptRegistryItemID.scriptRegistryItemID);
	}
	
	/**
	 * @deprecated for JDO only 
	 */
	protected PossibleValueProvider() {
		
	}

	public PossibleValueProvider(ScriptRegistryItemID scriptRegistryItemID)
	{
		this.organisationID = scriptRegistryItemID.organisationID;
		this.scriptRegistryItemType = scriptRegistryItemID.scriptRegistryItemType;
		this.scriptRegistryItemID = scriptRegistryItemID.scriptRegistryItemID;
	}
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String scriptRegistryItemType;
	
	protected String getScriptRegistryItemType() {
		return scriptRegistryItemType;
	}
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String scriptRegistryItemID;

	protected String getScriptRegistryItemID() {
		return scriptRegistryItemID;
	}
	
	/**
	 * 
	 * @return a {@link Collection} of possible values for the result of the {@link Script}
	 * with the {@link ScriptRegistryItemID} returned from {@link PossibleValueProvider#getScriptRegistryItemID()}
	 */
	public abstract Collection<Object> getPossibleValues();
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"  
	 */		
	private String labelProviderClassName = LabelProvider.class.getName();
	
	private ILabelProvider labelProvider;
	
	/**
	 * returns a {@link ILabelProvider} for the possibleValues,
	 * this method returns the default implemenation {@link LabelProvider}
	 * which just returns Object.toString().
	 * Inheritans can override this method to provide an own {@link ILabelProvider}
	 * for the possible Values 
	 * 
	 * @return a {@link ILabelProvider} for the possibleValues
	 */
	public ILabelProvider getLabelProvider() 	
	{
		if (labelProvider == null) 
		{
			try {
				Class labelProviderClass = Class.forName(labelProviderClassName);
				Object lp = labelProviderClass.newInstance();
				if (lp instanceof ILabelProvider) {
					labelProvider = (ILabelProvider) lp;
				}				
			} catch (Exception e) {
				logger.error("There occured an error while trying to create an instance for the class "+
						labelProviderClassName, e);
			}
		}
		return labelProvider;
	}
	
	public void setLabelProvider(ILabelProvider labelProvider) {
		this.labelProvider = labelProvider;
		labelProviderClassName = labelProvider.getClass().getName();
	}
	
}
