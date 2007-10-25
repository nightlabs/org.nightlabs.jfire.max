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
import java.io.Serializable;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.listener.StoreCallback;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.util.Util;
import org.nightlabs.util.Utils;

/**
 * Holds the contents of a properties file that is used in report localisation.
 * 
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
 * @jdo.fetch-group name="ReportLayoutLocalisationData.localisationData" fetch-groups="default" fields="localisationData"
 *
 *  @jdo.query
 *		name="getReportLayoutLocalisationBundle"
 *		query="SELECT 
 *			WHERE this.reportLayout == :paramReportLayout"
 *
 */
public class ReportLayoutLocalisationData implements StoreCallback, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String PROPERIES_FILE_PREFIX = "reportMessages";
	
	private static final Pattern localePattern = Pattern.compile(".*_(([a-z]+)(?:_*)([A-Z]*))\\.properties");

	public static final String FETCH_GROUP_LOCALISATOIN_DATA = "ReportLayoutLocalisationData.localisationData";

	
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
	@Deprecated
	protected ReportLayoutLocalisationData() {
	}

	/**
	 */
	public ReportLayoutLocalisationData(ReportLayout reportLayout, String locale) {
		this.organisationID = reportLayout.getOrganisationID();
		this.reportRegistryItemType = reportLayout.getReportRegistryItemType();
		this.reportRegistryItemID = reportLayout.getReportRegistryItemID();
		this.reportLayout = reportLayout;
		this.locale = locale;
	}
	
	/**
	 */
	public ReportLayoutLocalisationData(ReportRegistryItemID reportRegistryItemID, String locale) {
		this.organisationID = reportRegistryItemID.organisationID;
		this.reportRegistryItemType = reportRegistryItemID.reportRegistryItemType;
		this.reportRegistryItemID = reportRegistryItemID.reportRegistryItemID;
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
	
	/**
	 * Loads the contents of the given file into the {@link #localisationData} member.
	 * 
	 * @param f The {@link File} to load. 
	 * @throws IOException
	 */
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
					Util.transferStreamData(in, out);
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
	public InputStream createLocalisationDataInputStream() {
		return new InflaterInputStream(new ByteArrayInputStream(localisationData));
	}

	/**
	 * Returns all {@link ReportLayoutLocalisationData} objects related to the given ReportLayout.
	 * 
	 * @param pm
	 * @param reportLayout
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Collection<ReportLayoutLocalisationData> getReportLayoutLocalisationBundle(PersistenceManager pm, ReportLayout reportLayout) {
		Query q = pm.newNamedQuery(ReportLayoutLocalisationData.class, "getReportLayoutLocalisationBundle");
		return (Collection<ReportLayoutLocalisationData>) q.execute(reportLayout);
	}
	
	/**
	 * Assuming the given file name is was build by the naming conventions of
	 * resouce bundle properties files, this method extracts the whole locale part of a given file name.
	 * 
	 * @param fileName The fileName the locale part should be extractd from.
	 * @return The locale part of a given file name, null if none could be found.
	 */
	public static String extractLocale(String fileName) {
		Matcher matcher = localePattern.matcher(fileName);
		if (matcher.matches())
			return matcher.group(1);
		return null;
	}
	
	/**
	 * Assuming the given file name is was build by the naming conventions of
	 * resouce bundle properties files, this method extracts the language out
	 * of the locale part of the given file name.
	 * 
	 * @param fileName The fileName the language part should be extractd from.
	 * @return The language part of a given file name, null if none could be found.
	 */
	public static String extractLanguage(String fileName) {
		Matcher matcher = localePattern.matcher(fileName);
		if (matcher.matches())
			return matcher.group(2);
		return null;
	}
	
	/**
	 * Assuming the given file name is was build by the naming conventions of
	 * resouce bundle properties files, this method extracts the country out
	 * of the locale part of the given file name.
	 * 
	 * @param fileName The fileName the language part should be extractd from.
	 * @return The language part of a given file name, null if none could be found.
	 */
	public static String extractCountry(String fileName) {
		Matcher matcher = localePattern.matcher(fileName);
		if (matcher.matches())
			return matcher.group(3);
		return null;
	}

	public void jdoPreStore() {
		if (this.reportLayout == null) {
			PersistenceManager pm = JDOHelper.getPersistenceManager(this);
			ReportRegistryItemID itemID = ReportRegistryItemID.create(organisationID, reportRegistryItemType, reportRegistryItemID);
			ReportLayout layout = (ReportLayout) pm.getObjectById(itemID);
			this.reportLayout = layout;
		}
	}
	
}
