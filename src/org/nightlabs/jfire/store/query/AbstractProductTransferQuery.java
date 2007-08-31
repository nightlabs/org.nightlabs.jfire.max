package org.nightlabs.jfire.store.query;

import javax.jdo.Query;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.transfer.query.AbstractTransferQuery;

public abstract class AbstractProductTransferQuery<T>
extends AbstractTransferQuery<T>
{
	@Implement
	@Override
	protected void appendToFilter(Query q, StringBuffer filter)
	{
		// no additional fields to filter for
	}
}
