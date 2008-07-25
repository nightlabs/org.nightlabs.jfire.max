/**
 * 
 */
package org.nightlabs.jfire.trade.recurring.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;

/**
 * TODO: This handler should enable the task that creates {@link RecurredOffer}s
 */
public class ActionHandlerStartRecurrence extends AbstractActionHandler {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ActionHandlerStartRecurrence() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler#doExecute(org.jbpm.graph.exe.ExecutionContext)
	 */
	@Override
	protected void doExecute(ExecutionContext executionContext)
	throws Exception 
	{
		// TODO: Here the creation of the RecurredOffers by the task in RecurringOfferConfiguration needs to be started.
	}

}
