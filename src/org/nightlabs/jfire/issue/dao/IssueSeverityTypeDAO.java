package org.nightlabs.jfire.issue.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueSeverityType;
import org.nightlabs.jfire.issue.id.IssueSeverityTypeID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Data access object for {@link IssueSeverityType}s.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class IssueSeverityTypeDAO
		extends BaseJDOObjectDAO<IssueSeverityTypeID, IssueSeverityType>
{
	private IssueSeverityTypeDAO() {}

	private static IssueSeverityTypeDAO sharedInstance = null;

	public static IssueSeverityTypeDAO sharedInstance() 
	{
		if (sharedInstance == null) {
			synchronized (IssueSeverityTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new IssueSeverityTypeDAO();
			}
		}
		return sharedInstance;
	}
	
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected Collection<IssueSeverityType> retrieveJDOObjects(Set<IssueSeverityTypeID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Fetching "+objectIDs.size()+" severity types information", 1);
		Collection<IssueSeverityType> issueSeverityTypes;
		try {
			IssueManager im = JFireEjbUtil.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			issueSeverityTypes = im.getIssueSeverityTypes(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Failed downloading severity types information!", e);
		}
		
		monitor.done();
		return issueSeverityTypes;
	}

	public synchronized IssueSeverityType getIssueSeverityType(IssueSeverityTypeID issueSeverityTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, issueSeverityTypeID, fetchGroups, maxFetchDepth, monitor);
	}
	
	public List<IssueSeverityType> getIssueSeverityTypes(Set<IssueSeverityTypeID> issueSeverityTypeIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, issueSeverityTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public IssueSeverityType storeIssueSeverityType(IssueSeverityType issueSeverityType, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if(issueSeverityType == null)
			throw new NullPointerException("Issue to save must not be null");
		monitor.beginTask("Storing issueSeverityType: "+ issueSeverityType.getIssueSeverityTypeID(), 3);
		try {
			IssueManager im = JFireEjbUtil.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			IssueSeverityType result = im.storeIssueSeverityType(issueSeverityType, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing IssueSeverityType!\n" ,e);
		}
	}
}
