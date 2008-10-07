/**
 * 
 */
package org.nightlabs.jfire.trade.recurring.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.trade.jbpm.ActionHandlerAcceptOffer;
import org.nightlabs.jfire.trade.recurring.RecurredOffer;
import org.nightlabs.jfire.trade.recurring.RecurringOffer;

/**
 * In addition to the super implementation, this handler disables the task that is associated
 * to the {@link RecurringOffer} the workflow runs for that and creates {@link RecurredOffer}s.
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
		RecurringOffer recurringOffer = (RecurringOffer) getStatable();

		Task recurringTask = recurringOffer.getRecurringOfferConfiguration().getCreatorTask();
		if(	recurringTask.getTimePatternSet().getTimePatterns() != null)
			recurringTask.setEnabled(false);
		else
			throw new IllegalStateException("Recurrence cant be disabled because of a null timePattern");

	
	}

}
