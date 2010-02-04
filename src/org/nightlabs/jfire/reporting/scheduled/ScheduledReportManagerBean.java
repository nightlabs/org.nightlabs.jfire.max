/**
 * 
 */
package org.nightlabs.jfire.reporting.scheduled;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.QueryOption;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.layout.render.RenderReportException;
import org.nightlabs.jfire.reporting.layout.render.RenderReportRequest;
import org.nightlabs.jfire.reporting.layout.render.RenderedReportLayout;
import org.nightlabs.jfire.reporting.layout.render.ReportLayoutRendererUtil;
import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportID;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;
import org.nightlabs.timepattern.InputTimePattern;
import org.nightlabs.timepattern.InputTimePatternPeriod;
import org.nightlabs.util.TimePeriod;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class ScheduledReportManagerBean 
extends BaseSessionBeanImpl
implements ScheduledReportManagerLocal, ScheduledReportManagerRemote
{

	/** Logger for this class */
	private static Logger logger = Logger.getLogger(ScheduledReportManagerBean.class);
	
	public ScheduledReportManagerBean() {
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	@Override
	public void initialise() {
		PersistenceManager pm = createPersistenceManager();
		try {
			// Initialize meta-data of needed classes
			pm.getExtent(ScheduledReportDeliveryDelegateEMail.class);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS) // Never begin a transaction for report rendering
	@RolesAllowed({org.nightlabs.jfire.reporting.RoleConstants.renderReport_roleID})
	@Override
	public void processScheduledReport(TaskID taskID) throws Exception {
		
		PersistenceManager pm = createPersistenceManager();
		try {
			if (logger.isDebugEnabled())
				logger.debug("processScheduledReport started, determine parameters");
			Task task = (Task) pm.getObjectById(taskID);
			ScheduledReport scheduledReport = (ScheduledReport) task.getParam();
			if (logger.isDebugEnabled())
				logger.debug("processScheduledReport have " + ScheduledReport.describeScheduledReport(scheduledReport));
			RenderReportRequest renderReportRequest = scheduledReport.getRenderReportRequest();
			if (renderReportRequest == null) {
				throw new RenderReportException("Can not render ScheduledReport as the RenderReportRequest is not set. "
						+ ScheduledReport.describeScheduledReport(scheduledReport));
			}
			ReportRegistryItemID reportLayoutID = scheduledReport.getReportLayoutID();
			if (reportLayoutID == null) {
				throw new RenderReportException("Can not render ScheduledReport as no ReportLayout is set for it. "
						+ ScheduledReport.describeScheduledReport(scheduledReport));
			}
			if (logger.isDebugEnabled())
				logger.debug("processScheduledReport have reportLayoutID " + reportLayoutID);
			
			Map<String, Object> newParams = convertScheduledReportParams(scheduledReport, renderReportRequest);
			renderReportRequest.setParameters(newParams);
			
			
			IScheduledReportDeliveryDelegate deliveryDelegate = scheduledReport.getDeliveryDelegate();
			if (deliveryDelegate == null) {
				throw new IllegalStateException("Can not render ScheduledReport as no ScheduledReportDeliveryDelegate is set for it. "
						+ ScheduledReport.describeScheduledReport(scheduledReport));
			}
			if (logger.isDebugEnabled())
				logger.debug("processScheduledReport have deliveryDelegate " + deliveryDelegate);
			
			
			renderReportRequest.setReportRegistryItemID(reportLayoutID);
			
			if (logger.isDebugEnabled())
				logger.debug("processScheduledReport rendering ReportLayout " + reportLayoutID);
			RenderedReportLayout renderedReportLayout = ReportLayoutRendererUtil.renderReport(pm, renderReportRequest);
			
			if (logger.isDebugEnabled())
				logger.debug("processScheduledReport delivering rendered report.");			
			deliveryDelegate.deliverReportOutput(scheduledReport, renderedReportLayout);
			
		} finally {
			pm.close();
		}
	}

	private Map<String, Object> convertScheduledReportParams(ScheduledReport scheduledReport, RenderReportRequest renderReportRequest)
			throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("Check report parameters for InputTimePatterns and converting to Date");
		// We make sure we can modify the map
		Map<String, Object> newParams = new HashMap<String, Object>(renderReportRequest.getParameters());
		Map<String, Object> convertedParams = new HashMap<String, Object>();
		Date now = new Date();
		for (Map.Entry<String, Object> paramEntry : newParams.entrySet()) {
			Object paramValue = paramEntry.getValue();
			if (paramValue instanceof InputTimePattern) {
				if (logger.isDebugEnabled())
					logger.debug("Have InputTimePattern for parameter " + paramEntry.getKey() + ", converting....");
				Date convertedParam = null;
				try {
					convertedParam = ((InputTimePattern) paramValue).getTime(now);
					if (logger.isDebugEnabled())
						logger.debug("InputTimePattern for parameter " + paramEntry.getKey() + ", converted " + paramValue + " to " + convertedParam);
				} catch (Exception e) {
					logger.error("Error converting InputTimePattern parameter of schedledReport "
							+ ScheduledReport.describeScheduledReport(scheduledReport)
							+ " to a date. Will proceed with un-set parameter.", e);
					convertedParam = null;
				}
				convertedParams.put(paramEntry.getKey(), convertedParam);
			}
			if (paramValue instanceof InputTimePatternPeriod) {
				if (logger.isDebugEnabled())
					logger.debug("Have InputTimePatternPeriod for parameter " + paramEntry.getKey() + ", converting....");
				TimePeriod convertedParam = ((InputTimePatternPeriod) paramValue).getTimePeriod(now, false);
				if (logger.isDebugEnabled())
					logger.debug("InputTimePattern for parameter " + paramEntry.getKey() + ", converted " + paramValue + " to " + convertedParam);
				convertedParams.put(paramEntry.getKey(), convertedParam);
			}
		}
		newParams.putAll(convertedParams);
		return newParams;
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed({RoleConstants.editScheduledReport_roleID})
	@Override
	public ScheduledReport getScheduledReport(
			ScheduledReportID scheduledReportID, String[] fetchGroups,
			int maxFetchDepth) {
		return getScheduledReports(Collections.singleton(scheduledReportID), fetchGroups, maxFetchDepth).iterator().next();
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed({RoleConstants.editScheduledReport_roleID})
	@Override
	public Collection<ScheduledReportID> getScheduledReportIDs() {
		PersistenceManager pm = createPersistenceManager();
		try {
			return ScheduledReport.getScheduledReportIDsByUserID(pm);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed({RoleConstants.editScheduledReport_roleID})
	@Override
	public Collection<ScheduledReport> getScheduledReports(
			Set<ScheduledReportID> scheduledReportIDs, String[] fetchGroups,
			int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectSet(
					pm, scheduledReportIDs, ScheduledReport.class, 
					fetchGroups, maxFetchDepth, null, (QueryOption) null);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({RoleConstants.editScheduledReport_roleID})
	@Override
	public ScheduledReport storeScheduledReport(
			ScheduledReport scheduledReport, boolean get, String[] fetchGroups,
			int maxFetchDepth) {
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, scheduledReport, get, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed({RoleConstants.editScheduledReport_roleID})
	@Override
	public void deleteScheduledReport(ScheduledReportID scheduledReportID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			ScheduledReport scheduledReport = (ScheduledReport) pm.getObjectById(scheduledReportID);
			pm.deletePersistent(scheduledReport);
		} finally {
			pm.close();
		}
	}

}
