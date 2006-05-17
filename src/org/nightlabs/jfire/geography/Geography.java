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

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;

import org.nightlabs.jfire.organisation.LocalOrganisation;

/**
 * This is a JDO-singleton - means there exists exactly one instance in each datastore.
 * You obtain this instance by {@link #getGeography(PersistenceManager)}. This method
 * creates it, if it does not yet exist.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable 
 *		identity-type = "datastore"
 *		detachable = "true"
 *		table = "JFireGeography_Geography"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class Geography implements Serializable
{
	public static final Logger LOGGER = Logger.getLogger(Geography.class);

//	/**
//	 * key: String countryPK<br/>
//	 * value: Country country
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="Country"
//	 *
//	 * @jdo.join
//	 */
//	protected Map countries = new HashMap();

	/**
	 * @param pm The <tt>PersistenceManager</tt> used to access the datastore.
	 *
	 * @return Returns the one <tt>Geography</tt> instance that exists in every organisation-datastore.
	 */
	public static Geography getGeography(PersistenceManager pm)
	{
		Iterator it = pm.getExtent(Geography.class).iterator();
		if (it.hasNext())
			return (Geography)it.next();

		Geography geography = new Geography();

		geography.organisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

		// we do not store all the data, but store only those countries/regions/cities... that are
		// used by the user.
//		GeographySystem geographyCache = new GeographySystem();
//		try {
//			geographyCache.loadDefaults();
//		} catch (IOException x) {
//			throw new RuntimeException(x);
//		}
//
//		geography.countries = geographyCache.countries;

		LOGGER.info("making geography persistent...");
		long startDT = System.currentTimeMillis();
		pm.makePersistent(geography);
		long stopDT = System.currentTimeMillis();
		LOGGER.info("makePersistent(geography) took " + (stopDT - startDT) + "msec.");
		return geography;
	}

	private String organisationID;

	public Geography() { }

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

