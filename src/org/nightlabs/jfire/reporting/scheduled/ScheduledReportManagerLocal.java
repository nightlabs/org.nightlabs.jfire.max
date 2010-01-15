/**
 * 
 */
package org.nightlabs.jfire.reporting.scheduled;

import javax.ejb.Local;

import org.nightlabs.jfire.timer.id.TaskID;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@Local
public interface ScheduledReportManagerLocal {
	void processScheduledReport(TaskID taskID) throws Exception;
}
