package org.nightlabs.jfire.accounting.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.MoneyTransfer;

/**
 * 
 * 
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
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

	@Override
	protected Class<MoneyTransfer> init()
	{
		return MoneyTransfer.class;
	}
}
