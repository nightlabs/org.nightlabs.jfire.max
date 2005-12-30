/*
 * Created 	on Sep 3, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.store;

import org.nightlabs.jdo.search.SearchFilter;

/**
 * 
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 */
public abstract class ProductTypeGroupSearchFilter extends SearchFilter {

	public ProductTypeGroupSearchFilter(int conjunction) {
		super(conjunction);
	}

	/**
	 * @see org.nightlabs.jdo.search.SearchFilter#getExtendClass()
	 */
	protected Class getExtendClass() {
		Class productTypeGroupClass = getProductTypeGroupClass();
		if (!ProductTypeGroup.class.isAssignableFrom(productTypeGroupClass))
			throw new IllegalArgumentException("getProductTypeGroupClass must return a subclass of ProductTypeGroup");
		return productTypeGroupClass;
	}
	
	/**
	 * Return the extend-class for this filter. Must return an
	 * subclass of {@link ProductTypeGroup}.
	 */
	protected abstract Class getProductTypeGroupClass();
}
