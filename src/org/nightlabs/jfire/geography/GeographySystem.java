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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.util.FulltextMap;
import org.nightlabs.util.Utils;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class GeographySystem // implements Serializable
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(GeographySystem.class);

	/**
	 * @param initialContextProperties The properties needed to create a JNDI initial context
	 *		in order to access the server.
	 * @throws NamingException 
	 */
	public static GeographySystem createSharedInstance(Hashtable initialContextProperties)
	throws NamingException
	{
		if (_sharedInstance != null)
			return _sharedInstance;

		InitialContext ctx = new InitialContext(initialContextProperties);
		try {
			return createSharedInstance(ctx);
		} finally {
			ctx.close();
		}
	}

	public static GeographySystem createSharedInstance(InitialContext initialContext)
	{
		if (_sharedInstance == null) {
			_sharedInstance = new GeographySystem(Organisation.getRootOrganisationID(initialContext));
		}
		return _sharedInstance;
	}

	private static GeographySystem _sharedInstance = null;
	public static GeographySystem sharedInstance()
	{
		if (_sharedInstance == null)
			throw new IllegalStateException("shared instance does not exist! Use createSharedInstane(...) first!");

		return _sharedInstance;
	}

	public static boolean isSharedInstanceExisting() {
		return _sharedInstance != null;
	}

	public static final int FIND_MODE_BEGINS_WITH = FulltextMap.FIND_MODE_BEGINS_WITH;
	public static final int FIND_MODE_CONTAINS = FulltextMap.FIND_MODE_CONTAINS;
	public static final int FIND_MODE_ENDS_WITH = FulltextMap.FIND_MODE_ENDS_WITH;

	/**
	 * key: String countryID <br/>
	 * value: Country country
	 */
	protected Map<String, Country> countries = new HashMap<String, Country>();

	/**
	 * key: String regionPK <br/>
	 * value: Region region
	 */
	protected Map<String, Region> regions = new HashMap<String, Region>();

	/**
	 * key: String cityPK <br/>
	 * value: City city
	 */
	protected Map<String, City> cities = new HashMap<String, City>();

	/**
	 * key: String districtPK <br/>
	 * value: District district
	 */
	protected Map<String, District> districts = new HashMap<String, District>();

	/**
	 * key: String locationPK<br/>
	 * value: Location location
	 */
	protected Map<String, Location> locations = new HashMap<String, Location>();


	protected static String[] csvLine2Fields(String line)
	{
		StringTokenizer tok = new StringTokenizer(line, ";", true);
		LinkedList<String> res = new LinkedList<String>();
		boolean lastWasSep = false;
		while (tok.hasMoreTokens()) {
			String f = tok.nextToken();
			if (";".equals(f)) {
				if (lastWasSep)
					res.add("");
				lastWasSep = true;
			}
			else {
				lastWasSep = false;
				res.add(f);
			}
		}
		if (lastWasSep)
			res.add("");

		return (String[]) Utils.collection2TypedArray(res, String.class);
	}

	public Collection<Country> getCountries()
	{
		needCountries();

		return Collections.unmodifiableCollection(countries.values());
	}

	/**
	 * key: String languageID (this is important for sorting)<br/>
	 * value: List countries
	 */
	private transient Map<String, List<Country>> countriesSortedByLanguageID = null;

	public List<Country> getCountriesSorted(final Locale locale)
	{
		if (countriesSortedByLanguageID == null)
			countriesSortedByLanguageID = new HashMap<String, List<Country>>();

		final String languageID = locale.getLanguage();
		List<Country> countriesSorted = countriesSortedByLanguageID.get(languageID);

		if (countriesSorted == null) {
			countriesSorted = new ArrayList<Country>(getCountries());
			Collections.sort(countriesSorted, getCountryComparator(locale));

			countriesSortedByLanguageID.put(languageID, countriesSorted);
		}

		return countriesSorted;
	}
	
	/**
	 * key: String countryID<br/>
	 * value: Map {<br/>
	 *		key: String languageID<br/>
	 *		value: List locations<br/>
	 * }
	 */
	private transient Map<String, Map<String, List<Location>>> locationsSortedByLanguageIDByCountryID = null;

	/**
	 * key: String countryID<br/>
	 * value: Map {<br/>
	 *		key: String languageID<br/>
	 *		value: List regions<br/>
	 * }
	 */
	private transient Map<String, Map<String, List<Region>>> regionsSortedByLanguageIDByCountryID = null;

	/**
	 * key: RegionID regionID<br/>
	 * value: Map {<br/>
	 *		key: String languageID<br/>
	 *		value: List cities<br/>
	 * }
	 */
	private transient Map<RegionID, Map<String, List<City>>> citiesSortedByLanguageIDByRegionID = null;

	public Country getCountry(CountryID countryID, boolean throwExceptionIfNotFound)
	{
		needCountries();

		Country res = (Country) countries.get(countryID.countryID);

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No Country registered with countryID=\""+countryID+"\"!!!");

		return res;
	}

	public Region getRegion(RegionID regionID, boolean throwExceptionIfNotFound)
	{
		needRegions(regionID.countryID);

		Region res = (Region) regions.get(Region.getPrimaryKey(regionID));

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No Region registered with regionID=\""+regionID+"\"!!!");

		return res;
	}

	public City getCity(CityID cityID, boolean throwExceptionIfNotFound)
	{
		needCities(cityID.countryID);

		City res = (City) cities.get(City.getPrimaryKey(cityID));

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No City registered with cityID=\""+cityID+"\"!!!");

		return res;
	}

	protected static final Collection EMPTY_COLLECTION = Collections.unmodifiableCollection(new LinkedList());

	/**
	 * @param countryID
	 * @param throwExceptionIfNotFound
	 * @return Returns an empty collection if <tt>throwExceptionIfNotFound == false</tt> and
	 *		no <tt>Country</tt> exists for the given <tt>countryID</tt>.
	 */
	public Collection<Region> getRegions(CountryID countryID, boolean throwExceptionIfNotFound)
	{
		Country country = getCountry(countryID, throwExceptionIfNotFound);
		if (country == null)
			return EMPTY_COLLECTION;

		return Collections.unmodifiableCollection(country.getRegions());
	}

	public List<Region> getRegionsSorted(final CountryID countryID, final Locale locale)
	{
		if (regionsSortedByLanguageIDByCountryID == null)
			regionsSortedByLanguageIDByCountryID = new HashMap<String, Map<String,List<Region>>>();

		Map<String,List<Region>> regionsSortedByLanguageID = regionsSortedByLanguageIDByCountryID.get(countryID.countryID);
		if (regionsSortedByLanguageID == null) {
			regionsSortedByLanguageID = new HashMap<String, List<Region>>();
			regionsSortedByLanguageIDByCountryID.put(countryID.countryID, regionsSortedByLanguageID);
		}

		final String languageID = locale.getLanguage();

		List<Region> regionsSorted = regionsSortedByLanguageID.get(languageID);

		if (regionsSorted == null) {
			regionsSorted = new ArrayList<Region>(getRegions(countryID, false));
			Collections.sort(regionsSorted, getRegionComparator(locale));

			regionsSortedByLanguageID.put(languageID, regionsSorted);
		}

		return regionsSorted;
	}

	public Location getLocation(LocationID locationID, boolean throwExceptionIfNotFound)
	{
		needLocations(locationID.countryID);

		Location res = (Location) locations.get(Location.getPrimaryKey(locationID));

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No Location registered with locationID=\""+locationID+"\"!!!");

		return res;
	}

	/**
	 * @param countryID
	 * @param throwExceptionIfNotFound
	 * @return Returns an empty collection if <tt>throwExceptionIfNotFound == false</tt> and
	 *		no <tt>Country</tt> exists for the given <tt>countryID</tt>.
	 */
	public Collection<Location> getLocations(CityID cityID, boolean throwExceptionIfNotFound)
	{
		City city = getCity(cityID, throwExceptionIfNotFound);
		if (city == null)
			return EMPTY_COLLECTION;

		return Collections.unmodifiableCollection(city.getLocations());
	}

	public List<Location> getLocationsSorted(final CityID cityID, final Locale locale)
	{
		if (locationsSortedByLanguageIDByCountryID == null)
			locationsSortedByLanguageIDByCountryID = new HashMap<String, Map<String,List<Location>>>();

		Map<String,List<Location>> locationsSortedByLanguageID = locationsSortedByLanguageIDByCountryID.get(cityID.countryID);
		if (locationsSortedByLanguageID == null) {
			locationsSortedByLanguageID = new HashMap<String, List<Location>>();
			locationsSortedByLanguageIDByCountryID.put(cityID.countryID, locationsSortedByLanguageID);
		}

		final String languageID = locale.getLanguage();

		List<Location> locationsSorted = locationsSortedByLanguageID.get(languageID);

		if (locationsSorted == null) {
			locationsSorted = new ArrayList<Location>(getLocations(cityID, false));
			Collections.sort(locationsSorted, getLocationComparator(locale));

			locationsSortedByLanguageID.put(languageID, locationsSorted);
		}

		return locationsSorted;
	}

	/**
	 * @param regionID
	 * @param throwExceptionIfNotFound
	 * @return Returns an empty collection if <tt>throwExceptionIfNotFound == false</tt> and
	 *		no <tt>Country</tt> exists for the given <tt>countryID</tt>.
	 */
	public Collection<City> getCities(RegionID regionID, boolean throwExceptionIfNotFound)
	{
		needCities(regionID.countryID);

		Region region = getRegion(regionID, throwExceptionIfNotFound);
		if (region == null)
			return EMPTY_COLLECTION;

		return Collections.unmodifiableCollection(region.getCities());
	}

	protected Comparator<Country> getCountryComparator(final Locale locale)
	{
		return new Comparator<Country>() {
			private Collator collator = Collator.getInstance(locale);
			private String languageID = locale.getLanguage();

			public int compare(Country c0, Country c1)
			{
				String n0 = c0.getName().getText(languageID);
				String n1 = c1.getName().getText(languageID);

				return collator.compare(n0, n1);
			}
		};
	}

	protected Comparator getRegionComparator(final Locale locale)
	{
		return new Comparator() {
			private Collator collator = Collator.getInstance(locale);
			private String languageID = locale.getLanguage();

			public int compare(Object obj0, Object obj1)
			{
				Region r0 = (Region)obj0;
				Region r1 = (Region)obj1;

				String n0 = r0.getName().getText(languageID);
				String n1 = r1.getName().getText(languageID);

				return collator.compare(n0, n1);
			}
		};
	}

	protected Comparator getCityComparator(final Locale locale)
	{
		return new Comparator() {
			private Collator collator = Collator.getInstance(locale);
			private String languageID = locale.getLanguage();

			public int compare(Object obj0, Object obj1)
			{
				City c0 = (City)obj0;
				City c1 = (City)obj1;

				String n0 = c0.getName().getText(languageID);
				String n1 = c1.getName().getText(languageID);

				return collator.compare(n0, n1);
			}
		};
	}

	protected Comparator getLocationComparator(final Locale locale)
	{
		return new Comparator() {
			private Collator collator = Collator.getInstance(locale);
			private String languageID = locale.getLanguage();

			public int compare(Object obj0, Object obj1)
			{
				Location l0 = (Location)obj0;
				Location l1 = (Location)obj1;

				String n0 = l0.getName().getText(languageID);
				String n1 = l1.getName().getText(languageID);

				return collator.compare(n0, n1);
			}
		};
	}

	public List getCitiesSorted(final RegionID regionID, Locale locale)
	{
		if (citiesSortedByLanguageIDByRegionID == null)
			citiesSortedByLanguageIDByRegionID = new HashMap<RegionID, Map<String, List<City>>>();

		Map<String, List<City>> citiesSortedByLanguageID = citiesSortedByLanguageIDByRegionID.get(regionID);
		if (citiesSortedByLanguageID == null) {
			citiesSortedByLanguageID = new HashMap<String, List<City>>();
			citiesSortedByLanguageIDByRegionID.put(regionID, citiesSortedByLanguageID);
		}

		String languageID = locale.getLanguage();

		List<City> citiesSorted = citiesSortedByLanguageID.get(languageID);

		if (citiesSorted == null) {
			citiesSorted = new ArrayList<City>(getCities(regionID, false));
			Collections.sort(citiesSorted, getCityComparator(locale));

			citiesSortedByLanguageID.put(languageID, citiesSorted);
		}

		return citiesSorted;
	}

	/**
	 * key: String languageID<br/>
	 * value: FulltextMap countriesByCountryName {<br/>
	 *		key: String countryName
	 *		value: List countries (instances of {@link Country}
	 * }
	 */
	protected Map<String, FulltextMap> countriesByCountryNameByLanguageID = null;
	protected FulltextMap getCountriesByCountryNameMap(String languageID)
	{
		logger.debug("getCountriesByCountryNameMap(languageID=\""+languageID+"\") entered.");

		if (countriesByCountryNameByLanguageID == null)
			countriesByCountryNameByLanguageID = new HashMap<String, FulltextMap>();

		FulltextMap countriesByCountryName = countriesByCountryNameByLanguageID.get(languageID);
		if (countriesByCountryName == null) {
			countriesByCountryName = new FulltextMap(FULLTEXT_MAP_FEATURES);
			countriesByCountryNameByLanguageID.put(languageID, countriesByCountryName);
			for (Iterator it = getCountries().iterator(); it.hasNext(); ) {
				Country country = (Country) it.next();
				String countryName = country.getName().getText(languageID);
				List<Country> countryList = (List) countriesByCountryName.get(countryName);
				if (countryList == null) {
					countryList = new LinkedList<Country>();
					countriesByCountryName.put(countryName, countryList);
				}
				countryList.add(country);
			}
		}

		logger.debug("getCountriesByCountryNameMap(languageID=\""+languageID+"\") about to exit.");
		return countriesByCountryName;
	}

	/**
	 * key: CityID cityID<br/>
	 * value: Map {<br/>
	 *		key: String languageID<br/>
	 *		value: FulltextMap locationsByLocationName {<br/>
	 *			key: String locationName
	 *			value: List locations (instances of {@link Location}
	 *		}<br/>
	 * }
	 */
	protected transient Map locationsByLocationNameByLanguageIDByCityID = null;

	protected FulltextMap getLocationsByLocationNameMap(CityID cityID, String languageID)
	{
		logger.debug("getLocationsByLocationNameMap(cityID=\""+cityID+"\", languageID=\""+languageID+"\") entered.");

		needLocations(cityID.countryID);

		if (locationsByLocationNameByLanguageIDByCityID == null)
			locationsByLocationNameByLanguageIDByCityID = new HashMap();

		Map locationsByLocationNameByLanguageID = (Map) locationsByLocationNameByLanguageIDByCityID.get(cityID);
		if (locationsByLocationNameByLanguageID == null) {
			locationsByLocationNameByLanguageID = new HashMap();
			locationsByLocationNameByLanguageIDByCityID.put(cityID, locationsByLocationNameByLanguageID);
		}

		FulltextMap locationsByLocationName = (FulltextMap) locationsByLocationNameByLanguageID.get(languageID);
		if (locationsByLocationName == null) {
			locationsByLocationName = new FulltextMap(FULLTEXT_MAP_FEATURES);
			locationsByLocationNameByLanguageID.put(languageID, locationsByLocationName);

			City city = getCity(cityID, false);
			if (city != null) {
				for (Iterator it = city.getLocations().iterator(); it.hasNext(); ) {
					Location location = (Location) it.next();
					String locationName = location.getName().getText(languageID);
					List locationList = (List) locationsByLocationName.get(locationName);
					if (locationList == null) {
						locationList = new LinkedList();
						locationsByLocationName.put(locationName, locationList);
					}
					locationList.add(location);
				}
			}
		}

		logger.debug("getLocationsByLocationNameMap(cityID=\""+cityID+"\", languageID=\""+languageID+"\") about to exit.");
		return locationsByLocationName;
	}

	/**
	 * key: CountryID countryID<br/>
	 * value: Map {<br/>
	 *		key: String languageID<br/>
	 *		value: FulltextMap regionsByRegionName {<br/>
	 *			key: String regionName
	 *			value: List regions (instances of {@link Region}
	 *		}<br/>
	 * }
	 */
	protected transient Map regionsByRegionNameByLanguageIDByCountryID = null;

	protected FulltextMap getRegionsByRegionNameMap(CountryID countryID, String languageID)
	{
		logger.debug("getRegionsByRegionNameMap(countryID=\""+countryID+"\", languageID=\""+languageID+"\") entered.");

		needRegions(countryID.countryID);

		if (regionsByRegionNameByLanguageIDByCountryID == null)
			regionsByRegionNameByLanguageIDByCountryID = new HashMap();

		Map regionsByRegionNameByLanguageID = (Map) regionsByRegionNameByLanguageIDByCountryID.get(countryID);
		if (regionsByRegionNameByLanguageID == null) {
			regionsByRegionNameByLanguageID = new HashMap();
			regionsByRegionNameByLanguageIDByCountryID.put(countryID, regionsByRegionNameByLanguageID);
		}

		FulltextMap regionsByRegionName = (FulltextMap) regionsByRegionNameByLanguageID.get(languageID);
		if (regionsByRegionName == null) {
			regionsByRegionName = new FulltextMap(FULLTEXT_MAP_FEATURES);
			regionsByRegionNameByLanguageID.put(languageID, regionsByRegionName);

			Country country = getCountry(countryID, false);
			if (country != null) {
				for (Iterator it = country.getRegions().iterator(); it.hasNext(); ) {
					Region region = (Region) it.next();
					String regionName = region.getName().getText(languageID);
					List regionList = (List) regionsByRegionName.get(regionName);
					if (regionList == null) {
						regionList = new LinkedList();
						regionsByRegionName.put(regionName, regionList);
					}
					regionList.add(region);
				}
			}
		}

		logger.debug("getRegionsByRegionNameMap(countryID=\""+countryID+"\", languageID=\""+languageID+"\") about to exit.");
		return regionsByRegionName;
	}

	/**
	 * key: RegionID regionID<br/>
	 * value: FulltextMap districtsByZip {<br/>
	 *		key: String zip
	 *		value: List districts (instances of {@link District})
	 * }
	 */
	protected Map districtsByZipByRegionID = null;

	protected FulltextMap getDistrictsByZipMap(RegionID regionID)
	{
		logger.debug("getDistrictsByZipMap(regionID=\""+regionID+"\") entered.");

		needZips(regionID.countryID);

		if (districtsByZipByRegionID == null)
			districtsByZipByRegionID = new HashMap();

		FulltextMap districtsByZip = (FulltextMap) districtsByZipByRegionID.get(regionID);
		if (districtsByZip == null) {
			districtsByZip = new FulltextMap(FULLTEXT_MAP_FEATURES);
			districtsByZipByRegionID.put(regionID, districtsByZip);

			Region region = getRegion(regionID, false);
			if (region != null) {
				for (Iterator itC = region.getCities().iterator(); itC.hasNext(); ) {
					City city = (City) itC.next();
					for (Iterator itD = city.getDistricts().iterator(); itD.hasNext(); ) {
						District district = (District) itD.next();
						for (Iterator itZ = district.getZips().iterator(); itZ.hasNext(); ) {
							String zip = (String) itZ.next();

							List districtList = (List) districtsByZip.get(zip);
							if (districtList == null) {
								districtList = new LinkedList();
								districtsByZip.put(zip, districtList);
							}
							districtList.add(district);
						}
					}
				}
			}
		}

		logger.debug("getDistrictsByZipMap(regionID=\""+regionID+"\") about to exit.");
		return districtsByZip;
	}

	/**
	 * key: RegionID regionID<br/>
	 * value: Map {<br/>
	 *		key: String languageID<br/>
	 *		value: FulltextMap citiesByCityName {<br/>
	 *			key: String cityName<br/>
	 *			value: List cities (instances of {@link City})<br/>
	 *		}<br/>
	 * }
	 */
	protected Map citiesByCityNameByLanguageIDByRegionID = null;

	protected FulltextMap getCitiesByCityNameMap(RegionID regionID, String languageID)
	{
		logger.debug("getCitiesByCityNameMap(regionID=\""+regionID+"\", languageID=\""+languageID+"\") entered.");

		needCities(regionID.countryID);

		if (citiesByCityNameByLanguageIDByRegionID == null)
			citiesByCityNameByLanguageIDByRegionID = new HashMap();

		Map citiesByCityNameByLanguageID = (Map) citiesByCityNameByLanguageIDByRegionID.get(regionID);
		if (citiesByCityNameByLanguageID == null) {
			citiesByCityNameByLanguageID = new HashMap();
			citiesByCityNameByLanguageIDByRegionID.put(regionID, citiesByCityNameByLanguageID);
		}

		FulltextMap citiesByCityName = (FulltextMap) citiesByCityNameByLanguageID.get(languageID);
		if (citiesByCityName == null) {
			citiesByCityName = new FulltextMap(FULLTEXT_MAP_FEATURES);
			citiesByCityNameByLanguageID.put(languageID, citiesByCityName);

			Region region = getRegion(regionID, false);
			if (region != null) {
				for (Iterator it = region.getCities().iterator(); it.hasNext(); ) {
					City city = (City) it.next();
					String cityName = city.getName().getText(languageID);
					List cityList = (List) citiesByCityName.get(cityName);
					if (cityList == null) {
						cityList = new LinkedList();
						citiesByCityName.put(cityName, cityList);
					}
					cityList.add(city);
				}
			}
		}

		logger.debug("getCitiesByCityNameMap(regionID=\""+regionID+"\", languageID=\""+languageID+"\") about to exit.");
		return citiesByCityName;
	}

	public List findCountriesByCountryNameSorted(String countryNamePart, Locale locale, int findMode)
	{
		if ("".equals(countryNamePart))
			return getCountriesSorted(locale);

		List countriesSorted = (List) findCountriesByCountryName(countryNamePart, locale.getLanguage(), findMode);
		Collections.sort(countriesSorted, getCountryComparator(locale));
		return countriesSorted;
	}

	public Collection findCountriesByCountryName(String countryNamePart, Locale locale, int findMode)
	{
		return findCountriesByCountryName(countryNamePart, locale.getLanguage(), findMode);
	}

	protected Collection findCountriesByCountryName(String countryNamePart, String languageID, int findMode)
	{
		if ("".equals(countryNamePart))
			return getCountries();

		Collection res = new ArrayList();
		FulltextMap countriesByCountryName = getCountriesByCountryNameMap(languageID);
		for (Iterator it = countriesByCountryName.find(countryNamePart, findMode).iterator(); it.hasNext(); ) {
			List countries = (List) it.next();
			res.addAll(countries);
		}
		return res;
	}

	public Collection findRegionsByRegionName(CountryID countryID, String regionNamePart, Locale locale, int findMode)
	{
		return findRegionsByRegionName(countryID, regionNamePart, locale.getLanguage(), findMode);
	}

	protected Collection findRegionsByRegionName(CountryID countryID, String regionNamePart, String languageID, int findMode)
	{
		if ("".equals(regionNamePart))
			return getRegions(countryID, false);

		Collection res = new ArrayList();
		FulltextMap regionNames2regions = getRegionsByRegionNameMap(countryID, languageID);
		for (Iterator it = regionNames2regions.find(regionNamePart, findMode).iterator(); it.hasNext(); ) {
			List regions = (List) it.next();
			res.addAll(regions);
		}
		return res;
	}

	public List findRegionsByRegionNameSorted(CountryID countryID, String regionNamePart, Locale locale, int findMode)
	{
		if ("".equals(regionNamePart))
			return getRegionsSorted(countryID, locale);

		List regionsSorted = (List) findRegionsByRegionName(countryID, regionNamePart, locale.getLanguage(), findMode);
		Collections.sort(regionsSorted, getRegionComparator(locale));
		return regionsSorted;
	}

	public Collection findCitiesByZip(RegionID regionID, String zipPart, int findMode)
	{
		if ("".equals(zipPart))
			return getCities(regionID, false);

		// key: String cityPK
		// value: City city
		Map cities = new HashMap();
		FulltextMap zips2districts = getDistrictsByZipMap(regionID);
		for (Iterator itL = zips2districts.find(zipPart, findMode).iterator(); itL.hasNext(); ) {
			List districts = (List) itL.next();
			for (Iterator itD = districts.iterator(); itD.hasNext(); ) {
				District district = (District) itD.next();
				cities.put(district.getCity().getPrimaryKey(), district.getCity());
			}
		}
		return cities.values();
	}

	public List findCitiesByZipSorted(RegionID regionID, String zipPart, Locale locale, int findMode)
	{
		if ("".equals(zipPart))
			return getCitiesSorted(regionID, locale);

		List citiesSorted = new ArrayList(findCitiesByZip(regionID, zipPart, findMode));
		Collections.sort(citiesSorted, getCityComparator(locale));
		return citiesSorted;
	}

	public Collection findCitiesByCityName(RegionID regionID, String cityNamePart, Locale locale, int findMode)
	{
		return findCitiesByCityName(regionID, cityNamePart, locale.getLanguage(), findMode);
	}

	protected Collection findCitiesByCityName(RegionID regionID, String cityNamePart, String languageID, int findMode)
	{
		if ("".equals(cityNamePart))
			return getCities(regionID, false);

		Collection res = new ArrayList();
		FulltextMap cityNames2cities = getCitiesByCityNameMap(regionID, languageID); // (FulltextMap) cityNames2citiesByLanguageID.get(languageID);
		for (Iterator it = cityNames2cities.find(cityNamePart, findMode).iterator(); it.hasNext(); ) {
			List cities = (List) it.next();
			res.addAll(cities);
		}
		return res;
	}

	public List findCitiesByCityNameSorted(RegionID regionID, String cityNamePart, Locale locale, int findMode)
	{
		if ("".equals(cityNamePart))
			return getCitiesSorted(regionID, locale);

		List citiesSorted = (List) findCitiesByCityName(regionID, cityNamePart, locale.getLanguage(), findMode);
		Collections.sort(citiesSorted, getCityComparator(locale));
		return citiesSorted;
	}


	public Collection findLocationsByLocationName(CityID cityID, String locationNamePart, Locale locale, int findMode)
	{
		return findLocationsByLocationName(cityID, locationNamePart, locale.getLanguage(), findMode);
	}

	protected Collection findLocationsByLocationName(CityID cityID, String locationNamePart, String languageID, int findMode)
	{
		if ("".equals(locationNamePart))
			return getLocations(cityID, false);

		Collection res = new ArrayList();
		FulltextMap locationNames2locations = getLocationsByLocationNameMap(cityID, languageID); // (FulltextMap) locationNames2locationsByLanguageID.get(languageID);
		for (Iterator it = locationNames2locations.find(locationNamePart, findMode).iterator(); it.hasNext(); ) {
			List locations = (List) it.next();
			res.addAll(locations);
		}
		return res;
	}

	public List findLocationsByLocationNameSorted(CityID cityID, String locationNamePart, Locale locale, int findMode)
	{
		if ("".equals(locationNamePart))
			return getLocationsSorted(cityID, locale);

		List locationsSorted = (List) findLocationsByLocationName(cityID, locationNamePart, locale.getLanguage(), findMode);
		Collections.sort(locationsSorted, getLocationComparator(locale));
		return locationsSorted;
	}



