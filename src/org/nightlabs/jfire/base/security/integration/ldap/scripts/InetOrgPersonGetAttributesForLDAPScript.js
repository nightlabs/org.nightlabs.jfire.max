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
		objectClasses.add('person');
		objectClasses.add('organizationalPerson');
		objectClasses.add('inetOrgPerson');
		attributes.createAttribute('objectClass', objectClasses);
	}
	
	if ($userID$ != null){
		attributes.createAttribute('commonName', $userName$);
		attributes.createAttribute('surname', $userName$);
		attributes.createAttribute('uid', $userID$);
		attributes.createAttribute('description', $userDescription$);
	}else if ($personID$ != null){
		attributes.createAttribute('commonName', $personName$);
		attributes.createAttribute('surname', $personName$);
		attributes.createAttribute('description', $personComment$);
	}

	if ($personID$ != null){
		attributes.createAttribute('displayName', $personDisplayName$);
		attributes.createAttribute('facsimileTelephoneNumber', $personFax$);
		attributes.createAttribute('givenName', $personFirstName$);
		attributes.createAttribute('photo', $personPhoto$);
		attributes.createAttribute('jpegPhoto', $personPhoto$);
		attributes.createAttribute('localityName', $personCity$);
		attributes.createAttribute('labeledURI', $personHomepage$);
		attributes.createAttribute('mail', $personEMail$);
		attributes.createAttribute('organizationName', $personCompany$);
		attributes.createAttribute('postalAddress', $personAddress$ + ' ' + $personCity$ + ' ' + $personPostCode$ + ' ' + $personCity$ + ' ' + $personRegion$ + ' ' + $personCountry$);
		attributes.createAttribute('postalCode', $personPostCode$);
		attributes.createAttribute('preferredLanguage', $personLocaleLanguage$);
		attributes.createAttribute('stateOrProvinceName', $personRegion$);
		attributes.createAttribute('streetAddress', $personAddress$);
		attributes.createAttribute('telephoneNumber', $personPhonePrimary$);
		attributes.createAttribute('title', $personTitle$);
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
