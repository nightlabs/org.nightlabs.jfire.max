package org.nightlabs.jfire.store.notification;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductTypePermissionFlagSet;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		table="JFireTrade_ProductTypePermissionFlagSetNotificationFilter"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ProductTypePermissionFlagSetNotificationFilter")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class ProductTypePermissionFlagSetNotificationFilter
extends NotificationFilter
{
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTypePermissionFlagSetNotificationFilter() { }

	public ProductTypePermissionFlagSetNotificationFilter(
			String organisationID, String subscriberType, String subscriberID,
			String subscriptionID
	)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
		getCandidateClasses().add(ProductTypePermissionFlagSet.class.getName());
		getLifecycleStates().add(JDOLifecycleState.NEW);
		getLifecycleStates().add(JDOLifecycleState.DIRTY);
		getLifecycleStates().add(JDOLifecycleState.DELETED);
	}

	@Override
	public Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs)
	{
		String emitterOrganisationID = getOrganisationID();
		String userID = User.USER_ID_PREFIX_TYPE_ORGANISATION + getSubscriberID();
		for (Iterator<DirtyObjectID> it = dirtyObjectIDs.iterator(); it.hasNext();) {
			DirtyObjectID dirtyObjectID = it.next();

			ProductTypePermissionFlagSetID flagSetID = (ProductTypePermissionFlagSetID) dirtyObjectID.getObjectID();

			if (!userID.equals(flagSetID.userID) || !emitterOrganisationID.equals(flagSetID.userOrganisationID)) {
				it.remove();
				continue;
			}


		}

		return dirtyObjectIDs;
	}

}
