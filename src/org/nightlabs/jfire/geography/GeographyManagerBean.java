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

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.organisation.Organisation;

/**
 * @ejb.bean name="jfire/ejb/JFireGeography/GeographyManager"
 *           jndi-name="jfire/ejb/JFireGeography/GeographyManager"
 *           type="Stateless"
 *           transaction-type="Container"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
 */
public abstract class GeographyManagerBean
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(GeographyManagerBean.class);

	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#setSessionContext(javax.ejb.SessionContext)
	 */
	@Override
	public void setSessionContext(SessionContext sessionContext)
	throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	/**
	 * @see org.nightlabs.jfire.base.BaseSessionBeanImpl#unsetSessionContext()
	 */
	@Override
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

//	protected static StreamTokenizer getCSVTokenizer(InputStreamReader reader)
//	{
//	StreamTokenizer tokenizer = new StreamTokenizer(reader);
//	tokenizer.resetSyntax();
//	tokenizer.wordChars(0, Integer.MAX_VALUE);
//	tokenizer.quoteChar('"');
//	tokenizer.whitespaceChars(';', ';');
//	tokenizer.whitespaceChars('\r', '\r');
//	tokenizer.whitespaceChars('\n', '\n');
//	tokenizer.eolIsSignificant(true);
//	return tokenizer;
//	}

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise()
	{
//		GeographyImplResourceCSV.register();
		GeographyImplJDO.register();

//		PersistenceManager pm = getPersistenceManager();
//		try {
//		Geography.getGeography(pm);
//		} finally {
//		pm.close();
//		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Collection<Country> getCountries(String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ArrayList<Country> res = new ArrayList<Country>();
			for (Iterator<?> it = pm.getExtent(Country.class).iterator(); it.hasNext(); ) {
				Country country = (Country) it.next();

				res.add(pm.detachCopy(country));
			}

			return res;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Country getCountry(CountryID countryID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Country.class);
			Country country = (Country) pm.getObjectById(countryID);
			return pm.detachCopy(country);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Region getRegion(RegionID regionID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Region.class);
			Region region = (Region) pm.getObjectById(regionID);
			return pm.detachCopy(region);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public City getCity(CityID cityID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(City.class);
			City city = (City) pm.getObjectById(cityID);
			return pm.detachCopy(city);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	public Location getLocation(LocationID locationID, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			pm.getExtent(Location.class);
			Location location = (Location) pm.getObjectById(locationID);
			return pm.detachCopy(location);
		} finally {
			pm.close();
		}
	}


	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public Country storeCountry(Country country, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, country, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public Region storeRegion(Region region, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, region, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public City storeCity(City city, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, city, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public byte[] getCSVData(String csvType, String countryID)
	{
		PersistenceManager pm = getPersistenceManager();
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

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public Object getGeographyObject(ObjectID objectID, String[] fetchGroups, int maxFetchDepth){
		PersistenceManager pm = getPersistenceManager();
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

//	/**
//	 * Returns the {@link Country} with the given name and {@link Locale}
//	 * @param countryName the name of the country
//	 * @param locale the {@link Locale} to search in the multiLanguage name of the country
//	 * @return the {@link Country} with the given name and {@link Locale}  
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */	
//	public Country getCountryByName(String countryName, Locale locale) 
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Collection<Country> countries = Country.getCountryByName(pm, countryName, locale);
//			if (countries != null && !countries.isEmpty()) {
//				return countries.iterator().next();
//			}
//			return null;			
//		} finally {
//			pm.close();
//		}
//	}
//	
//	/**
//	 * Returns the {@link Region} with the given name and {@link Locale}
//	 * @param regionName the name of the region
//	 * @param locale the {@link Locale} to search in the multiLanguage name of the region
//	 * @return the {@link Region} with the given name and {@link Locale}  
//	 *
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */	
//	public Region getRegionByName(String regionName, Locale locale) 
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {		
//			Collection<Region> regions = Region.getRegionByName(pm, regionName, locale);
//			if (regions != null && !regions.isEmpty()) {
//				return regions.iterator().next();
//			}
//			return null;
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * Returns the {@link City} with the given name and {@link Locale}
//	 * @param cityName the name of the city
//	 * @param locale the {@link Locale} to search in the multiLanguage name of the city
//	 * @return the {@link City} with the given name and {@link Locale}  
//	 * 
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */	
//	public City getCityByName(String cityName, Locale locale) 
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {				
//			Collection<City> cities = City.getCityByName(getPersistenceManager(), 
//					cityName, locale);
//			if (cities != null && !cities.isEmpty()) {
//				return cities.iterator().next();
//			}
//			return null;
//		} finally {
//			pm.close();
//		}
//	}
//
//	/**
//	 * Returns the {@link Location} with the given name and {@link Locale}
//	 * @param locationName the name of the location
//	 * @param locale the {@link Locale} to search in the multiLanguage name of the location
//	 * @return the {@link Location} with the given name and {@link Locale}  
// 	*
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_Guest_"
//	 */	
//	public Location getLocationByName(String locationName, Locale locale) 
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {						
//			Collection<Location> locations = Location.getLocationByName(pm, locationName, locale);
//			if (locations != null && !locations.isEmpty()) {
//				return locations.iterator().next();
//			}
//			return null;
//		} finally {
//			pm.close();
//		}
//	}
	
}
