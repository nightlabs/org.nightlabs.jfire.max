
	public static ValueAcquisitionSetupID create(ValueAcquisitionSetup valueAcquisitionSetup)
	{
		ValueAcquisitionSetupID n = new ValueAcquisitionSetupID();
		n.organisationID = valueAcquisitionSetup.getOrganisationID();
		n.valueAcquisitionSetupID = valueAcquisitionSetup.getValueAcquisitionSetupID();
		return n;
	}