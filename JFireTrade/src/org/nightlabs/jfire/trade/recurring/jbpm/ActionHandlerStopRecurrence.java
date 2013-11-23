package org.nightlabs.jfire.trade.recurring.jbpm;

import org.jbpm.graph.exe.ExecutionContext;
import org.nightlabs.jfire.jbpm.graph.def.AbstractActionHandler;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.trade.recurring.RecurringOffer;

public class ActionHandlerStopRecurrence extends AbstractActionHandler {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public ActionHandlerStopRecurrence() {
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
		recurringTask.setEnabled(false);
	}	
}
