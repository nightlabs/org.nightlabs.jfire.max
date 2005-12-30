/*
 * Created 	on Apr 14, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.simpletrade.store;

import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.store.ProductTypeSearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class SimpleProductTypeSearchFilter extends ProductTypeSearchFilter {

	/**
	 * @param conjunction
	 */
	public SimpleProductTypeSearchFilter(int conjunction) {
		super(conjunction);
	}

	/**
	 * @see org.nightlabs.jfire.store.ProductTypeSearchFilter#getProductTypeClass()
	 */
	protected Class getProductTypeClass() {
		return SimpleProductType.class;
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchFilter#prepareQuery(java.util.Set, java.lang.StringBuffer, java.lang.StringBuffer, java.lang.StringBuffer, java.util.Map, java.lang.StringBuffer)
	 */
	protected void prepareQuery(Set imports, StringBuffer vars,
			StringBuffer filter, StringBuffer params, Map paramMap,
			StringBuffer result) {
		filter.append("this.published && this.saleable");
	}

}
