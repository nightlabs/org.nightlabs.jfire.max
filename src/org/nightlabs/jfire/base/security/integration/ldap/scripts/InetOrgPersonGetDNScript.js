/**
 * InetOrgPersonBindVariablesScript.js should be executed first
 */
var dn = null;
if ($userID$ != null){
	dn = 'uid='+$userID$+',ou=staff,ou=people,dc=nightlabs,dc=de';
}
dn;