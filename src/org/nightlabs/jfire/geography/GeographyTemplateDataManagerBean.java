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

package org.nightlabs.jfire.geography;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.zip.DeflaterOutputStream;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.DistrictID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.idgenerator.IDNamespace;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.Utils;

/**
 * @ejb.bean name="jfire/ejb/JFireGeography/GeographyTemplateDataManager"	
 *           jndi-name="jfire/ejb/JFireGeography/GeographyTemplateDataManager"
 *           type="Stateless" 
 *           transaction-type="Container"
 *
 * @ejb.util generate = "physical" 
 */
public abstract class GeographyTemplateDataManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(GeographyTemplateDataManagerBean.class);

	private static final String REGION_CSV_HEADER = "CountryID;RegionID;LanguageID;RegionName\n";
	private static final String CITY_CSV_HEADER = "CountryID;CityID;RegionID;LanguageID;CityName\n";
	private static final String LOCATION_CSV_HEADER = "CountryID;LocationID;CityID;DistrictID;LanguageID;LocationName\n";
	private static final String DISTRICT_CSV_HEADER = "CountryID;CityID;DistrictID;LanguageID;DistrictName;Latitute;Longitude\n";
	private static final String ZIP_CSV_HEADER = "CountryID;CityID;DistrictID;Zip\n";

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	public void unsetSessionContext() {
		super.unsetSessionContext();
	}
	/**
	 * @ejb.create-method  
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 * 
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	{
		Geography geography = Geography.sharedInstance();
		String organisationID = getOrganisationID();

		PersistenceManager pm = getPersistenceManager();
		try {
			iterateCountries: for (Country country : geography.getCountries()) {
				// CityIDs are local within the namespace of countryID and organisationID

				IDNamespace namespace = IDNamespace.getIDNamespace(pm, organisationID, City.class.getName() + "#" + country.getCountryID());
				long nextID = namespace.getNextID();
				if (nextID != 0)
					continue iterateCountries; // already initialised - skip this country

				nextID = -1;

				for (Region region : geography.getRegions(CountryID.create(country), true)) {
					iterateCities: for (City city : geography.getCities(RegionID.create(region), true)) {
						if (!organisationID.equals(city.getOrganisationID()))
							continue iterateCities;

						long cityID;
						try {
							cityID = Long.parseLong(city.getCityID());
						} catch (NumberFormatException x) {
							continue iterateCities;
						}

						nextID = Math.max(nextID, cityID);
					} // iterateCities: for (City city : geography.getCities(RegionID.create(region), true)) {
				} // for (Region region : geography.getRegions(CountryID.create(country), true)) {

				namespace.setNextID(++nextID);
			} // iterateCountries: for (Country country : geography.getCountries()) {


		// TODO initialise the namespaces for Regions, Locations etc.
		// note that a countryID is defined by the ISO standard and thus does NOT require ID generation!
		} finally {
			pm.close();
		}
	}

	private static class InvocationClearCache extends Invocation
	{
		@Implement
		public Serializable invoke()
		throws Exception
		{
			Geography.sharedInstance().clearCache();
			return null;
		}
	}

	private static void clearCache()
	{
		Geography.sharedInstance().clearCache();
		// we do this again in an AsyncInvoke, because the current transaction is not yet committed and another thread might cause OLD data to be read!
		try {
			AsyncInvoke.exec(new InvocationClearCache(), true);
		} catch (Exception e) {
			logger.error("Spawning AsyncInvoke failed!", e);
			throw new RuntimeException(e); // we escalate it transparently as RuntimeException as it should never happen anyway - maybe we should change the API of AsyncInvoke and drop the throws declarations
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeGeographyTemplateCountryData(Country storedCountry)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			Geography geography = Geography.sharedInstance();

			String rootOrganisationID;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				}//try 
				finally {
					initialContext.close();
				}//finally
			}//try 
			catch (NamingException x) {
				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
			}//catch

			CountryID countryID = CountryID.create(storedCountry);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), Utils.CHARSET_UTF_8);
			try {
				w.write(REGION_CSV_HEADER);
				for (Country existCountry : geography.getCountries()) {
					if (CountryID.create(existCountry).equals(countryID)) {
						existCountry = storedCountry;
						storedCountry = null; countryID = null;
					}//if

					String csvLines = GeographyImplResourceCSV.country2csvLines(existCountry);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

					w.write(csvLines);
				}//for

				if (storedCountry != null) {
					String csvLines = GeographyImplResourceCSV.country2csvLines(storedCountry);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

					csvLines.trim();
					w.write(csvLines);
				}//if
			}//try 
			finally {
				w.close();
			}//finally

			CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_COUNTRY, countryID.countryID, out.toByteArray());
			clearCache();
		}//try 
		finally {
			pm.close();
		}//finally
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeGeographyTemplateRegionData(Region storedRegion)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			Geography geography = Geography.sharedInstance();

			String rootOrganisationID;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				}//try 
				finally {
					initialContext.close();
				}//finally
			}//try 
			catch (NamingException x) {
				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
			}//catch

			RegionID regionID = RegionID.create(storedRegion);
			CountryID countryID = CountryID.create(storedRegion);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), Utils.CHARSET_UTF_8);
			try {
				w.write(REGION_CSV_HEADER);
				for (Region existRegion : geography.getRegions(countryID, true)) {
					if (RegionID.create(existRegion).equals(regionID)) {
						existRegion = storedRegion;
						storedRegion = null; regionID = null;
					}//if

					String csvLines = GeographyImplResourceCSV.region2csvLines(existRegion);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

					w.write(csvLines);
				}//for

				if (storedRegion != null) {
					String csvLines = GeographyImplResourceCSV.region2csvLines(storedRegion);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

					csvLines.trim();
					w.write(csvLines);
				}//if
			}//try 
			finally {
				w.close();
			}//finally

			CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_REGION, countryID.countryID, out.toByteArray());
			clearCache();
		}//try 
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeGeographyTemplateCityData(City storedCity)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			Geography geography = Geography.sharedInstance();

			String rootOrganisationID;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				}//try 
				finally {
					initialContext.close();
				}//finally
			}//try 
			catch (NamingException x) {
				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
			}//catch

			CityID cityID = CityID.create(storedCity);
			CountryID countryID = CountryID.create(storedCity);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), Utils.CHARSET_UTF_8);
			try {
				w.write(CITY_CSV_HEADER);
				for (Region existRegion : geography.getRegions(countryID, true)) {
					for (City existCity : geography.getCities(RegionID.create(existRegion), true)) {
						if (CityID.create(existCity).equals(cityID)) { //if come in this case it is update
							existCity = storedCity;
							storedCity = null; cityID = null;
						}//if
						
						String csvLines = GeographyImplResourceCSV.city2csvLines(existCity);

						if (logger.isDebugEnabled())
							logger.debug(csvLines);

						w.write(csvLines);
					}//for
				}//for

				if (storedCity != null) {	//add new city
					String csvLines = GeographyImplResourceCSV.city2csvLines(storedCity);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

					csvLines.trim();
					w.write(csvLines);
				}//if
			}//try 
			finally {
				w.close();
			}//finally

			CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_CITY, countryID.countryID, out.toByteArray());
			clearCache();
		}//try 
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeGeographyTemplateLocationData(Location storedLocation)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			Geography geography = Geography.sharedInstance();

			String rootOrganisationID;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				}//try 
				finally {
					initialContext.close();
				}//finally
			}//try 
			catch (NamingException x) {
				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
			}//catch

			LocationID locationID = LocationID.create(storedLocation.getCountryID(), rootOrganisationID, storedLocation.getLocationID());
			CountryID countryID = CountryID.create(storedLocation.getCountryID());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), Utils.CHARSET_UTF_8);
			try {
				w.write(LOCATION_CSV_HEADER);
				for (Region r : geography.getRegions(countryID, true)) {
					for (City c : geography.getCities(RegionID.create(r), true)) {
						for (Location existLocation : geography.getLocations(CityID.create(c), true)){
							if (LocationID.create(existLocation.getCountryID(), rootOrganisationID, existLocation.getLocationID()).equals(locationID)) {
								existLocation = storedLocation;
								storedLocation = null; locationID = null;
							}//if

							String csvLines = GeographyImplResourceCSV.location2csvLines(existLocation);

							if (logger.isDebugEnabled())
								logger.debug(csvLines);

							w.write(csvLines);
						}//for
					}//for
				}//for

				if (storedLocation != null) {
					String csvLines = GeographyImplResourceCSV.location2csvLines(storedLocation);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

					csvLines.trim();
					w.write(csvLines);
				}//if
			}//try 
			finally {
				w.close();
			}//finally

			CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_LOCATION, countryID.countryID, out.toByteArray());
			clearCache();
		}//try 
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeGeographyTemplateDistrictData(District storedDistrict)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			Geography geography = Geography.sharedInstance();

			String rootOrganisationID;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				}//try 
				finally {
					initialContext.close();
				}//finally
			}//try 
			catch (NamingException x) {
				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
			}//catch

			DistrictID districtID = DistrictID.create(storedDistrict.getCountryID(), rootOrganisationID, storedDistrict.getDistrictID());
			CountryID countryID = CountryID.create(storedDistrict.getCountryID());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), Utils.CHARSET_UTF_8);
			try {
				w.write(DISTRICT_CSV_HEADER);
//				for (Region r : geography.getRegions(countryID, true)) {
//				for (District d : geography.getDistrictsByZipMap(RegionID.create(r)){
//				if (DistrictID.create(d.getCountryID(), rootOrganisationID, d.getDistrictID()).equals(districtID)) {
//				c = city;
//				city = null; cityID = null;
//				}//if

//				String csvLines = GeographyImplResourceCSV.city2csvLines(c);

//				if (logger.isDebugEnabled())
//				logger.debug(csvLines);

//				w.write(csvLines);
//				}//for
//				}//for

				if (storedDistrict != null) {
					String csvLines = GeographyImplResourceCSV.district2csvLines(storedDistrict);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

					csvLines.trim();
					w.write(csvLines);
				}//if
			}//try 
			finally {
				w.close();
			}//finally

			CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_DISTRICT, countryID.countryID, out.toByteArray());
			clearCache();
		}//try 
		finally {
			pm.close();
		}//finally
	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void storeGeographyTemplateZipData(/*District district*/)
	throws IOException
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			Geography geography = Geography.sharedInstance();

			String rootOrganisationID;
			try {
				InitialContext initialContext = new InitialContext();
				try {
					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
				}//try 
				finally {
					initialContext.close();
				}//finally
			}//try 
			catch (NamingException x) {
				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
			}//catch

