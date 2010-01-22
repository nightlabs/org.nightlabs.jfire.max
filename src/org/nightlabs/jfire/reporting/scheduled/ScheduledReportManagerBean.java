/**
 * 
 */
package org.nightlabs.jfire.reporting.scheduled;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.QueryOption;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.reporting.scheduled.id.ScheduledReportID;
import org.nightlabs.jfire.timer.id.TaskID;

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

	@TransactionAttribute(TransactionAttributeType.NEVER) // Never begin a transaction for report rendering
	@RolesAllowed({org.nightlabs.jfire.reporting.RoleConstants.renderReport_roleID})
	@Override
	public void processScheduledReport(TaskID taskID) throws Exception {
		// TODO: processScheduledReport
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
