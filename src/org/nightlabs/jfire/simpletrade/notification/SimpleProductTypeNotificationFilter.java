package org.nightlabs.jfire.simpletrade.notification;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.nightlabs.annotation.Implement;
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
 *		persistence-capable-superclass="org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter"
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

	@Implement
	public Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs)
	{
		// we notify only about modified ProductTypes that are owned by this organisation
		for (Iterator<DirtyObjectID> it = dirtyObjectIDs.iterator(); it.hasNext(); ) {
			DirtyObjectID dirtyObjectID = it.next();
			ProductTypeID productTypeID = (ProductTypeID) dirtyObjectID.getObjectID();
			if (!getOrganisationID().equals(productTypeID.organisationID))
				it.remove();
		}
		return dirtyObjectIDs;
	}
}
