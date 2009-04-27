/**
 *
 */
package org.nightlabs.jfire.reporting.parameter.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.reporting.parameter.ReportParameterManagerRemote;
import org.nightlabs.jfire.reporting.parameter.ValueProvider;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProviderDAO
extends BaseJDOObjectDAO<ValueProviderID, ValueProvider> {

	/**
	 *
	 */
	public ValueProviderDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected Collection<ValueProvider> retrieveJDOObjects(
			Set<ValueProviderID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
		)
	throws Exception
	{
		ReportParameterManagerRemote rpm = JFireEjb3Factory.getRemoteBean(ReportParameterManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return rpm.getValueProviders(objectIDs, fetchGroups, maxFetchDepth);
	}


	public ValueProvider getValueProvider(ValueProviderID valueProviderID, String[] fetchGroups, ProgressMonitor monitor) {
		return getJDOObject(null, valueProviderID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	public Collection<ValueProvider> getValueProviders(Set<ValueProviderID> valueProviderIDs, String[] fetchGroups, ProgressMonitor monitor) {
		return getJDOObjects(null, valueProviderIDs, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}


	private static ValueProviderDAO sharedInstance;

	public static ValueProviderDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ValueProviderDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ValueProviderDAO();
			}
		}
		return sharedInstance;
	}

}
