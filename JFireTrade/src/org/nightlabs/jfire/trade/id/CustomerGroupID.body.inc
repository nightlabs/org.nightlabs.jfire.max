
	public static CustomerGroupID create(String customerGroupPK)
	{
		CustomerGroupID n = new CustomerGroupID();

		String[] sa = customerGroupPK.split("/");
		if (sa.length != 2)
			throw new IllegalArgumentException("customerGroupPK does not contain exactly 2 parts: " + customerGroupPK);

		n.organisationID = sa[0];
		n.customerGroupID = sa[1];
		return n;
	}

	public String getPrimaryKey()
	{
		return org.nightlabs.jfire.trade.CustomerGroup.getPrimaryKey(organisationID, customerGroupID);
	}