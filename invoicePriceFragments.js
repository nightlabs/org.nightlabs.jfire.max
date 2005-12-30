// ********************************
// prepareScript
// ********************************

metaData.addColumn("organisationID", DataType.STRING);
metaData.addColumn("priceFragmentTypeID", DataType.STRING);
metaData.addColumn("amount", DataType.BIGDECIMAL);
metaData.addColumn("priceFragmentTypeName", DataType.STRING);

// ********************************
// fetchScript
// ********************************
importPackage(Packages.org.nightlabs.jfire.accounting);

invoiceOrganisationID = p_1;
invoiceInvoiceID = p_2;
languageID = p_3;

q = persistenceManager.newQuery(
	persistenceManager.getExtent(
		Packages.org.nightlabs.jfire.accounting.Invoice
	)
);

q.setUnique(true);
q.setFilter("this.organisationID == \""+invoiceOrganisationID+"\" &&");
q.setFilter("this.invoiceID == "+invoiceInvoiceID);

invoice = q.execute();
if (invoice == null)
	throw "No Invoice found with invoice "+invoiceOrganisationID + "/"+invoiceInvoiceID;

for(it = invoice.getPrice().getFragments().iterator(); it.hasNext(); ) {
	priceFragment = it.next();
	resultSet.addRow();
	resultSet.addColumn(priceFragment.getOrganisationID());
	resultSet.addColumn(priceFragment.getPriceFragmentTypeID());
	resultSet.addColumn(new java.lang.Long(priceFragment.getAmount()));
	resultSet.addColumn(priceFragment.getPriceFragmentType().getName().getText(languageID));
}
