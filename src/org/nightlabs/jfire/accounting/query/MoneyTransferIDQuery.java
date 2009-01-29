package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.nightlabs.jfire.transfer.id.TransferID;

public class MoneyTransferIDQuery
	extends MoneyTransferQuery
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void setQueryResult(Query q)
	{
		q.setResult("JDOHelper.getObjectId(this)");
	}

	@Override
	protected Class<?> initResultClass()
	{
		return TransferID.class;
	}
}
