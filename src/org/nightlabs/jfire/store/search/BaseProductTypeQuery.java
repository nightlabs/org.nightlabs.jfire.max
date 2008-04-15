package org.nightlabs.jfire.store.search;

import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductType;

/**
 * Generic ProductType Query that may be used to retrieve all kinds of Product Types. 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class BaseProductTypeQuery
	extends AbstractProductTypeQuery<ProductType>
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

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
