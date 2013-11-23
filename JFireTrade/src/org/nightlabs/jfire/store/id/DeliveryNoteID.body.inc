
	@Override
	public String getOrganisationID() {
		return organisationID;
	}

	@Override
	public String getArticleContainerIDPrefix() {
		return deliveryNoteIDPrefix;
	}

	@Override
	public long getArticleContainerID() {
		return deliveryNoteID;
	}
	
	@Override
	public String getArticleContainerIDAsString() {
		return org.nightlabs.jdo.ObjectIDUtil.longObjectIDFieldToString(deliveryNoteID);
	}
