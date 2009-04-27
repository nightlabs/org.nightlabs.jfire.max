/**
 *
 */
package org.nightlabs.jfire.reporting.parameter.dao;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.FetchPlan;
import javax.naming.NamingException;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.reporting.layout.id.ReportRegistryItemID;
import org.nightlabs.jfire.reporting.parameter.ReportParameterManagerRemote;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ReportParameterAcquisitionUseCase;
import org.nightlabs.jfire.reporting.parameter.config.ValueAcquisitionSetup;
import org.nightlabs.jfire.reporting.parameter.config.ValueConsumerBinding;
import org.nightlabs.jfire.reporting.parameter.config.ValueProviderConfig;
import org.nightlabs.jfire.reporting.parameter.config.id.ReportParameterAcquisitionSetupID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ReportParameterAcquisitionSetupDAO
extends BaseJDOObjectDAO<ReportParameterAcquisitionSetupID, ReportParameterAcquisitionSetup>
{

	public static final String[] DEFAULT_FETCH_GROUPS = new String[] {
		FetchPlan.DEFAULT,
		ReportParameterAcquisitionSetup.FETCH_GROUP_DEFAULT_USE_CASE,
		ReportParameterAcquisitionSetup.FETCH_GROUP_VALUE_ACQUISITION_SETUPS,
		ValueAcquisitionSetup.FETCH_GROUP_THIS_VALUE_ACQUISITION_SETUP,
		ValueConsumerBinding.FETCH_GROUP_CONSUMER,
		ValueConsumerBinding.FETCH_GROUP_PROVIDER,
		ValueProviderConfig.FETCH_GROUP_MESSAGE,
		ReportParameterAcquisitionUseCase.FETCH_GROUP_NAME,
		ReportParameterAcquisitionUseCase.FETCH_GROUP_DESCRIPTION
	};

	/**
	 *
	 */
	public ReportParameterAcquisitionSetupDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<ReportParameterAcquisitionSetup> retrieveJDOObjects(
			Set<ReportParameterAcquisitionSetupID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
		)
		throws Exception
	{
		ReportParameterManagerRemote rpm = JFireEjb3Factory.getRemoteBean(ReportParameterManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return rpm.getReportParameterAcquisitionSetups(objectIDs, fetchGroups, maxFetchDepth);
	}

	public ReportParameterAcquisitionSetup getSetupForReportLayout(
			ReportRegistryItemID reportLayoutID,
			String[] fetchGroups,
			ProgressMonitor monitor
		)
	throws RemoteException, CreateException, NamingException, ModuleException
	{
		Set<ReportRegistryItemID> itemIDs = new HashSet<ReportRegistryItemID>(1);
		itemIDs.add(reportLayoutID);
		ReportParameterManagerRemote rpm = JFireEjb3Factory.getRemoteBean(ReportParameterManagerRemote.class, SecurityReflector.getInitialContextProperties());
		Map<ReportRegistryItemID, ReportParameterAcquisitionSetupID> ids = rpm.getReportParameterAcquisitionSetupIDs(itemIDs);
		ReportParameterAcquisitionSetupID setupID = ids.get(reportLayoutID);
		if (setupID == null)
			return null;
		return getJDOObject(null, setupID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	private static ReportParameterAcquisitionSetupDAO sharedInstance;

	public static ReportParameterAcquisitionSetupDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ReportParameterAcquisitionSetupDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ReportParameterAcquisitionSetupDAO();
			}
		}
		return sharedInstance;
	}
}
