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
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.util.Util;
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
 * @jdo.fetch-group name="ReportRegistryItem.parentCategory" fetch-groups="default" fields="parentCategory"
 * @jdo.fetch-group name="ReportRegistryItem.name" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="ReportRegistryItem.description" fetch-groups="default" fields="description"
 * @jdo.fetch-group name="ReportRegistryItem.this" fetch-groups="default" fields="parentCategory, name"
 * 
 *  
 * @jdo.query
 *		name="getReportRegistryItemByType"
 *		query="SELECT 
 *			WHERE this.organisationID == :paramOrganisationID &&
 *            this.reportRegistryItemType == :paramReportRegistryItemType            
 *			import java.lang.String"
 * 
 * @jdo.query
 *		name="getTopLevelReportRegistryItemByType"
 *		query="SELECT UNIQUE
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.reportRegistryItemType == paramReportRegistryItemType &&
 *            this.parentCategory == null    
 *			PARAMETERS String paramOrganisationID, String reportRegistryItemType
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelReportRegistryItemsByOrganisation"
 *		query="SELECT
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.parentCategory == null    
 *			PARAMETERS String paramOrganisationID
 *			import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelReportRegistryItems"
 *		query="SELECT
 *			WHERE this.parentCategory == null
 *			import java.lang.String"
 *
 * @jdo.query
 *	name="getReportRegistryItemsForParent"
 *	query="SELECT 
 *		WHERE this.parentCategory == :paramParent"
 *
 * @jdo.query
 *	name="getReportRegistryItemIDsForParent"
 *	query="SELECT JDOHelper.getObjectId(this) 
 *		WHERE this.parentCategory == :paramParent"
 */
public abstract class ReportRegistryItem implements Serializable, DetachCallback
{
	
	public static final String QUERY_GET_REPORT_REGISTRY_ITEM_BY_TYPE = "getReportRegistryItemByType";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEM_BY_TYPE = "getTopLevelReportRegistryItemByType";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS_BY_ORGANISATION = "getTopLevelReportRegistryItemsByOrganisation";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS = "getTopLevelReportRegistryItems";

	public static final String FETCH_GROUP_PARENT_CATEGORY = "ReportRegistryItem.parentCategory";
	/**
	 * Virtual. The {@link #parentCategoryID} is set 
	 */
	public static final String FETCH_GROUP_PARENT_CATEGORY_ID = "ReportRegistryItem.parentCategoryID";
	public static final String FETCH_GROUP_NAME = "ReportRegistryItem.name";
	public static final String FETCH_GROUP_DESCRIPTION = "ReportRegistryItem.description";
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
	 * @jdo.field persistence-modifier="persistent" mapped-by="reportRegistryItem"
	 */
	private ReportRegistryItemName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="reportRegistryItem"
	 */
	private ReportRegistryItemDescription description;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportCategory parentCategory;
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private ReportRegistryItemID parentCategoryID;


	/**
	 * Creates a new ReportRegistryItem with the given 
	 * parent and primary key parameters.
	 * 
	 * @param parentCategory The parent item of the new item.
	 * @param organisationID The organisation of the new item.
	 * @param reportRegistryItemType The type of the new item.
	 * @param reportRegistryItemID The itemID of the new item.
	 */
	protected ReportRegistryItem(
			ReportCategory parentCategory, 
			String organisationID, 
			String reportRegistryItemType,
			String reportRegistryItemID
		) 
	{
		this.parentCategory = parentCategory;
		this.organisationID = organisationID;
		this.reportRegistryItemType = reportRegistryItemType;
		this.reportRegistryItemID = reportRegistryItemID;
		this.name = new ReportRegistryItemName(this);
		this.description = new ReportRegistryItemDescription(this);
	}

	/**
	 * Return the organisationID of this item.
	 * This is part of the primary key.
	 * 
	 * @return The organisationID of this item.
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * Return the reportRegistryItemType of this item.
	 * This is part of the primary key.
	 * 
	 * @return The reportRegistryItemType of this item.
	 */
	public String getReportRegistryItemType() {
		return reportRegistryItemType;
	}
	
	/**
	 * Return the reportRegistryItemID of this item.
	 * This is part of the primary key.
	 * 
	 * @return The reportRegistryItemID of this item.
	 */
	public String getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	/**
	 * Returns the name of this item.
	 * 
	 * @return The name of this item.
	 */
	public ReportRegistryItemName getName() {
		return name;
	}

	/**
	 * Returns the description of this item.
	 * 
	 * @return The descriptioin of this item.
	 */
	public ReportRegistryItemDescription getDescription() {
		return description;
	}
	
	/**
	 * Returns the parent of this item.
	 * 
	 * @return The parent of this item.
	 */
	public ReportCategory getParentCategory() {
		return parentCategory;
	}

	/**
	 * Set the parent of this item. 
	 * 
	 * @param parentCategory The parent to set.
	 */
	protected void setParentCategory(ReportCategory parentCategory) {
		this.parentCategory = parentCategory;
		this.parentCategoryID = null;
	}

