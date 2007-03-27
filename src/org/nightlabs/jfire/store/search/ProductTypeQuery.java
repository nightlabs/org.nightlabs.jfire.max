package org.nightlabs.jfire.store.search;

import java.util.Set;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.store.ProductType;

/**
 * Searches {@link ProductType}s. Every field that's <code>null</code> is ignored, 
 * every field containing a value will cause the query to filter all non-matching instances.
 * 
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ProductTypeQuery 
extends JDOQuery<Set<ProductType>> 
{
	private static final Logger logger = Logger.getLogger(ProductTypeQuery.class);
	
	private static final long serialVersionUID = 1L;

	private String fullTextLanguageID;
	private String fullTextSearch;
	
	private Boolean published;
	private Boolean confirmed;
	private Boolean saleable;
	
	@Override
	protected Query prepareQuery() 
	{
		// FIXME: Query also subclasses when JPOX problem is solved
		Query q = getPersistenceManager().newQuery(getPersistenceManager().getExtent(
				ProductType.class, false));
		
		StringBuffer filter = new StringBuffer();
		StringBuffer vars = new StringBuffer();
//		StringBuffer imports = new StringBuffer();
		
		filter.append("\n");
		filter.append("true");		
		
		if (fullTextSearch != null) {
			filter.append("\n && ( ");			
			addFullTextSearch(filter, vars, "name");
			filter.append("\n )");
		}
		
		if (published != null)
			filter.append("\n && this.published == :published");
		
		if (confirmed != null)
			filter.append("\n && this.confirmed == :confirmed");
		
		if (saleable != null)
			filter.append("\n && this.saleable == :saleable");
				
		logger.error("Vars:");
		logger.error(vars.toString());
		logger.error("Filter:");
		logger.error(filter.toString());
		
		q.setFilter(filter.toString());
		q.declareVariables(vars.toString());
		
		return q;		
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
	 * @return the confirmed
	 */
	public Boolean getConfirmed() {
		return confirmed;
	}

	/**
	 * Sets the filter to include only ProductTypes whose confirmed flag matches the given value. 
	 * @param confirmed the confirmed to set
	 */
	public void setConfirmed(Boolean confirmed) {
		this.confirmed = confirmed;
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
	public void setFullTextLanguageID(String fullTextLanguageID) {
		this.fullTextLanguageID = fullTextLanguageID;
	}

	/**
	 * @return the fullTextSearch
	 */
	public String getFullTextSearch() {
		return fullTextSearch;
	}

	/**
	 * Sets the filter to include only ProductTypes whose name 
	 * matches the given fullTextSearch. The match will be performed
	 * following the rules of {@link String#matches(String)}, i.e.
	 * you need to pass a regular expression here.
	 * 
	 * @param fullTextSearch the fullTextSearch to set
	 */
	public void setFullTextSearch(String fullTextSearch) {
		this.fullTextSearch = fullTextSearch;
	}

	/**
	 * @return the published
	 */
	public Boolean getPublished() {
		return published;
	}

	/**
	 * Sets the filter to include only productTypes whose published flag matches the given value. 
	 * @param published the published to set
	 */
	public void setPublished(Boolean published) {
		this.published = published;
	}

	/**
	 * @return the saleable
	 */
	public Boolean getSaleable() {
		return saleable;
	}

	/**
	 * Sets the filter to include only productTypes whose saleable flag matches the given value.
	 * @param saleable the saleable to set
	 */
	public void setSaleable(Boolean saleable) {
		this.saleable = saleable;
	}

	
}
