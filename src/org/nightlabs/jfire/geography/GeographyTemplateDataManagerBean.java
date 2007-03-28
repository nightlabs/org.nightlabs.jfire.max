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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;
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
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	public void updateGeographyTemplateData(String countryID, Object obj){
		logger.info("Update Geography Template Data...");
		PersistenceManager pm = getPersistenceManager();
		String[] fetchGroup = {FetchPlan.ALL};
		try {
			CSV csv = null;
			pm.getFetchPlan().setMaxFetchDepth(1);
			pm.getFetchPlan().setGroup(FetchPlan.ALL);

			InitialContext initialContext = new InitialContext();

			ByteArrayOutputStream outp = new ByteArrayOutputStream();
			OutputStream out = new DeflaterOutputStream(outp);
			Writer w = new OutputStreamWriter(out, Utils.CHARSET_UTF_8);
			
			String csvLines = null;
			try {
				if(obj instanceof Location){
					Location newLocation = (Location)obj;

					Location existLocation = GeographyImplJDO.sharedInstance().getLocation(LocationID.create(newLocation.getCountryID()
							, newLocation.getOrganisationID(), newLocation.getLocationID()), false);

					if(existLocation != null){
						List locationList = new ArrayList();
						Collection existLocations = GeographyImplJDO.sharedInstance().getLocations(CityID.create(
								newLocation.getCity().getCountryID(), newLocation.getCity().getOrganisationID(), newLocation.getCity().getCityID()), true);

						for(Iterator locationIterator = existLocations.iterator(); locationIterator.hasNext();){
							Location location = (Location)locationIterator.next();
							if(location.getLocationID().equals(newLocation.getLocationID())){
								locationList.add(newLocation);
							}//if
							else{
								locationList.add(location);
							}//else
						}//for

						csvLines = GeographyImplResourceCSV.obj2csvLine(locationList);		
						w.write(csvLines);
					}//if
					else{
						Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(newLocation.getCountryID()), true);

						Collection<Region> regions = country.getRegions();

						for(Iterator<Region> it = regions.iterator(); it.hasNext();){
							Region region = (Region)it.next();
							Collection<City> cities = region.getCities();
							for(Iterator<City> it2 = cities.iterator(); it2.hasNext();){
								City city = (City)it2.next();
								Collection<Location> locations = city.getLocations();
								if(locations != null && locations.size() > 0){
									for(Iterator<Location> it3 = locations.iterator(); it3.hasNext();){
										Location location = it3.next();
										String csvLine = GeographyImplResourceCSV.obj2csvLine(location);
										w.write(csvLine);
									}//for
								}//if
							}//for
						}//for
						w.write(GeographyImplResourceCSV.obj2csvLine(newLocation));
					}//else

					csv = CSV.setCSVData(pm, Organisation.getRootOrganisationID(initialContext), CSV.CSV_TYPE_LOCATION, countryID, outp.toByteArray());
				}//if
				else if(obj instanceof Region){
					Region newRegion = (Region)obj;

					Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(newRegion.getCountryID()), true);

					Collection<Region> regions = country.getRegions();

					for(Iterator<Region> it = regions.iterator(); it.hasNext();){
						Region region = (Region)it.next();
						String csvLine = GeographyImplResourceCSV.obj2csvLine(region);
						w.write(csvLine);
					}//for

					String csvLine = GeographyImplResourceCSV.obj2csvLine(newRegion);
					w.write(csvLine);

					csv = CSV.setCSVData(pm, Organisation.getRootOrganisationID(initialContext), CSV.CSV_TYPE_CITY, country.getCountryID(), outp.toByteArray());
				}//else if
				else if(obj instanceof City){
					City updateCity = (City)obj;
					
					City existCity = GeographyImplJDO.sharedInstance().getCity(CityID.create(updateCity.getCountryID()
							, updateCity.getOrganisationID(), updateCity.getCityID()), false);
					
					if(existCity != null){
						List cityList = new ArrayList();
						Collection existCities = GeographyImplJDO.sharedInstance().getCities(RegionID.create(
								updateCity.getCountryID(), updateCity.getOrganisationID(), updateCity.getRegion().getRegionID()), true);

						for(Iterator cityIterator = existCities.iterator(); cityIterator.hasNext();){
							City city = (City)cityIterator.next();
							if(city.getCityID().equals(updateCity.getCityID())){
								cityList.add(updateCity);
							}//if
							else{
								cityList.add(city);
							}//else
						}//for

						csvLines = GeographyImplResourceCSV.obj2csvLine(cityList);		
						w.write(csvLines);
					}//if
					else{
						Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(updateCity.getCountryID()), true);

						Collection<Region> regions = country.getRegions();

						for(Iterator<Region> it = regions.iterator(); it.hasNext();){
							Region region = (Region)it.next();
							Collection<City> cities = region.getCities();
							for(Iterator<City> it2 = cities.iterator(); it2.hasNext();){
								City city = (City)it2.next();
								String csvLine = GeographyImplResourceCSV.obj2csvLine(city);
								w.write(csvLine);
							}//for
						}//for
						w.write(GeographyImplResourceCSV.obj2csvLine(updateCity));
						w.flush();
					}//else
					
					csv = CSV.setCSVData(pm, updateCity.getOrganisationID(), CSV.CSV_TYPE_CITY, countryID, outp.toByteArray());
				}//else if
				else if(obj instanceof District){
					District newDistrict = (District)obj;

					Country country = GeographyImplJDO.sharedInstance().getCountry(CountryID.create(newDistrict.getCountryID()), true);

					List<Object> districtList = new ArrayList();
					Collection<Region> regions = country.getRegions();
					for(Iterator<Region> it = regions.iterator(); it.hasNext();){
						Region region = (Region)it.next();
						Collection<City> cities = region.getCities();
						for(Iterator<City> it2 = cities.iterator(); it2.hasNext();){
							City city = (City)it2.next();
							Collection<District> districts = city.getDistricts();
							if(districts != null && districts.size() > 0){
								for(Iterator it3 = districts.iterator(); it3.hasNext();){
									districtList.add((Location)it3.next());
								}//for
							}//if
						}//for
					}//for

					districtList.add(newDistrict);

//					String csvLines = GeographyImplResourceCSV.collection2csvLines(districtList);


//					Deflater compresser  = new Deflater();
//					compresser.setInput(csvLines.getBytes());
//					compresser.finish();

//					byte[] output = new byte[compresser.getTotalOut()];
//					compresser.deflate(output);

//					csv = CSV.setCSVData(pm, Organisation.getRootOrganisationID(initialContext), CSV.CSV_TYPE_DISTRICT, country.getCountryID(), output);
				}//else if
			}//try 
			catch(IOException ex){
				ex.printStackTrace();
			}//catch
			finally {
				initialContext.close();
				w.close();
			}//finally
		}//try
		catch (IOException x) {
			throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
		}//catch
		catch (NamingException x) {
			throw new RuntimeException(x); // it's definitely an unexpected exception if we can't access the local JNDI.
		}//catch
		finally {
			pm.close();
		}//finally
	}
}
