package org.nightlabs.jfire.dynamictrade.store.search;

import org.nightlabs.jfire.dynamictrade.store.DynamicProductType;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class DynamicProductTypeQuery
extends AbstractProductTypeQuery
{
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<DynamicProductType> initCandidateClass()
	{
		return DynamicProductType.class;
	}
}
