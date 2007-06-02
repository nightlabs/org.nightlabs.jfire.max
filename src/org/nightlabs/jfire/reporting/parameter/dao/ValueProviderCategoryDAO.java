/**
 * 
 */
package org.nightlabs.jfire.reporting.parameter.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.reporting.parameter.ReportParameterManager;
import org.nightlabs.jfire.reporting.parameter.ReportParameterManagerUtil;
import org.nightlabs.jfire.reporting.parameter.ValueProviderCategory;
import org.nightlabs.jfire.reporting.parameter.id.ValueProviderCategoryID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class ValueProviderCategoryDAO 
extends BaseJDOObjectDAO<ValueProviderCategoryID, ValueProviderCategory> {

	/**
	 * 
	 */
	public ValueProviderCategoryDAO() {
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.JDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Collection<ValueProviderCategory> retrieveJDOObjects(
			Set<ValueProviderCategoryID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
		) 
	throws Exception 
	{
		ReportParameterManager rpm = ReportParameterManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return rpm.getValueProviderCategories(objectIDs, fetchGroups, maxFetchDepth);
	}
	
	
	public ValueProviderCategory getValueProviderCategory(ValueProviderCategoryID valueProviderCategoryID, String[] fetchGroups, ProgressMonitor monitor) {
		return getJDOObject(null, valueProviderCategoryID, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}
	
	public Collection<ValueProviderCategory> getValueProviderCategories(Set<ValueProviderCategoryID> valueProviderCategoryIDs, String[] fetchGroups, ProgressMonitor monitor) {
		return getJDOObjects(null, valueProviderCategoryIDs, fetchGroups, NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT, monitor);
	}

	
	
	private static ValueProviderCategoryDAO sharedInstance;
	
	public static ValueProviderCategoryDAO sharedInstance() {
		if (sharedInstance == null)
			sharedInstance = new ValueProviderCategoryDAO();
		return sharedInstance;
	}
	
}
