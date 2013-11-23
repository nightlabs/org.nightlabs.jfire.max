package org.nightlabs.jfire.trade.jbpm;

import javax.jdo.PersistenceManager;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Trader;

public class ActionHandlerAbortOffer
extends AbstractActionHandler
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		Offer offer = (Offer) getStatable();

		User user = SecurityReflector.getUserDescriptor().getUser(pm);
		Trader.getTrader(pm).onAbortOffer(user, offer);
	}
}
