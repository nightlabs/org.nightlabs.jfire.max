package org.nightlabs.jfire.entityuserset.notification;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.ejb.CreateException;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.naming.NamingException;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.entityuserset.EntityUserSet;
import org.nightlabs.jfire.entityuserset.EntityUserSetManagerRemote;
import org.nightlabs.jfire.entityuserset.ResellerEntityUserSetFactory;
import org.nightlabs.jfire.entityuserset.id.EntityUserSetID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class EntityUserSetNotificationReceiver extends NotificationReceiver
{
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EntityUserSetNotificationReceiver() { }

	public EntityUserSetNotificationReceiver(NotificationFilter notificationFilter)
	{
		super(notificationFilter);
	}

	public EntityUserSetNotificationReceiver(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
	}

	@Override
	public void onReceiveNotificationBundle(NotificationBundle notificationBundle)
	throws Exception
	{
		PersistenceManager pm = getPersistenceManager();

		HashSet<EntityUserSetID> entityUserSetIDs_load = new HashSet<EntityUserSetID>();
		HashSet<EntityUserSetID> entityUserSetIDs_delete = new HashSet<EntityUserSetID>();
		for (DirtyObjectID dirtyObjectID : notificationBundle.getDirtyObjectIDs()) {
			EntityUserSetID entityUserSetID = (EntityUserSetID) dirtyObjectID.getObjectID();
			try {
				pm.getObjectById(entityUserSetID);
			} catch (JDOObjectNotFoundException x) {
				// ignore this - we only update existing ones
				continue;
			}

			if (JDOLifecycleState.DELETED.equals(dirtyObjectID.getLifecycleState())) {
				entityUserSetIDs_delete.add((EntityUserSetID) dirtyObjectID.getObjectID());
				entityUserSetIDs_load.remove(dirtyObjectID.getObjectID());
			}
			else if (JDOLifecycleState.NEW.equals(dirtyObjectID.getLifecycleState())) {
				entityUserSetIDs_delete.remove(dirtyObjectID.getObjectID());
				entityUserSetIDs_load.add((EntityUserSetID) dirtyObjectID.getObjectID());
			}
			else
				entityUserSetIDs_load.add((EntityUserSetID) dirtyObjectID.getObjectID());
		}
		replicateEntityUserSets(notificationBundle.getOrganisationID(), entityUserSetIDs_load, entityUserSetIDs_delete);
	}

	public void replicateEntityUserSets(String emitterOrganisationID, Set<EntityUserSetID> entityUserSetIDs_load, Set<EntityUserSetID> entityUserSetIDs_delete)
	throws NamingException, RemoteException, CreateException
	{
		if (entityUserSetIDs_load.isEmpty())
			return;

		PersistenceManager pm = getPersistenceManager();

		Hashtable<?,?> initialContextProperties = Lookup.getInitialContextProperties(pm, emitterOrganisationID);
		EntityUserSetManagerRemote entityUserSetManager = JFireEjb3Factory.getRemoteBean(EntityUserSetManagerRemote.class, initialContextProperties);
		Collection<EntityUserSet<Object>> backendEntityUserSets = CollectionUtil.castCollection(
				entityUserSetManager.getEntityUserSetsForReseller(entityUserSetIDs_load)
		);
		NLJDOHelper.makeDirtyAllFieldsRecursively(backendEntityUserSets);
		backendEntityUserSets = pm.makePersistentAll(backendEntityUserSets);

		for (EntityUserSet<Object> backendEntityUserSet : backendEntityUserSets) {
			ResellerEntityUserSetFactory<Object> resellerEntityUserSetFactory = ResellerEntityUserSetFactory.getResellerEntityUserSetFactory(pm, backendEntityUserSet.getEntityClassName(), false);
			if (resellerEntityUserSetFactory != null)
				resellerEntityUserSetFactory.configureResellerEntityUserSetForBackendEntityUserSet(backendEntityUserSet);
		}
	}
}
