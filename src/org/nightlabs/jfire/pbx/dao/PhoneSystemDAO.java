package org.nightlabs.jfire.pbx.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.config.id.ConfigID;
import org.nightlabs.jfire.pbx.Call;
import org.nightlabs.jfire.pbx.PhoneSystem;
import org.nightlabs.jfire.pbx.PhoneSystemException;
import org.nightlabs.jfire.pbx.PhoneSystemManagerRemote;
import org.nightlabs.jfire.pbx.id.PhoneSystemID;
import org.nightlabs.jfire.pbx.resource.Messages;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.workstation.id.WorkstationID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

/**
 * Data access object for {@link PhoneSystem}s.
 *
 * @author Chairat Kongarayawetchakun - chairat [AT] nightlabs [DOT] de
 */
public class PhoneSystemDAO
extends BaseJDOObjectDAO<PhoneSystemID, PhoneSystem>
{
	private static PhoneSystemDAO sharedInstance = null;

	public static PhoneSystemDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (PhoneSystemDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new PhoneSystemDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected synchronized Collection<PhoneSystem> retrieveJDOObjects(Set<PhoneSystemID> phoneSystemIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception {

		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.retrievePhoneSystems.monitor.task.name"), 1); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			return ejb.getPhoneSystems(phoneSystemIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public synchronized PhoneSystem storePhoneSystem(PhoneSystem phoneSystem, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor){
		if(phoneSystem == null)
			throw new NullPointerException("PhoneSystem to save must not be null"); //$NON-NLS-1$

		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.storePhoneSystem.monitor.task.name"), 3); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);

			PhoneSystem result = ejb.storePhoneSystem(phoneSystem, get, fetchGroups, maxFetchDepth);
			if (result != null)
				getCache().put(null, result, fetchGroups, maxFetchDepth);

			monitor.worked(1);
			return result;
		} finally {
			monitor.done();
		}
	}

	public synchronized void deletePhoneSystem(PhoneSystemID phoneSystemID, ProgressMonitor monitor) {
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.deletePhoneSystem.monitor.task.name"), 3); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);
			ejb.deletePhoneSystem(phoneSystemID);
			getCache().removeByObjectID(phoneSystemID, false);
			monitor.worked(2);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Get a single phoneSystem.
	 * @param phoneSystemID The ID of the phoneSystem to get
	 * @param fetchGroups which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The requested phoneSystem object
	 */
	public synchronized PhoneSystem getPhoneSystem(PhoneSystemID phoneSystemID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.getPhoneSystem.monitor.task.name"), 1); //$NON-NLS-1$
		try{
			PhoneSystem phoneSystem = getJDOObject(null, phoneSystemID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
			return phoneSystem;
		} finally {
			monitor.done();
		}
	}

	public List<PhoneSystem> getPhoneSystems(Set<PhoneSystemID> phoneSystemIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, phoneSystemIDs, fetchGroups, maxFetchDepth, monitor);
	}


	public synchronized <T extends PhoneSystem> Collection<T> getPhoneSystems(Class<T> phoneSystemClass, boolean includeSubclasses, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.getPhoneSystems.monitor.task.name"), 1); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<PhoneSystemID> phoneSystemIDs = ejb.getPhoneSystemIDs(phoneSystemClass, includeSubclasses);
			@SuppressWarnings("unchecked")
			Collection<T> c = (Collection<T>) getJDOObjects(null, phoneSystemIDs, fetchGroups, maxFetchDepth, monitor);
			return c;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Get all phoneSystems.
	 * @param fetchGroups Wich fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @param monitor The progress monitor for this action. For every downloaded
	 * 					object, <code>monitor.worked(1)</code> will be called.
	 * @return The phoneSystems.
	 */
	public synchronized Collection<PhoneSystem> getPhoneSystems(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.getPhoneSystems.monitor.task.name"), 1); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			Set<PhoneSystemID> phoneSystemIDs = ejb.getPhoneSystemIDs();
			return getJDOObjects(null, phoneSystemIDs, fetchGroups, maxFetchDepth, monitor);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Get the {@link PhoneSystem} that is currently assigned to the current workstation.
	 *
	 * @param workstationID the identifier of the workstation or <code>null</code> for the current user's workstation.
	 */
	public synchronized PhoneSystem getPhoneSystem(WorkstationID workstationID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.getPhoneSystem.monitor.task.name"), 2); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			PhoneSystemID phoneSystemID = ejb.getPhoneSystemID(workstationID);
			monitor.worked(1);

			if (phoneSystemID == null)
				return null;

			return getJDOObject(null, phoneSystemID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		} finally {
			monitor.done();
		}
	}

	public synchronized PhoneSystem getPhoneSystem(ConfigID configID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.getPhoneSystem.monitor.task.name"), 2); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			PhoneSystemID phoneSystemID = ejb.getPhoneSystemID(configID);
			monitor.worked(1);

			if (phoneSystemID == null)
				return null;

			return getJDOObject(null, phoneSystemID, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 1));
		} finally {
			monitor.done();
		}
	}

	public synchronized void call(Call call, ProgressMonitor monitor) throws PhoneSystemException
	{
		if (call == null)
			throw new IllegalArgumentException("call must not be null!"); //$NON-NLS-1$

		monitor.beginTask(Messages.getString("org.nightlabs.jfire.pbx.dao.PhoneSystemDAO.call.monitor.task.name"), 2); //$NON-NLS-1$
		try {
			PhoneSystemManagerRemote ejb = JFireEjb3Factory.getRemoteBean(PhoneSystemManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(1);
			ejb.call(call);
			monitor.worked(2);
		} finally {
			monitor.done();
		}
	}
}
