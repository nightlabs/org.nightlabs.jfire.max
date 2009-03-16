package org.nightlabs.jfire.entityuserset.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.datastructure.IdentityHashSet;
import org.nightlabs.jfire.entityuserset.EntityUserSet;
import org.nightlabs.jfire.entityuserset.id.EntityUserSetID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
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
public class EntityUserSetNotificationFilter extends NotificationFilter
{
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EntityUserSetNotificationFilter() { }

	/**
	 * @param organisationID This is the organisation which emits notifications - i.e. where the JDO objects
	 *		focused by this <code>NotificationFilter</code> are added/modified/deleted.
	 * @param subscriberType This describes the type of the subscriber. If it is another organisation, then
	 *		this must be {@link #SUBSCRIBER_TYPE_ORGANISATION} - otherwise it is any other identifier-string
	 *		describing the type of the subscriber.
	 * @param subscriberID The identifier of the subscriber within the scope of the type. If the type is
	 *		{@link #SUBSCRIBER_TYPE_ORGANISATION}, then this subscriberID is the other organisationID (the one
	 *		that will be notified).
	 * @param subscriptionID An identifier chosen by the subscriber to reference this subscription.
	 */
	public EntityUserSetNotificationFilter(
			String organisationID,
			String subscriberType,
			String subscriberID,
			String subscriptionID
	)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
		getLifecycleStates().add(JDOLifecycleState.NEW);
		getLifecycleStates().add(JDOLifecycleState.DIRTY);
		getLifecycleStates().add(JDOLifecycleState.DELETED);
		getCandidateClasses().add(EntityUserSet.class.getName());
		setIncludeSubclasses(true); // EntityUserSet is an abstract class, thus we must include subclasses
	}

	@Override
	public Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs) {
		PersistenceManager pm = null;

		// We notify only about modified EntityUserSets that are owned by this organisation
		// and for which an EntityUserSetNotificationFilterEntry exists.

		Map<String, List<DirtyObjectID>> entityClassName2dirtyObjectIDs = new HashMap<String, List<DirtyObjectID>>();

		for (Iterator<DirtyObjectID> it = dirtyObjectIDs.iterator(); it.hasNext(); ) {
			DirtyObjectID dirtyObjectID = it.next();
			EntityUserSetID entityUserSetID = (EntityUserSetID) dirtyObjectID.getObjectID();
			if (!getOrganisationID().equals(entityUserSetID.organisationID)) {
				it.remove();
				continue;
			}

			List<DirtyObjectID> doids = entityClassName2dirtyObjectIDs.get(entityUserSetID.entityClassName);
			if (doids == null) {
				doids = new ArrayList<DirtyObjectID>();
				entityClassName2dirtyObjectIDs.put(entityUserSetID.entityClassName, doids);
			}
			doids.add(dirtyObjectID);
		}

		Query queryFindFilterEntriesForClass = null;

		IdentityHashSet<DirtyObjectID> passThrough = new IdentityHashSet<DirtyObjectID>();

		for (Map.Entry<String, List<DirtyObjectID>> me : entityClassName2dirtyObjectIDs.entrySet()) {
			String entityClassName = me.getKey();

			if (pm == null) pm = getPersistenceManager();
			if (queryFindFilterEntriesForClass == null) {
				queryFindFilterEntriesForClass = pm.newQuery(EntityUserSetNotificationFilterEntry.class);
				queryFindFilterEntriesForClass.setFilter("this.entityClassName == :entityClassName");
			}

			Collection<EntityUserSetNotificationFilterEntry> filterEntries = CollectionUtil.castCollection(
					(Collection<?>)queryFindFilterEntriesForClass.execute(entityClassName)
			);

			for (EntityUserSetNotificationFilterEntry filterEntry : filterEntries) {
				List<DirtyObjectID> doids = new ArrayList<DirtyObjectID>(me.getValue());
				Collection<DirtyObjectID> c = filterEntry.filter(doids);
				if (c != null)
					passThrough.addAll(c);
			}
		}

		if (passThrough.isEmpty())
			return null;

		List<DirtyObjectID> result = new ArrayList<DirtyObjectID>(passThrough.size());
		for (DirtyObjectID dirtyObjectID : dirtyObjectIDs) {
			if (passThrough.contains(dirtyObjectID))
				result.add(dirtyObjectID);
		}

		return result;
	}

}
