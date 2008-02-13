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

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.jfire.reporting.ReportingConstants;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.ReportParameterUtil.NameEntry;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionSetupID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.util.TimePeriod;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @ejb.bean name="jfire/ejb/JFireReporting/ReportParameterManager"
 *					 jndi-name="jfire/ejb/JFireReporting/ReportParameterManager"
 *					 type="Stateless"
 *					 transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class ReportParameterManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see com.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	@Override
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

	/**
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	public void initDefaultValueProviders() {
		PersistenceManager pm = getPersistenceManager();
		try {
			ValueProviderCategoryID categoryID = ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_SIMPLE_TYPES;
			ValueProviderCategory simpleTypes = ReportParameterUtil.createValueProviderCategory(pm, null, categoryID, new NameEntry[] {
					new NameEntry(Locale.ENGLISH.getLanguage(), "Simple types")
			});

			ReportParameterUtil.createValueProvider(pm, simpleTypes, ReportingConstants.VALUE_PROVIDER_ID_STRING, String.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "String")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Query a Text from the user")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Define a text")}
			);

			ReportParameterUtil.createValueProvider(pm, simpleTypes, ReportingConstants.VALUE_PROVIDER_ID_INTEGER, Integer.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Integer")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Query a number from the user")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Define a number")}
			);

			ReportParameterUtil.createValueProvider(pm, simpleTypes, ReportingConstants.VALUE_PROVIDER_ID_BIG_DECIMAL, BigDecimal.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "BigDecimal")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Query a long number from the user")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Define a long number")}
			);

			ReportParameterUtil.createValueProvider(pm, simpleTypes, ReportingConstants.VALUE_PROVIDER_ID_DOUBLE, Double.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Double")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Query a decimal number from the user")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Define a decimal")}
			);

			ReportParameterUtil.createValueProvider(pm, simpleTypes, ReportingConstants.VALUE_PROVIDER_ID_DATE, Date.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Date")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Query a date/time from the user")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a date")}
			);

			ReportParameterUtil.createValueProvider(pm, simpleTypes, ReportingConstants.VALUE_PROVIDER_ID_TIME_PERIOD, TimePeriod.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Timeperiod")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Query a time period from the user")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a time period")}
			);


			ValueProviderCategory jfireObjects = ReportParameterUtil.createValueProviderCategory(pm, null, ReportingConstants.VALUE_PROVIDER_CATEGORY_ID_JFIRE_OBJECTS,
					new NameEntry[]{new NameEntry(Locale.ENGLISH.getLanguage(), "JFire objects")}
			);

			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_USER, UserID.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "User")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Let the user select a user.")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a user")}
			);
			
			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_CURRENT_USER, UserID.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current user")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current user pre-selected (change possible).")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current user is selected. You might select an other user.")}
			);
			
			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_CURRENT_USER_MULTIPLE,
					Collection.class.getName() + "<" + UserID.class.getName() + ">",
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current user (with multiple selection)")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current user pre-selected (change possible with multiple selection).")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current user is selected. You might select an a list of other users.")}
			);
			
			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_USERS,
					Collection.class.getName() + "<" + UserID.class.getName() + ">",
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of users")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Let the user select a list of users.")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of users")}
			);

			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_USER_GROUP, UserID.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "User group")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Let the user select a user group.")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a user group")}
			);

			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_USER_GROUPS,
					Collection.class.getName() + "<" + UserID.class.getName() + ">",
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of user groups")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Let the user select a list of user groups.")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of user groups")}
			);
			
			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_ORGANISATION, OrganisationID.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Organisation")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Let the user select an organisation.")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select an organisation")}
			);

			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_WORKSTATION, WorkstationID.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Workstation")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Let the user select a workstation.")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a workstation")}
			);
			
			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_CURRENT_WORKSTATION, WorkstationID.class.getName(),
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current workstation")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current workstation pre-selected (change possible).")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Current workstation is selected. You might select another one.")}
			);
			
			ReportParameterUtil.createValueProvider(pm, jfireObjects, ReportingConstants.VALUE_PROVIDER_ID_WORKSTATIONS,
					Collection.class.getName() + "<" + WorkstationID.class.getName() + ">",
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "List of workstations")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Let the user select a list of workstations.")},
					new NameEntry[] {new NameEntry(Locale.ENGLISH.getLanguage(), "Select a list of workstations")}
			);
		} finally {
			pm.close();
		}
	}


	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@SuppressWarnings("unchecked")
	public Set<ReportParameterAcquisitionSetup> getValueProviders(
			Set<ValueProviderID> providerIDs, String[] fetchGroups, int maxFetchDepth
	)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, providerIDs, ValueProvider.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
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
	 * @ejb.transaction type="Required"
	 */
	@SuppressWarnings("unchecked")
	public Set<ReportParameterAcquisitionSetup> getReportParameterAcquisitionSetups(
			Set<ReportParameterAcquisitionSetupID> setupIDs, String[] fetchGroups, int maxFetchDepth
	)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, setupIDs, ReportParameterAcquisitionSetup.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}


	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Set<ValueProviderCategoryID> getValueProviderCategoryIDsForParent(ValueProviderCategoryID valueProviderCategoryID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ValueProviderCategory category = valueProviderCategoryID != null ? (ValueProviderCategory) pm.getObjectById(valueProviderCategoryID) : null;
			return new HashSet<ValueProviderCategoryID>(ValueProviderCategory.getValueProviderCategoryIDsForParent(pm, category));
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	@SuppressWarnings("unchecked")
	public Set<ValueProviderCategory> getValueProviderCategories(
			Set<ValueProviderCategoryID> categoryIDs, String[] fetchGroups, int maxFetchDepth
	)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, categoryIDs, ValueProviderCategory.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Set<ValueProviderID> getValueProviderIDsForParent(ValueProviderCategoryID valueProviderCategoryID)
	throws ModuleException
	{
		PersistenceManager pm;
		pm = getPersistenceManager();
		try {
			ValueProviderCategory category = (ValueProviderCategory) pm.getObjectById(valueProviderCategoryID);
			return new HashSet<ValueProviderID>(ValueProvider.getValueProviderIDsForParent(pm, category));
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public ReportParameterAcquisitionSetup storeReportParameterAcquisitionSetup(
			ReportParameterAcquisitionSetup setup,
			boolean get,
			String[] fetchGroups, int maxFetchDepth)
	{
		if (setup == null)
			throw new IllegalArgumentException("ReportParameterAcquisitionSetup must not be null!");

		return NLJDOHelper.storeJDO(getPersistenceManager(),
				setup, get, fetchGroups, maxFetchDepth);
	}
}
