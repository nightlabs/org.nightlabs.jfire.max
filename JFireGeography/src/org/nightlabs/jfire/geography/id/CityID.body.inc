
	public static CityID create(City city)
	{
		CityID n = new CityID();
		n.countryID = city.getCountryID();
		n.organisationID = city.getOrganisationID();
		n.cityID = city.getCityID();
		return n;
	}
