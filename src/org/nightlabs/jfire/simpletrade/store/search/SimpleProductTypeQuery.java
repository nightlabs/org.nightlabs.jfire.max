package org.nightlabs.jfire.simpletrade.store.search;

import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.search.ProductTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class SimpleProductTypeQuery
extends ProductTypeQuery
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(SimpleProductTypeQuery.class);
	
	public SimpleProductTypeQuery() {
	}

	@Override
	protected Query prepareQuery()
	{
		super.prepareQuery();
		Query q = getPersistenceManager().newQuery(getPersistenceManager().getExtent(
				SimpleProductType.class, false));
		
		logger.debug("Vars:");
		logger.debug(getVars().toString());
		logger.debug("Filter:");
		logger.debug(getFilter().toString());
		
		q.setFilter(getFilter().toString());
		q.declareVariables(getVars().toString());
		
		return q;
	}

	
}