//	private long nextCityID = 0;
//	private long nextDistrictID = 0;
//
//	protected synchronized long generateCityID()
//	{
//		long res = nextCityID;
//		nextCityID = res + 1;
//		return res;
//	}
//
//	protected synchronized long generateDistrictID()
//	{
//		long res = nextDistrictID;
//		nextDistrictID = res + 1;
//		return res;
//	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of Geography is currently not persistent! Cannot obtain PersistenceManager!");
		return pm;
	}

	public synchronized Country addCountry(Country country)
	{
		if (!JDOHelper.isPersistent(country))
			return (Country) getPersistenceManager().makePersistent(country);

		return country;
	}

	public Collection getCountries()
	{
		return (Collection) getPersistenceManager().newQuery(Country.class).execute();
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

	//public static final String IMPORTCHARSET = "ISO8859-1";

//	private void importDefaultData() throws IOException
//	{
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("Geography is not persistent! Can't import!");
//
//		// initialize the meta data
//		pm.getExtent(Country.class);
//		pm.getExtent(Region.class);
//		pm.getExtent(City.class);
//		pm.getExtent(District.class);
//
//		// import countries
//		String file = "resource/Country.csv";
//		InputStream in = Geography.class.getResourceAsStream(file);
//		try {
//			InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//			try {
//				StreamTokenizer tokenizer = getCSVTokenizer(reader);
//				tokenizer.nextToken();
//				while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//					System.out.println("line no: " + tokenizer.lineno());
//					String countryID = tokenizer.sval;
//					tokenizer.nextToken();
//					String languageID = tokenizer.sval;
//					tokenizer.nextToken();
//					String countryName = tokenizer.sval;
//					System.out.println(" countryID = \""+countryID+"\" countryName=\""+countryName+"\"");
//
//					if (tokenizer.lineno() != 1) {
//						Country country;
//						try {
//							country = (Country) pm.getObjectById(
//									CountryID.create(Organisation.ROOT_ORGANISATIONID, countryID));
//						} catch (JDOObjectNotFoundException x) {
//							country = new Country(Organisation.ROOT_ORGANISATIONID, countryID);
//						}
//						country.getName().setText(languageID, countryName);
//
//						if (!JDOHelper.isPersistent(country))
//							pm.makePersistent(country);
//					}
//
//					while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//						tokenizer.nextToken();
//					tokenizer.nextToken();
//				}
//			} finally {
//				reader.close();
//			}
//		} finally {
//			in.close();
//		}
//		
//		// import regions
//		file = "resource/Region.csv";
//		in = Geography.class.getResourceAsStream(file);
//		try {
//			InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//			try {
//				StreamTokenizer tokenizer = getCSVTokenizer(reader);
//				tokenizer.nextToken();
//				while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//					System.out.println("line no: " + tokenizer.lineno());
//					String countryID = tokenizer.sval;
//					tokenizer.nextToken();
//					String regionID = tokenizer.sval;
//					tokenizer.nextToken();
//					String languageID = tokenizer.sval;
//					tokenizer.nextToken();
//					String regionName = tokenizer.sval;
//					System.out.println(" countryID=\""+countryID+"\" regionID=\""+regionID+"\" regionName=\""+regionName+"\"");
//
//					if (tokenizer.lineno() != 1) {
//						Country country;
//						try {
//							country = (Country) pm.getObjectById(
//									CountryID.create(Organisation.ROOT_ORGANISATIONID, countryID));
//						} catch (JDOObjectNotFoundException x) {
//							throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": country with ID \""+countryID+"\" does not exist!");
//						}
//
//						Region region;
//						try {
//							region = (Region) pm.getObjectById(
//									RegionID.create(
//											Organisation.ROOT_ORGANISATIONID, countryID,
//											Organisation.ROOT_ORGANISATIONID, regionID));
//						} catch (JDOObjectNotFoundException x) {
//							region = new Region(Organisation.ROOT_ORGANISATIONID, regionID, country);
//						}
//						region.getName().setText(languageID, regionName);
//
//						if (!JDOHelper.isPersistent(region))
//							pm.makePersistent(region);
//					}
//
//					while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//						tokenizer.nextToken();
//					tokenizer.nextToken();
//				}
//			} finally {
//				reader.close();
//			}
//		} finally {
//			in.close();
//		}
//
//
//		// import cities and districts
//		Query queryFindCity = pm.newQuery(City.class);
//		queryFindCity.declareImports("import java.lang.String");
//		queryFindCity.declareParameters("String languageID, String cityName");
//		queryFindCity.setFilter("this.name.names.containsEntry(languageID, cityName)");
//
//		for (int mode = 0; mode <= 1; ++mode) {
//			file = "resource/City.csv";
//			in = Geography.class.getResourceAsStream(file);
//			try {
//				InputStreamReader reader = new InputStreamReader(in, IMPORTCHARSET);
//				try {
//					StreamTokenizer tokenizer = getCSVTokenizer(reader);
//					tokenizer.nextToken();
//					while (tokenizer.ttype != StreamTokenizer.TT_EOF) {
//						String cityIDStr = tokenizer.sval;
//						tokenizer.nextToken();
//						String languageID = tokenizer.sval;
//						tokenizer.nextToken();
//						String countryID = tokenizer.sval;
//						tokenizer.nextToken();
//						String regionID = tokenizer.sval;
//						tokenizer.nextToken();
//						String cityName = tokenizer.sval;
//						tokenizer.nextToken();
//						String districtName = tokenizer.sval;
//						tokenizer.nextToken();
//						String zipStr = tokenizer.sval;
//						tokenizer.nextToken();
//						String latitudeStr = tokenizer.sval;
//						tokenizer.nextToken();
//						String longitudeStr = tokenizer.sval;
//
//						System.out.println("lineno=\""+tokenizer.lineno()+"\" cityIDStr=\""+cityIDStr+"\" countryID=\""+countryID+"\" regionID=\""+regionID+"\" cityName=\""+cityName+"\" districtName=\""+districtName+"\" zipStr=\""+zipStr+"\" latitudeStr=\""+latitudeStr+"\" longitudeStr=\""+longitudeStr+"\"");
//
//						if (tokenizer.lineno() != 1 &&
//								((mode == 0 && "".equals(districtName)) || (mode == 1 && !"".equals(districtName))))
//						{
//							long cityID;
//							try {
//								cityID = Long.parseLong(cityIDStr);
//							} catch (NumberFormatException x) {
//								throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": cityID is not a long!", x);
//							}
//
//							double latitude;
//							try {
//								latitude = Double.parseDouble(latitudeStr);
//							} catch (NumberFormatException x) {
//								throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": latitude is not a double!", x);
//							}
//
//							double longitude;
//							try {
//								longitude = Double.parseDouble(longitudeStr);
//							} catch (NumberFormatException x) {
//								throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": longitude is not a double!", x);
//							}
//
//							Set zips = new HashSet();
//							StringTokenizer zipTokenizer = new StringTokenizer(zipStr, ",");
//							while (zipTokenizer.hasMoreTokens()) {
//								String zip = zipTokenizer.nextToken();
//								zips.add(zip);
//							}
//
//							Region region;
//							try {
//								region = (Region) pm.getObjectById(
//										RegionID.create(
//												Organisation.ROOT_ORGANISATIONID, countryID,
//												Organisation.ROOT_ORGANISATIONID, regionID));
//							} catch (JDOObjectNotFoundException x) {
//								throw new RuntimeException("CSV \""+file+"\", line "+tokenizer.lineno()+": region with countyID=\""+countryID+"\" regionID=\""+regionID+"\" does not exist!");
//							}
//							
//							City city = null;
//							if (mode == 1) {
//								// find the city that matches the name
//								Collection c = (Collection)queryFindCity.execute(languageID, cityName);
//								if (!c.isEmpty()) 
//									city = (City)c.iterator().next();
//								else {
//									queryFindCity.setFilter("this.name.names.containsValue(cityName)");
//									c = (Collection)queryFindCity.execute(languageID, cityName);
//
//									if (!c.isEmpty())
//										city = (City)c.iterator().next();
//								}
//							} // if (mode == 1) {
//
//							if (city == null) {
//								try {
//									city = (City)pm.getObjectById(CityID.create(Organisation.ROOT_ORGANISATIONID, cityID));
//								} catch (JDOObjectNotFoundException x) {
//									city = new City(Organisation.ROOT_ORGANISATIONID, cityID, region);
//								}
//								city.getName().setText(languageID, cityName);
//
//								if (!JDOHelper.isPersistent(city))
//									pm.makePersistent(city);
//							}
//
//							District district;
//							try {
//								district = (District)pm.getObjectById(DistrictID.create(Organisation.ROOT_ORGANISATIONID, cityID));
//							} catch (JDOObjectNotFoundException x) {
//								district = new District(Organisation.ROOT_ORGANISATIONID, cityID, city);
//							}
//							district.setName(districtName);
//							district.setLatitude(latitude);
//							district.setLongitude(longitude);
//							for (Iterator it = zips.iterator(); it.hasNext(); ) {
//								String zip = (String)it.next();
//								district.addZip(zip);
//							}
//							
//							if (!JDOHelper.isPersistent(district))
//								pm.makePersistent(district);
//						}
//
//						while (tokenizer.ttype != StreamTokenizer.TT_EOL)
//							tokenizer.nextToken();
//						tokenizer.nextToken();
//					}
//				} finally {
//					reader.close();
//				}
//			} finally {
//				in.close();
//			}
//		} // for (int mode = 0; mode <= 1; ++mode) {
//	}

}
