/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script makes use of variables from CommonBindVariablesScript.js, so it SHOULD be executed first.
 * 
 * This script is used for generating a Map with attributes names and values which is then passed in LDAP modidifcation calls.
 * Used for synchronization when JFire is a leading system. 
 * 
 * Returns attributes Map<String, Object>.
 *  
 */
var attributes = new java.util.HashMap();

if (isNewEntry){
	var objectClasses = java.lang.reflect.Array.newInstance(java.lang.String, 4);
	objectClasses[0] = 'top';
	objectClasses[1] = 'posixAccount';
	objectClasses[2] = 'sambaSamAccount';
	objectClasses[3] = 'sambaSidEntry';
	attributes.put('objectClass', objectClasses);
	if (userData != null){
		attributes.put('cn', $userName$);
		attributes.put('uid', $userID$);
		attributes.put('uidNumber', ''+$userID$.hashCode());
	}else if (personData != null){
		attributes.put('cn', $personName$);
		attributes.put('uid', $personID$ +'@'+$personOrganisationID$);
		attributes.put('uidNumber', ''+$personID$);
	}
	attributes.put('gidNumber', '0');	// security group ID number, not used but is a MUST attribute
	attributes.put('homeDirectory', '');	// ???
	attributes.put('sambaSID', '');	// ???
}

if (userData != null){
	// User fields
	attributes.put('cn', $userName$);
	attributes.put('uid', $userID$);
	attributes.put('description', $userDescription$);
}else if (personData != null){
	attributes.put('cn', $personName$);
	attributes.put('description', $personComment$);
}

// Person fields
if (personData != null){
	attributes.put('displayName', $personDisplayName$);
}
attributes;