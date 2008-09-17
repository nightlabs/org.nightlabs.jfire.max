
	@Override
	public String getOrganisationID() {
		return organisationID;
	}

	@Override
	public String getArticleContainerIDPrefix() {
		return receptionNoteIDPrefix;
	}

	@Override
	public long getArticleContainerID() {
		return receptionNoteID;
	}
	
	@Override
	public String getArticleContainerIDAsString() {
		return org.nightlabs.jdo.ObjectIDUtil.longObjectIDFieldToString(receptionNoteID);
	}
