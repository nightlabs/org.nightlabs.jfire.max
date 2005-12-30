/**
 * 
 */
package org.nightlabs.ipanema.reporting.layout;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.io.DataBuffer;
import org.nightlabs.ipanema.reporting.layout.id.ReportRegistryItemID;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 * 		persistence-capable-superclass="org.nightlabs.ipanema.reporting.layout.ReportRegistryItem"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 * 
 * @jdo.fetch-group name="ReportLayout.childItems" fetch-groups="default" fields="reportDesign"
 * @jdo.fetch-group name="ReportLayout.this" fetch-groups="default, ReportRegistryItem.this" fields="reportDesign"
 */
public class ReportLayout extends ReportRegistryItem {

	protected static Logger LOGGER = Logger.getLogger(ReportLayout.class);
	
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
	 * Creates a new ReportLayout as parent of the given
	 * ReportCategory with the given reportDesign.
	 * @param pm The PersistenceManager to retrieve the ReportRegistry with
	 * @param parentItem The parent category of the new layout.
	 * @param reportDesign The report design data of the new layout.
	 */
	public ReportLayout(
			PersistenceManager pm,
			ReportCategory parentItem, 
			byte[] reportDesign
		) 
	{
		super(pm, parentItem, parentItem.getOrganisationID(), parentItem.getReportRegistryItemType());
		this.reportDesign = reportDesign;
	}
	
	/**
	 * Creates a new ReportLayout as parent of the given
	 * ReportCategory with the given reportDesign.
	 * @param parentItem The parent category of the new layout.
	 * @param reportDesign The report design data of the new layout.
	 */
	public ReportLayout(
			ReportCategory parentItem, 
			byte[] reportDesign
		) 
	{
		super(null, parentItem, parentItem.getOrganisationID(), parentItem.getReportRegistryItemType());
		this.reportDesign = reportDesign;
	}
	
	public ReportLayout(
			ReportCategory parentItem,
			String organisationID,
			String reportRegistryItemType			
		) 
	{
		super(null, parentItem, organisationID, reportRegistryItemType);
	}
	
	
	/**
	 * @jdo.field persistence-modifier="persistent"
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
		LOGGER.info("loadFile(\""+f.getAbsolutePath()+"\"): loading " + f.length() + " bytes into RAM.");

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

	@Override
	public void jdoPreStore() {
		super.jdoPreStore();
		ReportRegistryItemID parentID = (ReportRegistryItemID) JDOHelper.getObjectId(this.getParentItem());
		if (parentID == null)
			throw new IllegalStateException("ReportLayout has to be child of a ReportCategory but is not.");
		ReportRegistryItem parent = (ReportRegistryItem) getPersistenceManager().getObjectById(parentID);
		ReportCategory.ensureRelationWithParent(parent, this);
		LOGGER.info("Called ensureRelationWithParent for ReportLayout");
	}
	
}
