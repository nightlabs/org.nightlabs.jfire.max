package org.nightlabs.jfire.trade.recurring;

import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;

public interface RecurringTradeManagerLocal {
	/**
	 * This is the EJB method called by the {@link Task} of a {@link RecurringOfferConfiguration}
	 * in order to process its associated {@link RecurringOffer}.
	 */
	void processRecurringOfferTimed(TaskID taskID) throws Exception;
}