//	public static void main(String[] args)
//	{
//		GeographySystem gc = new GeographySystem();
//		try {
//			long startDT = System.currentTimeMillis();
//			for (Iterator it = gc.getCountries().iterator(); it.hasNext(); ) {
//				Country country = (Country) it.next();
//				gc.needZips(country.getCountryID());
//			}
//			long stopDT = System.currentTimeMillis();
//			System.out.println("Loading csv files took "+(stopDT - startDT)+" msec.");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	private String rootOrganisationID;

	public GeographySystem(String rootOrganisationID)
	{
		if (rootOrganisationID == null)
			throw new NullPointerException("rootOrganisationID");

		this.rootOrganisationID = rootOrganisationID;
	}

	protected static final int FULLTEXT_MAP_FEATURES =
			FulltextMap.FEATURE_BEGINS_WITH | FulltextMap.FEATURE_CONTAINS | FulltextMap.FEATURE_ENDS_WITH;
//	protected static final int FULLTEXT_MAP_FIND_MODE = FulltextMap.FIND_MODE_CONTAINS;

	public static final String IMPORTCHARSET = "UTF-8";


	protected boolean loadedCountries = false;

	/**
	 * This method loads the countries, if they're not yet loaded. It uses the file
	 * "resource/Data-Country.csv" relative to this class. Additionally, it adds all
	 * the countries from {@link Locale} that are missing in the csv file.
	 */
	protected synchronized void needCountries()
	{
		if (loadedCountries)
			return;

		int row = 0;
		int countryCount = 0;
		int countryLangCount = 0;

		try {
//			LOGGER.info("Loading countries from "+Locale.class.getName()+"...");
//			String[] countryIDs = Locale.getISOCountries();
//			String[] languageIDs = Locale.getISOLanguages();
//			String defaultLanguageID = Locale.getDefault().getLanguage();
//
//			// make sure, the default languageID is the first - we ignore a subsequent duplicate
//			String[] l = new String[languageIDs.length + 1];
//			System.arraycopy(languageIDs, 0, l, 1, languageIDs.length);
//			l[0] = defaultLanguageID;
//			languageIDs = l;
//
////			Locale[] locales = Locale.getAvailableLocales();
//			for (int i = 0; i < countryIDs.length; ++i) {
////				Locale locale = locales[i];
////				String countryID = locale.getCountry();
////				String languageID = locale.getLanguage();
//				String countryID = countryIDs[i];
//
//				for (int m = 0; m < languageIDs.length; ++m) {
//					String languageID = languageIDs[m];
//					if (m != 0 && defaultLanguageID.equals(languageID)) // if the default language is not the first, we'll ignore it (it's a duplicate)
//						continue;
//
//					Locale locale = new Locale(languageID, countryID);
//
////					if (LOGGER.isDebugEnabled())
////						LOGGER.debug("Processing Locale: " + languageID + "_" + countryID + ": " + locale.getDisplayName());
//
//					if ("".equals(countryID)) {
////						if (LOGGER.isDebugEnabled())
////							LOGGER.debug("Ignoring Locale because of missing country: " + languageID + "_" + countryID);
//
//						continue;
//					}
//
//					Country country = (Country) countries.get(countryID);
//					if (country == null) {
//						country = new Country(this, countryID);
//						countries.put(countryID, country);
//						++countryCount;
//					}
//					String countryName = locale.getDisplayCountry(locale);
//					if (defaultLanguageID.equals(languageID) || !country.getName().getText(defaultLanguageID).equals(countryName)) {
//						country.getName().setText(languageID, countryName);
//						++countryLangCount;
//					}
//				}
//			}
//			LOGGER.info("Added "+countryCount+" new  countries & "+countryLangCount+" names from "+Locale.class.getName()+".");
//
//			countryCount = 0;
//			countryLangCount = 0;

			String file = "resource/Data-Country.csv";
			logger.info("Loading countries from \""+file+"\"...");
			InputStream in = Geography.class.getResourceAsStream(file);
			if (in == null) {
				logger.warn("File \"" + file + "\" does not exist!");
				return;
			}
			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
	        while ((line = reader.readLine()) != null) {
	        	++row;
	
	        	if ("".equals(line) || line.startsWith("#"))
	        		continue;
	
	        	String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

	        	if (row == 1)
	        		continue; // 1st line is header.

						String countryID = fields[0];
						String languageID = fields[1];
						String countryName = fields[2];

	        	if ("".equals(countryID))
							countryID = null;

						if (countryID != null) {
							if (!ObjectIDUtil.isValidIDString(countryID)) {
								logger.warn("CSV \""+file+"\", line "+row+": countryID \"" + countryID + "\" is not a valid ID String! Row ignored!");
								continue;
							}

							Country country = (Country) countries.get(countryID);
							if (country == null) {
								country = new Country(this, countryID);
								countries.put(country.getCountryID(), country);
								++countryCount;
							}

							country.getName().setText(languageID, countryName);
							++countryLangCount;
						}
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}
			logger.info("Read " +row+ " rows from \""+file+"\" and added " + countryCount + " new countries & " + countryLangCount + " names.");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		loadedCountries = true;
	}

	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the regions have already been loaded.
	 */
	protected Set loadedRegionsCountryIDSet = new HashSet();
	/**
	 * This method loads all the regions for the given country, if they have not yet been loaded.
	 *
	 * @param countryID
	 * @throws IOException
	 */
	protected synchronized void needRegions(String countryID)
	{
		if (loadedRegionsCountryIDSet.contains(countryID))
			return;

		needCountries();

		Country country = (Country) countries.get(countryID);
		if (country == null) {
			logger.warn("Country with ID \""+countryID+"\" does not exist! Cannot load regions of this country.");
			return;
		}

		int row = 0;
		int regionCount = 0;
		int regionLangCount = 0;

		try {
			logger.info("Loading regions of country \""+countryID+"\"...");
			String file = "resource/Data-Region-" + countryID + ".csv";
			InputStream in = Geography.class.getResourceAsStream(file);
			if (in == null) {
				logger.warn("File \"" + file + "\" does not exist!");
				return;
			}
			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
	        while ((line = reader.readLine()) != null) {
	        	++row;
	
	        	if ("".equals(line) || line.startsWith("#"))
	        		continue;
	
	        	String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

	        	if (fields.length != 4) {
	        		logger.warn(file + ": Invalid number of fields in row "+row+"! Row ignored.");
	        		continue;
	        	}
	
	        	if (row == 1)
	        		continue; // 1st line is header.

						String fcountryID = fields[0];
						String regionID = fields[1];
						String languageID = fields[2];
						String regionName = fields[3];

						if (!countryID.equals(fcountryID)) {
							logger.warn(file + ": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}

						if ("".equals(regionID))
							regionID = countryID;

						if (!ObjectIDUtil.isValidIDString(regionID)) {
							logger.warn("CSV \""+file+"\", line "+row+": regionID \"" + regionID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						Region region = (Region) regions.get(
								Region.getPrimaryKey(countryID, rootOrganisationID, regionID));
						if (region == null) {
							region = new Region(this, rootOrganisationID, regionID, country);
							country.addRegion(region);
							regions.put(region.getPrimaryKey(), region);
							++regionCount;
						}
						if (!"".equals(regionName)) {
							region.getName().setText(languageID, regionName);
							++regionLangCount;
						}
					}
				} finally {
					reader.close();
					r.close();
				}
			} finally {
				in.close();
			}
			logger.info("Read " +row+ " rows and added " + regionCount + " new regions & " + regionLangCount + " names for country \"" + countryID + "\".");

			if (regionCount == 0) {
				logger.info("The country \"" + countryID + "\" does not have regions. Creating dummy region with regionID=\""+countryID+"\"!");
				String regionID = countryID;
				Region region = new Region(this, rootOrganisationID, regionID, country);
				region.getName().copyFrom(country.getName());
				country.addRegion(region);
				regions.put(region.getPrimaryKey(), region);
			}

		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		loadedRegionsCountryIDSet.add(countryID);
	}

	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the locations have already been loaded.
	 */
	protected Set loadedLocationsCountryIDSet = new HashSet();
	/**
	 * This method loads all the locations for the given country, if they have not yet been loaded.
	 *
	 * @param countryID
	 * @throws IOException
	 */
	protected synchronized void needLocations(String countryID)
	{
		if (loadedLocationsCountryIDSet.contains(countryID))
			return;

		needCities(countryID);

		int row = 0;
		int locationCount = 0;
		int locationLangCount = 0;

		try {
			logger.info("Loading locations of country \""+countryID+"\"...");

			String file = "resource/Data-Location-" + countryID + ".csv";
			InputStream in = Geography.class.getResourceAsStream(file);
			if (in == null) {
				logger.warn("File \"" + file + "\" does not exist!");
				return;
			}
			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
	        while ((line = reader.readLine()) != null) {
	        	++row;
	
	        	if ("".equals(line) || line.startsWith("#"))
	        		continue;

	        	String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

	        	if (fields.length != 6) {
	        		logger.warn(file + ": Invalid number of fields in row "+row+"! Row ignored.");
	        		continue;
	        	}

	        	if (row == 1)
	        		continue; // 1st line is header

	        	String fcountryID = fields[0];
						String locationID = fields[1];
						String cityID = fields[2];
						String districtID = fields[3];
						String languageID = fields[4];
						String locationName = fields[5];

						if (!countryID.equals(fcountryID)) {
							logger.warn(file + ": Row "+row+" declares the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}
	
						if (!ObjectIDUtil.isValidIDString(locationID)) {
							logger.warn("CSV \""+file+"\", line "+row+": locationID \"" + locationID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						String locationPK = Location.getPrimaryKey(countryID, rootOrganisationID, locationID);

						Country country = (Country) countries.get(countryID);
						if (country == null) {
							logger.warn("CSV \""+file+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}

						String cityPK = City.getPrimaryKey(countryID, rootOrganisationID, cityID);
						City city = cities.get(cityPK);
						if (city == null) {
							logger.warn("CSV \""+file+"\", line "+row+": City with PK \""+cityPK+"\" does not exist! Row ignored.");
							continue;
						}

						District district = null;
						if (!"".equals(districtID)) {
							String districtPK = District.getPrimaryKey(countryID, rootOrganisationID, districtID);
							district = districts.get(districtPK);
							if (district == null)
								logger.warn("CSV \""+file+"\", line "+row+": District with PK \""+districtPK+"\" does not exist! Will NOT assign a district to location \""+locationPK+"\"!");
						}

						Location location = (Location) locations.get(
								Location.getPrimaryKey(countryID, rootOrganisationID, locationID));
						if (location == null) {
							location = new Location(this, rootOrganisationID, locationID, city);
							city.addLocation(location);
							locations.put(location.getPrimaryKey(), location);
							++locationCount;
						}
						location.getName().setText(languageID, locationName);
						++locationLangCount;
	        }
	      } finally {
	 				reader.close();
	 				r.close();
	 			}
	 		} finally {
	 			in.close();
	 		}

	 		logger.info("Read " +row+ " rows and added " + locationCount + " new locations & " + locationLangCount + " names for country \"" + countryID + "\".");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		loadedLocationsCountryIDSet.add(countryID);
	}


	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the cities have already been loaded.
	 */
	protected Set loadedCitiesCountryIDSet = new HashSet();
	/**
	 * This method loads all the cities for the given country, if they have not yet been loaded.
	 *
	 * @param countryID
	 * @throws IOException
	 */
	protected synchronized void needCities(String countryID)
	{
		if (loadedCitiesCountryIDSet.contains(countryID))
			return;

		needRegions(countryID);

		int row = 0;
		int cityCount = 0;
		int cityLangCount = 0;

		try {
			logger.info("Loading cities of country \""+countryID+"\"...");

			String file = "resource/Data-City-" + countryID + ".csv";
			InputStream in = Geography.class.getResourceAsStream(file);
			if (in == null) {
				logger.warn("File \"" + file + "\" does not exist!");
				return;
			}
			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
	        while ((line = reader.readLine()) != null) {
	        	++row;
	
	        	if ("".equals(line) || line.startsWith("#"))
	        		continue;
	
	        	String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

	        	if (fields.length != 5) {
	        		logger.warn(file + ": Invalid number of fields in row "+row+"! Row ignored.");
	        		continue;
	        	}
	
	        	if (row == 1)
	        		continue; // 1st line is header
	
	        	String fcountryID = fields[0];
						String cityID = fields[1];
						String regionID = fields[2];
						String languageID = fields[3];
						String cityName = fields[4];
	
						if (!countryID.equals(fcountryID)) {
							logger.warn(file + ": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}
	
						if (!ObjectIDUtil.isValidIDString(cityID)) {
							logger.warn("CSV \""+file+"\", line "+row+": cityID \"" + cityID + "\" is not a valid ID String! Row ignored!");
							continue;
						}
	
						Country country = (Country) countries.get(countryID);
						if (country == null) {
							logger.warn("CSV \""+file+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}

						if ("".equals(regionID))
							regionID = countryID;

						String regionPK = Region.getPrimaryKey(countryID, rootOrganisationID, regionID);
						Region region = (Region) regions.get(regionPK);
						if (region == null) {
							logger.warn("CSV \""+file+"\", line "+row+": Region with PK \""+regionPK+"\" does not exist! Row ignored.");
							continue;
						}
	
						City city = (City) cities.get(
								City.getPrimaryKey(countryID, rootOrganisationID, cityID));
						if (city == null) {
							city = new City(this, rootOrganisationID, cityID, region);
							region.addCity(city);
							cities.put(city.getPrimaryKey(), city);
							++cityCount;
						}
						city.getName().setText(languageID, cityName);
						++cityLangCount;
	        }
	      } finally {
	 				reader.close();
	 				r.close();
	 			}
	 		} finally {
	 			in.close();
	 		}

	 		logger.info("Read " +row+ " rows and added " + cityCount + " new cities & " + cityLangCount + " names for country \"" + countryID + "\".");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		loadedCitiesCountryIDSet.add(countryID);
	}


	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the districts have already been loaded.
	 */
	protected Set loadedDistrictsCountryIDSet = new HashSet();
	/**
	 * This method loads all the cities for the given country, if they have not yet been loaded.
	 *
	 * @param countryID
	 * @throws IOException
	 */
	protected synchronized void needDistricts(String countryID)
	{
		if (loadedDistrictsCountryIDSet.contains(countryID))
			return;

		needCities(countryID);

		int row = 0;
		int districtCount = 0;
		int districtLangCount = 0;

		try {
			logger.info("Loading districts of country \""+countryID+"\"...");

			String file = "resource/Data-District-" + countryID + ".csv";
			InputStream in = Geography.class.getResourceAsStream(file);
			if (in == null) {
				logger.warn("File \"" + file + "\" does not exist!");
				return;
			}
			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
	        while ((line = reader.readLine()) != null) {
	        	++row;
	
	        	if ("".equals(line) || line.startsWith("#"))
	        		continue;
	
	        	String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

	        	if (fields.length != 7) {
	        		logger.warn(file + ": Invalid number of fields ("+fields.length+") in row "+row+"! Row ignored.");
	        		continue;
	        	}
	
	        	if (row == 1)
	        		continue; // 1st line is header
	
	        	String fcountryID = fields[0];
						String cityID = fields[1];
						String districtID = fields[2];
	// TODO					String languageID = fields[3];
						String districtName = fields[4];
						String latitudeStr = fields[5];
						String longitudeStr = fields[6];
	
						if (!countryID.equals(fcountryID)) {
							logger.warn(file + ": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}
	
						Country country = (Country) countries.get(countryID);
						if (country == null) {
							logger.warn("CSV \""+file+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}
	
						if ("".equals(districtID))
							districtID = cityID;
	
						if (!ObjectIDUtil.isValidIDString(districtID)) {
							logger.warn("CSV \""+file+"\", line "+row+": districtID \"" + districtID + "\" is not a valid ID String! Row ignored!");
							continue;
						}
	
						String cityPK = City.getPrimaryKey(countryID, rootOrganisationID, cityID);
						City city = (City) cities.get(cityPK);
						if (city == null) {
							logger.warn("CSV \""+file+"\", line "+row+": City with PK \""+cityPK+"\" does not exist! Row ignored.");
							continue;
						}
	
						if ("".equals(districtName))
							districtName = city.getName().getText(Locale.getDefault().getLanguage());
	
						double latitude = 0;
						try {
							latitude = Double.parseDouble(latitudeStr);
						} catch (NumberFormatException x) {
							logger.warn("CSV \""+file+"\", line "+row+": latitude \"" + latitudeStr + "\" is not a double! Setting latitude = 0.");
						}
	
						double longitude = 0;
						try {
							longitude = Double.parseDouble(longitudeStr);
						} catch (NumberFormatException x) {
							logger.warn("CSV \""+file+"\", line "+row+": longitude \"" + longitudeStr + "\" is not a double! Setting longitude = 0.");
						}


						District district = (District) districts.get(
								District.getPrimaryKey(countryID, rootOrganisationID, districtID));
						if (district == null) {
							district = new District(this, rootOrganisationID, districtID, city);
							city.addDistrict(district);
							districts.put(district.getPrimaryKey(), district);
							++districtCount;
						}
	//					district.getName().setText(languageID, districtName);
						district.setName(districtName);
						district.setLatitude(latitude);
						district.setLongitude(longitude);
						++districtLangCount;
	        }
	      } finally {
	 				reader.close();
	 				r.close();
	 			}
	 		} finally {
	 			in.close();
	 		}

	 		logger.info("Read " +row+ " rows and added " + districtCount + " new districts & " + districtLangCount + " names for country \"" + countryID + "\".");

		} catch (Exception x) {
			throw new RuntimeException(x);
		}

		loadedDistrictsCountryIDSet.add(countryID);
	}


	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the zips have already been loaded.
	 */
	protected Set loadedZipsCountryIDSet = new HashSet();
	/**
	 * This method loads all the cities for the given country, if they have not yet been loaded.
	 *
	 * @param countryID
	 * @throws IOException
	 */
	protected synchronized void needZips(String countryID)
	{
		if (loadedZipsCountryIDSet.contains(countryID))
			return;

		needDistricts(countryID);
		
		int row = 0;
		int zipCount = 0;

		try {
			logger.info("Loading zips of country \""+countryID+"\"...");

			String file = "resource/Data-Zip-" + countryID + ".csv";
			InputStream in = Geography.class.getResourceAsStream(file);
			if (in == null) {
				logger.warn("File \"" + file + "\" does not exist!");
				return;
			}
			try {
				InputStreamReader r = new InputStreamReader(in, IMPORTCHARSET);
				BufferedReader reader = new BufferedReader(r);
				try {
					String line;
	        while ((line = reader.readLine()) != null) {
	        	++row;

	        	if ("".equals(line) || line.startsWith("#"))
	        		continue;

	        	String[] fields = csvLine2Fields(line); // line.replaceAll("\"", "").split(";");

	        	if (fields.length != 4) {
	        		logger.warn(file + ": Invalid number of fields in row "+row+"! Row ignored.");
	        		continue;
	        	}

	        	if (row == 1)
	        		continue; // 1st line is header

	        	String fcountryID = fields[0];
						String cityID = fields[1];
						String districtID = fields[2];
						String zip = fields[3];
	
						if (!countryID.equals(fcountryID)) {
							logger.warn(file + ": Row "+row+" does declare the wrong country! Should be \""+countryID+"\" but is \""+fcountryID+"\"! Row ignored.");
							continue;
						}
	
						if ("".equals(districtID))
							districtID = cityID;
	
						if ("".equals(zip)) {
							logger.warn("CSV \""+file+"\", line "+row+": zip field is empty! Row ignored!");
							continue;
						}
	
						Country country = (Country) countries.get(countryID);
						if (country == null) {
							logger.warn("CSV \""+file+"\", line "+row+": country with ID \""+countryID+"\" does not exist! Row ignored.");
							continue;
						}
	
						if (!ObjectIDUtil.isValidIDString(cityID)) {
							logger.warn("CSV \""+file+"\", line "+row+": cityID \"" + cityID + "\" is not a valid ID String! Row ignored!");
							continue;
						}

						String districtPK = District.getPrimaryKey(countryID, rootOrganisationID, districtID);
						District district = (District) districts.get(districtPK);
						if (district == null) {
							logger.warn("CSV \""+file+"\", line "+row+": District with PK \""+districtPK+"\" does not exist! Row ignored.");
							continue;
						}

						if (!district.getCity().getCityID().equals(cityID)) {
							String languageID = Locale.getDefault().getLanguage();
							City csvCity = (City) cities.get(City.getPrimaryKey(countryID, rootOrganisationID, cityID));
							String csvCityName = csvCity == null ? "{unknown city}" : csvCity.getName().getText(languageID);

							logger.warn("CSV \""+file+"\", line "+row+": District with PK \""+districtPK+"\" (named \""+district.getName()+"\") has cityID \""+district.getCity().getCityID()+"\" (named \""+district.getCity().getName().getText(languageID)+"\") but csv row declares cityID \""+cityID+"\" (named \"" + csvCityName + "\")! Will add zip \""+zip+"\" to district \""+districtPK+"\" anyway.");
						}
	
						district.addZip(zip);
						++zipCount;
	        }
	      } finally {
	 				reader.close();
	 				r.close();
	 			}
	 		} finally {
	 			in.close();
	 		}

	 		logger.info("Read " +row+ " rows and added " + zipCount + " new zips for country \"" + countryID + "\".");
		} catch (Exception x) {
			throw new RuntimeException(x);
		}

 		loadedZipsCountryIDSet.add(countryID);
	}
}
