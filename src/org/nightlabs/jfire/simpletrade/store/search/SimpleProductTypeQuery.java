package org.nightlabs.jfire.simpletrade.store.search;

import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class SimpleProductTypeQuery
extends AbstractProductTypeQuery<SimpleProductType>
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
//	private static final Logger logger = Logger.getLogger(SimpleProductTypeQuery.class);
	
//	@Override
//	protected Query prepareQuery()
//	{
//		super.prepareQuery();
//		Query q = getPersistenceManager().newQuery(getPersistenceManager().getExtent(
//				SimpleProductType.class, false));
//		
//		logger.debug("Vars:");
//		logger.debug(getVars().toString());
//		logger.debug("Filter:");
//		logger.debug(getFilter().toString());
//		
//		q.setFilter(getFilter().toString());
//		q.declareVariables(getVars().toString());
//		
//		return q;
//	}

	@Override
	protected Class<SimpleProductType> initCandidateClass()
	{
		return SimpleProductType.class;
	}

}
