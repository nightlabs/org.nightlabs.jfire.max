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

package org.nightlabs.jfire.reporting.layout;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.util.Utils;

/**
 * Common type for report registry item (ReportCategory, ReportLayout).
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID"
 *		detachable = "true"
 *		table="JFireReporting_ReportRegistryItem"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportRegistryItemType, reportRegistryItemID"
 *
 *
 * @jdo.inheritance strategy = "new-table" 
 * @jdo.inheritance-discriminator strategy="class-name"
 * 
 * @jdo.fetch-group name="ReportRegistryItem.parentItem" fetch-groups="default" fields="parentItem"
 * @jdo.fetch-group name="ReportRegistryItem.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ReportRegistryItem.this" fetch-groups="default" fields="parentItem, name"
 * 
 *  
 * @jdo.query
 *		name="getReportRegistryItemByType"
 *		query="SELECT 
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.reportRegistryItemType == paramReportRegistryItemType            
 *			PARAMETERS String paramOrganisationID, String reportRegistryItemType
 *			import java.lang.String"
 * 
 * @jdo.query
 *		name="getTopLevelReportRegistryItemByType"
 *		query="SELECT UNIQUE
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.reportRegistryItemType == paramReportRegistryItemType &&
 *            this.parentItem == null    
 *			PARAMETERS String paramOrganisationID, String reportRegistryItemType
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelReportRegistryItemsByOrganisation"
 *		query="SELECT
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.parentItem == null    
 *			PARAMETERS String paramOrganisationID
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelReportRegistryItems"
 *		query="SELECT
 *			WHERE this.parentItem == null
 *			import java.lang.String"
 *
 * @jdo.query
 *	name="getReportRegistryItemsForParent"
 *	query="SELECT 
 *		WHERE this.parentItem == :paramParent"
 *
 * @jdo.query
 *	name="getReportRegistryItemIDsForParent"
 *	query="SELECT JDOHelper.getObjectId(this) 
 *		WHERE this.parentItem == :paramParent"
 */
public abstract class ReportRegistryItem implements Serializable, StoreCallback, DetachCallback
{
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
			.getLogger(ReportRegistryItem.class);
	
	public static final String QUERY_GET_REPORT_REGISTRY_ITEM_BY_TYPE = "getReportRegistryItemByType";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEM_BY_TYPE = "getTopLevelReportRegistryItemByType";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS_BY_ORGANISATION = "getTopLevelReportRegistryItemsByOrganisation";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS = "getTopLevelReportRegistryItems";

	public static final String FETCH_GROUP_PARENT_ITEM = "ReportRegistryItem.parentItem";
	/**
	 * Virtual. The {@link #parentItemID} is set 
	 */
	public static final String FETCH_GROUP_PARENT_ITEM_ID = "ReportRegistryItem.parentItemID";
	public static final String FETCH_GROUP_NAME = "ReportRegistryItem.name";
	public static final String FETCH_GROUP_THIS_REPORT_REGISTRY_ITEM = "ReportRegistryItem.this";
	
	
	protected ReportRegistryItem() {}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportRegistryItemType;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String reportRegistryItemID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportRegistryItemName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportRegistryItem parentItem;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private ReportRegistryItemID parentItemID;


