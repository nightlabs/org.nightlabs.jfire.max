/**
 * 
 */
package org.nightlabs.jfire.reporting.scheduled;

import javax.ejb.Local;

import org.nightlabs.jfire.timer.id.TaskID;

/**
 * Local EJB interface for the management of {@link ScheduledReport}s.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
@Local
public interface ScheduledReportManagerLocal {
	/**
	 * This method is called for the Task of a {@link ScheduledReport} and renders the report-layout
	 * it references and lets its {@link IScheduledReportDeliveryDelegate} deliver the rendered
	 * report.
	 * 
	 * @param taskID The id of the tasks run.
	 * @throws Exception If something fails.
	 */
	void processScheduledReport(TaskID taskID) throws Exception;
}
