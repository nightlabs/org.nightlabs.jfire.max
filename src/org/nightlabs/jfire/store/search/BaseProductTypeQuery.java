package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductType;

/**
 * Generic ProductType Query that may be used to retrieve all kinds of Product types. 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class BaseProductTypeQuery
	extends AbstractProductTypeQuery
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 20080811L;

	@Override
	protected Class<ProductType> initCandidateClass()
	{
		return ProductType.class;
	}
	
	@Override
	protected Query createQuery()
	{
		return getPersistenceManager().newQuery(
			getPersistenceManager().getExtent(getCandidateClass(), true)
			);
	}

}
