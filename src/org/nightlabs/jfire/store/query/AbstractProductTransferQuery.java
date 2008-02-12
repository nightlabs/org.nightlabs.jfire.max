package org.nightlabs.jfire.store.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.query.AbstractTransferQuery;

public abstract class AbstractProductTransferQuery<T>
extends AbstractTransferQuery<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Implement
	@Override
	protected Class<? extends Transfer> getCandidateClass()
	{
		return ProductTransfer.class;
	}

	@Implement
	@Override
	protected void appendToFilter(Query q, StringBuffer filter)
	{
		// no additional fields to filter for
	}
}
