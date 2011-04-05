/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script makes use of variables from CommonBindVariablesScript.js, so it SHOULD be executed first.
 * 
 * This script is used for generating a String with LDAP entry DN usign User/Person data.  
 * 
 * Returns a String with LDAP entry DN.
 *  
 */
var dn = null;
if ($userID$ != null){
	dn = 'uid='+$userID$+',ou=Users,dc=nightlabs,dc=de';
}
dn;