
	public static OfferID create(OfferLocalID offerLocalID)
	{
		return create(offerLocalID.organisationID, offerLocalID.offerIDPrefix, offerLocalID.offerID);
	}