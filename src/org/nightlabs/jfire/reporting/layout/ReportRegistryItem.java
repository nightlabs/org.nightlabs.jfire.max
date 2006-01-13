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
import javax.jdo.listener.StoreCallback;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID"
 *		detachable = "true"
 *		table="JFireReporting_ReportRegistryItem"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportRegistryItemID"
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
 *			IMPORTS import java.lang.String"
 * 
 * @jdo.query
 *		name="getTopLevelReportRegistryItemByType"
 *		query="SELECT UNIQUE
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.reportRegistryItemType == paramReportRegistryItemType &&
 *            this.parentItem == null    
 *			PARAMETERS String paramOrganisationID, String reportRegistryItemType
 *			IMPORTS import java.lang.String"
 *
 * @jdo.query
 *		name="getTopLevelReportRegistryItems"
 *		query="SELECT
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.parentItem == null    
 *			PARAMETERS String paramOrganisationID
 *			IMPORTS import java.lang.String"
 */
public abstract class ReportRegistryItem implements Serializable, StoreCallback  {
	
	public static final String QUERY_GET_REPORT_REGISTRY_ITEM_BY_TYPE = "getReportRegistryItemByType";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEM_BY_TYPE = "getTopLevelReportRegistryItemByType";
	public static final String QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS = "getTopLevelReportRegistryItems";

	public static final String FETCH_GROUP_PARENT_ITEM = "ReportRegistryItem.parentItem";
	public static final String FETCH_GROUP_NAME = "ReportRegistryItem.name";
	public static final String FETCH_GROUP_THIS_REPORT_REGISTRY_ITEM = "ReportRegistryItem.this";
	
	
	protected ReportRegistryItem() {}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="100"
	 */
	private String reportRegistryItemType;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private long reportRegistryItemID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportRegistryItemName name;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportRegistryItem parentItem;


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
			PersistenceManager pm,
			ReportRegistryItem parentItem, 
			String organisationID, 
			String reportRegistryItemType
		) 
	{
		this.parentItem = parentItem;
		this.organisationID = organisationID;
		this.reportRegistryItemType = reportRegistryItemType;
		if (pm != null) 
			this.reportRegistryItemID = ReportRegistry.getReportCategoryRegistry(pm).createNewReportCategoryID();
		else
			this.reportRegistryItemID = -1;
		this.name = new ReportRegistryItemName(this);
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getReportRegistryItemType() {
		return reportRegistryItemType;
	}
	
	public long getReportRegistryItemID() {
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
	
	public static Collection getReportRegistryItemByType(PersistenceManager pm, String organisatinID, String reportRegistryItemType) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_GET_REPORT_REGISTRY_ITEM_BY_TYPE);
		return (Collection)q.execute(organisatinID, reportRegistryItemType);
	}

	public static ReportRegistryItem getTopReportRegistryItemByType(PersistenceManager pm, String organisatinID, String reportRegistryItemType) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEM_BY_TYPE);
		return (ReportRegistryItem)q.execute(organisatinID, reportRegistryItemType);
	}
	
	public static Collection getTopReportRegistryItems(PersistenceManager pm, String organisatinID) {
		Query q = pm.newNamedQuery(ReportRegistryItem.class, QUERY_TOP_LEVEL_GET_REPORT_REGISTRY_ITEMS);
//		return (Collection)q.execute(organisatinID);
		return (Collection)q.execute(organisatinID);
	}
	
	/**
	 * Assigns a reportRegistryItemID for this ReportRegistryItem it this is not set yet.
	 *   
	 * @see javax.jdo.listener.StoreCallback#jdoPreStore()
	 */
	public void jdoPreStore() {
		if (this.reportRegistryItemID < 0) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			if (pm == null)
				throw new IllegalStateException("Could not get PersistenceManager jdoPreStore()");
			ReportRegistry registry = ReportRegistry.getReportCategoryRegistry(pm);
			this.reportRegistryItemID = registry.createNewReportCategoryID();
		}
		
		if (this.name.getReportRegistryItemID() < 0) {
			this.name.setReportRegistryItemID(this.reportRegistryItemID);
		}
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("No PersistenceManager assigned!");
		
		return pm;
	}
}
