/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script makes use of variables from CommonBindVariablesScript.js, so it SHOULD be executed first.
 * 
 * This script is used for generating a LDAPAttributeSet with attributes names and values which is then passed in LDAP modidifcation calls.
 * Used for synchronization when JFire is a leading system.
 *  
 * It makes java object passed to evaluating ScriptContext: <code>atributes</code> - LDAPAttributeSet to add all attributes to.
 *   
 * Returns LDAPAttributeSet.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 * 
 */
importClass(org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet);

var attributes = new LDAPAttributeSet();

if (isNewEntry){
	var objectClasses = new java.util.ArrayList();
	objectClasses.add('top');
	objectClasses.add('posixAccount');
	objectClasses.add('sambaSamAccount');
	objectClasses.add('sambaSidEntry');
	attributes.createAttribute('objectClass', objectClasses);
	if (userData != null){
		attributes.createAttribute('commonName', $userName$);
		attributes.createAttribute('uid', $userID$);
		attributes.createAttribute('uidNumber', ''+$userID$.hashCode());
	}else if (personData != null){
		attributes.createAttribute('commonName', $personName$);
		attributes.createAttribute('uid', $personID$ +'@'+$personOrganisationID$);
		attributes.createAttribute('uidNumber', ''+$personID$);
	}
	attributes.createAttribute('gidNumber', '0');	// security group ID number, not used but is a MUST attribute
	attributes.createAttribute('homeDirectory', '');	// ???
	attributes.createAttribute('sambaSID', '');	// ???
}

if (userData != null){
	// User fields
	attributes.createAttribute('commonName', $userName$);
	attributes.createAttribute('uid', $userID$);
	attributes.createAttribute('description', $userDescription$);
}else if (personData != null){
	attributes.createAttribute('commonName', $personName$);
	attributes.createAttribute('description', $personComment$);
}

// Person fields
if (personData != null){
	attributes.createAttribute('displayName', $personDisplayName$);
}
attributes;