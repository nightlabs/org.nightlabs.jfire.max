
	public static ValueProviderConfigID create(ValueProviderConfig valueProviderConfig)
	{
		ValueProviderConfigID n = new ValueProviderConfigID();
		n.organisationID = valueProviderConfig.getOrganisationID();
		n.valueAcquisitionSetupID = valueProviderConfig.getValueAcquisitionSetupID();
		n.valueProviderCategoryID = valueProviderConfig.getValueProviderCategoryID();
		n.valueProviderID = valueProviderConfig.getValueProviderID();
		n.valueProviderOrganisationID = valueProviderConfig.getValueProviderOrganisationID();
		return n;
	}