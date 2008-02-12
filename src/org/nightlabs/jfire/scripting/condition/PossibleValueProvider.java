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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.scripting.Script;
import org.nightlabs.jfire.scripting.condition.id.PossibleValueProviderID;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.scripting.condition.id.PossibleValueProviderID"
 *		detachable="true"
 *		table="JFireScripting_PossibleValueProvider"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @jdo.create-objectid-class field-order="organisationID, scriptRegistryItemType, scriptRegistryItemID"
 *
 * @jdo.fetch-group name="PossibleValueProvider.this" fetch-groups="default" fields="organisationID, scriptRegistryItemID, scriptRegistryItemType, labelProviderClassName" 
 */
public abstract class PossibleValueProvider
implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(PossibleValueProvider.class);

	public static final String FETCH_GROUP_THIS_POSSIBLE_VALUE_PROVIDER = "PossibleValueProvider.this";
	
	public static final int LIMIT_UNLIMITED = -1;

	/**
	 * This method returns the <code>PossibleValueProvider</code> for
	 * the given {@link Script}.
	 *
	 * @param pm
	 * @param script
	 * @return Never returns <code>null</code>. If necessary, an instance of
	 *		{@link DefaultPossibleValueProvider} is created and registered.
	 */
	public static final PossibleValueProvider getDefaultPossibleValueProvider(
			PersistenceManager pm, Script script)
	{
		try {
			PossibleValueProvider provider = (PossibleValueProvider) pm.getObjectById(PossibleValueProviderID.create(
					script.getOrganisationID(),
					script.getScriptRegistryItemType(),
					script.getScriptRegistryItemID()));
			provider.getLabelProviderClassName(); // JPOX WORKAROUND
			return provider;
		} catch (JDOObjectNotFoundException x) {
			DefaultPossibleValueProvider provider = pm.makePersistent(new DefaultPossibleValueProvider(script));
			return provider;
		}
	}
		
	/**
	 * @deprecated for JDO only 
	 */
	@Deprecated
	protected PossibleValueProvider() {
	}

	public PossibleValueProvider(Script script) //ScriptRegistryItemID scriptRegistryItemID)
	{
		this.script = script;
		this.organisationID = script.getOrganisationID();
		this.scriptRegistryItemType = script.getScriptRegistryItemType();
		this.scriptRegistryItemID = script.getScriptRegistryItemID();
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	protected String getOrganisationID() {
		return organisationID;
	}
	
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
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Script script;

	public Script getScript() {
		return script;
	}
	
	/**
	 * 
	 * @return a {@link Collection} of possible values for the result of the {@link Script}
	 * returned from {@link PossibleValueProvider#getScript()}
	 * based on given the parameterValues, the limit flag can be determined to limit the results
	 * 
	 */
	public abstract Collection getPossibleValues(Map<String, Object> parameterValues, int limit);
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"  
	 */		
	private String labelProviderClassName = LabelProvider.class.getName();
	
	public String getLabelProviderClassName() {
		return labelProviderClassName;
	}
	
	public void setLabelProviderClassName(String labelProviderClassName) {
		this.labelProviderClassName = labelProviderClassName;
	}
	
//	private ILabelProvider labelProvider;
//	/**
//	 * returns a {@link ILabelProvider} for the possibleValues,
//	 * this method returns the default implemenation {@link LabelProvider}
//	 * which just returns Object.toString().
//	 * Inheritans can override this method to provide an own {@link ILabelProvider}
//	 * for the possible Values 
//	 * 
//	 * @return a {@link ILabelProvider} for the possibleValues
//	 */
//	public ILabelProvider getLabelProvider() 	
//	{
//		if (labelProvider == null) 
//		{
//			try {
//				Class labelProviderClass = Class.forName(labelProviderClassName);
//				Object lp = labelProviderClass.newInstance();
//				if (lp instanceof ILabelProvider) {
//					labelProvider = (ILabelProvider) lp;
//				}				
//			} catch (Exception e) {
//				logger.error("There occured an error while trying to create an instance for the class "+
//						labelProviderClassName, e);
//			}
//		}
//		return labelProvider;
//	}
//	
//	public void setLabelProvider(ILabelProvider labelProvider) {
//		this.labelProvider = labelProvider;
//		labelProviderClassName = labelProvider.getClass().getName();
//	}
	
}
