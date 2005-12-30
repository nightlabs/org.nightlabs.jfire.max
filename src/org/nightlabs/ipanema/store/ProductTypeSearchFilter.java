/*
 * Created 	on Apr 14, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.store;

import org.nightlabs.jdo.search.SearchFilter;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public abstract class ProductTypeSearchFilter extends SearchFilter {

	/**
	 * @param conjunction
	 */
	public ProductTypeSearchFilter(int conjunction) {
		super(conjunction);
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchFilter#getExtendClass()
	 */
	protected Class getExtendClass() {
		Class productTypeClass = getProductTypeClass();
		if (!ProductType.class.isAssignableFrom(productTypeClass))
			throw new IllegalArgumentException("getProductTypeClass must return a subclass of ProductType");
		return productTypeClass;
	}
	
	protected abstract Class getProductTypeClass();
	
}
