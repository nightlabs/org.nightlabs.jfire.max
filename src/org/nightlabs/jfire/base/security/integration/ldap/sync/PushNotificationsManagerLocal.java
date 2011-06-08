package org.nightlabs.jfire.base.security.integration.ldap.sync;
import javax.ejb.Local;

import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;

/**
 * Local interface for {@link PushNotificationsManagerBean}
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Local
public interface PushNotificationsManagerLocal {

	/**
	 * Called by timer {@link Task} for adding push notification listener to leading {@link LDAPServer} instances
	 * 
	 * @param taskID
	 */
	void configurePushNotificationListeners(TaskID taskID);
	
}
