	
	public static AcquisitionParameterConfigID create(AcquisitionParameterConfig acquisitionParameterConfig)
	{
		AcquisitionParameterConfigID n = new AcquisitionParameterConfigID();
		n.organisationID = acquisitionParameterConfig.getOrganisationID();
		n.parameterID = acquisitionParameterConfig.getParameterID();
		n.valueAcquisitionSetupID = acquisitionParameterConfig.getValueAcquisitionSetupID();
		return n;
	}