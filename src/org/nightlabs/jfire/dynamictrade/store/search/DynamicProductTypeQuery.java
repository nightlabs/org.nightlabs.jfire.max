package org.nightlabs.jfire.dynamictrade.store.search;

import javax.jdo.Query;

import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class DynamicProductTypeQuery
extends AbstractProductTypeQuery<DynamicProductType>
{
	private static final long serialVersionUID = 1L;

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

	@Override
	protected Class<DynamicProductType> init()
	{
		return DynamicProductType.class;
	}
}
