package org.nightlabs.jfire.base.security.integration.ldap.sync;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.base.BaseSessionBeanImpl;
import org.nightlabs.jfire.timer.id.TaskID;

/**
 * EJB for {@link PushNotificationsConfigurator} stuff, i.e. method for adding push notification listeners by timer task.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@Stateless(
		mappedName="jfire/ejb/JFireLDAP/PushNotificationsManager", 
		name="jfire/ejb/JFireLDAP/PushNotificationsManager"
			)
public class PushNotificationsManagerBean extends BaseSessionBeanImpl implements PushNotificationsManagerLocal {
	
	/**
	 * {@inheritDoc}
	 */
	@RolesAllowed("_System_")
	@Override
	public void configurePushNotificationListeners(TaskID taskID) {
		PersistenceManager pm = createPersistenceManager();
		try{
			
			PushNotificationsConfigurator.sharedInstance().initialize(pm, getOrganisationID());
			PushNotificationsConfigurator.sharedInstance().addPushNotificationListeners(pm);
			PushNotificationsConfigurator.sharedInstance().removePushNotificationListeners(pm);
			
		}finally{
			pm.close();
		}
	}

}
