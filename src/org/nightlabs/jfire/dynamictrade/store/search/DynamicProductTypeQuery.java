package org.nightlabs.jfire.dynamictrade.store.search;

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

//	@Override
//	protected Query prepareQuery()
//	{
//		Query q = super.prepareQuery();
//		// FIXME: Query also subclasses when JPOX problem is solved
////		 = getPersistenceManager().newQuery(getPersistenceManager().getExtent(
////				DynamicProductType.class, false));
//		
//		q.setFilter(getFilter().toString());
//		q.declareVariables(getVars().toString());
//
//		return q;
//	}

	@Override
	protected Class<DynamicProductType> initCandidateClass()
	{
		return DynamicProductType.class;
	}
}
