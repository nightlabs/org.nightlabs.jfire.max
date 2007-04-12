
	public static TariffID create(String tariffPK)
	{
		TariffID n = new TariffID();
		String[] sa = tariffPK.split("/");
		if (sa.length != 2)
			throw new IllegalArgumentException("tariffPK does not contain exactly 2 parts: " + tariffPK);

		n.organisationID = sa[0];
		try {
			n.tariffID = Long.parseLong(sa[1], 16);
		} catch (NumberFormatException x) {
			throw new IllegalArgumentException("2nd part of tariffPK is not a valid hex-number-string: " + tariffPK, x);
		}
		return n;
	}