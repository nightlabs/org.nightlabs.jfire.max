
	@Override
	public String getOrganisationID() {
		return organisationID;
	}

	@Override
	public String getArticleContainerIDPrefix() {
		return invoiceIDPrefix;
	}

	@Override
	public long getArticleContainerID() {
		return invoiceID;
	}
	
	@Override
	public String getArticleContainerIDAsString() {
		return org.nightlabs.jdo.ObjectIDUtil.longObjectIDFieldToString(invoiceID);
	}
