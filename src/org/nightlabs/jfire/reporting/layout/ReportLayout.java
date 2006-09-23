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

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.jdo.JDOHelper;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;

/**
 * A ReportLayout holds the BIRT report definition.
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.jfire.reporting.layout.ReportRegistryItem"
 *		detachable="true"
 *		table="JFireReporting_ReportLayout"
 *
 * WORKAROUND: Workaround for MySQL table lock timeout when initializing datastore on ADD COLUMN when using superclass-table
 * ...and: superclass-table fails with HSQLDB! (MySQL works) 
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ReportLayout.reportDesign" fetch-groups="default" fields="reportDesign"
 * @jdo.fetch-group name="ReportLayout.this" fetch-groups="default, ReportRegistryItem.this" fields="reportDesign"
 */
public class ReportLayout extends ReportRegistryItem {

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ReportLayout.class);
	
	public static final String FETCH_GROUP_REPORT_DESIGN = "ReportLayout.reportDesign";
	public static final String FETCH_GROUP_THIS_REPORT_LAYOUT = "ReportLayout.this";
	
	/**
	 * Serial version UID. Don't forget to change after changing members. 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO
	 */
	protected ReportLayout() {
		super();
	}

	/**
	 * Creates a new ReportLayout as child of the given
	 * ReportCategory with the given reportDesign.
	 * 
	 * @param parentItem The parent category of the new layout. The reportItemRegistryItem type of the new layout will also match its parent.
	 * @param reportRegistryItemID The reportRegistryItemID of the new layout.
	 * @param reportDesign The report design data of the new layout.
	 */
	public ReportLayout(
			ReportCategory parentItem,
			String reportRegistryItemID,
			byte[] reportDesign
		) 
	{
		super(parentItem, parentItem.getOrganisationID(), parentItem.getReportRegistryItemType(), reportRegistryItemID);
		this.reportDesign = reportDesign;
	}

	/**
	 * Creates a new ReportLayout as child of the given ReportCategory and the given primary-key fields.
	 * No reportDesign data will be set.
	 * 
	 * @param parentItem The parent category of the new layout.
	 * @param organisationID The organisationID of the new layout.
	 * @param reportRegistryItemType The reportRegistryItemType of the new layout.
	 * @param reportRegistryItemID The reportRegistryItemID of the new layout.
	 */
	public ReportLayout(
			ReportCategory parentItem,
			String organisationID,
			String reportRegistryItemType,
			String reportRegistryItemID
		) 
	{
		super(parentItem, organisationID, reportRegistryItemType, reportRegistryItemID);
	}
	
	
	/**
	 * @jdo.field persistence-modifier="persistent" collection-type="array" serialized-element="true"
	 */
	private byte[] reportDesign;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Date fileTimestamp;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private String fileName;
	
	public void loadFile(File f)
	throws IOException
	{
		logger.info("loadFile(\""+f.getAbsolutePath()+"\"): loading " + f.length() + " bytes into RAM.");

		boolean error = true;
		try {
			DataBuffer db = new DataBuffer(f.length(), f);
			reportDesign = db.createByteArray();

			fileTimestamp = new Date(f.lastModified());
			fileName = f.getName();

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				fileName = null;
				fileTimestamp = null;
				reportDesign = null;
			}
		}
	}

	public byte[] getReportDesign() { 
		return reportDesign;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Date getFileTimestamp() {
		return fileTimestamp;
	}

//	@Override
//	public void jdoPreStore() {
//		super.jdoPreStore();
//		ReportRegistryItemID parentID = (ReportRegistryItemID) JDOHelper.getObjectId(this.getParentItem());
//		if (parentID == null)
//			throw new IllegalStateException("ReportLayout has to be child of a ReportCategory but is not.");
//		ReportRegistryItem parent = (ReportRegistryItem) getPersistenceManager().getObjectById(parentID);
//		ReportCategory.ensureRelationWithParent(parent, this);
//		logger.info("Called ensureRelationWithParent for ReportLayout");
//	}

}
