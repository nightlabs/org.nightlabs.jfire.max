package org.nightlabs.jfire.geography;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import javax.jdo.PersistenceManager;

import org.nightlabs.io.DataBuffer;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 */
public class GeographyImplJDO
extends GeographyImplResourceCSV
{
	/**
	 * This method sets the system property {@link Geography#PROPERTY_KEY_GEOGRAPHY_CLASS}
	 * to the fully qualified class name of <code>GeographyImplResourceCSV</code>. This method
	 * does not create a shared instance!
	 */
	public static void register()
	{
		System.setProperty(PROPERTY_KEY_GEOGRAPHY_CLASS, GeographyImplJDO.class.getName());
	}

	/**
	 * This method creates a new instance of <code>GeographyImplResourceCSV</code> and sets it
	 * as shared instance. Therefore, a subsequent call to {@link Geography#sharedInstance()} will
	 * return this instance (if it is not overridden by other code). Note, that there is one
	 * shared instance per organisation. The organisationID is determined by {@link SecurityReflector}.
	 */
	public static void createSharedInstance()
	{
		register();
		setSharedInstance(new GeographyImplJDO());
	}

	/**
	 * @param in The {@link InputStream} from which to read. This stream will be closed! May be <code>null</code>.
	 * @return Returns the contents of the given {@link InputStream} or <code>null</code>, if <code>in == null</code>.
	 */
	protected static byte[] deflate(InputStream in)
	{
		if (in == null)
			return null;

		try {
			try {
				DataBuffer db = new DataBuffer(10240);
				OutputStream out = new DeflaterOutputStream(db.createOutputStream());
				try {
					Utils.transferStreamData(in, out);
				} finally {
					out.close();
				}
				return db.createByteArray();
			} finally {
				in.close();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	private PersistenceManager getPersistenceManager()
	{
		return new Lookup(getOrganisationID()).getPersistenceManager();
	}

	@Override
	protected InputStream createCountryCSVInputStream()
	{
		String organisationID = getRootOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		byte[] data = CSV.getCSVData(pm, organisationID, CSV.CSV_TYPE_COUNTRY, "");
		if (data == null) {
			data = deflate(super.createCountryCSVInputStream());
			CSV.setCSVData(pm, organisationID, CSV.CSV_TYPE_COUNTRY, "", data);
		}
		return data == null ? null : new InflaterInputStream(new ByteArrayInputStream(data));
	}

	@Override
	protected InputStream createRegionCSVInputStream(String countryID)
	{
		String organisationID = getRootOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		byte[] data = CSV.getCSVData(pm, organisationID, CSV.CSV_TYPE_REGION, countryID);
		if (data == null) {
			data = deflate(super.createRegionCSVInputStream(countryID));
			CSV.setCSVData(pm, organisationID, CSV.CSV_TYPE_REGION, countryID, data);
		}
		return data == null ? null : new InflaterInputStream(new ByteArrayInputStream(data));
	}

	@Override
	protected InputStream createCityCSVInputStream(String countryID)
	{
		String organisationID = getRootOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		byte[] data = CSV.getCSVData(pm, organisationID, CSV.CSV_TYPE_CITY, countryID);
		if (data == null) {
			data = deflate(super.createCityCSVInputStream(countryID));
			CSV.setCSVData(pm, organisationID, CSV.CSV_TYPE_CITY, countryID, data);
		}
		return data == null ? null : new InflaterInputStream(new ByteArrayInputStream(data));
	}

	@Override
	protected InputStream createDistrictCSVInputStream(String countryID)
	{
		String organisationID = getRootOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		byte[] data = CSV.getCSVData(pm, organisationID, CSV.CSV_TYPE_DISTRICT, countryID);
		if (data == null) {
			data = deflate(super.createDistrictCSVInputStream(countryID));
			CSV.setCSVData(pm, organisationID, CSV.CSV_TYPE_DISTRICT, countryID, data);
		}
		return data == null ? null : new InflaterInputStream(new ByteArrayInputStream(data));
	}

	@Override
	protected InputStream createZipCSVInputStream(String countryID)
	{
		String organisationID = getRootOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		byte[] data = CSV.getCSVData(pm, organisationID, CSV.CSV_TYPE_ZIP, countryID);
		if (data == null) {
			data = deflate(super.createZipCSVInputStream(countryID));
			CSV.setCSVData(pm, organisationID, CSV.CSV_TYPE_ZIP, countryID, data);
		}
		return data == null ? null : new InflaterInputStream(new ByteArrayInputStream(data));
	}

	@Override
	protected InputStream createLocationCSVInputStream(String countryID)
	{
		String organisationID = getRootOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		byte[] data = CSV.getCSVData(pm, organisationID, CSV.CSV_TYPE_LOCATION, countryID);
		if (data == null) {
			data = deflate(super.createLocationCSVInputStream(countryID));
			CSV.setCSVData(pm, organisationID, CSV.CSV_TYPE_LOCATION, countryID, data);
		}
		return data == null ? null : new InflaterInputStream(new ByteArrayInputStream(data));
	}
}
