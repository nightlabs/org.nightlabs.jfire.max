package org.nightlabs.jfire.simpletrade.notification;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.simpletrade.store.SimpleProductType;
import org.nightlabs.jfire.store.id.ProductTypeID;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class SimpleProductTypeNotificationFilter
		extends NotificationFilter
{
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SimpleProductTypeNotificationFilter() { }

	public SimpleProductTypeNotificationFilter(String organisationID,
			String subscriberType, String subscriberID, String subscriptionID)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
		getCandidateClasses().add(SimpleProductType.class.getName());
		getLifecycleStates().add(JDOLifecycleState.NEW);
		getLifecycleStates().add(JDOLifecycleState.DIRTY);
		getLifecycleStates().add(JDOLifecycleState.DELETED);
	}

	@Override
	public Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs)
	{
		PersistenceManager pm = null;

		// we notify only about modified ProductTypes that are owned by this organisation
		for (Iterator<DirtyObjectID> it = dirtyObjectIDs.iterator(); it.hasNext(); ) {
			DirtyObjectID dirtyObjectID = it.next();
			ProductTypeID productTypeID = (ProductTypeID) dirtyObjectID.getObjectID();
			if (!getOrganisationID().equals(productTypeID.organisationID)) {
				it.remove();
				continue;
			}

			if (pm == null) pm = getPersistenceManager();
			SimpleProductType simpleProductType = (SimpleProductType) pm.getObjectById(productTypeID);
			if (!simpleProductType.isPublished())
				it.remove();
		}
		return dirtyObjectIDs;
	}
}
