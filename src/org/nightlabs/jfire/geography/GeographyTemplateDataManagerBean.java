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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.geography.id.CSVID;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.geography.notification.GeographyTemplateDataNotificationFilter;
import org.nightlabs.jfire.geography.notification.GeographyTemplateDataNotificationReceiver;
import org.nightlabs.jfire.idgenerator.IDNamespace;
import org.nightlabs.jfire.jdo.notification.persistent.PersistentNotificationEJBRemote;
import org.nightlabs.jfire.jdo.notification.persistent.SubscriptionUtil;
import org.nightlabs.jfire.jdo.notification.persistent.id.NotificationReceiverID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.organisation.id.OrganisationID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.IOUtil;

/**
 * @ejb.bean name="jfire/ejb/JFireGeography/GeographyTemplateDataManager"
 *           jndi-name="jfire/ejb/JFireGeography/GeographyTemplateDataManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless

public class GeographyTemplateDataManagerBean
extends BaseSessionBeanImpl
implements GeographyTemplateDataManagerRemote
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(GeographyTemplateDataManagerBean.class);

	private static final String COUNTRY_CSV_HEADER = "CountryID;LanguageID;CountryName\n";
	private static final String REGION_CSV_HEADER = "CountryID;RegionID;LanguageID;RegionName\n";
	private static final String CITY_CSV_HEADER = "CountryID;CityID;RegionID;LanguageID;CityName\n";
	private static final String LOCATION_CSV_HEADER = "CountryID;LocationID;CityID;DistrictID;LanguageID;LocationName\n";
	private static final String DISTRICT_CSV_HEADER = "CountryID;CityID;DistrictID;LanguageID;DistrictName;Latitute;Longitude\n";

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#initialiseJDOLifecycleListeners(org.nightlabs.jfire.crossorganisationregistrationinit.Context)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialiseJDOLifecycleListeners(Context context)
	throws Exception
	{
		if (context.getOtherOrganisationID().equals(getRootOrganisationID()))
			initialiseJDOLifecycleListeners();
		else
			logger.info("initialiseJDOLifecycleListeners: Other organisation is not the root organisation => nothing to do.");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#initialiseJDOLifecycleListeners()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialiseJDOLifecycleListeners()
	throws Exception
	{
		String subscriberOrganisationID = getOrganisationID();
		String rootOrganisationID = getRootOrganisationID();
		if (subscriberOrganisationID.equals(rootOrganisationID)) // only register in the root organisation, if that's not the local organisation
			return;

		PersistenceManager pm = getPersistenceManager();
		try {
			NotificationReceiverID notificationReceiverID = NotificationReceiverID.create(
					rootOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, subscriberOrganisationID,
					GeographyTemplateDataNotificationFilter.class.getName());
			try {
				pm.getObjectById(notificationReceiverID);
				logger.info("initialiseJDOLifecycleListeners: NotificationReceiver for CSV changes in the root organisation has already been registered. Skipping!");
				return; // it exists already => doing nothing
			} catch (JDOObjectNotFoundException x) {
				// it doesn't exist => register the persistent lifecycle listener
			}

			// check if the root-organisation is already registered
			try {
				pm.getObjectById(OrganisationID.create(rootOrganisationID));
			} catch (JDOObjectNotFoundException x) {
				logger.info("initialiseJDOLifecycleListeners: NotificationReceiver does not yet exist, but I cannot register the JDO lifecycle listener in the root organisation, because the root organisation is not yet registered.");
				return;
			}
			logger.info("initialiseJDOLifecycleListeners: NotificationReceiver does not yet exist. Will register persistent JDO lifecycle listener in root organisation and persist NotificationReceiver now.");

			GeographyTemplateDataNotificationFilter notificationFilter = new GeographyTemplateDataNotificationFilter(
					rootOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, subscriberOrganisationID,
					GeographyTemplateDataNotificationFilter.class.getName());
			PersistentNotificationEJBRemote persistentNotificationEJB;

			try {
				persistentNotificationEJB = JFireEjb3Factory.getRemoteBean(PersistentNotificationEJBRemote.class, getInitialContextProperties(rootOrganisationID));
			} catch (JDOObjectNotFoundException x) {
				logger.warn("Creating JDO lifecycle listeners for CSV instances in the root organisation failed. Reason: Root organisation " + rootOrganisationID + " does not exist.");
				return;
			}

			persistentNotificationEJB.storeNotificationFilter(notificationFilter, false, null, 1);

			GeographyTemplateDataNotificationReceiver notificationReceiver = new GeographyTemplateDataNotificationReceiver(notificationFilter);
			pm.makePersistent(notificationReceiver);

			logger.info("initialiseJDOLifecycleListeners: NotificationReceiver for changes of CSV instances in the root organisation has been successfully created.");
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	throws Exception
	{
		Geography geography = Geography.sharedInstance();
		String organisationID = getOrganisationID();

		PersistenceManager pm = createPersistenceManager();
		try {
			// As the ModuleMetaData is not managed by GeographyManagerBean, we can do it here (this stuff is expensive and we should therefore avoid to
			// run it on every boot).
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireGeographyEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			// version is {major}.{minor}.{release}-{patchlevel}-{suffix}
			moduleMetaData = ModuleMetaData.createModuleMetaDataFromManifest(
					JFireGeographyEAR.MODULE_NAME, JFireGeographyEAR.class
			);
			pm.makePersistent(moduleMetaData);


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
		/**
		 * The serial version of this class.
		 */
		private static final long serialVersionUID = 1L;

		@Override
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
	 * It is only allowed to write template data, if we're the root-organisation or if there is no root-organisation.
	 * @throws SecurityException if writing is not allowed.
	 */
	private void assertWritingAllowed()
	throws SecurityException
	{
		if (!hasRootOrganisation()) // if there is no root-organisation, writing is allowed
			return;

		String rootOrganisationID = getRootOrganisationID();

		if (getOrganisationID().equals(rootOrganisationID)) // if we are the root-organisation, writing is ok
			return;

		throw new SecurityException("Writing geography template data is exclusively allowed to the root-organisation (" + rootOrganisationID + ") when using JFire in network mode (with a root-organisation present). Your organisation (" + getOrganisationID() + ") cannot modify the network-wide geography data!");
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#storeGeographyTemplateCountryData(org.nightlabs.jfire.geography.Country)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void storeGeographyTemplateCountryData(Country storedCountry)
	throws IOException, SecurityException
	{
		assertWritingAllowed();

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
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), IOUtil.CHARSET_UTF_8);
			try {
				w.write(COUNTRY_CSV_HEADER);
				for (Country existCountry : geography.getCountries()) {
					if (CountryID.create(existCountry).equals(countryID)) {
						existCountry = storedCountry;
						storedCountry = null;
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#storeGeographyTemplateRegionData(org.nightlabs.jfire.geography.Region)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void storeGeographyTemplateRegionData(Region storedRegion)
	throws IOException, SecurityException
	{
		assertWritingAllowed();

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
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), IOUtil.CHARSET_UTF_8);
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#storeGeographyTemplateCityData(org.nightlabs.jfire.geography.City)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void storeGeographyTemplateCityData(City storedCity)
	throws IOException, SecurityException
	{
		assertWritingAllowed();

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
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), IOUtil.CHARSET_UTF_8);
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#storeGeographyTemplateLocationData(org.nightlabs.jfire.geography.Location)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void storeGeographyTemplateLocationData(Location storedLocation)
	throws IOException, SecurityException
	{
		assertWritingAllowed();

		PersistenceManager pm = getPersistenceManager();
		try {
			NLJDOHelper.enableTransactionSerializeReadObjects(pm);
			try {
				pm.getFetchPlan().setMaxFetchDepth(1);
				pm.getFetchPlan().setGroup(FetchPlan.ALL);

				Geography geography = Geography.sharedInstance();
				geography.clearCache(); // ensure that the data we're going to manipulate in this transaction are really up-to-date.

				String rootOrganisationID;

				try {
					InitialContext initialContext = new InitialContext();
					try {
						rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
					} finally {
						initialContext.close();
					}
				} catch (NamingException x) {
					throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
				}

				LocationID locationID = LocationID.create(storedLocation.getCountryID(), rootOrganisationID, storedLocation.getLocationID());
				CountryID countryID = CountryID.create(storedLocation.getCountryID());

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), IOUtil.CHARSET_UTF_8);
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
				} finally {
					w.close();
				}

				CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_LOCATION, countryID.countryID, out.toByteArray());

				clearCache();

			} finally {
				NLJDOHelper.disableTransactionSerializeReadObjects(pm);
			}
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyTemplateDataManagerRemote#storeGeographyTemplateDistrictData(org.nightlabs.jfire.geography.District)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void storeGeographyTemplateDistrictData(District storedDistrict)
	throws IOException, SecurityException
	{
		assertWritingAllowed();

		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			// initialize:
			Geography.sharedInstance();

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

			CountryID countryID = CountryID.create(storedDistrict.getCountryID());

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), IOUtil.CHARSET_UTF_8);
			try {
				w.write(DISTRICT_CSV_HEADER);
				// TODO: write all old data!
				if (storedDistrict != null) {
					String csvLines = GeographyImplResourceCSV.district2csvLines(storedDistrict);

					if (logger.isDebugEnabled())
						logger.debug(csvLines);

//					csvLines.trim();
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
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")

	public Set<CSVID> getCSVIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(CSV.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<CSVID> c = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<CSVID>(c);
		} finally{
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	public Set<CSV> getCSVs(Set<CSVID> csvIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(pm, csvIDs, CSV.class, fetchGroups, maxFetchDepth);
		} finally{
			pm.close();
		}
	}

	// TODO: needs implementation!
//	private static final String ZIP_CSV_HEADER = "CountryID;CityID;DistrictID;Zip\n";
//	/**
//	 * @ejb.interface-method
//	 * @ejb.transaction type="Required"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void storeGeographyTemplateZipData(/*District district*/)
//	throws IOException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			pm.getFetchPlan().setMaxFetchDepth(1);
//			pm.getFetchPlan().setGroup(FetchPlan.ALL);
//
//			// initialize
//			Geography.sharedInstance();
//
//			String rootOrganisationID;
//
//			try {
//				InitialContext initialContext = new InitialContext();
//				try {
//					rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
//				}//try
//				finally {
//					initialContext.close();
//				}//finally
//			}//try
//			catch (NamingException x) {
//				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
//			}//catch
//
////			DistrictID districtID = DistrictID.create(district.getCountryID(), rootOrganisationID, district.getDistrictID());
////			CountryID countryID = CountryID.create(district.getCountryID());
//
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), IOUtil.CHARSET_UTF_8);
//			try {
//				w.write(ZIP_CSV_HEADER);
////				for (Region r : geography.getRegions(countryID, true)) {
////				for (District d : geography.getDistrictsByZipMap(RegionID.create(r)){
////				if (DistrictID.create(d.getCountryID(), rootOrganisationID, d.getDistrictID()).equals(districtID)) {
////				c = city;
////				city = null; cityID = null;
////				}//if
//
////				String csvLines = GeographyImplResourceCSV.city2csvLines(c);
//
////				if (logger.isDebugEnabled())
////				logger.debug(csvLines);
//
////				w.write(csvLines);
////				}//for
////				}//for
//
////				if (district != null) {
////				String csvLines = GeographyImplResourceCSV.district2csvLines(district);
//
////				if (logger.isDebugEnabled())
////				logger.debug(csvLines);
//
////				csvLines.trim();
////				w.write(csvLines);
////				}//if
//			}//try
//			finally {
//				w.close();
//			}//finally
//
////			CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_DISTRICT, countryID.countryID, out.toByteArray());
//
////			GeographyTemplateDataNotificationFilter notificationFilter = new GeographyTemplateDataNotificationFilter(
////					rootOrganisationID, SubscriptionUtil.SUBSCRIBER_TYPE_ORGANISATION, getOrganisationID(),
////					GeographyTemplateDataNotificationFilter.class.getName(), Zip.class.getName());
////			PersistentNotificationEJB persistentNotificationEJB;
////			try {
////				persistentNotificationEJB = PersistentNotificationEJBUtil.getHome(initialProperties).create();
////				persistentNotificationEJB.storeNotificationFilter(notificationFilter, false, null, 1);
////			} catch (CreateException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			} catch (NamingException e) {
////				// TODO Auto-generated catch block
////				e.printStackTrace();
////			}
//
//			clearCache();
//		}//try
//		finally {
//			pm.close();
//		}//finally
//	}
}
