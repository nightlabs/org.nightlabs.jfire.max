package org.nightlabs.jfire.dynamictrade.store.search;

import javax.jdo.Query;

import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.store.search.ProductTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class DynamicProductTypeQuery 
extends ProductTypeQuery 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DynamicProductTypeQuery() {
	}

	@Override
	protected Query prepareQuery() 
	{
		super.prepareQuery();
		// FIXME: Query also subclasses when JPOX problem is solved
		Query q = getPersistenceManager().newQuery(getPersistenceManager().getExtent(
				DynamicProductType.class, false));
		
		q.setFilter(getFilter().toString());
		q.declareVariables(getVars().toString());

		return q;
	}

}
