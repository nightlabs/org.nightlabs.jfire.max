/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script holds functions which are used for generating a List with names of entries 
 * which are parents to all LDAP user entries which should be synchronized. Used for synchronization 
 * when LDAPServer is a leading system.
 *  
 * NOTE that you COULD NOT change the names of this functions without making corresponding changes inside JFire code!
 *  
 * NOTE that "BASE_USER_ENTRY_NAME_PLACEHOLDER" and "BASE_GROUP_ENTRY_NAME_PLACEHOLDER" could be replaced 
 * with another existing LDAP entry name.
 * 
 * Returns an ArrayList<String> with entries names.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 * 
 */

/**
 * Returns parent entries names for User/Person related LDAP entries.
 */
function getUserParentEntries(){
	var parentEntries = new java.util.ArrayList();
	parentEntries.add('BASE_USER_ENTRY_NAME_PLACEHOLDER');
	return parentEntries;	
}

/**
 * Returns parent entries names for UserSecurityGroups related entries.
 */
function getGroupParentEntries(){
	var parentEntries = new java.util.ArrayList();
	parentEntries.add('BASE_GROUP_ENTRY_NAME_PLACEHOLDER');
	return parentEntries;	
}