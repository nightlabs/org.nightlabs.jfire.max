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
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.reporting.parameter;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.reporting.ReportingConstants;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionSetupID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @ejb.bean name="jfire/ejb/JFireReporting/ReportParameterManager"	
 *					 jndi-name="jfire/ejb/JFireReporting/ReportParameterManager"
 *					 type="Stateless" 
 *					 transaction-type="Container"
 *
 * @ejb.util generate = "physical"
 */
public abstract class ReportParameterManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ReportParameterManagerBean.class);

	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"	
	 */
	public void ejbCreate() throws CreateException
	{
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException { }

	public void initDefaultValueProviders() {
		PersistenceManager pm = getPersistenceManager();
		try {
			
			ValueProviderCategoryID categoryID = ValueProviderCategoryID.create(Organisation.DEVIL_ORGANISATION_ID, ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES);
			ValueProviderCategory rootCategory = createValueProviderCategory(pm, null, categoryID);
			rootCategory.getName().setText(Locale.ENGLISH.getLanguage(), "Simple types");
			
			ValueProviderID stringID = ValueProviderID.create(
					Organisation.DEVIL_ORGANISATION_ID, 
					ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES, 
					ReportingConstants.VALUE_PROVIDER_ID_STRING
				);
			ValueProvider string = createValueProvider(pm, rootCategory, stringID, String.class.getName());
			string.getName().setText(Locale.ENGLISH.getLanguage(), "String");
			string.getDescription().setText(Locale.ENGLISH.getLanguage(), "Query a Text from the user");
			
			ValueProviderID integerID = ValueProviderID.create(
					Organisation.DEVIL_ORGANISATION_ID, 
					ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES, 
					ReportingConstants.VALUE_PROVIDER_ID_INTEGER
				);
			ValueProvider integer = createValueProvider(pm, rootCategory, integerID, Integer.class.getName());
			integer.getName().setText(Locale.ENGLISH.getLanguage(), "Integer");
			integer.getDescription().setText(Locale.ENGLISH.getLanguage(), "Query a number from the user");
			
			ValueProviderID doubleID = ValueProviderID.create(
					Organisation.DEVIL_ORGANISATION_ID, 
					ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES, 
					ReportingConstants.VALUE_PROVIDER_ID_DOUBLE
				);
			ValueProvider doub = createValueProvider(pm, rootCategory, doubleID, Double.class.getName());
			doub.getName().setText(Locale.ENGLISH.getLanguage(), "Double");
			doub.getDescription().setText(Locale.ENGLISH.getLanguage(), "Query a decimal number from the user");
			
			ValueProviderID dateID = ValueProviderID.create(
					Organisation.DEVIL_ORGANISATION_ID, 
					ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES, 
					ReportingConstants.VALUE_PROVIDER_ID_DATE
				);
			ValueProvider date = createValueProvider(pm, rootCategory, dateID, Date.class.getName());
			date.getName().setText(Locale.ENGLISH.getLanguage(), "Date");
			date.getDescription().setText(Locale.ENGLISH.getLanguage(), "Query a date/time from the user");
			
			ValueProviderID timePeriodID = ValueProviderID.create(
					Organisation.DEVIL_ORGANISATION_ID, 
					ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES, 
					ReportingConstants.VALUE_PROVIDER_ID_TIME_PERIOD
				);
			ValueProvider timePeriod = createValueProvider(pm, rootCategory, timePeriodID, Date.class.getName());
			timePeriod.getName().setText(Locale.ENGLISH.getLanguage(), "Timeperiod");
			timePeriod.getDescription().setText(Locale.ENGLISH.getLanguage(), "Query a time period from the user");
			
		} finally {
			pm.close();
		}
		
	}
	
	private ValueProviderCategory createValueProviderCategory(PersistenceManager pm, ValueProviderCategory parent, ValueProviderCategoryID categoryID) {
		ValueProviderCategory category = null;
		try {
			category = (ValueProviderCategory) pm.getObjectById(categoryID);
			logger.debug("Have ValueProviderCategory "+categoryID);
		} catch (JDOObjectNotFoundException e) {
			logger.debug("Creating ValueProviderCategory "+categoryID);
			category = new ValueProviderCategory(parent, categoryID.organisationID, categoryID.valueProviderCategoryID, true);
			category = (ValueProviderCategory) pm.makePersistent(category);
			logger.debug("Created ValueProviderCategory "+categoryID);
		}
		return category;
	}
	
	private ValueProvider createValueProvider(PersistenceManager pm, ValueProviderCategory category, ValueProviderID valueProviderID, String outputType) {
		ValueProvider valueProvider = null;
		try {
			valueProvider = (ValueProvider) pm.getObjectById(valueProviderID);
			logger.debug("Have ValueProvider "+valueProviderID);
		} catch (JDOObjectNotFoundException e) {
			logger.debug("Creating ValueProvider "+valueProviderID);
			valueProvider = new ValueProvider(category, valueProviderID.valueProviderID, outputType);
			valueProvider = (ValueProvider) pm.makePersistent(valueProvider);
			logger.debug("Created ValueProvider "+valueProviderID);
		}
		return valueProvider;
	}
	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ReportParameterAcquisitionSetup> getValueProviders(
			Set<ValueProviderID> providerIDs, String[] fetchGroups, int maxFetchDepth
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, providerIDs, ValueProvider.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	@SuppressWarnings("unchecked")
	public Map<ReportRegistryItemID, ReportParameterAcquisitionSetupID> getReportParameterAcquisitionSetupIDs(Collection<ReportRegistryItemID> reportLayoutIDs)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			Map<ReportRegistryItemID, ReportParameterAcquisitionSetupID> result = new HashMap<ReportRegistryItemID, ReportParameterAcquisitionSetupID>();
			for (ReportRegistryItemID itemID : reportLayoutIDs) {
				ReportParameterAcquisitionSetup setup = ReportParameterAcquisitionSetup.getSetupForReportLayout(pm, itemID);
				if (setup == null)
					result.put(itemID, null);
				else
					result.put(itemID, (ReportParameterAcquisitionSetupID) JDOHelper.getObjectId(setup));
			}
			return result;
		} finally {
			pm.close();
		}		
	}	
	
	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type = "Required"
	 */
	@SuppressWarnings("unchecked")
	public Collection<ReportParameterAcquisitionSetup> getReportParameterAcquisitionSetups(
			Set<ReportParameterAcquisitionSetupID> setupIDs, String[] fetchGroups, int maxFetchDepth
		)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, setupIDs, ReportParameterAcquisitionSetup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	
}
