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
import java.io.ObjectOutputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Deflater;

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
 * @ejb.util generate = "physical" 
 */
public abstract class GeographyManagerBean
	extends BaseSessionBeanImpl
	implements SessionBean
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(GeographyManagerBean.class);

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

//	protected static StreamTokenizer getCSVTokenizer(InputStreamReader reader)
//	{
//		StreamTokenizer tokenizer = new StreamTokenizer(reader);
//		tokenizer.resetSyntax();
//		tokenizer.wordChars(0, Integer.MAX_VALUE);
//		tokenizer.quoteChar('"');
//		tokenizer.whitespaceChars(';', ';');
//		tokenizer.whitespaceChars('\r', '\r');
//		tokenizer.whitespaceChars('\n', '\n');
//		tokenizer.eolIsSignificant(true);
//		return tokenizer;
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
//			Geography.getGeography(pm);
//		} finally {
//			pm.close();
//		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
	 */
	public Collection<Country> getCountries(String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			pm.getFetchPlan().setMaxFetchDepth(maxFetchDepth);
			if (fetchGroups != null)
				pm.getFetchPlan().setGroups(fetchGroups);

			ArrayList<Country> res = new ArrayList<Country>();
			for (Iterator it = pm.getExtent(Country.class).iterator(); it.hasNext(); ) {
				Country country = (Country) it.next();

				res.add((Country) pm.detachCopy(country));
			}

			return res;
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
			return (Country) pm.detachCopy(country);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
			return (Region) pm.detachCopy(region);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
			return (City) pm.detachCopy(city);
		} finally {
			pm.close();
		}
	}

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Supports"
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
			return (Location) pm.detachCopy(location);
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
			return (Country) NLJDOHelper.storeJDO(pm, country, get, fetchGroups, maxFetchDepth);
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
			return (Region) NLJDOHelper.storeJDO(pm, region, get, fetchGroups, maxFetchDepth);
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
			return (City) NLJDOHelper.storeJDO(pm, city, get, fetchGroups, maxFetchDepth);
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

