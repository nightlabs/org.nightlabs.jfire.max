
	public static ValueConsumerBindingID create(ValueConsumerBinding valueConsumerBinding)
	{
		ValueConsumerBindingID n = new ValueConsumerBindingID();
		n.organisationID = valueConsumerBinding.getOrganisationID();
		n.valueConsumerBindingID = valueConsumerBinding.getValueConsumerBindingID();
		return n;
	}