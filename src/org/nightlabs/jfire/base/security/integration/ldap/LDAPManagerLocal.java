package org.nightlabs.jfire.base.security.integration.ldap;
import java.util.Collection;

import javax.ejb.Local;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.security.integration.UserManagementSystemSyncException;
import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;
import org.nightlabs.jfire.timer.Task;
import org.nightlabs.jfire.timer.id.TaskID;

/**
 * Local interface for {@link LDAPManagerBean}
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
	 * @throws UserManagementSystemSyncException
	 * @throws UserManagementSystemCommunicationException
	 */
	void syncUserDataFromLDAP(TaskID taskID) throws LoginException, UserManagementSystemSyncException, UserManagementSystemCommunicationException;
	
	/**
	 * Get parent LDAP entries from {@link LDAPServer} scripts
	 * 
	 * @param ldapServerID
	 * @return Names of base parent entries
	 */
	Collection<String> getLDAPServerParentEntries(UserManagementSystemID ldapServerID);

}
