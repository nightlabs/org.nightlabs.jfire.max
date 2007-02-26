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

import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;


/**
 * ReportCategories are used to organise reports on the server. 
 * The JFire sytem itself uses internal categories to organise
 * report-layouts for invoices, orders etc.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.reporting.layout.ReportRegistryItem"
 *		detachable="true"
 *		table="JFireReporting_ReportCategory"
 *
 * @!jdo.inheritance strategy="superclass-table"
 * JPOX-BUG: superclass-table fails with HSQLDB! (MySQL works)
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ReportCategory.childItems" fetch-groups="default" fields="childItems"
 * @jdo.fetch-group name="ReportCategory.this" fetch-groups="default, ReportRegistryItem.this" fields="childItems"
 * 
 *  @jdo.query
 *		name="getReportCategory"
 *		query="SELECT UNIQUE
 *			WHERE this.organisationID == paramOrganisationID &&
 *            this.reportRegistryItemType == paramCategoryType            
 *			PARAMETERS String paramOrganisationID, String paramCategoryType
 *			import java.lang.String"
 * 
 */
public class ReportCategory extends ReportRegistryItem implements NestableReportRegistryItem {

//	public static final String INTERNAL_CATEGORY_TYPE_ORDER = "OrderLayout";
//	public static final String INTERNAL_CATEGORY_TYPE_OFFER = "OfferLayout";
//	public static final String INTERNAL_CATEGORY_TYPE_INVOICE = "InvoiceLayout";
//	public static final String INTERNAL_CATEGORY_TYPE_DELIVERY_NOTE = "DeliveryNoteLayout";
	
	public static final String CATEGORY_TYPE_GENERAL = "GeneralLayouts";
	
	public static final String QUERY_GET_REPORT_CATEGORY = "getReportCategory";
	
	// TODO: Would be great to have recursion depth here and for ReportRegistryItem.parentItem when thinking of caching in client 
	public static final String FETCH_GROUP_CHILD_ITEMS = "ReportCategory.childItems";
	public static final String FETCH_GROUP_THIS_REPORT_CATEGORY = "ReportCategory.this";
	
	
	/**
	 * Serial version UID. Don't forget to change when changing members. 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean internal;
	
	/**
	 * value: {@link ReportRegistryItem} childItem
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.reporting.layout.ReportRegistryItem"
	 *		mapped-by="parentItem"
	 *		dependent-element="true"
	 */
	private Set<ReportRegistryItem> childItems;	

//// TODO This constructor should not exist, because it is very likely, that the child item
////		is created by another organisation! It's error prone - better get rid of this constructor.
//	/**
//	 * Creates a new ReportCategory with the given one as parent.
//	 * 
//	 * @param pm PersistenceManager to retrieve the ReportRegistry with.
//	 * @param parentItem A non null ReportCategory that will be the parent of the new one.
//	 * @throws NullPointerException if parentItem is null
//	 */
//	public ReportCategory(
//			ReportCategory parentItem,
//			String reportRegistryItemID 
//		) 
//	{
//		super(parentItem, parentItem.getOrganisationID(), parentItem.getReportRegistryItemType(), reportRegistryItemID);
//		this.internal = parentItem.isInternal();
//		this.childItems = new HashSet<ReportRegistryItem>();
//	}
	
	public ReportCategory(
			ReportCategory parentItem,
			String organisationID,
			String reportRegistryItemType,
			String reportRegistryItemID,
			boolean internal
		)
	{
		super(parentItem, organisationID, reportRegistryItemType, reportRegistryItemID);
		if (parentItem != null)
			this.internal = parentItem.isInternal();
		else
			this.internal = internal;		
		this.childItems = new HashSet<ReportRegistryItem>();
	}
	
	
	
	public boolean isInternal() {
		return internal;
	}
	

	public static ReportCategory getReportCategory(
			PersistenceManager pm, 
			String organisationID,
			String categoryType
		) 
	{
		Query q = pm.newNamedQuery(ReportCategory.class, QUERY_GET_REPORT_CATEGORY);
		return (ReportCategory)q.execute(organisationID, categoryType);
	}

	public Set<ReportRegistryItem> getChildItems() {
		return childItems;
	}
	
	public void addChildItem(ReportRegistryItem childItem) {
		childItem.setParentItem(this);
		this.childItems.add(childItem);
	}

//	public static void ensureRelationWithParent(ReportRegistryItem parent, ReportRegistryItem childItem) {
//		if (parent == null)
//			return;
//
//		if (parent instanceof ReportCategory) {
//			ReportCategory category = (ReportCategory)parent;
////			if (category.getChildItems().contains(childItem))
//				category.addChildItem(childItem);
//		}
//	}

//	@Override
//	public void jdoPreStore() {
//		super.jdoPreStore();
//		ReportRegistryItemID parentID = (ReportRegistryItemID) JDOHelper.getObjectId(this.getParentItem());
//		if (parentID == null)
//			return;
//		ReportRegistryItem parent = (ReportRegistryItem) getPersistenceManager().getObjectById(parentID);
//		ReportCategory.ensureRelationWithParent(parent, this);
//	}
}
