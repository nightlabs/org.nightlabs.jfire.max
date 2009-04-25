package org.nightlabs.jfire.department.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.department.Department;
import org.nightlabs.jfire.department.DepartmentManagerRemote;
import org.nightlabs.jfire.department.id.DepartmentID;
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

	@Override
	protected Collection<Department> retrieveJDOObjects(Set<DepartmentID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
			{
		monitor.beginTask("Fetching "+objectIDs.size()+" department information", 1);
		Collection<Department> departments = null;
		try {
			DepartmentManagerRemote departmentManager = JFireEjb3Factory.getRemoteBean(DepartmentManagerRemote.class, SecurityReflector.getInitialContextProperties());
			departments = departmentManager.getDepartments(objectIDs, fetchGroups, maxFetchDepth);
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

	public synchronized List<Department> getDepartments(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		monitor.beginTask("Loading departments", 1);
		try {
			DepartmentManagerRemote departmentManager = JFireEjb3Factory.getRemoteBean(DepartmentManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<DepartmentID> departmentIDs = departmentManager.getDepartmentIDs();
			monitor.done();
			return getJDOObjects(null, departmentIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
			DepartmentManagerRemote departmentManager = JFireEjb3Factory.getRemoteBean(DepartmentManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);

			Department result = departmentManager.storeDepartment(department, get, fetchGroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			monitor.worked(1);
			monitor.done();
			return result;
		} catch (Exception e) {
			monitor.done();
			throw new RuntimeException("Error while storing Department!\n" ,e);
		}
	}
}
