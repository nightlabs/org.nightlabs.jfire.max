
	@Override
	public String getOrganisationID() {
		return organisationID;
	}

	@Override
	public String getArticleContainerIDPrefix() {
		return orderIDPrefix;
	}

	@Override
	public long getArticleContainerID() {
		return orderID;
	}
	
	@Override
	public String getArticleContainerIDAsString() {
		return org.nightlabs.jdo.ObjectIDUtil.longObjectIDFieldToString(orderID);
	}
