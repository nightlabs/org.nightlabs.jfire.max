package org.nightlabs.jfire.department;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.department.id.DepartmentID;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;

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
public class DepartmentManagerBean 
extends BaseSessionBeanImpl
implements SessionBean
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(DepartmentManagerBean.class);
	
	/**
	 * @ejb.create-method
	 * @ejb.permission role-name="_Guest_"
	 */
	public void ejbCreate()
	throws CreateException
	{
		logger.debug(this.getClass().getName() + ".ejbCreate()");
	}
	
	/**
	 * @see javax.ejb.SessionBean#ejbRemove()
	 *
	 * @ejb.permission unchecked="true"
	 */
	public void ejbRemove() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbRemove()");
	}

	/**
	 * @see javax.ejb.SessionBean#ejbActivate()
	 */
	public void ejbActivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbActivate()");
	}
	/**
	 * @see javax.ejb.SessionBean#ejbPassivate()
	 */
	public void ejbPassivate() throws EJBException, RemoteException
	{
		logger.debug(this.getClass().getName() + ".ejbPassivate()");
	}

	@Override
	public void setSessionContext(SessionContext sessionContext) throws EJBException,
			RemoteException {
		logger.debug(this.getClass().getName() + ".setSessionContext("+sessionContext+")");
		super.setSessionContext(sessionContext);
	}
	
	//Department//
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 * @ejb.transaction type="Required"
	 */
	public Department storeDepartment(Department department, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, department, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public List<Department> getDepartments(Collection<DepartmentID> departmentIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, departmentIDs, Department.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}
	
	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@SuppressWarnings("unchecked")
	public Set<DepartmentID> getDepartmentIDs()
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			Query q = pm.newQuery(Department.class);
			q.setResult("JDOHelper.getObjectId(this)");
			return new HashSet<DepartmentID>((Collection<? extends DepartmentID>) q.execute());
		} finally {
			pm.close();
		}
	}
	
	//Bean//
	/**
	 * @throws IOException While loading an icon from a local resource, this might happen and we don't care in the initialise method.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	public void initialise() throws Exception
	{
		PersistenceManager pm = getPersistenceManager();
		try {
			UserID systemUserID = UserID.create(getOrganisationID(), getUserID());
			User systemUser = (User)pm.getObjectById(systemUserID);
			
			pm.getExtent(Department.class);

			// The complete method is executed in *one* transaction. So if one thing fails, all fail.
			// => We check once at the beginning, if this module has already been initialised.
			ModuleMetaData moduleMetaData = ModuleMetaData.getModuleMetaData(pm, JFireDepartmentEAR.MODULE_NAME);
			if (moduleMetaData != null)
				return;

			logger.info("Initialization of " + JFireDepartmentEAR.MODULE_NAME + " started...");

			pm.makePersistent(new ModuleMetaData(
					JFireDepartmentEAR.MODULE_NAME, "0.9.7-0-beta", "0.9.7-0-beta")
			);
			
			String baseName = "org.nightlabs.jfire.department.resource.messages";
			ClassLoader loader = DepartmentManagerBean.class.getClassLoader();
			
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
