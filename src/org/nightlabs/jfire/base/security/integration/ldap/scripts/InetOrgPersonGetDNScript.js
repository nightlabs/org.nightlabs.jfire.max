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
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 * 
 */
// REV Alex: Is this correct User => uid=, Person => cn= we (at NightLabs use it
// differently, we have our users as cn, and other Persons with sn)
var dn = null;
if (userData != null && $userID$ != null){
	dn = 'uid='+$userID$+',ou=staff,ou=people,dc=nightlabs,dc=de';
}else if (personData != null && $personName$ != null){
	// REV Alex: Maybe use personDisplayName or some other concatenated value as cn
	dn = 'cn='+$personName$+',ou=staff,ou=people,dc=nightlabs,dc=de';
}
dn;