
	public static ProductTypePermissionFlagSetID create(org.nightlabs.jfire.store.ProductType productType, org.nightlabs.jfire.security.User user)
	{
		ProductTypePermissionFlagSetID n = new ProductTypePermissionFlagSetID();
		n.productTypeOrganisationID = productType.getOrganisationID();
		n.productTypeID = productType.getProductTypeID();
		n.userOrganisationID = user.getOrganisationID();
		n.userID = user.getUserID();
		return n;
	}

	public static ProductTypePermissionFlagSetID create(org.nightlabs.jfire.store.id.ProductTypeID productTypeID, org.nightlabs.jfire.security.id.UserID userID)
	{
		ProductTypePermissionFlagSetID n = new ProductTypePermissionFlagSetID();
		n.productTypeOrganisationID = productTypeID.organisationID;
		n.productTypeID = productTypeID.productTypeID;
		n.userOrganisationID = userID.organisationID;
		n.userID = userID.userID;
		return n;
	}