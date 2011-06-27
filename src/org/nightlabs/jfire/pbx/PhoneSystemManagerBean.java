package org.nightlabs.jfire.pbx;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.ModuleMetaData;
import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.config.Config;
import org.nightlabs.jfire.config.ConfigSetup;
import org.nightlabs.jfire.config.WorkstationConfigSetup;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.pbx.config.PhoneSystemConfigModule;
import org.nightlabs.jfire.pbx.id.PhoneSystemID;
import org.nightlabs.jfire.workstation.Workstation;
import org.nightlabs.jfire.workstation.WorkstationResolveStrategy;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless
public class PhoneSystemManagerBean
extends BaseSessionBeanImpl
implements PhoneSystemManagerRemote
{
	private static final long serialVersionUID = 1L;

	/**
	 * LOG4J logger used by this class
	 */
	private static final Logger logger = Logger.getLogger(PhoneSystemManagerBean.class);

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public PhoneSystem storePhoneSystem(PhoneSystem phoneSystem, boolean get, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.storeJDO(pm, phoneSystem, get, fetchGroups, maxFetchDepth);
		}//try
		finally {
			pm.close();
		}//finally
	}


	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	@Override
	public void deletePhoneSystem(PhoneSystemID projectID)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			pm.getFetchPlan().setGroup(FetchPlan.DEFAULT);
			pm.getExtent(PhoneSystem.class, true);
			PhoneSystem phoneSystem = (PhoneSystem) pm.getObjectById(projectID);
			pm.deletePersistent(phoneSystem);
			pm.flush();
		}//try
		finally {
			pm.close();
		}//finally
	}

	@RolesAllowed("_Guest_")
	@Override
	public List<PhoneSystem> getPhoneSystems(Collection<PhoneSystemID> phoneSystemIDs, String[] fetchGroups, int maxFetchDepth)
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			return NLJDOHelper.getDetachedObjectList(pm, phoneSystemIDs, PhoneSystem.class, fetchGroups, maxFetchDepth);
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<PhoneSystemID> getPhoneSystemIDs(Class<? extends PhoneSystem> phoneSystemClass, boolean includeSubclasses)
	{
		if (phoneSystemClass == null)
			throw new IllegalArgumentException("phoneSystemClass must not be null!"); //$NON-NLS-1$

		if (!PhoneSystem.class.isAssignableFrom(phoneSystemClass))
			throw new IllegalArgumentException("phoneSystemClass is neither PhoneSystem nor a subclass of PhoneSystem: " + phoneSystemClass.getName()); //$NON-NLS-1$

		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(pm.getExtent(phoneSystemClass, includeSubclasses));
			q.setResult("JDOHelper.getObjectId(this)"); //$NON-NLS-1$

			return CollectionUtil.createHashSetFromCollection( q.execute() );
		} finally {
			pm.close();
		}
	}

	@RolesAllowed("_Guest_")
	@Override
	public Set<PhoneSystemID> getPhoneSystemIDs()
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			Query q = pm.newQuery(PhoneSystem.class);
			q.setResult("JDOHelper.getObjectId(this)"); //$NON-NLS-1$

			return CollectionUtil.createHashSetFromCollection( q.execute() );
		} finally {
			pm.close();
		}
	}

	public static String CALL_FILE_TEMP_DIRECTORY = "jfire.tmp"; //$NON-NLS-1$

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	public void call(Call call) throws PhoneSystemException
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			PhoneSystemConfigModule phoneSystemConfigModule = WorkstationConfigSetup.getWorkstationConfigModule(
					pm,
					PhoneSystemConfigModule.class
			);

			PhoneSystem phoneSystem = phoneSystemConfigModule.getPhoneSystem();

			if (phoneSystem == null) {
				WorkstationID workstationID = null;
				if (getWorkstationID() != null)
					workstationID = WorkstationID.create(getOrganisationID(), getWorkstationID());

				throw new NoPhoneSystemAssignedException(workstationID);
			}

			phoneSystem.call(call);
		} finally {
			pm.close();
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	public void initialise() throws Exception
	{
		PersistenceManager pm = createPersistenceManager();
		try {
			ModuleMetaData.initModuleMetadata(pm, JFirePBXEAR.MODULE_NAME, JFirePBXEAR.class);
			
			pm.getExtent(ClientOnlyPhoneSystem.class);
			DefaultPhoneSystem defaultPhoneSystem = DefaultPhoneSystem.getDefaultPhoneSystem(pm);

			ClientOnlyPhoneSystem clientOnlyPhoneSystem;
			try {
				clientOnlyPhoneSystem = (ClientOnlyPhoneSystem) pm.getObjectById(PhoneSystemID.create(getOrganisationID(), ClientOnlyPhoneSystem.ID_DEFAULT_CLIENT_ONLY_PHONE_SYSTEM));
			} catch (JDOObjectNotFoundException x) {
				clientOnlyPhoneSystem = pm.makePersistent(new ClientOnlyPhoneSystem(getOrganisationID(), ClientOnlyPhoneSystem.ID_DEFAULT_CLIENT_ONLY_PHONE_SYSTEM));
				defaultPhoneSystem.setPhoneSystem(clientOnlyPhoneSystem);
			}

			clientOnlyPhoneSystem.getName().readFromProperties(
					"org.nightlabs.jfire.pbx.resource.messages", //$NON-NLS-1$
					ClientOnlyPhoneSystem.class.getClassLoader(),
					"org.nightlabs.jfire.pbx.ClientOnlyPhoneSystem.defaultClientOnlyPhoneSystem.name" //$NON-NLS-1$
			);

			ConfigSetup workstationConfigSetup = ConfigSetup.getConfigSetup(
					pm,
					getOrganisationID(),
					WorkstationConfigSetup.CONFIG_SETUP_TYPE_WORKSTATION
			);
			Set<String> configModuleClasses = workstationConfigSetup.getConfigModuleClasses();
			if (!configModuleClasses.contains(PhoneSystemConfigModule.class.getName())) // DataNucleus workaround: the following add causes an exception, if it's already there. thus, we need to check.
				configModuleClasses.add(PhoneSystemConfigModule.class.getName());
		} finally {
			pm.close();
		}
	}

	@Override
	public PhoneSystemID getPhoneSystemID(WorkstationID workstationID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Workstation workstation = Workstation.getWorkstation(
					pm,
					(workstationID == null ? getOrganisationID() : workstationID.organisationID),
					(workstationID == null ? null : workstationID.workstationID),
					WorkstationResolveStrategy.FALLBACK
			);
			Config config = Config.getConfig(pm, workstation.getOrganisationID(), workstation);
			PhoneSystemConfigModule phoneSystemConfigModule = config.createConfigModule(PhoneSystemConfigModule.class);
			PhoneSystem phoneSystem = phoneSystemConfigModule.getPhoneSystem();
			return (PhoneSystemID) JDOHelper.getObjectId(phoneSystem);
		} finally {
			pm.close();
		}
	}

	@Override
	public PhoneSystemID getPhoneSystemID(ConfigID configID) {
		PersistenceManager pm = createPersistenceManager();
		try {
			Config config = (Config) pm.getObjectById(configID);
			PhoneSystemConfigModule phoneSystemConfigModule = config.createConfigModule(PhoneSystemConfigModule.class);
			PhoneSystem phoneSystem = phoneSystemConfigModule.getPhoneSystem();
			return (PhoneSystemID) JDOHelper.getObjectId(phoneSystem);
		} finally {
			pm.close();
		}
	}
}