package org.nightlabs.jfire.trade.recurring;

import javax.ejb.Local;

import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;

@Local
public interface RecurringTradeManagerLocal {
	/**
	 * This is the EJB method called by the {@link Task} of a {@link RecurringOfferConfiguration}
	 * in order to process its associated {@link RecurringOffer}.
	 */
	void processRecurringOfferTimed(TaskID taskID) throws Exception;
}
