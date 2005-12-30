importPackage(Packages.java.util);
importClass(Packages.javax.jdo.Query);
importClass(Packages.org.nightlabs.jfire.accounting.Invoice);

resultSet.addRow();
resultSet.addColumn("Test");
resultSet.addColumn("Test");
resultSet.addColumn(new java.lang.Integer(1));
resultSet.addColumn("MrQuickFix");

resultSet.addRow();
resultSet.addColumn("XXX");
resultSet.addColumn("XXX");
resultSet.addColumn(new java.lang.Integer(1));
resultSet.addColumn("MrQuickFix");

q = persistenceManager.newQuery(
  persistenceManager.getExtent(
    Packages.org.nightlabs.jfire.accounting.Invoice
  )
);

q.setUnique(true);
q.setFilter("this.invoiceID == "+p_1);

invoice = q.execute();
if (invoice == null)
  throw "No Invoice found with invoiceID "+p_1;
 
fetchGroups = new HashSet();
fetchGroups.add(Packages.javax.jdo.FetchPlan.DEFAULT);
fetchGroups.add(Invoice.FETCH_GROUP_ARTICLES);

persistenceManager.getFetchPlan().setGroups(fetchGroups);

resultSet.addRow();
resultSet.addColumn("total articles = "+invoice.getArticles().size());
resultSet.addColumn("test param1"+p_1);
resultSet.addColumn(new java.lang.Integer(p_1));
resultSet.addColumn("test");



