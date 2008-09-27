/**
 * 
 */
package org.nightlabs.jfire.reporting.textpart.dao;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.textpart.ReportTextPartConfiguration;
import org.nightlabs.jfire.reporting.textpart.ReportTextPartManager;
import org.nightlabs.jfire.reporting.textpart.ReportTextPartManagerUtil;
import org.nightlabs.jfire.reporting.textpart.id.ReportTextPartConfigurationID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * DAO object that gives access to {@link ReportTextPartConfiguration}s.
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ReportTextPartConfigurationDAO extends BaseJDOObjectDAO<ReportTextPartConfigurationID, ReportTextPartConfiguration> {

	private static ReportTextPartConfigurationDAO sharedInstance;

	/**
	 * Returns and lazily creates a static instance of ReportTextPartConfigurationDAO
	 */
	public static ReportTextPartConfigurationDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ReportTextPartConfigurationDAO.class) {
				if (sharedInstance == null) {
					sharedInstance = new ReportTextPartConfigurationDAO();
				}
			}
		}
		return sharedInstance;
	}
	
	/**
	 * 
	 */
	public ReportTextPartConfigurationDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<ReportTextPartConfiguration> retrieveJDOObjects(
			Set<ReportTextPartConfigurationID> configurationIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception {
		monitor.beginTask("Loading ReportTextPartConfigurations", 2);
		ReportTextPartManager rtpm = getReportTextPartManager();
		monitor.worked(1);
		Collection<ReportTextPartConfiguration> result = rtpm.getReportTextPartConfigurations(configurationIDs, fetchGroups, maxFetchDepth);
		monitor.done();
		return result;
	}
	
	public ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportTextPartConfigurationID configurationID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, configurationID, fetchGroups, maxFetchDepth, monitor);
	}
	
	public Collection<ReportTextPartConfiguration> getReportTextPartConfiguration(
			Set<ReportTextPartConfigurationID> configurationIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, configurationIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	public ReportTextPartConfiguration getReportTextPartConfiguration(
			ReportRegistryItemID reportRegistryItemID, ObjectID linkedObjectID, 
			boolean synthesize, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		monitor.beginTask("Loading ReportTextPartConfiguration", 2);
		ReportTextPartManager rtpm = getReportTextPartManager();
		monitor.worked(1);
		ReportTextPartConfiguration result = null;
		try {
			result = rtpm.getReportTextPartConfiguration(
					reportRegistryItemID, linkedObjectID, synthesize, fetchGroups, maxFetchDepth);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		monitor.done();
		return result;
	}
	
	/**
	 * This method stores the given {@link ReportTextPartConfiguration} if it is 
	 * a configuration linked to an object in the datastore.
	 * 
	 * @param reportTextPartConfiguration 
	 * 			The configuration to store.
	 * @param get 
	 * 			Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups 
	 * 			If get is <code>true</code>, this defines the fetch-groups the
	 * 			retuned item will be detached with.
	 * @param maxFetchDepth 
	 * 			If get is <code>true</code>, this defines the maximum fetch-depth
	 * 			when detaching.
	 * @return
	 * 			If get is <code>true</code> the detached {@link ReportTextPartConfiguration}
	 * 			is returned, <code>null</code> otherwise.
	 */
	public ReportTextPartConfiguration storeLinkedObjectReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration, 
			boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {

		monitor.beginTask("Store ReportTextPartConfiguration", 2);
		ReportTextPartManager rtpm = getReportTextPartManager();
		monitor.worked(1);
		ReportTextPartConfiguration result = null;
		try {
			result = rtpm.storeLinkedObjectReportTextPartConfiguration(reportTextPartConfiguration, get, fetchGroups, maxFetchDepth);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		monitor.done();
		return result;
	}
	
	/**
	 * This method stores the given {@link ReportTextPartConfiguration}.
	 * 
	 * @param reportTextPartConfiguration 
	 * 			The configuration to store.
	 * @param get 
	 * 			Wheter a detached copy of the stored item should be returned.
	 * @param fetchGroups 
	 * 			If get is <code>true</code>, this defines the fetch-groups the
	 * 			retuned item will be detached with.
	 * @param maxFetchDepth 
	 * 			If get is <code>true</code>, this defines the maximum fetch-depth
	 * 			when detaching.
	 * @return
	 * 			If get is <code>true</code> the detached {@link ReportTextPartConfiguration}
	 * 			is returned, <code>null</code> otherwise.
	 * 
	 * @ejb.interface-method
	 * @ejb.permission role-name="org.nightlabs.jfire.reporting.editReport"
	 * @ejb.transaction type="Required"
	 */
	public ReportTextPartConfiguration storeReportTextPartConfiguration(
			ReportTextPartConfiguration reportTextPartConfiguration, 
			boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		
		monitor.beginTask("Store ReportTextPartConfiguration", 2);
		ReportTextPartManager rtpm = getReportTextPartManager();
		monitor.worked(1);
		ReportTextPartConfiguration result = null;
		try {
			result = rtpm.storeReportTextPartConfiguration(reportTextPartConfiguration, get, fetchGroups, maxFetchDepth);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
		monitor.done();
		return result;
	}
	
	
	private ReportTextPartManager getReportTextPartManager() {
		try {
			return ReportTextPartManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
