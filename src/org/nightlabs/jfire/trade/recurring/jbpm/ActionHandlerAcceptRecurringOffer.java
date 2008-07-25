/**
 * 
 */
package org.nightlabs.jfire.trade.recurring.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerAcceptOffer;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;

/**
 * TODO: In addition to the super implementation, this handler should disable the task that creates {@link RecurredOffer}s
 */
public class ActionHandlerAcceptRecurringOffer extends ActionHandlerAcceptOffer {

	private static final long serialVersionUID = 1L;

	public ActionHandlerAcceptRecurringOffer() {
		super();
	}
	
	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception 
	{
		super.doExecute(executionContext);
		// TODO: Here the creation of the RecurredOffers by the task in RecurringOfferConfiguration needs to be stopped.
	}
	
}
