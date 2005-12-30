
	public static ProductReferenceID create(
			org.nightlabs.jfire.transfer.Anchor anchor,
			org.nightlabs.jfire.store.Product product)
	{
		ProductReferenceID n = new ProductReferenceID();
		n.anchorOrganisationID = anchor.getOrganisationID();
		n.anchorAnchorTypeID = anchor.getAnchorTypeID();
		n.anchorAnchorID = anchor.getAnchorID();
		n.productOrganisationID = product.getOrganisationID();
		n.productProductID = product.getProductID();
		return n;
	}

	public static ProductReferenceID create(
			org.nightlabs.jfire.transfer.id.AnchorID anchorID,
			org.nightlabs.jfire.store.id.ProductID productID)
	{
		ProductReferenceID n = new ProductReferenceID();
		n.anchorOrganisationID = anchorID.organisationID;
		n.anchorAnchorTypeID = anchorID.anchorTypeID;
		n.anchorAnchorID = anchorID.anchorID;
		n.productOrganisationID = productID.organisationID;
		n.productProductID = productID.productID;
		return n;
	}
