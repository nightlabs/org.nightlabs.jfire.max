package org.nightlabs.jfire.store.query;

import javax.jdo.Query;

import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.transfer.query.AbstractTransferQuery;

public class ProductTransferQuery
	extends AbstractTransferQuery<ProductTransfer>
{
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<? extends ProductTransfer> initCandidateClass()
	{
		return ProductTransfer.class;
	}
	
	@Override
	protected void appendToFilter(Query q, StringBuffer filter)
	{
		// no additional fields to filter for
	}

	@Override
	protected void setQueryResult(Query q)
	{
	}
}
