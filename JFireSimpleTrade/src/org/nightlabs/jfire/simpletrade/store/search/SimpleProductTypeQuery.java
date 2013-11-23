package org.nightlabs.jfire.simpletrade.store.search;

import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class SimpleProductTypeQuery
	extends AbstractProductTypeQuery
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<SimpleProductType> initCandidateClass() {
		return SimpleProductType.class;
	}

}
