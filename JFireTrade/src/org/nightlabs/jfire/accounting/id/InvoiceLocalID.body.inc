
	public static InvoiceLocalID create(InvoiceID invoiceID)
	{
		return create(invoiceID.organisationID, invoiceID.invoiceIDPrefix, invoiceID.invoiceID);
	}