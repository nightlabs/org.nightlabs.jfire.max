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

import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import org.nightlabs.datastructure.FulltextMap;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;

/**
 * Geography data access class.
 * @author Marco Schulze - marco at nightlabs dot de
 * @author Marc Klinger - marc[at]nightlabs[dot]de
 */
public abstract class Geography
{
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(Geography.class);

	private static Map<String, Geography> organisationID2sharedInstance = new HashMap<String, Geography>();

	public static final String PROPERTY_KEY_GEOGRAPHY_CLASS = "org.nightlabs.jfire.geography.Geography";

	/**
	 * This method finds out the current organisation by consulting the {@link SecurityReflector}
	 * and looks up the shared instance for this organisation. If there is none existing, a new
	 * instance will be created based on the system property with the key {@link #PROPERTY_KEY_GEOGRAPHY_CLASS}.
	 *
	 * @return The shared instance of Geography.
	 */
	public static Geography sharedInstance()
	{
		String organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();

		Geography sharedInstance = null;
		createLocalVMSharedInstance:
			synchronized (organisationID2sharedInstance) {
				sharedInstance = organisationID2sharedInstance.get(organisationID);
				if (sharedInstance == null) {
					String className = System.getProperty(PROPERTY_KEY_GEOGRAPHY_CLASS);
					if (className == null)
						break createLocalVMSharedInstance;

					Class<?> clazz;
					try {
						clazz = Class.forName(className);
					} catch (ClassNotFoundException e) {
						throw new RuntimeException(e);
					}

					try {
						sharedInstance = (Geography) clazz.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					sharedInstance.setOrganisationID(organisationID);
					organisationID2sharedInstance.put(organisationID, sharedInstance);
				}
			} // synchronized (organisationID2sharedInstance) {

		if (sharedInstance == null)
			throw new IllegalStateException("Neither does a shared instance of Geography exist, nor is the property '" + PROPERTY_KEY_GEOGRAPHY_CLASS + "' set!");

		return sharedInstance;
	}

	public Geography()
	{
		InitialContext initialContext = SecurityReflector.createInitialContext();
		try {
			rootOrganisationID = Organisation.getRootOrganisationID(initialContext);
		} finally {
			try {
				initialContext.close();
			} catch (Exception e) {
				logger.error("Closing InitialContext failed!", e);
			}
		}
	}

	private String organisationID;

	public String getOrganisationID()
	{
		return organisationID;
	}

	private String rootOrganisationID;

	public String getRootOrganisationID()
	{
		return rootOrganisationID;
	}

	/**
	 * This method is called by {@link #sharedInstance()}, after a new instance of <code>Geography</code>
	 * was created and by {@link #setSharedInstance(Geography)}, if {@link #getOrganisationID()} returns <code>null</code>.
	 * <p>
	 * If this method is called, after an organisationID has already been set, an exception is thrown.
	 * </p>
	 *
	 * @param organisationID The organisationID to set.
	 */
	protected void setOrganisationID(String organisationID)
	{
		if (this.organisationID != null && !this.organisationID.equals(organisationID))
			throw new IllegalStateException("This Geography's organisationID is already initialized to the value \""+this.organisationID+"\" - cannot change it to \""+organisationID+"\"!!!");

		this.organisationID = organisationID;
	}

	protected static void setSharedInstance(Geography sharedInstance)
	{
		String organisationID = sharedInstance.getOrganisationID();
		if (organisationID == null)
			organisationID = SecurityReflector.getUserDescriptor().getOrganisationID();

		sharedInstance.setOrganisationID(organisationID);
		synchronized (organisationID2sharedInstance) {
			organisationID2sharedInstance.put(organisationID, sharedInstance);
		}
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


	public synchronized Collection<Country> getCountries()
	{
		needCountries();

		return Collections.unmodifiableCollection(countries.values());
	}

	/**
	 * key: String languageID (this is important for sorting)<br/>
	 * value: List countries
	 */
	private transient Map<String, List<Country>> countriesSortedByLanguageID = null;

	public synchronized List<Country> getCountriesSorted(final Locale locale)
	{
		if (countriesSortedByLanguageID == null)
			countriesSortedByLanguageID = new HashMap<String, List<Country>>();

		final String languageID = locale.getLanguage();
		List<Country> countriesSorted = countriesSortedByLanguageID.get(languageID);

		if (countriesSorted == null) {
			countriesSorted = new ArrayList<Country>(getCountries());
			Collections.sort(countriesSorted, getCountryComparator(locale));
			countriesSorted = Collections.unmodifiableList(countriesSorted);
			countriesSortedByLanguageID.put(languageID, countriesSorted);
		}

		return countriesSorted;
	}

	/**
	 * key: CityID cityID<br/>
	 * value: Map {<br/>
	 *		key: String languageID<br/>
	 *		value: List locations<br/>
	 * }
	 */
	private transient Map<CityID, Map<String, List<Location>>> locationsSortedByLanguageIDByCityID = null;

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

	public synchronized Country getCountry(CountryID countryID, boolean throwExceptionIfNotFound)
	{
		needCountries();

		Country res = countries.get(countryID.countryID);

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No Country registered with countryID=\""+countryID+"\"!!!");

		return res;
	}

	public synchronized Region getRegion(RegionID regionID, boolean throwExceptionIfNotFound)
	{
		needRegions(regionID.countryID);

		Region res = regions.get(Region.getPrimaryKey(regionID));

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No Region registered with regionID=\""+regionID+"\"!!!");

		return res;
	}

