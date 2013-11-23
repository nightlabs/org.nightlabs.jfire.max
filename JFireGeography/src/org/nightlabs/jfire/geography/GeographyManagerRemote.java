package org.nightlabs.jfire.geography;

import java.util.Collection;

import javax.ejb.Remote;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;

@Remote
public interface GeographyManagerRemote {

	String ping(String message);

	void initialise();

	Collection<CountryID> getCountryIDs();

	Collection<RegionID> getRegionIDs(CountryID countryID);

	Collection<CityID> getCityIDs(RegionID regionID);

	Collection<LocationID> getLocationIDs(CityID cityID);

	Collection<Country> getCountries(Collection<CountryID> countryIDs,
			String[] fetchGroups, int maxFetchDepth);

	Collection<Region> getRegions(Collection<RegionID> regionIDs,
			String[] fetchGroups, int maxFetchDepth);

	Collection<City> getCities(Collection<CityID> cityIDs,
			String[] fetchGroups, int maxFetchDepth);

	Collection<Location> getLocations(Collection<LocationID> locationIDs,
			String[] fetchGroups, int maxFetchDepth);

	Country importCountry(CountryID countryID, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	Region importRegion(RegionID regionID, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	City importCity(CityID cityID, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	Location importLocation(LocationID locationID, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	byte[] getCSVData(String csvType, String countryID);
	
	byte[] getCSVsData(String csvType);
	
	Object getGeographyObject(ObjectID objectID, String[] fetchGroups,
			int maxFetchDepth);
}