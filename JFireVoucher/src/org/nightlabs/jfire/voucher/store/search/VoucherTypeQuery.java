package org.nightlabs.jfire.voucher.store.search;

import org.nightlabs.jfire.store.search.AbstractProductTypeQuery;
import org.nightlabs.jfire.voucher.store.VoucherType;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class VoucherTypeQuery
	extends AbstractProductTypeQuery
{
	/**
	 * The serial version id.
	 */
	private static final long serialVersionUID = 1L;
	
//	@Override
//	protected Query prepareQuery()
//	{
//		super.prepareQuery();
//		// FIXME: Query also subclasses when JPOX problem is solved
//		Query q = getPersistenceManager().newQuery(getPersistenceManager().getExtent(
//				VoucherType.class, false));
//		
//		q.setFilter(getFilter().toString());
//		q.declareVariables(getVars().toString());
//		
//		return q;
//	}

	@Override
	protected Class<VoucherType> initCandidateClass()
	{
		return VoucherType.class;
	}

	
}
