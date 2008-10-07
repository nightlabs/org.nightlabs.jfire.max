/**
 * 
 */
package org.nightlabs.jfire.trade.recurring.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringOffer;

/**
 * This handler enables the task that creates {@link RecurredOffer}s
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
		RecurringOffer recurringOffer = (RecurringOffer) getStatable();
		
		Task recurringTask = recurringOffer.getRecurringOfferConfiguration().getCreatorTask();
		
		if(	recurringTask.getTimePatternSet().getTimePatterns() != null)
			recurringOffer.getRecurringOfferConfiguration().getCreatorTask().setEnabled(true);
		else
			throw new IllegalStateException("Recurrence cant be started because of a null timePattern");
	}

}
