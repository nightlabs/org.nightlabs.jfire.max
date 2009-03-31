package org.nightlabs.jfire.issue.project;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.issue.IssueManager;
import org.nightlabs.jfire.issue.IssueManagerUtil;
import org.nightlabs.jfire.issue.project.id.DepartmentID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Data access object for {@link Department}s.
 * 
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class DepartmentDAO
extends BaseJDOObjectDAO<DepartmentID, Department>
{
	private DepartmentDAO() {}

	private static DepartmentDAO sharedInstance = null;

	public static DepartmentDAO sharedInstance() 
	{
		if (sharedInstance == null) {
			synchronized (DepartmentDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new DepartmentDAO();
			}
		}
		return sharedInstance;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	@Override
	/**
	 * {@inheritDoc}
	 */
	protected Collection<Department> retrieveJDOObjects(Set<DepartmentID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
			{
		monitor.beginTask("Fetching "+objectIDs.size()+" department information", 1);
		Collection<Department> departments;
		try {
			IssueManager im = JFireEjbFactory.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			departments = im.getDepartments(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(1);
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Failed downloading departments information!", e);
		}

		monitor.done();
		return departments;
			}

	public synchronized Department getDepartment(DepartmentID departmentID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, departmentID, fetchGroups, maxFetchDepth, monitor);
	}

	@SuppressWarnings("unchecked")
	public synchronized List<Department> getDepartments(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		Set<DepartmentID> departmentIDs;
		IssueManager issueManager;
		try {
			try {
				issueManager = IssueManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
				departmentIDs = issueManager.getDepartmentIDs();
			} catch (Exception x) {
				throw new RuntimeException(x);
			}
			return getJDOObjects(null, departmentIDs, fetchGroups, maxFetchDepth, monitor);
		} finally {
			issueManager = null;
		}
	}

	public List<Department> getDepartments(Set<DepartmentID> departmentIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, departmentIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public Department storeDepartment(Department department, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if(department == null)
			throw new NullPointerException("Department to save must not be null");
		monitor.beginTask("Storing department: "+ department.getDepartmentID(), 3);
		try {
			IssueManager im = JFireEjbFactory.getBean(IssueManager.class, SecurityReflector.getInitialContextProperties());
			Department result = im.storeDepartment(department, get, fetchGroups, maxFetchDepth);
			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing Department!\n" ,e);
		}
	}
}
