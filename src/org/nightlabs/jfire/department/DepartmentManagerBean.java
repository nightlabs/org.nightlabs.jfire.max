package org.nightlabs.jfire.department;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.department.id.DepartmentID;
import org.nightlabs.jfire.idgenerator.IDGenerator;

/**
 * An EJB session bean provides methods for managing every objects used in the departments
 * <p>
 *
 * </p>
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 *
 * @ejb.bean name="jfire/ejb/JFireDepartment/DepartmentManager"
 *           jndi-name="jfire/ejb/JFireDepartment/DepartmentManager"
 *           type="Stateless"
 *
 * @ejb.util generate="physical"
 * @ejb.transaction type="Required"
*/
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class DepartmentManagerBean
extends BaseSessionBeanImpl
implements DepartmentManagerRemote
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DepartmentManagerBean.class);

	//Department//
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.department.DepartmentManagerRemote#storeDepartment(org.nightlabs.jfire.department.Department, boolean, java.lang.String[], int)
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public Department storeDepartment(Department department, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, department, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.department.DepartmentManagerRemote#getDepartments(java.util.Collection, java.lang.String[], int)
	 */
	@RolesAllowed("_Guest_")
	public List<Department> getDepartments(Collection<DepartmentID> departmentIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, departmentIDs, Department.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.department.DepartmentManagerRemote#getDepartmentIDs()
	 */
	@RolesAllowed("_Guest_")
	@SuppressWarnings("unchecked")
	public Set<DepartmentID> getDepartmentIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(Department.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<DepartmentID>((Collection<? extends DepartmentID>) q.execute());
		} finally {
			pm.close();
		}
	}

	//Bean//
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.department.DepartmentManagerRemote#initialise()
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
//			UserID systemUserID = UserID.create(getOrganisationID(), getUserID());
//			User systemUser = (User)pm.getObjectById(systemUserID);

			pm.getExtent(Department.class);

			// The complete method is executed in *one* transaction. So if one thing fails, all fail.
			// => We check once at the beginning, if this module has already been initialised.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireDepartmentEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of " + JFireDepartmentEAR.MODULE_NAME + " started...");

			moduleMetaData = pm.makePersistent(
					ModuleMetaData.createModuleMetaDataFromManifest(JFireDepartmentEAR.MODULE_NAME, JFireDepartmentEAR.class)
			);

//			String baseName = "org.nightlabs.jfire.department.resource.messages";
//			ClassLoader loader = DepartmentManagerBean.class.getClassLoader();

			pm.getExtent(Department.class);

			Department department = new Department(IDGenerator.getOrganisationID(), IDGenerator.nextID(Department.class));
			department.getName().setText(Locale.ENGLISH.getLanguage(), "Department 1");
//			department.getName().readFromProperties(baseName, loader,
//			"org.nightlabs.jfire.department.DepartmentManagerBean.department1"); //$NON-NLS-1$
			department = pm.makePersistent(department);

			department = new Department(IDGenerator.getOrganisationID(), IDGenerator.nextID(Department.class));
			department.getName().setText(Locale.ENGLISH.getLanguage(), "Department 2");
//			department.getName().readFromProperties(baseName, loader,
//			"org.nightlabs.jfire.department.DepartmentManagerBean.department2"); //$NON-NLS-1$
			department = pm.makePersistent(department);

			department = new Department(IDGenerator.getOrganisationID(), IDGenerator.nextID(Department.class));
			department.getName().setText(Locale.ENGLISH.getLanguage(), "Department 3");
//			department.getName().readFromProperties(baseName, loader,
//			"org.nightlabs.jfire.department.DepartmentManagerBean.department3"); //$NON-NLS-1$
			department = pm.makePersistent(department);
		} finally {
			pm.close();
		}
	}
}
