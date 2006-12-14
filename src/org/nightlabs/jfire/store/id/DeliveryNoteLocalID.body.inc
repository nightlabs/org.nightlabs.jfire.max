
	public static DeliveryNoteLocalID create(DeliveryNoteID deliveryNoteID)
	{
		return create(deliveryNoteID.organisationID, deliveryNoteID.deliveryNoteIDPrefix, deliveryNoteID.deliveryNoteID);
	}