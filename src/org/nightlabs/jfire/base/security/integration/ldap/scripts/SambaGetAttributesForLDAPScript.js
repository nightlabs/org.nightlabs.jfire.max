/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * Functions from this script are used for synchronization from JFire to LDAP directory and generate attributes
 * to be modified in LDAP. Note that you COULD NOT change the names of this functions without making corresponding
 * changes inside JFire code!
 * 
 * This script depends on CommonBindVariablesScript, so it should be also executed.
 * 
 * The script itseld does not return anything, assumed that all the work is done inside defined functions.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 * 
 */
importClass(org.nightlabs.jfire.base.security.integration.ldap.scripts.LDAPScriptUtil);
importClass(org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet);

/**
 * Maps LDAP attributes on existing Person datafields and returns resulting attribute set. These attributes are sent to LDAP directory.
 * 
 * @returns {LDAPAttributeSet}
 */
function getMappedAttributes(){
	var attributes = new LDAPAttributeSet();
	if (isNewEntry){
		var objectClasses = new java.util.ArrayList();
		objectClasses.add('top');
		objectClasses.add('posixAccount');
		objectClasses.add('sambaSamAccount');
		objectClasses.add('sambaSidEntry');
		attributes.createAttribute('objectClass', objectClasses);
	}

	if ($userID$ != null){
		// User fields
		attributes.createAttribute('commonName', $userID$);
		attributes.createAttribute('uid', $userCompleteUserID$);
		attributes.createAttribute('description', $userDescription$);

		// DEFAULT VALUE for this mandatory attribute is '1'
		attributes.createAttribute('uidNumber', '1');
	}else if ($personID$ != null){
		attributes.createAttribute('commonName', $personName$);
		attributes.createAttribute('description', $personComment$);
		attributes.createAttribute('uid', $personCompleteID$);

		// DEFAULT VALUE for this mandatory attribute is '0'
		attributes.createAttribute('uidNumber', '1');
	}

	// DEFAULT VALUE for this mandatory attribute is '0'
	attributes.createAttribute('gidNumber', '0');
	// DEFAULT VALUE for this mandatory attribute is '/'
	attributes.createAttribute('homeDirectory', '/');
	// DEFAULT VALUE for this mandatory attribute is '0'
	attributes.createAttribute('sambaSID', '0');

	if (personData != null){
		attributes.createAttribute('displayName', $personDisplayName$);
	}
	return attributes;
}

/**
 * Get the name of LDAP attribute which holds user password
 * 
 * @returns {String}
 */
function getPasswordAttributeName(){
	return 'userPassword';
}

/**
 * Get JFire UserID as String by given attributes of an LDAP entry.
 */
function getUserIDFromLDAPEntry(attributes){
	var displayName = LDAPScriptUtil.getAttributeValue(attributes, 'displayName', null);
	if (displayName == null || displayName == ''){	// assume it's not a Person
		var uid = LDAPScriptUtil.getAttributeValue(attributes, 'uid', 'userid');
		if (uid != null){
			return uid;
		}
	}
	return null;
}

/**
 * Get the name of SASL realm for given bind principal 
 */
function getSASLRealm(bindPrincipal){
	return "nightlabs.de";
}