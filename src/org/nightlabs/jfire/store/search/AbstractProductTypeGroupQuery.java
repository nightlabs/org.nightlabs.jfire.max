package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeGroup;

/**
 * Abstract base class for queries which searches for {@link ProductTypeGroup}s.
 * Every field that's <code>null</code> is ignored,
 * every field containing a value will cause the query to filter all non-matching instances.
 *
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public abstract class AbstractProductTypeGroupQuery
	extends VendorDependentQuery
	implements ISaleAccessQuery
{
	private static final Logger logger = Logger.getLogger(AbstractProductTypeGroupQuery.class);

	public static final class FieldName
	{
		public static final String fullTextLanguageID = "fullTextLanguageID";
		public static final String fullTextSearch = "fullTextSearch";
		public static final String closed = "closed";
		public static final String confirmed = "confirmed";
		public static final String published = "published";
		public static final String saleable = "saleable";
		public static final String organisationID = "organisationID";
	}

	private Boolean published = null;
	private Boolean confirmed = null;
	private Boolean saleable = null;
	private Boolean closed = null;

	private String fullTextLanguageID = null;
	private String fullTextSearch = null;

	private String organisationID = null;

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#prepareQuery(javax.jdo.Query)
	 */
	@Override
	protected void prepareQuery(Query q)
	{
		StringBuffer filter = getFilter();
		StringBuffer vars = getVars();

		filter.append("productType."+ProductType.FieldName.productTypeGroups+".containsValue(this)");

		if (isFieldEnabled(FieldName.organisationID) && organisationID != null)
			filter.append("\n && this."+org.nightlabs.jfire.store.ProductTypeGroup.FieldName.organisationID+" == :"+org.nightlabs.jfire.store.ProductTypeGroup.FieldName.organisationID);

		if (isFieldEnabled(FieldName.fullTextSearch) && fullTextSearch != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, ProductType.FieldName.name);
			filter.append("\n )");
		}

		if (isFieldEnabled(FieldName.published) && published != null)
			filter.append("\n && productType."+ProductType.FieldName.published+" == :published");

		if (isFieldEnabled(FieldName.confirmed) && confirmed != null)
			filter.append("\n && productType."+ProductType.FieldName.confirmed+" == :confirmed");

		if (isFieldEnabled(FieldName.saleable) && saleable != null)
			filter.append("\n && productType."+ProductType.FieldName.saleable+" == :saleable");

		if (isFieldEnabled(FieldName.closed) && closed != null)
			filter.append("\n && productType."+ProductType.FieldName.closed+" == :closed");

		if (isFieldEnabled(VendorDependentQuery.FieldName.vendorID) && getVendorID() != null)
			filter.append("\n && JDOHelper.getObjectId(productType."+ProductType.FieldName.vendor+") == :vendorID");

		vars.append(ProductType.class.getName()+" productType;");
		addImport(ProductType.class.getName());

//		result.append("distinct this");
		if (logger.isDebugEnabled()) {
			logger.debug("Vars:");
			logger.debug(vars.toString());
			logger.debug("Filter:");
			logger.debug(filter.toString());
		}

		q.setFilter(filter.toString());
		q.declareVariables(vars.toString());
	}

	protected void addFullTextSearch(StringBuffer filter, StringBuffer vars, String member) {
		if (vars.length() > 0)
			vars.append("; ");
		String varName = member+"Var";
		vars.append(String.class.getName()+" "+varName);
		String containsStr = "containsValue("+varName+")";
		if (fullTextLanguageID != null)
			containsStr = "containsEntry(\""+fullTextLanguageID+"\","+varName+")";
		filter.append("\n (\n" +
				"  this."+member+".names."+containsStr+"\n" +
				"  && "+varName+".toLowerCase().matches(:fullTextSearch.toLowerCase())" +
				" )");
	}

	/**
	 * Returns whether only published ProductTypes should be searched for.
	 * @return whether only published ProductTypes should be searched for.
	 */
	public Boolean getPublished() {
		return published;
	}

	/**
	 * Returns whether only saleable ProductTypes should be searched for.
	 * @return whether only saleable ProductTypes should be searched for.
	 */
	public Boolean getSaleable() {
		return saleable;
	}

	/**
	 * Sets the saleable.
	 * @param saleable the saleable to set
	 */
	public void setSaleable(Boolean saleable) {
		Boolean oldSaleable = this.saleable;
		this.saleable = saleable;
		notifyListeners(FieldName.saleable, oldSaleable, saleable);
	}

	/**
	 * Sets the published.
	 * @param published the published to set
	 */
	public void setPublished(Boolean published) {
		Boolean oldPublished = this.published;
		this.published = published;
		notifyListeners(FieldName.published, oldPublished, published);
	}

	/**
	 * @return the fullTextLanguageID
	 */
	public String getFullTextLanguageID() {
		return fullTextLanguageID;
	}

	/**
	 * @param fullTextLanguageID the fullTextLanguageID to set
	 */
	public void setFullTextLanguageID(String fullTextLanguageID)
	{
		final String oldFullTextLanguageID = this.fullTextLanguageID;
		this.fullTextLanguageID = fullTextLanguageID;
		notifyListeners(FieldName.fullTextLanguageID, oldFullTextLanguageID, fullTextLanguageID);
	}

	/**
	 * @return the fullTextSearch
	 */
	public String getFullTextSearch() {
		return fullTextSearch;
	}

	/**
	 * Sets the filter to include only ProductTypeGroupss whose name
	 * matches the given fullTextSearch. The match will be performed
	 * following the rules of {@link String#matches(String)}, i.e.
	 * you need to pass a regular expression here.
	 *
	 * @param fullTextSearch the fullTextSearch to set
	 */
	public void setFullTextSearch(String fullTextSearch)
	{
		final String oldFullTextSearch = this.fullTextSearch;
		this.fullTextSearch = fullTextSearch;
		notifyListeners(FieldName.fullTextSearch, oldFullTextSearch, fullTextSearch);
	}

	/**
	 * @return the confirmed
	 */
	public Boolean getConfirmed() {
		return confirmed;
	}

	/**
	 * Sets the filter to include only ProductTypeGroupes which have ProductTypes whose confirmed flag matches the given value.
	 * @param confirmed the confirmed to set
	 */
	public void setConfirmed(Boolean confirmed)
	{
		final Boolean oldConfirmed = this.confirmed;
		this.confirmed = confirmed;
		notifyListeners(FieldName.confirmed, oldConfirmed, confirmed);
	}

	/**
	 * returns the closed.
	 * @return the closed
	 */
	public Boolean getClosed() {
		return closed;
	}

	/**
	 * sets the closed
	 * @param closed the closed to set
	 */
	public void setClosed(Boolean closed)
	{
		final Boolean oldClosed = this.closed;
		this.closed = closed;
		notifyListeners(FieldName.closed, oldClosed, closed);
	}

	/**
	 * Returns the organisationID.
	 * @return the organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}

	/**
	 * Sets the organisationID.
	 * @param organisationID the organisationID to set
	 */
	public void setOrganisationID(String organisationID) {
		String oldOrganisationID = this.organisationID;
		this.organisationID = organisationID;
		notifyListeners(FieldName.organisationID, oldOrganisationID, organisationID);
	}

}