//		/**
//		 * @ejb.interface-method
//		 * @!ejb.transaction type = "Required"
//		 * @ejb.permission role-name="_Guest_"
//		 */
//		public void initialize()
//			throws ModuleException
//		{
//			try {
//				GeographyManagerLocal geoMan = GeographyManagerUtil.getLocalHome().create();
//				
//	//			PersistenceManager pm = getPersistenceManager();
//	//			try {
//	//				Geography geography = Geography.getGeography(pm);
//	//	//		 initialize the meta data
//	//				pm.getExtent(Country.class);
//	//				pm.getExtent(Region.class);
//	//				pm.getExtent(City.class);
//	//				pm.getExtent(District.class);
//				
//				geoMan._importCountries();
//				geoMan._importRegions();
//		
//	//				// import countries
//	//				String file = "resource/Country.csv";
//	//				InputStream in = Geography.class.getResourceAsStream(file);
//	//				try {
//	//					InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//	//					try {
//	//						StreamTokenizer tokenizer = getCSVTokenizer(reader);
//	//						tokenizer.nextToken();
//	//						while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//	//							System.out.println("line no: " + tokenizer.lineno());
//	//							String countryID = tokenizer.sval;
//	//							tokenizer.nextToken();
//	//							String languageID = tokenizer.sval;
//	//							tokenizer.nextToken();
//	//							String countryName = tokenizer.sval;
//	//							System.out.println(" countryID = \""+countryID+"\" countryName=\""+countryName+"\"");
//	//	
//	//							if (tokenizer.lineno() != 1) {
//	//								Country country;
//	//								try {
//	//									country = (Country) pm.getObjectById(
//	//											CountryID.create(Organisation.ROOT_ORGANISATIONID, countryID));
//	//								} catch (JDOObjectNotFoundException x) {
//	//									country = new Country(Organisation.ROOT_ORGANISATIONID, countryID);
//	//								}
//	//								country.getName().setText(languageID, countryName);
//	//	
//	//								if (!JDOHelper.isPersistent(country))
//	//									pm.makePersistent(country);
//	//							}
//	//	
//	//							while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//	//								tokenizer.nextToken();
//	//							tokenizer.nextToken();
//	//						}
//	//					} finally {
//	//						reader.close();
//	//					}
//	//				} finally {
//	//					in.close();
//	//				}
//					
//	//				// import regions
//	//				file = "resource/Region.csv";
//	//				in = Geography.class.getResourceAsStream(file);
//	//				try {
//	//					InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//	//					try {
//	//						StreamTokenizer tokenizer = getCSVTokenizer(reader);
//	//						tokenizer.nextToken();
//	//						while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//	//							System.out.println("line no: " + tokenizer.lineno());
//	//							String countryID = tokenizer.sval;
//	//							tokenizer.nextToken();
//	//							String regionID = tokenizer.sval;
//	//							tokenizer.nextToken();
//	//							String languageID = tokenizer.sval;
//	//							tokenizer.nextToken();
//	//							String regionName = tokenizer.sval;
//	//							System.out.println(" countryID=\""+countryID+"\" regionID=\""+regionID+"\" regionName=\""+regionName+"\"");
//	//	
//	//							if (tokenizer.lineno() != 1) {
//	//								Country country;
//	//								try {
//	//									country = (Country) pm.getObjectById(
//	//											CountryID.create(Organisation.ROOT_ORGANISATIONID, countryID));
//	//								} catch (JDOObjectNotFoundException x) {
//	//									throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": country with ID \""+countryID+"\" does not exist!");
//	//								}
//	//	
//	//								Region region;
//	//								try {
//	//									region = (Region) pm.getObjectById(
//	//											RegionID.create(
//	//													Organisation.ROOT_ORGANISATIONID, countryID,
//	//													Organisation.ROOT_ORGANISATIONID, regionID));
//	//								} catch (JDOObjectNotFoundException x) {
//	//									region = new Region(Organisation.ROOT_ORGANISATIONID, regionID, country);
//	//								}
//	//								region.getName().setText(languageID, regionName);
//	//	
//	//								if (!JDOHelper.isPersistent(region))
//	//									pm.makePersistent(region);
//	//							}
//	//	
//	//							while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//	//								tokenizer.nextToken();
//	//							tokenizer.nextToken();
//	//						}
//	//					} finally {
//	//						reader.close();
//	//					}
//	//				} finally {
//	//					in.close();
//	//				}
//		
//		
//					// import cities and districts
//					for (int mode = 0; mode <= 1; ++mode) {
//						String file = "resource/City.csv";
//						InputStream in = Geography.class.getResourceAsStream(file);
//						try {
//							InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//							try {
//								StreamTokenizer tokenizer = getCSVTokenizer(reader);
//								tokenizer.nextToken();
//								while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//									String cityIDStr = tokenizer.sval;
//									tokenizer.nextToken();
//									String languageID = tokenizer.sval;
//									tokenizer.nextToken();
//									String countryID = tokenizer.sval;
//									tokenizer.nextToken();
//									String regionID = tokenizer.sval;
//									tokenizer.nextToken();
//									String cityName = tokenizer.sval;
//									tokenizer.nextToken();
//									String districtName = tokenizer.sval;
//									tokenizer.nextToken();
//									String zipStr = tokenizer.sval;
//									tokenizer.nextToken();
//									String latitudeStr = tokenizer.sval;
//									tokenizer.nextToken();
//									String longitudeStr = tokenizer.sval;
//		
//									System.out.println("lineno=\""+tokenizer.lineno()+"\" cityIDStr=\""+cityIDStr+"\" countryID=\""+countryID+"\" regionID=\""+regionID+"\" cityName=\""+cityName+"\" districtName=\""+districtName+"\" zipStr=\""+zipStr+"\" latitudeStr=\""+latitudeStr+"\" longitudeStr=\""+longitudeStr+"\"");
//		
//									if (tokenizer.lineno() != 1 &&
//											((mode == 0 && "".equals(districtName)) || (mode == 1 && !"".equals(districtName))))
//									{
//										long cityID;
//										try {
//											cityID = Long.parseLong(cityIDStr);
//										} catch (NumberFormatException x) {
//											throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": cityID is not a long!", x);
//										}
//		
//										double latitude;
//										try {
//											latitude = Double.parseDouble(latitudeStr);
//										} catch (NumberFormatException x) {
//											throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": latitude is not a double!", x);
//										}
//		
//										double longitude;
//										try {
//											longitude = Double.parseDouble(longitudeStr);
//										} catch (NumberFormatException x) {
//											throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": longitude is not a double!", x);
//										}
//		
//										Set zips = new HashSet();
//										StringTokenizer zipTokenizer = new StringTokenizer(zipStr, ",");
//										while (zipTokenizer.hasMoreTokens()) {
//											String zip = zipTokenizer.nextToken();
//											zips.add(zip);
//										}
//										
//		//								Country country;
//		//								try {
//		//									country = (Country) pm.getObjectById(
//		//											CountryID.create(Organisation.ROOT_ORGANISATIONID, countryID));
//		//								} catch (JDOObjectNotFoundException x) {
//		//									throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": country with ID \""+countryID+"\" does not exist!");
//		//								}
//	
//										geoMan._storeDistrictRecord(
//												file, tokenizer.lineno(), mode == 1,
//												cityIDStr, languageID, 
//												countryID, regionID, cityName,
//												districtName, cityID, latitude, longitude,
//												zips);
//									}
//		
//									while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//										tokenizer.nextToken();
//									tokenizer.nextToken();
//								}
//							} finally {
//								reader.close();
//							}
//						} finally {
//							in.close();
//						}
//					} // for (int mode = 0; mode <= 1; ++mode) {
//	//			} finally {
//	//				pm.close();
//	//			}
//			} catch (RuntimeException x) {
//				throw x;
//			} catch (ModuleException x) {
//				throw x;
//			} catch (Exception x) {
//				throw new ModuleException(x);
//			}
//		}
	
