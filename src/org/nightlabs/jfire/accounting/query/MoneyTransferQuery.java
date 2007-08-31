package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.MoneyTransfer;

public class MoneyTransferQuery
extends AbstractMoneyTransferQuery<MoneyTransfer>
{
	private static final long serialVersionUID = 1L;

	@Implement
	@Override
	protected void setQueryResult(Query q)
	{
		// nothing to do
	}
}
