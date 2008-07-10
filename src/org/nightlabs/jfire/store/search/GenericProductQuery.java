/**
 * 
 */
package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.nightlabs.jfire.store.Product;

/**
 * Generic {@link Product} Query that may be used to retrieve all kinds of Products.
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 */
public class GenericProductQuery 
extends AbstractProductQuery 
{
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractSearchQuery#initCandidateClass()
	 */
	@Override
	protected Class<?> initCandidateClass() {
		return Product.class;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jdo.query.AbstractJDOQuery#createQuery()
	 */
	@Override
	protected Query createQuery() {
		return getPersistenceManager().newQuery(getPersistenceManager().getExtent(getCandidateClass(), true));
	}

}
