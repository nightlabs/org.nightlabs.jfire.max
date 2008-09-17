
	@Override
	public String getOrganisationID() {
		return organisationID;
	}

	@Override
	public String getArticleContainerIDPrefix() {
		return offerIDPrefix;
	}

	@Override
	public long getArticleContainerID() {
		return offerID;
	}
	
	@Override
	public String getArticleContainerIDAsString() {
		return org.nightlabs.jdo.ObjectIDUtil.longObjectIDFieldToString(offerID);
	}
	
	public static OfferID create(OfferLocalID offerLocalID)
	{
		return create(offerLocalID.organisationID, offerLocalID.offerIDPrefix, offerLocalID.offerID);
	}