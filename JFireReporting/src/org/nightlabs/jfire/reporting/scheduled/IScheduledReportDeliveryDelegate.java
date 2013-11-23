package org.nightlabs.jfire.reporting.scheduled;

import java.io.Serializable;

import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;

/**
 * Instances of this interface are registered for one {@link ScheduledReport} and 
 * store the configuration of how and where to a scheduled report should be delivered 
 * after its rendering was finished.
 * <p>
 * Additionally the work of actually delivering the report output is delegate
 * to the instance of {@link IScheduledReportDeliveryDelegate} assigned to a {@link ScheduledReport}. 
 * </p>
 * Possible implementations could for example store an email-address and send the report-output to this address.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface IScheduledReportDeliveryDelegate extends Serializable {
	
	void deliverReportOutput(ScheduledReport scheduledReport, RenderedReportLayout renderedReportLayout) throws Exception;
}