//			DistrictID districtID = DistrictID.create(district.getCountryID(), rootOrganisationID, district.getDistrictID());
//			CountryID countryID = CountryID.create(district.getCountryID());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), Utils.CHARSET_UTF_8);
			try {
				w.write(ZIP_CSV_HEADER);
//				for (Region r : geography.getRegions(countryID, true)) {
//				for (District d : geography.getDistrictsByZipMap(RegionID.create(r)){
//				if (DistrictID.create(d.getCountryID(), rootOrganisationID, d.getDistrictID()).equals(districtID)) {
//				c = city;
//				city = null; cityID = null;
//				}//if

//				String csvLines = GeographyImplResourceCSV.city2csvLines(c);

//				if (logger.isDebugEnabled())
//				logger.debug(csvLines);

//				w.write(csvLines);
//				}//for
//				}//for

//				if (district != null) {
//				String csvLines = GeographyImplResourceCSV.district2csvLines(district);

//				if (logger.isDebugEnabled())
//				logger.debug(csvLines);

//				csvLines.trim();
//				w.write(csvLines);
//				}//if
			}//try 
			finally {
				w.close();
			}//finally

//			CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_DISTRICT, countryID.countryID, out.toByteArray());
			clearCache();
		}//try 
		finally {
			pm.close();
		}//finally
	}
}
