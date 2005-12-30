metaData.addColumn("pos", DataType.INTEGER);
metaData.addColumn("name", DataType.STRING);
metaData.addColumn("amount", DataType.BIGDECIMAL);

// *************************************
// fetchScript
// *************************************
importPackage(Packages.org.nightlabs.jfire.accounting);
importPackage(Packages.org.nightlabs.jfire.person);

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

articles = invoice.getArticles();
if (articles == null)
	throw "Articles not assigned!";

i = 1;
for (it = articles.iterator(); it.hasNext(); ) {
	article = it.next();
	resultSet.addRow();
	resultSet.addColumn(new java.lang.Integer(i));
	pTypeName = article.getProductType().getName().getText(languageID);
	resultSet.addColumn(pTypeName);
	resultSet.addColumn(new java.lang.Long(article.getPrice().getAmount()));
}


