/**
 * 
 */
package org.nightlabs.jfire.reporting.layout;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.util.Utils;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		objectid-class = "org.nightlabs.jfire.reporting.layout.id.ReportLayoutLocalisationDataID"
 *		detachable = "true"
 *		table="JFireReporting_ReportLayoutLocalisationData"
 *
 * @jdo.create-objectid-class field-order="organisationID, reportRegistryItemType, reportRegistryItemID, locale"
 *
 * @jdo.inheritance strategy = "new-table" 
 * 
 * @jdo.fetch-group name="ReportLayout.localisationData" fetch-groups="default" fields="reportLayout, localisationData"
 *
 */
public class ReportLayoutLocalisationData {

	public static final String PROPERIES_FILE_PREFIX = "reportMessages";
	
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
	 * @jdo.field primary-key="true"
	 * @jdo.column length="10"
	 */
	private String locale;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ReportLayout reportLayout;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte[] localisationData;
	
	/**
	 * @deprecated Only for JDO
	 */
	protected ReportLayoutLocalisationData() {
	}

	/**
	 */
	protected ReportLayoutLocalisationData(ReportLayout reportLayout, String locale) {
		this.organisationID = reportLayout.getOrganisationID();
		this.reportRegistryItemType = reportLayout.getReportRegistryItemType();
		this.reportRegistryItemID = reportLayout.getReportRegistryItemID();
		this.reportLayout = reportLayout;
		this.locale = locale;
	}
	
	/**
	 * 
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}
	
	/**
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * @return the reportRegistryItemID
	 */
	public String getReportRegistryItemID() {
		return reportRegistryItemID;
	}

	/**
	 * @return the reportRegistryItemType
	 */
	public String getReportRegistryItemType() {
		return reportRegistryItemType;
	}
	
	public ReportLayout getReportLayout() {
		return reportLayout;
	}
	
	public byte[] getLocalisationData() {
		return localisationData;
	}
	
	public void loadFile(File f)
	throws IOException
	{
		boolean error = true;
		try {
			DataBuffer db = new DataBuffer((long) (f.length() * 0.6));
			OutputStream out = new DeflaterOutputStream(db.createOutputStream());
			try {
				FileInputStream in = new FileInputStream(f);
				try {
					Utils.transferStreamData(in, out);
				} finally {
					in.close();
				}
			} finally {
				out.close();
			}
			localisationData = db.createByteArray();

			error = false;
		} finally {
			if (error) { 
				localisationData = null;
			}
		}
	}

	/**
	 * Creates a new {@link InputStream} for the report messages file
	 * that is wrapped by an {@link InflaterInputStream}.
	 * This means you can read the messages unzipped from the returend stream.
	 */
	public InputStream createReportDesignInputStream() {
		return new InflaterInputStream(new ByteArrayInputStream(localisationData));
	}
	

}
