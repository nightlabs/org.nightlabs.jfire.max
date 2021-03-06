package org.nightlabs.jfire.base.security.integration.ldap;
import java.util.List;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.security.integration.ldap.id.LDAPScriptSetID;
import org.nightlabs.jfire.base.security.integration.ldap.scripts.ILDAPScriptProvider;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.integration.UserManagementSystemType;
import org.nightlabs.jfire.security.integration.id.UserManagementSystemID;

/**
 * Remote interface for {@link LDAPManagerBean}
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@Remote
public interface LDAPManagerRemote {

	/**
	 * Organisation-init: creates single instances for LDAP {@link UserManagementSystemType}s, configures JFire-LDAP synchronization
	 * and could init {@link LDAPServer} instances from a proerties file.
	 */
	void initialise();

	/**
	 * Retrieves a {@link List} of detached {@link LDAPScriptSet}s by given {@link LDAPScriptSetID}s.
	 * 
	 * @param objectIDs IDs of {@link LDAPScriptSet}s, will throw {@link IllegalArgumentException} if <code>null</code>
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @return list of detached {@link LDAPScriptSet} objects
	 */
	List<LDAPScriptSet> getLDAPScriptSets(Set<LDAPScriptSetID> objectIDs, String[] fetchGroups, int maxFetchDepth);
	
	/**
	 * Retrieves {@link LDAPScriptSet} by corresponding {@link LDAPServer} ID. 
	 * 
	 * @param ldapServerID ID of {@link LDAPServer} which owns {@link LDAPScriptSet}
	 * @return found {@link LDAPScriptSetID}
	 */
	LDAPScriptSetID getLDAPScriptSetID(UserManagementSystemID ldapServerID);
	
	/**
	 * Get initial script content by specified scriptID (one of values from {@link ILDAPScriptProvider}).
	 * 
	 * @param ldapScriptSetID ID of the {@link LDAPScriptSet} where initial script content will be set
	 * @param scriptID scriptID ID of script wich will be rolled back to initial content
	 * @return initial script content
	 */
	String getInitialScriptContent(LDAPScriptSetID ldapScriptSetID, String scriptID);
	
	/**
	 * Get LDAP entry name for given {@link UserID} using {@link LDAPServer} by given {@link UserManagementSystemID}
	 * 
	 * @param ldapServerID The ID of {@link LDAPServer} which will generate LDAP entry name
	 * @param userID The ID of a {@link User} to generate LDAP entry name for
	 * @return full LDAP entry name as a {@link String}
	 */
	String getLDAPEntryName(UserManagementSystemID ldapServerID, UserID userID);
	
	/**
	 * Stores {@link LDAPScriptSet} object.
	 * 
	 * @param ldapScriptSet {@link LDAPScriptSet} to store, will return <code>null</code> with a warning if <code>null</code> was specified
	 * @param get If stored object should be detached and returned
	 * @param fetchGroups Which fetch groups to use
	 * @param maxFetchDepth Fetch depth or {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT}
	 * @return Stored detached {@link LDAPScriptSet} object if <code>get</code> is <code>true</code> or <code>null</code> otherwise 
	 */
	LDAPScriptSet storeLDAPScriptSet(LDAPScriptSet ldapScriptSet, boolean get, String[] fetchGroups, int maxFetchDepth);
	
}
