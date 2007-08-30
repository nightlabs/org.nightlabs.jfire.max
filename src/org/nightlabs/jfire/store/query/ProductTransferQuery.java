package org.nightlabs.jfire.store.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.store.ProductTransfer;

public class ProductTransferQuery
extends AbstractProductTransferQuery<ProductTransfer>
{
	private static final long serialVersionUID = 1L;

	@Implement
	@Override
	protected void setQueryResult(Query q)
	{
		// nothing to do
	}
}
