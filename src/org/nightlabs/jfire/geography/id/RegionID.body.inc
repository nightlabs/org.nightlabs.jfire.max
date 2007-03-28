
	public static RegionID create(Region region)
	{
		RegionID n = new RegionID();
		n.countryID = region.getCountryID();
		n.organisationID = region.getOrganisationID();
		n.regionID = region.getRegionID();
		return n;
	}