package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.query.AbstractTransferQuery;

public abstract class AbstractMoneyTransferQuery<T>
extends AbstractTransferQuery<T>
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<? extends Transfer> getCandidateClass()
	{
		return MoneyTransfer.class;
	}

	@Implement
	@Override
	protected void appendToFilter(Query q, StringBuffer filter)
	{
		// no additional fields to append
	}
}
