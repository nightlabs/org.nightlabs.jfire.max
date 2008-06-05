/**
 * 
 */
package org.nightlabs.jfire.store.search;

import java.util.List;

import javax.jdo.Query;

import org.nightlabs.jdo.query.AbstractSearchQuery;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeGroup;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public abstract class AbstractProductTypeGroupQuery 
extends VendorDependentQuery
{
	private static final String PROPERTY_PREFIX = "AbstractProductTypeGroupQuery.";
	public static final String PROPERTY_FULL_TEXT_LANGUAGE_ID = PROPERTY_PREFIX + "fullTextLanguageID";
	public static final String PROPERTY_FULL_TEXT_SEARCH = PROPERTY_PREFIX + "fullTextSearch";
	public static final String PROPERTY_CLOSED = PROPERTY_PREFIX + "closed";
	public static final String PROPERTY_CONFIRMED = PROPERTY_PREFIX + "confirmed";
	public static final String PROPERTY_PUBLISHED = PROPERTY_PREFIX + "published";
	public static final String PROPERTY_SALEABLE = PROPERTY_PREFIX + "saleable";

	private Boolean published = null;
	private Boolean confirmed = null;
	private Boolean saleable = null;
	private Boolean closed = null;

	private String fullTextLanguageID = null;
	private String fullTextSearch = null;

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#prepareQuery(javax.jdo.Query)
	 */
	@Override
	protected void prepareQuery(Query q) {
//		super.prepareQuery(q);
		StringBuffer filter = getFilter();
		StringBuffer vars = getVars();
		StringBuffer imports = getImports();
		
//		filter.append("productType.productTypeGroups.containsValue(this)");
//		FIXME Workaround for JPOX - begin
		filter.append("productType.productTypeGroups.containsValue(workaroundGroup) &&");
		filter.append("workaroundGroup == this");
		vars.append(ProductTypeGroup.class.getName()+" workaroundGroup;");
		addImport(ProductTypeGroup.class.getName());
//		FIXME Workaround for JPOX - end

		filter.append(" && ");
		filter.append("productType.organisationID == :myOrganisationID");
//		paramMap.put("myOrganisationID", LocalOrganisation.getLocalOrganisation(getPersistenceManager()).getOrganisationID());

		if (fullTextSearch != null) {
			filter.append("\n && ( ");
			addFullTextSearch(filter, vars, "name");
			filter.append("\n )");
		}

		if (published != null)
			filter.append("\n && productType.published == :published");
		
		if (confirmed != null)
			filter.append("\n && productType.confirmed == :confirmed");
		
		if (saleable != null)
			filter.append("\n && productType.saleable == :saleable");
				
		if (closed != null)
			filter.append("\n && productType.closed == :closed");

		if (getVendorID() != null)
			filter.append("\n && productType.vendorID == :vendorID");

//		int dateCount = 0;
//		if (isUsePerformaceTimeFilter()) {
//			if ((getPerformanceTimeFilters() != null) && (getPerformanceTimeFilters().size() > 0) ) {
//				filter.append(" && (");
//				for (Iterator iter = getPerformanceTimeFilters().iterator(); iter.hasNext();) {
//					PerformanceTimeFilterItem filterItem = (PerformanceTimeFilterItem) iter.next();
//					filterItem.appendSubQuery(dateCount, 0, imports, vars, filter, params, paramMap);
//					if (iter.hasNext()) {
//						switch (getPerformanceTimeFilterConjunction()) {
//							case SearchFilter.CONJUNCTION_AND:
//								filter.append(" && ");
//								break;
//							case SearchFilter.CONJUNCTION_OR:
//								filter.append(" || ");
//								break;
//						}
//					}
//				}
//				filter.append(")");
//			}
//		}
		
		vars.append(ProductType.class.getName()+" productType;");
		addImport(ProductType.class.getName());

//		result.append("distinct this");		 
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
	public boolean getPublished() {
		return published;
	}

	/**
	 * Returns whether only saleable ProductTypes should be searched for.
	 * @return whether only saleable ProductTypes should be searched for.
	 */
	public boolean getSaleable() {
		return saleable;
	}

	/**
	 * Sets the saleable.
	 * @param saleable the saleable to set
	 */
	public void setSaleable(boolean saleable) {
		this.saleable = saleable;
	}

	/**
	 * Sets the published.
	 * @param published the published to set
	 */
	public void setPublished(boolean published) {
		this.published = published;
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
	public void setConfirmed(boolean confirmed)
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
	public void setClosed(boolean closed)
	{
		final Boolean oldClosed = this.closed;
		this.closed = closed;
		notifyListeners(PROPERTY_CLOSED, oldClosed, closed);
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
		
		return changedFields;
	}
}
