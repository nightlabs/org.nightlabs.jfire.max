package org.nightlabs.jfire.store.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.transfer.id.TransferID;

/**
 * @author Marco Schulze
 * @author Marius Heinzmann - marius[at]nightlabs[dot]com
 */
public class ProductTransferIDQuery
	extends AbstractProductTransferQuery<TransferID>
{
	private static final long serialVersionUID = 1L;

	@Implement
	@Override
	protected void setQueryResult(Query q)
	{
		q.setResult("JDOHelper.getObjectId(this)");
	}

	@Override
	protected Class<TransferID> init()
	{
		return TransferID.class;
	}
}
