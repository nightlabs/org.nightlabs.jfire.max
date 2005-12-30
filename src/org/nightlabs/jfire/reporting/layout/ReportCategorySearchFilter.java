/**
 * 
 */
package org.nightlabs.jfire.reporting.layout;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.SearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportCategorySearchFilter extends SearchFilter {

	
	protected ReportCategorySearchFilter() {
		super(SearchFilter.CONJUNCTION_DEFAULT);
	}

	/* (non-Javadoc)
	 * @see com.nightlabs.jdo.search.SearchFilter#getExtendClass()
	 */
	protected Class getExtendClass() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nightlabs.jdo.search.SearchFilter#prepareQuery(java.util.Set, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer, java.util.Map, java.lang.StringBuffer)
	 */
	protected void prepareQuery(Set imports, StringBuffer vars,
			StringBuffer filter, StringBuffer params, Map paramMap,
			StringBuffer result
		) 
	{
		
	}

}
