// REV: Alex: These are the scripts that will be used initially 
// when a LDAP-Server (LDAPScriptSet) is created, so maybe there 
// should be a comment on top of every script telling the administrator 
// what this script is for, when it is executed and which 
// variables are published into it when it is executed.
// Additionally the comment should tell, whether the script is 
// supposed to return a value and for what this value is used

/**
 * InetOrgPersonBindVariablesScript.js should be executed first
 */
var dn = null;
if ($userID$ != null){
	dn = 'uid='+$userID$+',ou=staff,ou=people,dc=nightlabs,dc=de';
}
dn;