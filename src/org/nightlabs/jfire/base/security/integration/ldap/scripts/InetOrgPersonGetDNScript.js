/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script makes use of variables from CommonBindVariablesScript.js, so it SHOULD be executed first.
 * 
 * This script is used for generating a String with LDAP entry DN usign User/Person data.
 * 
 * NOTE that "BASE_ENTRY_NAME_PLACEHOLDER" could be replaced with another existing LDAP entry.
 * 
 * Returns a String with LDAP entry DN.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 * 
 */
var dn = null;
if (userData != null && $userID$ != null){
	dn = 'cn='+$userID$+',BASE_ENTRY_NAME_PLACEHOLDER';
}else if (personData != null && $personName$ != null){
	// REV Alex: Maybe use personDisplayName or some other concatenated value as cn
	dn = 'sn='+$personName$+',BASE_ENTRY_NAME_PLACEHOLDER';
}
dn;