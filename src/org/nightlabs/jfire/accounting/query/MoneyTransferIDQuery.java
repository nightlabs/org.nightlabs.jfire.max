package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.transfer.id.TransferID;

public class MoneyTransferIDQuery
extends AbstractMoneyTransferQuery<TransferID>
{
	private static final long serialVersionUID = 1L;

	@Implement
	@Override
	protected void setQueryResult(Query q)
	{
		q.setResult("JDOHelper.getObjectId(this)");
	}

}
