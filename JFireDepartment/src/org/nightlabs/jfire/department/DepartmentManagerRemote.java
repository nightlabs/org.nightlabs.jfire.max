package org.nightlabs.jfire.department;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.department.id.DepartmentID;

@Remote
public interface DepartmentManagerRemote {

	Department storeDepartment(Department department, boolean get, String[] fetchGroups, int maxFetchDepth);

	List<Department> getDepartments(Collection<DepartmentID> departmentIDs, String[] fetchGroups, int maxFetchDepth);

	Set<DepartmentID> getDepartmentIDs();

	void initialise() throws Exception;

}