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

import java.util.Collection;
import java.util.HashSet;

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

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.CollectionUtil;

/**
 * Geography manager implementation.
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
@Stateless
public class GeographyManagerBean
extends BaseSessionBeanImpl
implements GeographyManagerRemote
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise()
	{
		GeographyImplJDO.register();
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getCountryIDs()
	 */
	@RolesAllowed("_Guest_")
	public Collection<CountryID> getCountryIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(Country.class);
			q.setResult("JDOHelper.getObjectId(this)");
			Collection<CountryID> res = CollectionUtil.castCollection((Collection<?>) q.execute());
			return new HashSet<CountryID>(res);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getRegionIDs(org.nightlabs.jfire.geography.id.CountryID)
	 */
	@RolesAllowed("_Guest_")
	public Collection<RegionID> getRegionIDs(CountryID countryID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(Region.class);
			q.setResult("JDOHelper.getObjectId(this)");
			if (countryID != null)
				q.setFilter("JDOHelper.getObjectId(this.country) == :countryID");

			Collection<RegionID> res = CollectionUtil.castCollection((Collection<?>) q.execute(countryID));
			return new HashSet<RegionID>(res);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getCityIDs(org.nightlabs.jfire.geography.id.RegionID)
	 */
	@RolesAllowed("_Guest_")
	public Collection<CityID> getCityIDs(RegionID regionID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(City.class);
			q.setResult("JDOHelper.getObjectId(this)");
			if (regionID != null)
				q.setFilter("JDOHelper.getObjectId(this.region) == :regionID");

			Collection<CityID> res = CollectionUtil.castCollection((Collection<?>) q.execute(regionID));
			return new HashSet<CityID>(res);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getLocationIDs(org.nightlabs.jfire.geography.id.CityID)
	 */
	@RolesAllowed("_Guest_")
	public Collection<LocationID> getLocationIDs(CityID cityID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(Location.class);
			q.setResult("JDOHelper.getObjectId(this)");
			if (cityID != null)
				q.setFilter("JDOHelper.getObjectId(this.city) == :cityID");

			Collection<LocationID> res = CollectionUtil.castCollection((Collection<?>) q.execute(cityID));
			return new HashSet<LocationID>(res);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getCountries(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Collection<Country> getCountries(Collection<CountryID> countryIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, countryIDs, Country.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getRegions(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Collection<Region> getRegions(Collection<RegionID> regionIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, regionIDs, Region.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getCities(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Collection<City> getCities(Collection<CityID> cityIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, cityIDs, City.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getLocations(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Collection<Location> getLocations(Collection<LocationID> locationIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, locationIDs, Location.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#importCountry(org.nightlabs.jfire.geography.id.CountryID, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Country importCountry(CountryID countryID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		if (countryID == null)
			throw new IllegalArgumentException("countryID must not be null!");

		PersistenceManager pm = createPersistenceManager();
		try {
			Country country;
			try {
				country = (Country) pm.getObjectById(countryID);
			} catch (JDOObjectNotFoundException x) {
				country = null;
			}

			if (country == null) {
				country = Geography.sharedInstance().getCountry(countryID, true);
				country = country.copyForJDOStorage();
				country = pm.makePersistent(country);
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(country);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#importRegion(org.nightlabs.jfire.geography.id.RegionID, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Region importRegion(RegionID regionID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Region region;
			try {
				region = (Region) pm.getObjectById(regionID);
			} catch (JDOObjectNotFoundException x) {
				region = null;
			}

			if (region == null) {
				region = Geography.sharedInstance().getRegion(regionID, true);
				CountryID countryID = CountryID.create(region);
				Country persistentCountry = (Country) pm.getObjectById(countryID);
				region = region.copyForJDOStorage(persistentCountry);
				region = persistentCountry.addRegion(region);
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(region);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#importCity(org.nightlabs.jfire.geography.id.CityID, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public City importCity(CityID cityID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			City city;
			try {
				city = (City) pm.getObjectById(cityID);
			} catch (JDOObjectNotFoundException x) {
				city = null;
			}

			if (city == null) {
				city = Geography.sharedInstance().getCity(cityID, true);
				RegionID regionID = RegionID.create(city.getRegion());
				Region persistentRegion = (Region) pm.getObjectById(regionID);
				city = city.copyForJDOStorage(persistentRegion);
				city = persistentRegion.addCity(city);
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(city);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#importLocation(org.nightlabs.jfire.geography.id.LocationID, boolean, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Location importLocation(LocationID locationID, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Location location;
			try {
				location = (Location) pm.getObjectById(locationID);
			} catch (JDOObjectNotFoundException x) {
				location = null;
			}

			if (location == null) {
				location = Geography.sharedInstance().getLocation(locationID, true);
				CityID cityID = CityID.create(location.getCity());
				City persistentCity = (City) pm.getObjectById(cityID);
				location = location.copyForJDOStorage(persistentCity);
				location = persistentCity.addLocation(location);
			}

			if (!get)
				return null;

			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			return pm.detachCopy(location);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getCSVData(java.lang.String, java.lang.String)
	 */
	@RolesAllowed("_Guest_")
	public byte[] getCSVData(String csvType, String countryID)
	{
		PersistenceManager pm = createPersistenceManager();
		pm.getFetchPlan().setMaxFetchDepth(1);
		pm.getFetchPlan().setGroup(FetchPlan.ALL);

		try {
			InitialContext initialContext = new InitialContext();
			try {
				byte[] data = CSV.getCSVData(pm, Organisation.getRootOrganisationID(initialContext), csvType, countryID);
				if (data == null) {
					Geography.sharedInstance().needCountries();
					Geography.sharedInstance().needRegions(countryID);
					Geography.sharedInstance().needCities(countryID);
					Geography.sharedInstance().needDistricts(countryID);
					Geography.sharedInstance().needZips(countryID);
					Geography.sharedInstance().needLocations(countryID);
					data = CSV.getCSVData(pm, Organisation.getRootOrganisationID(initialContext), csvType, countryID);
				}
				return data;
			} finally {
				initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
		}
		finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getCSVsData(java.lang.String)
	 */
	@RolesAllowed("_Guest_")
	public byte[] getCSVsData(String csvType)
	{
		PersistenceManager pm = createPersistenceManager();
		pm.getFetchPlan().setMaxFetchDepth(1);
		pm.getFetchPlan().setGroup(FetchPlan.ALL);
		try {
			InitialContext initialContext = new InitialContext();
			try {
				byte[] data = CSV.getCSVsData(pm, Organisation.getRootOrganisationID(initialContext), csvType);
				if (data == null) {
					Geography.sharedInstance().needCountries();
					data = CSV.getCSVsData(pm, Organisation.getRootOrganisationID(initialContext), csvType);
				}
				return data;
			} finally {
				initialContext.close();
			}
		} catch (NamingException x) {
			throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
		}
		finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.geography.GeographyManagerRemote#getGeographyObject(org.nightlabs.jdo.ObjectID, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public Object getGeographyObject(ObjectID objectID, String[] fetchGroups, int maxFetchDepth){
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(CSV.class);
			Object obj = pm.getObjectById(objectID, true);
			return pm.detachCopy(obj);
		} finally {
			pm.close();
		}
	}
}