	public synchronized City getCity(CityID cityID, boolean throwExceptionIfNotFound)
	{
		needCities(cityID.countryID);

		City res = cities.get(City.getPrimaryKey(cityID));

		if (res == null && throwExceptionIfNotFound)
			throw new IllegalArgumentException("No City registered with cityID=\""+cityID+"\"!!!");

		return res;
	}

	/**
	 * @param countryID
	 * @param throwExceptionIfNotFound
	 * @return Returns an empty collection if <tt>throwExceptionIfNotFound == false</tt> and
	 *		no <tt>Country</tt> exists for the given <tt>countryID</tt>.
	 */
	public synchronized Collection<Region> getRegions(CountryID countryID, boolean throwExceptionIfNotFound)
	{
		Country country = getCountry(countryID, throwExceptionIfNotFound);
		if (country == null)
			return Collections.emptyList();

		return Collections.unmodifiableCollection(country.getRegions());
	}

	public synchronized List<Region> getRegionsSorted(final CountryID countryID, final Locale locale)
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
			regionsSorted = Collections.unmodifiableList(regionsSorted);
			regionsSortedByLanguageID.put(languageID, regionsSorted);
		}

		return regionsSorted;
	}

	public synchronized Location getLocation(LocationID locationID, boolean throwExceptionIfNotFound)
	{
		needLocations(locationID.countryID);

		Location res = locations.get(Location.getPrimaryKey(locationID));

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
	public synchronized Collection<Location> getLocations(CityID cityID, boolean throwExceptionIfNotFound)
	{
		City city = getCity(cityID, throwExceptionIfNotFound);
		if (city == null)
			return Collections.emptyList();

		return Collections.unmodifiableCollection(city.getLocations());
	}

	public synchronized List<Location> getLocationsSorted(final CityID cityID, final Locale locale)
	{
		if (locationsSortedByLanguageIDByCityID == null)
			locationsSortedByLanguageIDByCityID = new HashMap<CityID, Map<String,List<Location>>>();

		Map<String,List<Location>> locationsSortedByLanguageID = locationsSortedByLanguageIDByCityID.get(cityID);
		if (locationsSortedByLanguageID == null) {
			locationsSortedByLanguageID = new HashMap<String, List<Location>>();
			locationsSortedByLanguageIDByCityID.put(cityID, locationsSortedByLanguageID);
		}

		final String languageID = locale.getLanguage();

		List<Location> locationsSorted = locationsSortedByLanguageID.get(languageID);

		if (locationsSorted == null) {
			locationsSorted = new ArrayList<Location>(getLocations(cityID, false));
			Collections.sort(locationsSorted, getLocationComparator(locale));
			locationsSorted = Collections.unmodifiableList(locationsSorted);
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
	public synchronized Collection<City> getCities(RegionID regionID, boolean throwExceptionIfNotFound)
	{
		needCities(regionID.countryID);

		Region region = getRegion(regionID, throwExceptionIfNotFound);
		if (region == null)
			return Collections.emptyList();

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

	protected Comparator<Region> getRegionComparator(final Locale locale)
	{
		return new Comparator<Region>() {
			private Collator collator = Collator.getInstance(locale);
			private String languageID = locale.getLanguage();

			public int compare(Region r0, Region r1)
			{
				String n0 = r0.getName().getText(languageID);
				String n1 = r1.getName().getText(languageID);

				return collator.compare(n0, n1);
			}
		};
	}

	protected Comparator<City> getCityComparator(final Locale locale)
	{
		return new Comparator<City>() {
			private Collator collator = Collator.getInstance(locale);
			private String languageID = locale.getLanguage();

			public int compare(City c0, City c1)
			{
				String n0 = c0.getName().getText(languageID);
				String n1 = c1.getName().getText(languageID);

				return collator.compare(n0, n1);
			}
		};
	}

	protected Comparator<Location> getLocationComparator(final Locale locale)
	{
		return new Comparator<Location>() {
			private Collator collator = Collator.getInstance(locale);
			private String languageID = locale.getLanguage();

			public int compare(Location l0, Location l1)
			{
				String n0 = l0.getName().getText(languageID);
				String n1 = l1.getName().getText(languageID);

				return collator.compare(n0, n1);
			}
		};
	}

	public synchronized List<City> getCitiesSorted(final RegionID regionID, Locale locale)
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
			citiesSorted = Collections.unmodifiableList(citiesSorted);
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
	protected transient Map<String, FulltextMap<String, List<Country>>> countriesByCountryNameByLanguageID = null;

	protected synchronized FulltextMap<String, List<Country>> getCountriesByCountryNameMap(String languageID)
	{
		logger.debug("getCountriesByCountryNameMap(languageID=\""+languageID+"\") entered.");

		if (countriesByCountryNameByLanguageID == null)
			countriesByCountryNameByLanguageID = new HashMap<String, FulltextMap<String, List<Country>>>();

		FulltextMap<String, List<Country>> countriesByCountryName = countriesByCountryNameByLanguageID.get(languageID);
		if (countriesByCountryName == null) {
			countriesByCountryName = new FulltextMap<String, List<Country>>(FULLTEXT_MAP_FEATURES);
			countriesByCountryNameByLanguageID.put(languageID, countriesByCountryName);
			for (Iterator<Country> it = getCountries().iterator(); it.hasNext(); ) {
				Country country = it.next();
				String countryName = country.getName().getText(languageID);
				List<Country> countryList = countriesByCountryName.get(countryName);
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
	protected transient Map<CityID, Map<String, FulltextMap<String, List<Location>>>> locationsByLocationNameByLanguageIDByCityID = null;

	protected synchronized FulltextMap<String, List<Location>> getLocationsByLocationNameMap(CityID cityID, String languageID)
	{
		logger.debug("getLocationsByLocationNameMap(cityID=\""+cityID+"\", languageID=\""+languageID+"\") entered.");

		needLocations(cityID.countryID);

		if (locationsByLocationNameByLanguageIDByCityID == null)
			locationsByLocationNameByLanguageIDByCityID = new HashMap<CityID, Map<String, FulltextMap<String, List<Location>>>>();

		Map<String, FulltextMap<String, List<Location>>> locationsByLocationNameByLanguageID = locationsByLocationNameByLanguageIDByCityID.get(cityID);
		if (locationsByLocationNameByLanguageID == null) {
			locationsByLocationNameByLanguageID = new HashMap<String, FulltextMap<String,List<Location>>>();
			locationsByLocationNameByLanguageIDByCityID.put(cityID, locationsByLocationNameByLanguageID);
		}

		FulltextMap<String, List<Location>> locationsByLocationName = locationsByLocationNameByLanguageID.get(languageID);
		if (locationsByLocationName == null) {
			locationsByLocationName = new FulltextMap<String, List<Location>>(FULLTEXT_MAP_FEATURES);
			locationsByLocationNameByLanguageID.put(languageID, locationsByLocationName);

			City city = getCity(cityID, false);
			if (city != null) {
				for (Iterator<Location> it = city.getLocations().iterator(); it.hasNext(); ) {
					Location location = it.next();
					String locationName = location.getName().getText(languageID);
					List<Location> locationList = locationsByLocationName.get(locationName);
					if (locationList == null) {
						locationList = new LinkedList<Location>();
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
	protected transient Map<CountryID, Map<String, FulltextMap<String, List<Region>>>> regionsByRegionNameByLanguageIDByCountryID = null;

	protected synchronized FulltextMap<String, List<Region>> getRegionsByRegionNameMap(CountryID countryID, String languageID)
	{
		logger.debug("getRegionsByRegionNameMap(countryID=\""+countryID+"\", languageID=\""+languageID+"\") entered.");

		needRegions(countryID.countryID);

		if (regionsByRegionNameByLanguageIDByCountryID == null)
			regionsByRegionNameByLanguageIDByCountryID = new HashMap<CountryID, Map<String, FulltextMap<String, List<Region>>>>();

		Map<String, FulltextMap<String, List<Region>>> regionsByRegionNameByLanguageID = regionsByRegionNameByLanguageIDByCountryID.get(countryID);
		if (regionsByRegionNameByLanguageID == null) {
			regionsByRegionNameByLanguageID = new HashMap<String, FulltextMap<String, List<Region>>>();
			regionsByRegionNameByLanguageIDByCountryID.put(countryID, regionsByRegionNameByLanguageID);
		}

		FulltextMap<String, List<Region>> regionsByRegionName = regionsByRegionNameByLanguageID.get(languageID);
		if (regionsByRegionName == null) {
			regionsByRegionName = new FulltextMap<String, List<Region>>(FULLTEXT_MAP_FEATURES);
			regionsByRegionNameByLanguageID.put(languageID, regionsByRegionName);

			Country country = getCountry(countryID, false);
			if (country != null) {
				for (Iterator<Region> it = country.getRegions().iterator(); it.hasNext(); ) {
					Region region = it.next();
					String regionName = region.getName().getText(languageID);
					List<Region> regionList = regionsByRegionName.get(regionName);
					if (regionList == null) {
						regionList = new LinkedList<Region>();
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
	protected Map<RegionID, FulltextMap<String, List<District>>> districtsByZipByRegionID = null;

	protected synchronized FulltextMap<String, List<District>> getDistrictsByZipMap(RegionID regionID)
	{
		logger.debug("getDistrictsByZipMap(regionID=\""+regionID+"\") entered.");

		needZips(regionID.countryID);

		if (districtsByZipByRegionID == null)
			districtsByZipByRegionID = new HashMap<RegionID, FulltextMap<String,List<District>>>();

		FulltextMap<String, List<District>> districtsByZip = districtsByZipByRegionID.get(regionID);
		if (districtsByZip == null) {
			districtsByZip = new FulltextMap<String, List<District>>(FULLTEXT_MAP_FEATURES);
			districtsByZipByRegionID.put(regionID, districtsByZip);

			Region region = getRegion(regionID, false);
			if (region != null) {
				for (Iterator<City> itC = region.getCities().iterator(); itC.hasNext(); ) {
					City city = itC.next();
					for (Iterator<District> itD = city.getDistricts().iterator(); itD.hasNext(); ) {
						District district = itD.next();
						for (Iterator<String> itZ = district.getZips().iterator(); itZ.hasNext(); ) {
							String zip = itZ.next();

							List<District> districtList = districtsByZip.get(zip);
							if (districtList == null) {
								districtList = new LinkedList<District>();
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
	protected Map<RegionID, Map<String, FulltextMap<String, List<City>>>> citiesByCityNameByLanguageIDByRegionID = null;

	protected synchronized FulltextMap<String, List<City>> getCitiesByCityNameMap(RegionID regionID, String languageID)
	{
		logger.debug("getCitiesByCityNameMap(regionID=\""+regionID+"\", languageID=\""+languageID+"\") entered.");

		needCities(regionID.countryID);

		if (citiesByCityNameByLanguageIDByRegionID == null)
			citiesByCityNameByLanguageIDByRegionID = new HashMap<RegionID, Map<String,FulltextMap<String,List<City>>>>();

		Map<String, FulltextMap<String, List<City>>> citiesByCityNameByLanguageID = citiesByCityNameByLanguageIDByRegionID.get(regionID);
		if (citiesByCityNameByLanguageID == null) {
			citiesByCityNameByLanguageID = new HashMap<String, FulltextMap<String,List<City>>>();
			citiesByCityNameByLanguageIDByRegionID.put(regionID, citiesByCityNameByLanguageID);
		}

		FulltextMap<String, List<City>> citiesByCityName = citiesByCityNameByLanguageID.get(languageID);
		if (citiesByCityName == null) {
			citiesByCityName = new FulltextMap<String, List<City>>(FULLTEXT_MAP_FEATURES);
			citiesByCityNameByLanguageID.put(languageID, citiesByCityName);

			Region region = getRegion(regionID, false);
			if (region != null) {
				for (Iterator<City> it = region.getCities().iterator(); it.hasNext(); ) {
					City city = it.next();
					String cityName = city.getName().getText(languageID);
					List<City> cityList = citiesByCityName.get(cityName);
					if (cityList == null) {
						cityList = new LinkedList<City>();
						citiesByCityName.put(cityName, cityList);
					}
					cityList.add(city);
				}
			}
		}

		logger.debug("getCitiesByCityNameMap(regionID=\""+regionID+"\", languageID=\""+languageID+"\") about to exit.");
		return citiesByCityName;
	}

	public synchronized List<Country> findCountriesByCountryNameSorted(String countryNamePart, Locale locale, int findMode)
	{
		if ("".equals(countryNamePart))
			return getCountriesSorted(locale);

		List<Country> countriesSorted = new ArrayList<Country>(findCountriesByCountryName(countryNamePart, locale.getLanguage(), findMode));
		Collections.sort(countriesSorted, getCountryComparator(locale));
		return countriesSorted;
	}

	public synchronized Collection<Country> findCountriesByCountryName(String countryNamePart, Locale locale, int findMode)
	{
		return findCountriesByCountryName(countryNamePart, locale.getLanguage(), findMode);
	}

	protected Collection<Country> findCountriesByCountryName(String countryNamePart, String languageID, int findMode)
	{
		if ("".equals(countryNamePart))
			return getCountries();

		Collection<Country> res = new ArrayList<Country>();
		FulltextMap<String, List<Country>> countriesByCountryName = getCountriesByCountryNameMap(languageID);
		for (Iterator<List<Country>> it = countriesByCountryName.find(countryNamePart, findMode).iterator(); it.hasNext(); ) {
			List<Country> countries = it.next();
			res.addAll(countries);
		}
		return res;
	}

	public synchronized Collection<Region> findRegionsByRegionName(CountryID countryID, String regionNamePart, Locale locale, int findMode)
	{
		return findRegionsByRegionName(countryID, regionNamePart, locale.getLanguage(), findMode);
	}

	protected synchronized Collection<Region> findRegionsByRegionName(CountryID countryID, String regionNamePart, String languageID, int findMode)
	{
		if ("".equals(regionNamePart))
			return getRegions(countryID, false);

		Collection<Region> res = new ArrayList<Region>();
		FulltextMap<String, List<Region>> regionNames2regions = getRegionsByRegionNameMap(countryID, languageID);
		for (Iterator<List<Region>> it = regionNames2regions.find(regionNamePart, findMode).iterator(); it.hasNext(); ) {
			res.addAll(it.next());
		}
		return res;
	}

	public synchronized List<Region> findRegionsByRegionNameSorted(CountryID countryID, String regionNamePart, Locale locale, int findMode)
	{
		if ("".equals(regionNamePart))
			return getRegionsSorted(countryID, locale);

		ArrayList<Region> regionsSorted = new ArrayList<Region>(findRegionsByRegionName(countryID, regionNamePart, locale.getLanguage(), findMode));
		Collections.sort(regionsSorted, getRegionComparator(locale));
		return regionsSorted;
	}

	public synchronized Collection<City> findCitiesByZip(RegionID regionID, String zipPart, int findMode)
	{
		if ("".equals(zipPart))
			return getCities(regionID, false);

		// key: String cityPK
		// value: City city
		Map<String, City> cities = new HashMap<String, City>();
		FulltextMap<String, List<District>> zips2districts = getDistrictsByZipMap(regionID);
		for (Iterator<List<District>> itL = zips2districts.find(zipPart, findMode).iterator(); itL.hasNext(); ) {
			List<District> districts = itL.next();
			for (Iterator<District> itD = districts.iterator(); itD.hasNext(); ) {
				District district = itD.next();
				cities.put(district.getCity().getPrimaryKey(), district.getCity());
			}
		}
		return cities.values();
	}

	public synchronized List<City> findCitiesByZipSorted(RegionID regionID, String zipPart, Locale locale, int findMode)
	{
		if ("".equals(zipPart))
			return getCitiesSorted(regionID, locale);

		ArrayList<City> citiesSorted = new ArrayList<City>(findCitiesByZip(regionID, zipPart, findMode));
		Collections.sort(citiesSorted, getCityComparator(locale));
		return citiesSorted;
	}

	public synchronized Collection<City> findCitiesByCityName(RegionID regionID, String cityNamePart, Locale locale, int findMode)
	{
		return findCitiesByCityName(regionID, cityNamePart, locale.getLanguage(), findMode);
	}

	protected synchronized Collection<City> findCitiesByCityName(RegionID regionID, String cityNamePart, String languageID, int findMode)
	{
		if ("".equals(cityNamePart))
			return getCities(regionID, false);

		Collection<City> res = new ArrayList<City>();
		FulltextMap<String, List<City>> cityNames2cities = getCitiesByCityNameMap(regionID, languageID); // (FulltextMap) cityNames2citiesByLanguageID.get(languageID);
		for (Iterator<List<City>> it = cityNames2cities.find(cityNamePart, findMode).iterator(); it.hasNext(); ) {
			List<City> cities = it.next();
			res.addAll(cities);
		}
		return res;
	}

	public synchronized List<City> findCitiesByCityNameSorted(RegionID regionID, String cityNamePart, Locale locale, int findMode)
	{
		if ("".equals(cityNamePart))
			return getCitiesSorted(regionID, locale);

		List<City> citiesSorted = new ArrayList<City>(findCitiesByCityName(regionID, cityNamePart, locale.getLanguage(), findMode));
		Collections.sort(citiesSorted, getCityComparator(locale));
		return citiesSorted;
	}


	public synchronized Collection<Location> findLocationsByLocationName(CityID cityID, String locationNamePart, Locale locale, int findMode)
	{
		return findLocationsByLocationName(cityID, locationNamePart, locale.getLanguage(), findMode);
	}

	protected synchronized Collection<Location> findLocationsByLocationName(CityID cityID, String locationNamePart, String languageID, int findMode)
	{
		if ("".equals(locationNamePart))
			return getLocations(cityID, false);

		Collection<Location> res = new ArrayList<Location>();
		FulltextMap<String, List<Location>> locationNames2locations = getLocationsByLocationNameMap(cityID, languageID); // (FulltextMap) locationNames2locationsByLanguageID.get(languageID);
		for (Iterator<List<Location>> it = locationNames2locations.find(locationNamePart, findMode).iterator(); it.hasNext(); ) {
			res.addAll(it.next());
		}
		return res;
	}

	public synchronized List<Location> findLocationsByLocationNameSorted(CityID cityID, String locationNamePart, Locale locale, int findMode)
	{
		if ("".equals(locationNamePart))
			return getLocationsSorted(cityID, locale);

		List<Location> locationsSorted = new ArrayList<Location>(findLocationsByLocationName(cityID, locationNamePart, locale.getLanguage(), findMode));
		Collections.sort(locationsSorted, getLocationComparator(locale));
		return locationsSorted;
	}

	protected static final int FULLTEXT_MAP_FEATURES =
			FulltextMap.FEATURE_BEGINS_WITH | FulltextMap.FEATURE_CONTAINS | FulltextMap.FEATURE_ENDS_WITH;
//	protected static final int FULLTEXT_MAP_FIND_MODE = FulltextMap.FIND_MODE_CONTAINS;

	public static final String IMPORTCHARSET = "UTF-8";


	protected boolean loadedCountries = false;

	protected abstract void loadCountries();

	/**
	 * This method loads the countries, if they're not yet loaded. It uses the file
	 * "resource/Data-Country.csv" relative to this class. Additionally, it adds all
	 * the countries from {@link Locale} that are missing in the csv file.
	 */
	protected synchronized void needCountries()
	{
		if (loadedCountries)
			return;

		loadCountries();

		loadedCountries = true;
	}

	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the regions have already been loaded.
	 */
	protected Set<String> loadedRegionsCountryIDSet = new HashSet<String>();

	protected abstract void loadRegions(String countryID);

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

		loadRegions(countryID);

		loadedRegionsCountryIDSet.add(countryID);
	}

	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the locations have already been loaded.
	 */
	protected Set<String> loadedLocationsCountryIDSet = new HashSet<String>();

	protected abstract void loadLocations(String countryID);

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

		loadLocations(countryID);

		loadedLocationsCountryIDSet.add(countryID);
	}


	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the cities have already been loaded.
	 */
	protected Set<String> loadedCitiesCountryIDSet = new HashSet<String>();

	protected abstract void loadCities(String countryID);

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

		loadCities(countryID);

		loadedCitiesCountryIDSet.add(countryID);
	}


	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the districts have already been loaded.
	 */
	protected Set<String> loadedDistrictsCountryIDSet = new HashSet<String>();

	protected abstract void loadDistricts(String countryID);

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

		loadDistricts(countryID);

		loadedDistrictsCountryIDSet.add(countryID);
	}


	/**
	 * Contains instances of {@link String} representing the <tt>countryID</tt> of all countries
	 * for which the zips have already been loaded.
	 */
	protected Set<String> loadedZipsCountryIDSet = new HashSet<String>();

	protected abstract void loadZips(String countryID);

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

		loadZips(countryID);

 		loadedZipsCountryIDSet.add(countryID);
	}

	/**
	 * This method clears the complete cache in order to reload all data again. Call this method after you
	 * modified data.
	 */
	public synchronized void clearCache()
	{
		loadedCountries = false;
		loadedCitiesCountryIDSet.clear();
		loadedDistrictsCountryIDSet.clear();
		loadedLocationsCountryIDSet.clear();
		loadedRegionsCountryIDSet.clear();
		loadedZipsCountryIDSet.clear();

		countries.clear();
		regions.clear();
		cities.clear();
		districts.clear();
		locations.clear();

		countriesByCountryNameByLanguageID = null;
		countriesSortedByLanguageID = null;
		regionsByRegionNameByLanguageIDByCountryID = null;
		regionsSortedByLanguageIDByCountryID = null;
		citiesByCityNameByLanguageIDByRegionID = null;
		citiesSortedByLanguageIDByRegionID = null;
		districtsByZipByRegionID = null;
		locationsByLocationNameByLanguageIDByCityID = null;
		locationsSortedByLanguageIDByCityID = null;
	}

	// countryIDs are ISO standard Strings - we don't need an ID generator method for them!

	public static String nextRegionID(String countryID, String organisationID)
	{
		if (!IDGenerator.getOrganisationID().equals(organisationID))
			throw new IllegalArgumentException("Can only generate an ID for the organisation '"+IDGenerator.getOrganisationID()+"' - the argument is invalid: " + organisationID);

		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Region.class.getName() + "#" + countryID));
	}

	public static String nextCityID(String countryID, String organisationID)
	{
		if (!IDGenerator.getOrganisationID().equals(organisationID))
			throw new IllegalArgumentException("Can only generate an ID for the organisation '"+IDGenerator.getOrganisationID()+"' - the argument is invalid: " + organisationID);

		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(City.class.getName() + "#" + countryID));
	}

	// TODO implement the other ID generator methods - don't forget to initialise the namespace correctly!
	public static String nextLocationID(String countryID, String organisationID)
	{
		if (!IDGenerator.getOrganisationID().equals(organisationID))
			throw new IllegalArgumentException("Can only generate an ID for the organisation '"+IDGenerator.getOrganisationID()+"' - the argument is invalid: " + organisationID);

		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(Location.class.getName() + "#" + countryID));
	}

	public static String nextDistrictID(String districtID, String organisationID)
	{
		if (!IDGenerator.getOrganisationID().equals(organisationID))
			throw new IllegalArgumentException("Can only generate an ID for the organisation '"+IDGenerator.getOrganisationID()+"' - the argument is invalid: " + organisationID);

		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(District.class.getName() + "#" + districtID));
	}

}
