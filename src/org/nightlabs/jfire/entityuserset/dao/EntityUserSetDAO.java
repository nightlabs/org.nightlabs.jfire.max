package org.nightlabs.jfire.entityuserset.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.entityuserset.EntityUserSet;
import org.nightlabs.jfire.entityuserset.EntityUserSetManagerRemote;
import org.nightlabs.jfire.entityuserset.id.EntityUserSetID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * Data Access Object for retrieving {@link EntityUserSet}s from a client.
 *
 * @author Daniel Mazurek - Daniel.Mazurek [dot] nightlabs [dot] de
 */
public class EntityUserSetDAO
extends BaseJDOObjectDAO<EntityUserSetID, EntityUserSet<?>>
implements IJDOObjectDAO<EntityUserSet<?>>
{
	private static EntityUserSetDAO sharedInstance;

	/**
	 * Returns the (static) EntityUserSetDAO singleton.
	 * @return the (static) singleton as shared instance.
	 */
	public static EntityUserSetDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (EntityUserSetDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new EntityUserSetDAO();
			}
		}
		return sharedInstance;
	}

	private EntityUserSetDAO() {}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<EntityUserSet<?>> retrieveJDOObjects(Set<EntityUserSetID> entityUserSetsIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Loading EntityUserSet", 100);
		try {
			EntityUserSetManagerRemote entityUserSetManager = getEjbProvider().getRemoteBean(EntityUserSetManagerRemote.class);
			List<EntityUserSet<?>> entityUserSets = entityUserSetManager.getEntityUserSets(entityUserSetsIDs, fetchGroups, maxFetchDepth);
			monitor.worked(100);
			return entityUserSets;
		} finally {
			monitor.done();
		}
	}

	/**
	 * Returns the {@link EntityUserSet}s for the given {@link EntityUserSetID}s with the given content (fetchGroups, fetchDepth).
	 *
	 * @param entityUserSetsIDs the {@link EntityUserSetID}s to get the {@link EntityUserSet}s for.
	 * @param fetchGroups the JDO Fetchgroups will determine which information should be transfered/detached.
	 * @param maxFetchDepth the maximum fetch depth of the detached objects.
	 * @param monitor the {@link ProgressMonitor} to display the progress.
	 * @return the detached {@link EntityUserSet}s.
	 */
	@SuppressWarnings("unchecked")
	public <EUS extends EntityUserSet<?>> List<EUS> getEntityUserSets(Set<EntityUserSetID> entityUserSetsIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return (List<EUS>) getJDOObjects(null, entityUserSetsIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns the {@link EntityUserSet} for the given {@link EntityUserSetID} with the given content (fetchGroups, fetchDepth).
	 *
	 * @param entityUserSetID the {@link EntityUserSetID} to get the {@link EntityUserSet} for.
	 * @param fetchGroups the JDO Fetchgroups will determine which information should be transfered/detached.
	 * @param maxFetchDepth the maximum fetch depth of the detached objects.
	 * @param monitor the {@link ProgressMonitor} to display the progress.
	 * @return the detached {@link EntityUserSet}.
	 */
	@SuppressWarnings("unchecked")
	public <E> EntityUserSet<E> getEntityUserSet(EntityUserSetID entityUserSetID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return (EntityUserSet<E>) getJDOObject(null, entityUserSetID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Stores the given EntityUserSet.
	 *
	 * @param entityUserSet The {@link EntityUserSet} to save.
	 * @param get determines whether the stored instance should be returned if true or null if false.
	 * @param fetchGroups the JDO Fetchgroups will determine which information should be transfered/detached.
	 * @param maxFetchDepth the maximum fetch depth of the detached objects.
	 * @param monitor the {@link ProgressMonitor} to display the progress.
	 * @return the stored EntityUserSet if get is true or null if get is false
	 */
	@SuppressWarnings("unchecked")
	public <E> EntityUserSet<E> storeEntityUserSet(EntityUserSet<?> entityUserSet, boolean get, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Saving EntityUserSet", 100);
		try {
			EntityUserSetManagerRemote entityUserSetManager = getEjbProvider().getRemoteBean(EntityUserSetManagerRemote.class);
			return (EntityUserSet<E>) entityUserSetManager.storeEntityUserSet(entityUserSet, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	public EntityUserSet<?> storeJDOObject(EntityUserSet<?> jdoObject, boolean get, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return storeEntityUserSet(jdoObject, get, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Returns all {@link EntityUserSetID}s of all {@link EntityUserSet} from the given organisation,
	 * which manage an entity of the given entityClass.
	 *
	 * @param organisationID the organisationID
	 * @param entityClass the class of the entity type to get all corresponding {@link EntityUserSet} of the given organisation for.
	 * @param monitor the {@link ProgressMonitor} to display the progress.
	 * @return all {@link EntityUserSetID}s of all {@link EntityUserSet} which manage an entity of the given entityClass.
	 */
	public Set<EntityUserSetID> getEntityUserSetIDs(String organisationID, Class<?> entityClass, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading EntityUserSets", 100);
		try {
			EntityUserSetManagerRemote entityUserSetManager = getEjbProvider().getRemoteBean(EntityUserSetManagerRemote.class);
			return entityUserSetManager.getEntityUserSetIDs(organisationID, entityClass);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}
}