	/**
	 * Creates a new ReportRegistryItem with the given parameters.
	 * If the PersistenceManager passed is not null the 
	 * reportRegistryItemID will be set to the next available
	 * long id. If null is passed it will be set to -1 and
	 * replaced upon storing.
	 * 
	 * @param pm The PersistenceManager to retrieve the ReportRegistry with.
	 * @param parentItem The parent item of the new item.
	 * @param organisationID The organisation of the new item.
	 * @param reportRegistryItemType The type of the new item.
	 */
	public ReportRegistryItem(
			ReportRegistryItem parentItem, 
			String organisationID, 
			String reportRegistryItemType,
			String reportRegistryItemID
		) 
	{
		this.parentItem = parentItem;
		this.organisationID = organisationID;
		this.reportRegistryItemType = reportRegistryItemType;
		this.reportRegistryItemID = reportRegistryItemID;
		this.name = new ReportRegistryItemName(this);
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getReportRegistryItemType() {
		return reportRegistryItemType;
	}
	
	public String getReportRegistryItemID() {
		return reportRegistryItemID;
	}
	
	public ReportRegistryItemName getName() {
		return name;
	}
	
	public ReportRegistryItem getParentItem() {
		return parentItem;
	}
	
	protected void setParentItem(ReportRegistryItem parentItem) {
		this.parentItem = parentItem;
	}

	public ReportRegistryItemID getParentItemID() {
		if (parentItemID == null) {
			parentItemID = (ReportRegistryItemID) JDOHelper.getObjectId(parentItem);
		}
		return parentItemID;
	}
	
	public static Collection getReportRegistryItemByType(PersistenceManager pm, String organisatinID, String reportRegistryItemType) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_GET_REPORT_REGISTRY_ITEM_BY_TYPE);
		return (Collection)q.execute(organisatinID, reportRegistryItemType);
	}

	public static ReportRegistryItem getTopReportRegistryItemByType(PersistenceManager pm, String organisatinID, String reportRegistryItemType) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEM_BY_TYPE);
		return (ReportRegistryItem)q.execute(organisatinID, reportRegistryItemType);
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<ReportRegistryItem> getTopReportRegistryItems(PersistenceManager pm, String organisatinID) {
		if (organisatinID == null || "".equals(organisatinID))
			return getTopReportRegistryItems(pm);
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS_BY_ORGANISATION);
		return (Collection)q.execute(organisatinID);
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<ReportRegistryItem> getTopReportRegistryItems(PersistenceManager pm) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS);
		return (Collection)q.execute();
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<ReportRegistryItem> getReportRegistryItemsForParent(PersistenceManager pm, ReportRegistryItem reportRegistryItem) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, "getReportRegistryItemsForParent");
		return (Collection<ReportRegistryItem>) q.execute(reportRegistryItem);
	}

	@SuppressWarnings("unchecked")
	public static Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(PersistenceManager pm, ReportRegistryItem reportRegistryItem) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, "getReportRegistryItemIDsForParent");
		return (Collection<ReportRegistryItemID>) q.execute(reportRegistryItem);
	}
	
	/**
	 * TODO isn't this wrong: Assigns a reportRegistryItemID for this ReportRegistryItem it this is not set yet.
	 *
	 * IMHO, it creates a ChangeEvent, but this isn't necessary anymore - is it?
	 *   
	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
	 */
	public void jdoPreStore() {
		if (!JDOHelper.isNew(this)) 
			return;

		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Could not get PersistenceManager jdoPreStore()");
		ReportRegistryItemID id = ReportRegistryItemID.create(getOrganisationID(), getReportRegistryItemType(), getReportRegistryItemID());
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException e) {
			if(logger.isDebugEnabled())
				logger.debug("Adding change event for item "+this.getReportRegistryItemType()+" "+this.getReportRegistryItemID()+" parent is "+getParentItem());
			ReportRegistryItemChangeEvent.addChangeEventToController(
					pm,
					ReportRegistryItemChangeEvent.EVENT_TYPE_ITEM_ADDED,
					this,
					getParentItem()
			);
		}
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("No PersistenceManager assigned!");
		
		return pm;
	}
	
	@Override
	public int hashCode() {
		return 
			Utils.hashCode(organisationID) ^
			Utils.hashCode(reportRegistryItemType) ^
			Utils.hashCode(reportRegistryItemID);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReportRegistryItem))
			return false;
		ReportRegistryItem other = (ReportRegistryItem) obj;
		return
			Utils.equals(this.organisationID, other.organisationID) &&
			Utils.equals(this.reportRegistryItemType, other.reportRegistryItemType) &&
			Utils.equals(this.reportRegistryItemID, other.reportRegistryItemID);
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.DetachCallback#jdoPostDetach(java.lang.Object)
	 */
	public void jdoPostDetach(Object obj) {
		ReportRegistryItem attached = (ReportRegistryItem) obj;
		ReportRegistryItem detached = this;
		if (attached.getPersistenceManager().getFetchPlan().getGroups().contains(FETCH_GROUP_PARENT_ITEM_ID))
			detached.parentItemID = attached.getParentItemID();
	}

	/* (non-Javadoc)
	 * @see javax.jdo.listener.DetachCallback#jdoPreDetach()
	 */
	public void jdoPreDetach() {
	}
	
	
}
