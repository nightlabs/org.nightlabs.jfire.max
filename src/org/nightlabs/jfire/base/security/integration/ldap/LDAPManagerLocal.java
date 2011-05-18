package org.nightlabs.jfire.base.security.integration.ldap;
import javax.ejb.Local;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;

/**
 * Local interface for LDAPManagerBean
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Local
public interface LDAPManagerLocal {

	/**
	 * Synchronizes user data from LDAP directory to JFire objects. This method is supposed to be called by a timer {@link Task}.
	 * 
	 * @param taskID
	 * @throws LoginException
	 * @throws LDAPSyncException
	 * @throws UserManagementSystemCommunicationException
	 */
	void syncUserDataFromLDAP(TaskID taskID) throws LoginException, LDAPSyncException, UserManagementSystemCommunicationException;

}
