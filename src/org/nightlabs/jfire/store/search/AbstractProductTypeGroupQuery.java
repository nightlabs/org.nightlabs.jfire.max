/**
 * 
 */
package org.nightlabs.jfire.store.search;

import java.util.List;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeGroup;
import org.nightlabs.jfire.store.ProductType.FieldName;

/**
 * Abstract base class for queries which searches for {@link ProductTypeGroup}s. 
 * Every field that's <code>null</code> is ignored,
 * every field containing a value will cause the query to filter all non-matching instances.
 *  
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public abstract class AbstractProductTypeGroupQuery 
extends VendorDependentQuery
implements ISaleAccessQuery
{
	private static final Logger logger = Logger.getLogger(AbstractProductTypeGroupQuery.class);
	
	private static final String PROPERTY_PREFIX = "AbstractProductTypeGroupQuery.";
	public static final String PROPERTY_FULL_TEXT_LANGUAGE_ID = PROPERTY_PREFIX + "fullTextLanguageID";
	public static final String PROPERTY_FULL_TEXT_SEARCH = PROPERTY_PREFIX + "fullTextSearch";
	public static final String PROPERTY_CLOSED = PROPERTY_PREFIX + "closed";
	public static final String PROPERTY_CONFIRMED = PROPERTY_PREFIX + "confirmed";
	public static final String PROPERTY_PUBLISHED = PROPERTY_PREFIX + "published";
	public static final String PROPERTY_SALEABLE = PROPERTY_PREFIX + "saleable";
	public static final String PROPERTY_ORGANISATION_ID = PROPERTY_PREFIX + "organisationID";

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
	protected void prepareQuery(Query q) {
//		super.prepareQuery(q);
		StringBuffer filter = getFilter();
		StringBuffer vars = getVars();
		StringBuffer imports = getImports();
				
		filter.append("productType."+FieldName.productTypeGroups+".containsValue(this)");

		if (organisationID != null)
			filter.append("\n && this."+org.nightlabs.jfire.store.ProductTypeGroup.FieldName.organisationID+" == :"+org.nightlabs.jfire.store.ProductTypeGroup.FieldName.organisationID);

		if (fullTextSearch != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, FieldName.name);
			filter.append("\n )");
		}

		if (published != null)
			filter.append("\n && productType."+FieldName.published+" == :published");

		if (confirmed != null)
			filter.append("\n && productType."+FieldName.confirmed+" == :confirmed");

		if (saleable != null)
			filter.append("\n && productType."+FieldName.saleable+" == :saleable");

		if (closed != null)
			filter.append("\n && productType."+FieldName.closed+" == :closed");

		if (getVendorID() != null)
			filter.append("\n && JDOHelper.getObjectId(productType."+FieldName.vendor+") == :vendorID");

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
		notifyListeners(PROPERTY_SALEABLE, oldSaleable, saleable);
	}

	/**
	 * Sets the published.
	 * @param published the published to set
	 */
	public void setPublished(Boolean published) {
		Boolean oldPublished = this.published;
		this.published = published;
		notifyListeners(PROPERTY_PUBLISHED, oldPublished, published);
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
		notifyListeners(PROPERTY_FULL_TEXT_LANGUAGE_ID, oldFullTextLanguageID, fullTextLanguageID);
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
		notifyListeners(PROPERTY_FULL_TEXT_SEARCH, oldFullTextSearch, fullTextSearch);
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
		notifyListeners(PROPERTY_CONFIRMED, oldConfirmed, confirmed);
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
		notifyListeners(PROPERTY_CLOSED, oldClosed, closed);
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
		notifyListeners(PROPERTY_ORGANISATION_ID, oldOrganisationID, organisationID);
	}

	@Override
	public List<FieldChangeCarrier> getChangedFields(String propertyName)
	{
		List<FieldChangeCarrier> changedFields = super.getChangedFields(propertyName);
		boolean allFields = AbstractSearchQuery.PROPERTY_WHOLE_QUERY.equals(propertyName);
		
		if (allFields || PROPERTY_CLOSED.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_CLOSED, closed) );
		}
		if (allFields || PROPERTY_CONFIRMED.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_CONFIRMED, confirmed) );
		}
		if (allFields || PROPERTY_FULL_TEXT_LANGUAGE_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_FULL_TEXT_LANGUAGE_ID, fullTextLanguageID) );
		}
		if (allFields || PROPERTY_FULL_TEXT_SEARCH.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_FULL_TEXT_SEARCH, fullTextSearch) );
		}
		if (allFields || PROPERTY_PUBLISHED.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_PUBLISHED, published) );
		}
		if (allFields || PROPERTY_SALEABLE.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_SALEABLE, saleable) );
		}
		if (allFields || PROPERTY_ORGANISATION_ID.equals(propertyName))
		{
			changedFields.add( new FieldChangeCarrier(PROPERTY_ORGANISATION_ID, organisationID) );
		}
		
		return changedFields;
	}
}
