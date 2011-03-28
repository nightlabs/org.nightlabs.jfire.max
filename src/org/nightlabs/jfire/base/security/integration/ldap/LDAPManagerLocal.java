package org.nightlabs.jfire.base.security.integration.ldap;
import javax.ejb.Local;
import javax.security.auth.login.LoginException;

import org.nightlabs.jfire.security.integration.UserManagementSystemCommunicationException;
import org.nightlabs.jfire.timer.id.TaskID;

/**
 * Local interface for LDAPManagerBean
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Local
public interface LDAPManagerLocal {

	void syncUserDataFromLDAP(TaskID taskID) throws LoginException, LDAPSyncException, UserManagementSystemCommunicationException;

}
