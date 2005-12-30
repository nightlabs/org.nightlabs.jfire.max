// *************************************
// prepareScript
// *************************************
metaData.addColumn("Company", DataType.STRING);
metaData.addColumn("Name", DataType.STRING);
metaData.addColumn("FirstName", DataType.STRING);
metaData.addColumn("Address", DataType.STRING);
metaData.addColumn("City", DataType.STRING);
metaData.addColumn("PostCode", DataType.STRING);


// *************************************
// fetchScript
// *************************************
importPackage(Packages.org.nightlabs.ipanema.accounting);
importPackage(Packages.org.nightlabs.ipanema.person);

invoiceOrganisationID = p_1;
invoiceInvoiceID = p_2;
languageID = p_3;

q = persistenceManager.newQuery(
	persistenceManager.getExtent(
		Packages.org.nightlabs.ipanema.accounting.Invoice
	)
);

q.setUnique(true);
q.setFilter("this.organisationID == \""+invoiceOrganisationID+"\" &&");
q.setFilter("this.invoiceID == "+invoiceInvoiceID);

invoice = q.execute();
if (invoice == null)
	throw "No Invoice found with invoice "+invoiceOrganisationID + "/"+invoiceInvoiceID;

customer = invoice.getCustomer();
if (customer == null)
	throw "Customer is not assigned!";

person = customer.getPerson();
if (customer == null)
	throw "Person is not assigned!";

resultSet.addRow();
resultSet.addColumn(person.getPersonDataField(PersonStruct.PERSONALDATA_COMPANY).getText());
resultSet.addColumn(person.getPersonDataField(PersonStruct.PERSONALDATA_NAME).getText());
resultSet.addColumn(person.getPersonDataField(PersonStruct.PERSONALDATA_FIRSTNAME).getText());
resultSet.addColumn(person.getPersonDataField(PersonStruct.POSTADDRESS_ADDRESS).getText());
resultSet.addColumn(person.getPersonDataField(PersonStruct.POSTADDRESS_CITY).getText());
resultSet.addColumn(person.getPersonDataField(PersonStruct.POSTADDRESS_POSTCODE).getText());

