/**
 * 
 */
package org.nightlabs.jfire.reporting.layout;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jdo.search.SearchFilterItem;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ReportCategorySearchFilterItem extends SearchFilterItem {

	public static final int INTERNAL_FILTER_BOTH = 0;
	public static final int INTERNAL_FILTER_INTERNAL = 1;
	public static final int INTERNAL_FILTER__NOT_INTERNAL = 2;
	
	private int internalFilter = INTERNAL_FILTER_BOTH;
	
	private String nameFilter = null;
	
	/**
	 * @param matchType
	 * @param needle
	 */
	public ReportCategorySearchFilterItem(int matchType, String needle) {
		super(matchType, needle);
	}

	/* (non-Javadoc)
	 * @see com.nightlabs.jdo.search.SearchFilterItem#getSearchField()
	 */
	public Object getSearchField() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nightlabs.jdo.search.SearchFilterItem#isConstraint()
	 */
	public boolean isConstraint() {
		return (nameFilter != null) || (internalFilter != INTERNAL_FILTER_BOTH);
	}

	/* (non-Javadoc)
	 * @see com.nightlabs.jdo.search.SearchFilterItem#getItemTargetClass()
	 */
	public Class getItemTargetClass() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.nightlabs.jdo.search.SearchFilterItem#appendSubQuery(int, int, java.util.Set, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer, java.util.Map)
	 */
	public void appendSubQuery(int itemIndex, int itemSubIndex, Set imports,
			StringBuffer vars, StringBuffer filter, StringBuffer params,
			Map paramMap) {
		if (!isConstraint())
			return;
		
		filter.append("(");
		if (nameFilter != null)
			filter.append("this.name.names.contains()");
		// TODO: Howto search for name in map ...
		
		filter.append(")");
	}
	
	public int getInternalFilter() {
		return internalFilter;
	}
	
	public String getNameFilter() {
		return nameFilter;
	}
	
	public void setInternalFilter(int internalFilter) {
		this.internalFilter = internalFilter;
	}
	
	public void setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
	}

}