//	/**
//	 * @ejb.interface-method view-type="local"
//	 * @ejb.transaction type = "RequiresNew"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void _importCountries()
//		throws ModuleException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				Geography geography = Geography.getGeography(pm);
//				pm.getExtent(Country.class);
//
//				// import countries
//				String file = "resource/Country.csv";
//				InputStream in = Geography.class.getResourceAsStream(file);
//				try {
//					InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//					try {
//						StreamTokenizer tokenizer = getCSVTokenizer(reader);
//						tokenizer.nextToken();
//						while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//							System.out.println("line no: " + tokenizer.lineno());
//							String countryID = tokenizer.sval;
//							tokenizer.nextToken();
//							String languageID = tokenizer.sval;
//							tokenizer.nextToken();
//							String countryName = tokenizer.sval;
//							System.out.println(" countryID = \""+countryID+"\" countryName=\""+countryName+"\"");
//	
//							if (tokenizer.lineno() != 1) {
//								Country country;
//								try {
//									country = (Country) pm.getObjectById(
//											CountryID.create(Organisation.ROOT_ORGANISATIONID, countryID));
//								} catch (JDOObjectNotFoundException x) {
//									country = new Country(Organisation.ROOT_ORGANISATIONID, countryID);
//								}
//								country.getName().setText(languageID, countryName);
//	
//								if (!JDOHelper.isPersistent(country))
//									pm.makePersistent(country);
//							}
//	
//							while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//								tokenizer.nextToken();
//							tokenizer.nextToken();
//						}
//					} finally {
//						reader.close();
//					}
//				} finally {
//					in.close();
//				}
//			} finally {
//				pm.close();
//			}
//		} catch (RuntimeException x) {
//			throw x;
//		} catch (ModuleException x) {
//			throw x;
//		} catch (Exception x) {
//			throw new ModuleException(x);
//		}
//	}
//
//	/**
//	 * @ejb.interface-method view-type="local"
//	 * @ejb.transaction type = "RequiresNew"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void _importRegions()
//		throws ModuleException
//	{
//		try {
//			PersistenceManager pm = getPersistenceManager();
//			try {
//				pm.getExtent(Region.class);
//
//				String file = "resource/Region.csv";
//				InputStream in = Geography.class.getResourceAsStream(file);
//				try {
//					InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//					try {
//						StreamTokenizer tokenizer = getCSVTokenizer(reader);
//						tokenizer.nextToken();
//						while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//							System.out.println("line no: " + tokenizer.lineno());
//							String countryID = tokenizer.sval;
//							tokenizer.nextToken();
//							String regionID = tokenizer.sval;
//							tokenizer.nextToken();
//							String languageID = tokenizer.sval;
//							tokenizer.nextToken();
//							String regionName = tokenizer.sval;
//							System.out.println(" countryID=\""+countryID+"\" regionID=\""+regionID+"\" regionName=\""+regionName+"\"");
//	
//							if (tokenizer.lineno() != 1) {
//								Country country;
//								try {
//									country = (Country) pm.getObjectById(
//											CountryID.create(Organisation.ROOT_ORGANISATIONID, countryID));
//								} catch (JDOObjectNotFoundException x) {
//									throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": country with ID \""+countryID+"\" does not exist!");
//								}
//	
//								Region region;
//								try {
//									region = (Region) pm.getObjectById(
//											RegionID.create(
//													Organisation.ROOT_ORGANISATIONID, countryID,
//													Organisation.ROOT_ORGANISATIONID, regionID));
//								} catch (JDOObjectNotFoundException x) {
//									region = new Region(Organisation.ROOT_ORGANISATIONID, regionID, country);
//								}
//								region.getName().setText(languageID, regionName);
//	
//								if (!JDOHelper.isPersistent(region))
//									pm.makePersistent(region);
//							}
//	
//							while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//								tokenizer.nextToken();
//							tokenizer.nextToken();
//						}
//					} finally {
//						reader.close();
//					}
//				} finally {
//					in.close();
//				}
//
//			} finally {
//				pm.close();
//			}
//		} catch (RuntimeException x) {
//			throw x;
//		} catch (ModuleException x) {
//			throw x;
//		} catch (Exception x) {
//			throw new ModuleException(x);
//		}
//	}
//
//
//	/**
//	 * @ejb.interface-method view-type="local"
//	 * @ejb.transaction type = "RequiresNew"
//	 * @ejb.permission role-name="_Guest_"
//	 */
//	public void _storeDistrictRecord(
//			String file, int lineno, boolean searchCityByName,
//			String cityIDStr, String languageID, 
//			String countryID, String regionID, String cityName,
//			String districtName, long cityID, double latitude, double longitude,
//			Set zips)
//		throws ModuleException
//	{
//		PersistenceManager pm = getPersistenceManager();
//		try {
//			Region region;
//			try {
//				region = (Region) pm.getObjectById(
//						RegionID.create(
//								Organisation.ROOT_ORGANISATIONID, countryID,
//								Organisation.ROOT_ORGANISATIONID, regionID));
//			} catch (JDOObjectNotFoundException x) {
//				throw new RuntimeException("CSV \""+file+"\", line "+lineno+": region with countyID=\""+countryID+"\" regionID=\""+regionID+"\" does not exist!");
//			}
//			
//			City city = null;
//			if (searchCityByName) {
//				// find the city that matches the name
//				Query query = pm.newQuery(City.class);
//				query.declareImports("import java.lang.String");
//				query.declareVariables("District district");
//				query.declareParameters("String languageID, String cityName");
//				query.setFilter("this.name.names.containsEntry(languageID, cityName)");
//				Collection c = (Collection)query.execute(languageID, cityName);
//				if (!c.isEmpty()) 
//					city = (City)c.iterator().next();
//				else {
//					query.setFilter("this.name.names.containsValue(cityName)");
//					c = (Collection)query.execute(languageID, cityName);
//
//					if (!c.isEmpty())
//						city = (City)c.iterator().next();
//				}
//			} // if (mode == 1) {
//
//			if (city == null) {
//				try {
//					city = (City)pm.getObjectById(CityID.create(Organisation.ROOT_ORGANISATIONID, cityID));
//				} catch (JDOObjectNotFoundException x) {
//					city = new City(Organisation.ROOT_ORGANISATIONID, cityID, region);
//				}
//				city.getName().setText(languageID, cityName);
//
//				if (!JDOHelper.isPersistent(city))
//					pm.makePersistent(city);
//			}
//
//			District district;
//			try {
//				district = (District)pm.getObjectById(DistrictID.create(Organisation.ROOT_ORGANISATIONID, cityID));
//			} catch (JDOObjectNotFoundException x) {
//				district = new District(Organisation.ROOT_ORGANISATIONID, cityID, city);
//			}
//			district.setName(districtName);
//			district.setLatitude(latitude);
//			district.setLongitude(longitude);
//			for (Iterator it = zips.iterator(); it.hasNext(); ) {
//				String zip = (String)it.next();
//				district.addZip(zip);
//			}
//			
//			if (!JDOHelper.isPersistent(district))
//				pm.makePersistent(district);
//
//			
//		} finally {
//			pm.close();
//		}
//	}

}
