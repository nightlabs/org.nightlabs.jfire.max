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
	attributes.createAttribute('objectClass', 'top', 'person', 'organizationalPerson', 'inetOrgPerson');
	if (userData != null){
		attributes.createAttribute('commonName', $userName$);
		attributes.createAttribute('surname', $userName$);
	}else if (personData != null){
		attributes.createAttribute('commonName', $personName$);
		attributes.createAttribute('surname', $personName$);
	}
}

if (userData != null){
	// User fields
	attributes.createAttribute('commonName', $userName$);
	attributes.createAttribute('surname', $userName$);
	attributes.createAttribute('userid', $userID$);
	attributes.createAttribute('description', $userDescription$);
}else if (personData != null){
	attributes.createAttribute('commonName', $personName$);
	attributes.createAttribute('surname', $personName$);
	attributes.createAttribute('description', $personComment$);
}

// Person fields
if (personData != null){
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
attributes;