
	public static CountryID create(Country country)
	{
		CountryID n = new CountryID();
		n.countryID = country.getCountryID();
		return n;
	}

	public static CountryID create(Region region)
	{
		CountryID n = new CountryID();
		n.countryID = region.getCountryID();
		return n;
	}

	public static CountryID create(City city)
	{
		CountryID n = new CountryID();
		n.countryID = city.getCountryID();
		return n;
	}