package org.nightlabs.jfire.geography;

import java.util.Collection;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.jfire.geography.id.RegionID;

@Remote
public interface GeographyManagerRemote {

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<CountryID> getCountryIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<RegionID> getRegionIDs(CountryID countryID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<CityID> getCityIDs(RegionID regionID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<LocationID> getLocationIDs(CityID cityID);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<Country> getCountries(Collection<CountryID> countryIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<Region> getRegions(Collection<RegionID> regionIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<City> getCities(Collection<CityID> cityIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_Guest_")
	Collection<Location> getLocations(Collection<LocationID> locationIDs,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Country importCountry(CountryID countryID, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Region importRegion(RegionID regionID, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	City importCity(CityID cityID, boolean get, String[] fetchGroups,
			int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Location importLocation(LocationID locationID, boolean get,
			String[] fetchGroups, int maxFetchDepth);

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	byte[] getCSVData(String csvType, String countryID);

	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	byte[] getCSVsData(String csvType);
	
	
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Object getGeographyObject(ObjectID objectID, String[] fetchGroups,
			int maxFetchDepth);

}