package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.nightlabs.jfire.accounting.MoneyTransfer;
import org.nightlabs.jfire.transfer.query.AbstractTransferQuery;

public class MoneyTransferQuery
	extends AbstractTransferQuery
{
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<? extends MoneyTransfer> initCandidateClass()
	{
		return MoneyTransfer.class;
	}
	
	@Override
	protected void appendToFilter(Query q, StringBuffer filter)
	{
		// no additional fields to append
	}

	@Override
	protected void setQueryResult(Query q)
	{
	}
}
