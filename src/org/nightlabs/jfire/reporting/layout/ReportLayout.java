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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.util.Util;

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
 * @!jdo.fetch-group name="ReportLayout.localisationData" fetch-groups="default" fields="localisationData"
 * @jdo.fetch-group name="ReportLayout.this" fetch-groups="default, ReportRegistryItem.this" fields="reportDesign"
 */
public class ReportLayout extends ReportRegistryItem {

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(ReportLayout.class);
	
	public static final String FETCH_GROUP_REPORT_DESIGN = "ReportLayout.reportDesign";
	public static final String FETCH_GROUP_REPORT_LOCALISATION_DATA = "ReportLayout.localisationData";
	public static final String FETCH_GROUP_THIS_REPORT_LAYOUT = "ReportLayout.this";
	
	/**
	 * Serial version UID. Don't forget to change after changing members.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO
	 */
	@Deprecated
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
//		this.localisationData = new HashMap<String, RepsortLayoutLocalisationData>();
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
//		this.localisationData = new HashMap<String, ReportLayoutLocalisationData>();
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
	
	
//	/**
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="org.nightlabs.jfire.reporting.layout.ReportLayoutLocalisationData"
//	 *		mapped-by="reportLayout"
//	 *		dependent-value="true"
//	 *
//	 * @jdo.key
//	 * 		mapped-by="locale"
//	 */
//	private Map<String, ReportLayoutLocalisationData> localisationData;
	
	public void loadStream(InputStream in, long length, Date timeStamp, String name)
	throws IOException
	{
		logger.debug("Loading stream as ReportLayout");
		boolean error = true;
		try {
			DataBuffer db = new DataBuffer((long) (length * 0.6));
			OutputStream out = new DeflaterOutputStream(db.createOutputStream());
			try {
				Util.transferStreamData(in, out);
			} finally {
				out.close();
			}
			reportDesign = db.createByteArray();

			fileTimestamp = timeStamp;
			fileName = name;

			error = false;
		} finally {
			if (error) { // make sure that in case of an error all the file members are null.
				fileName = null;
				fileTimestamp = null;
				reportDesign = null;
			}
		}
	}
	
	public void loadStream(InputStream in, String name)
	throws IOException
	{
		loadStream(in, 10 * 1024, new Date(), name);
	}
	
	public void loadFile(File f)
	throws IOException
	{
		logger.debug("Loading file "+f+" as ReportLayout");
		FileInputStream in = new FileInputStream(f);
		try {
			loadStream(in, f.length(), new Date(f.lastModified()), f.getName());
		} finally {
			in.close();
		}
	}

	/**
	 * Creates a new {@link InputStream} for the report design
	 * that is wrapped by an {@link InflaterInputStream}.
	 * This means you can read the report design unzipped from the returend stream.
	 */
	public InputStream createReportDesignInputStream() {
		return new InflaterInputStream(new ByteArrayInputStream(reportDesign));
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public Date getFileTimestamp() {
		return fileTimestamp;
	}

//	/**
//	 * Returns the map of localisation data (message files).
//	 *
//	 * @return the localisationData
//	 */
//	public Map<String, ReportLayoutLocalisationData> getLocalisationData() {
//		return localisationData;
//	}
	
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
