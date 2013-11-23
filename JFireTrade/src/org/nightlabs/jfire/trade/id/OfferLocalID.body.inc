
	public static OfferLocalID create(OfferID offerID)
	{
		return create(offerID.organisationID, offerID.offerIDPrefix, offerID.offerID);
	}