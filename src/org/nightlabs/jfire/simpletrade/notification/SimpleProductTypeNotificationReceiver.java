package org.nightlabs.jfire.simpletrade.notification;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationBundle;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver;
import org.nightlabs.jfire.simpletrade.SimpleTradeManager;
import org.nightlabs.jfire.simpletrade.SimpleTradeManagerUtil;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.jdo.notification.persistent.NotificationReceiver"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class SimpleProductTypeNotificationReceiver
extends NotificationReceiver
{
	/**
	 * @deprecated Only for JDO!
	 */
	protected SimpleProductTypeNotificationReceiver() { }

	public SimpleProductTypeNotificationReceiver(String organisationID, String subscriberType, String subscriberID, String subscriptionID)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
	}

	public SimpleProductTypeNotificationReceiver(NotificationFilter notificationFilter)
	{
		super(notificationFilter);
	}

	@SuppressWarnings("unchecked")
	@Implement
	public void onReceiveNotificationBundle(NotificationBundle notificationBundle)
	throws Exception
	{
		HashSet productTypeIDs_load = new HashSet();
		HashSet productTypeIDs_delete = new HashSet();
		for (DirtyObjectID dirtyObjectID : notificationBundle.getDirtyObjectIDs()) {
			if (JDOLifecycleState.DELETED.equals(dirtyObjectID.getLifecycleState())) {
				productTypeIDs_delete.add(dirtyObjectID.getObjectID());
				productTypeIDs_load.remove(dirtyObjectID.getObjectID());
			}
			else if (JDOLifecycleState.NEW.equals(dirtyObjectID.getLifecycleState())) {
				productTypeIDs_delete.remove(dirtyObjectID.getObjectID());
				productTypeIDs_load.add(dirtyObjectID.getObjectID());
			}
			else
				productTypeIDs_load.add(dirtyObjectID.getObjectID());
		}

		if (productTypeIDs_load.isEmpty())
			return;

		PersistenceManager pm = getPersistenceManager();

		Hashtable initialContextProperties = Lookup.getInitialContextProperties(pm, notificationBundle.getOrganisationID());
		SimpleTradeManager simpleTradeManager = SimpleTradeManagerUtil.getHome(initialContextProperties).create();
		Collection productTypes = simpleTradeManager.getSimpleProductTypesForReseller(productTypeIDs_load, false);
		pm.makePersistentAll(productTypes);
	}
}
