package org.nightlabs.jfire.geography.notification;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.geography.CSV;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter;

import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @jdo.persistence-capable
 *              identity-type="application"
 *              persistence-capable-superclass="org.nightlabs.jfire.jdo.notification.persistent.NotificationFilter"
 *              detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class GeographyTemplateDataNotificationFilter
extends NotificationFilter
{
	private static final Logger logger = Logger.getLogger(GeographyTemplateDataNotificationFilter.class);

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected GeographyTemplateDataNotificationFilter() { }

	public GeographyTemplateDataNotificationFilter(String organisationID,
			String subscriberType, String subscriberID, String subscriptionID)
	{
		super(organisationID, subscriberType, subscriberID, subscriptionID);
		getCandidateClasses().add(CSV.class.getName());
		getLifecycleStates().add(JDOLifecycleState.NEW);
		getLifecycleStates().add(JDOLifecycleState.DIRTY);
//		getLifecycleStates().add(JDOLifecycleState.DELETED); // probably, there will never be this case - CSV instances cannot be deleted
	}

	@Override
	public Collection<DirtyObjectID> filter(List<DirtyObjectID> dirtyObjectIDs)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("filter: dirtyObjectIDs:");
			for (Iterator<DirtyObjectID> it = dirtyObjectIDs.iterator(); it.hasNext();) {
				DirtyObjectID dirtyObjectID = it.next();
				logger.debug("  " + dirtyObjectID);
			}
		}

		return dirtyObjectIDs;
	}
}