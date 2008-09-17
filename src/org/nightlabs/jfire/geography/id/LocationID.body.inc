
	public static LocationID create(Location location)
	{
		LocationID n = new LocationID();
		n.countryID = location.getCountryID();
		n.organisationID = location.getOrganisationID();
		n.locationID = location.getLocationID();
		return n;
	}
