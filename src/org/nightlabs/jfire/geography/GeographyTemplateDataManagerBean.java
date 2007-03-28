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
	public void storeGeographyTemplateData(Object obj)
	throws IOException
	{
		logger.info("Update Geography Template Data...");
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
				} finally {
					initialContext.close();
				}
			} catch (NamingException x) {
				throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
			}

				if(obj instanceof Location){
//					Location newLocation = (Location)obj;
//
//					Location existLocation = GeographyImplJDO.sharedInstance().getLocation(LocationID.create(newLocation.getCountryID()
//							, newLocation.getOrganisationID(), newLocation.getLocationID()), false);
//
//					if(existLocation != null){
//						List locationList = new ArrayList();
//						Collection existLocations = GeographyImplJDO.sharedInstance().getLocations(CityID.create(
//								newLocation.getCity().getCountryID(), newLocation.getCity().getOrganisationID(), newLocation.getCity().getCityID()), true);
//
//						for(Iterator locationIterator = existLocations.iterator(); locationIterator.hasNext();){
//							Location location = (Location)locationIterator.next();
//							if(location.getLocationID().equals(newLocation.getLocationID())){
//								locationList.add(newLocation);
//							}//if
//							else{
//								locationList.add(location);
//							}//else
//						}//for
//
//						csvLines = GeographyImplResourceCSV.obj2csvLine(locationList);		
//						w.write(csvLines);
//					}//if
//					else{
//						Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(newLocation.getCountryID()), true);
//
//						Collection<Region> regions = country.getRegions();
//
//						for(Iterator<Region> it = regions.iterator(); it.hasNext();){
//							Region region = (Region)it.next();
//							Collection<City> cities = region.getCities();
//							for(Iterator<City> it2 = cities.iterator(); it2.hasNext();){
//								City city = (City)it2.next();
//								Collection<Location> locations = city.getLocations();
//								if(locations != null && locations.size() > 0){
//									for(Iterator<Location> it3 = locations.iterator(); it3.hasNext();){
//										Location location = it3.next();
//										String csvLine = GeographyImplResourceCSV.obj2csvLine(location);
//										w.write(csvLine);
//									}//for
//								}//if
//							}//for
//						}//for
//						w.write(GeographyImplResourceCSV.obj2csvLine(newLocation));
//					}//else
//
//					csv = CSV.setCSVData(pm, Organisation.getRootOrganisationID(initialContext), CSV.CSV_TYPE_LOCATION, newLocation.getCountryID(), outp.toByteArray());
				}//if
				else if(obj instanceof Region){
//					Region newRegion = (Region)obj;
//
//					Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(newRegion.getCountryID()), true);
//
//					Collection<Region> regions = country.getRegions();
//
//					for(Iterator<Region> it = regions.iterator(); it.hasNext();){
//						Region region = (Region)it.next();
//						String csvLine = GeographyImplResourceCSV.obj2csvLine(region);
//						w.write(csvLine);
//					}//for
//
//					String csvLine = GeographyImplResourceCSV.obj2csvLine(newRegion);
//					w.write(csvLine);
//
//					csv = CSV.setCSVData(pm, Organisation.getRootOrganisationID(initialContext), CSV.CSV_TYPE_CITY, country.getCountryID(), outp.toByteArray());
				}//else if
				else if(obj instanceof City){
					City city = (City)obj;
					CityID cityID = CityID.create(city);
					CountryID countryID = CountryID.create(city);
//					City existingCity = Geography.sharedInstance().getCity(cityID, false);


					ByteArrayOutputStream out = new ByteArrayOutputStream();
					Writer w = new OutputStreamWriter(new DeflaterOutputStream(out), Utils.CHARSET_UTF_8);
					try {
						for (Region r : geography.getRegions(countryID, true)) {
							for (City c : geography.getCities(RegionID.create(r), true)) {
								if (CityID.create(c).equals(cityID)) {
									c = city;
									city = null; cityID = null;
								}

								String csvLines = GeographyImplResourceCSV.city2csvLines(c);

								if (logger.isDebugEnabled())
									logger.debug(csvLines);

								w.write(csvLines);
							}
						}

						if (city != null) {
							String csvLines = GeographyImplResourceCSV.city2csvLines(city);

							if (logger.isDebugEnabled())
								logger.debug(csvLines);

							w.write(csvLines);
						}
					} finally {
						w.close();
					}

					CSV.setCSVData(pm, rootOrganisationID, CSV.CSV_TYPE_CITY, countryID.countryID, out.toByteArray());
					clearCache();

//					if(existCity != null){
//						List cityList = new ArrayList();
//						Collection existCities = GeographyImplJDO.sharedInstance().getCities(RegionID.create(
//								updateCity.getCountryID(), updateCity.getOrganisationID(), updateCity.getRegion().getRegionID()), true);
//
//						for(Iterator cityIterator = existCities.iterator(); cityIterator.hasNext();){
//							City city = (City)cityIterator.next();
//							if(city.getCityID().equals(updateCity.getCityID())){
//								cityList.add(updateCity);
//							}//if
//							else{
//								cityList.add(city);
//							}//else
//						}//for
//
//						csvLines = GeographyImplResourceCSV.obj2csvLine(cityList);		
//						w.write(csvLines);
//					}//if
//					else{
//						Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(updateCity.getCountryID()), true);
//
//						Collection<Region> regions = country.getRegions();
//
//						for(Iterator<Region> it = regions.iterator(); it.hasNext();){
//							Region region = (Region)it.next();
//							Collection<City> cities = region.getCities();
//							for(Iterator<City> it2 = cities.iterator(); it2.hasNext();){
//								City city = (City)it2.next();
//								String csvLine = GeographyImplResourceCSV.obj2csvLine(city);
//								w.write(csvLine);
//							}//for
//						}//for
//						w.write(GeographyImplResourceCSV.obj2csvLine(updateCity));
//						w.flush();
//					}//else
//					
//					csv = CSV.setCSVData(pm, updateCity.getOrganisationID(), CSV.CSV_TYPE_CITY, updateCity.getCountryID(), outp.toByteArray());
				}//else if
				else if(obj instanceof District){
//					District newDistrict = (District)obj;
//
//					Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(newDistrict.getCountryID()), true);
//
//					List<Object> districtList = new ArrayList();
//					Collection<Region> regions = country.getRegions();
//					for(Iterator<Region> it = regions.iterator(); it.hasNext();){
//						Region region = (Region)it.next();
//						Collection<City> cities = region.getCities();
//						for(Iterator<City> it2 = cities.iterator(); it2.hasNext();){
//							City city = (City)it2.next();
//							Collection<District> districts = city.getDistricts();
//							if(districts != null && districts.size() > 0){
//								for(Iterator it3 = districts.iterator(); it3.hasNext();){
//									districtList.add((Location)it3.next());
//								}//for
//							}//if
//						}//for
//					}//for
//
//					districtList.add(newDistrict);
//
////					String csvLines = GeographyImplResourceCSV.collection2csvLines(districtList);
//
//
////					Deflater compresser  = new Deflater();
////					compresser.setInput(csvLines.getBytes());
////					compresser.finish();
//
////					byte[] output = new byte[compresser.getTotalOut()];
////					compresser.deflate(output);
//
////					csv = CSV.setCSVData(pm, Organisation.getRootOrganisationID(initialContext), CSV.CSV_TYPE_DISTRICT, country.getCountryID(), output);
				}//else if
		} finally {
			pm.close();
		}
	}
}