	/**
	 * Returns the object-id of the parent of this item.
	 * 
	 * @return The object-id of the parent of this item.
	 */
	public ReportRegistryItemID getParentCategoryID() {
		if (!parentCategoryIDDetached && parentCategoryID == null) {
			parentCategoryID = (ReportRegistryItemID) JDOHelper.getObjectId(parentCategory);
		}
		return parentCategoryID;
	}

	/**
	 * Return all {@link ReportRegistryItem}s with the given type.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisationID The organisationID of the items to search.
	 * @param reportRegistryItemType The type of the items to search.
	 * @return All {@link ReportRegistryItem}s with the given type.
	 */
	public static Collection getReportRegistryItemByType(PersistenceManager pm, String organisationID, String reportRegistryItemType) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_GET_REPORT_REGISTRY_ITEM_BY_TYPE);
		return (Collection)q.execute(organisationID, reportRegistryItemType);
	}

	/**
	 * Returns the top-level {@link ReportRegistryItem}s with the given type. 
	 * Top-level means here that the items do not have parent.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisatinID The organisationID of the items to search.
	 * @param reportRegistryItemType The type of the items to search.
	 * @return The top-level {@link ReportRegistryItem}s with the given type.
	 */
	public static ReportRegistryItem getTopReportRegistryItemByType(PersistenceManager pm, String organisatinID, String reportRegistryItemType) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEM_BY_TYPE);
		return (ReportRegistryItem)q.execute(organisatinID, reportRegistryItemType);
	}
	
	/**
	 * Returns all top-level {@link ReportRegistryItem}s of the given organisations. 
	 * Top-level means here that the items do not have parent.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @param organisatinID The organisationID of the items to search.
	 * @return All top-level {@link ReportRegistryItem}s of the given organisation.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ReportRegistryItem> getTopReportRegistryItems(PersistenceManager pm, String organisatinID) {
		if (organisatinID == null || "".equals(organisatinID))
			return getTopReportRegistryItems(pm);
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS_BY_ORGANISATION);
		return (Collection)q.execute(organisatinID);
	}
	
	/**
	 * Returns all top-level {@link ReportRegistryItem}s. 
	 * Top-level means here that the items do not have parent.
	 * 
	 * @param pm The {@link PersistenceManager} to use.
	 * @return All top-level {@link ReportRegistryItem}s.
	 */
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

	/**
	 * Returns all {@link ReportRegistryItem}s that have with the given reportCategory
	 * as parent.
	 *  
	 * @param pm The {@link PersistenceManager} to use.
	 * @param reportCategory The {@link ReportCategory} children should be searched for.
	 * @return all {@link ReportRegistryItem}s that have with the given reportCategory
	 * 		as parent.
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ReportRegistryItemID> getReportRegistryItemIDsForParent(PersistenceManager pm, ReportCategory reportCategory) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, "getReportRegistryItemIDsForParent");
		return (Collection<ReportRegistryItemID>) q.execute(reportCategory);
	}

	/**
	 * Returns the {@link PersistenceManager} associated with this item.
	 * Throws an {@link IllegalStateException} if this instance of 
	 * {@link ReportRegistryItem} is not attached. (e.g. detached, not yet persistent) 
	 * 
	 * @return The {@link PersistenceManager} associated with this item.
	 */
	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("No PersistenceManager assigned!");
		
		return pm;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return 
			Util.hashCode(organisationID) ^
			Util.hashCode(reportRegistryItemType) ^
			Util.hashCode(reportRegistryItemID);
	}

	/**
	 * {@inheritDoc}
	 * Checks if primary key fields are equal.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ReportRegistryItem))
			return false;
		ReportRegistryItem other = (ReportRegistryItem) obj;
		return
			Util.equals(this.organisationID, other.organisationID) &&
			Util.equals(this.reportRegistryItemType, other.reportRegistryItemType) &&
			Util.equals(this.reportRegistryItemID, other.reportRegistryItemID);
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean parentCategoryIDDetached = false;
	/** 
	 * {@inheritDoc}
	 * Sets the {@link #parentCategoryID} member if {@link #FETCH_GROUP_PARENT_CATEGORY_ID} is in the fetch-plan.
	 * 
	 * @see javax.jdo.listener.DetachCallback#jdoPostDetach(java.lang.Object)
	 */
	public void jdoPostDetach(Object obj) {
		ReportRegistryItem attached = (ReportRegistryItem) obj;
		ReportRegistryItem detached = this;
		if (attached.getPersistenceManager().getFetchPlan().getGroups().contains(FETCH_GROUP_PARENT_CATEGORY_ID)) {
			detached.parentCategoryID = attached.getParentCategoryID();
			detached.parentCategoryIDDetached = true; 
		}
	}

	/** 
	 * {@inheritDoc}
	 * This implementation does nothing.
	 * 
	 * @see javax.jdo.listener.DetachCallback#jdoPreDetach()
	 */
	public void jdoPreDetach() {
	}
	
	
}
