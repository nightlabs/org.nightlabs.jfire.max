/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script makes use of variables from InetOrgPersonBindVariablesScript.js, so it SHOULD be executed first.
 * 
 * This script is used for generating a Map with attributes names and values which is then passed in LDAP modidifcation calls.
 * Used for synchronization when JFire is a leading system. 
 * 
 * Returns attributes Map<String, Object>.
 *  
 */
var attributes = new java.util.HashMap();

if (isNewEntry){
	attributes.put('objectClass', 'top');
	attributes.put('objectClass', 'person');
	attributes.put('objectClass', 'organizationalPerson');
	attributes.put('objectClass', 'inetOrgPerson');
}

if (userData != null){
	// User fields
	attributes.put('cn', $userName$);
	attributes.put('uid', $userID$);
	attributes.put('description', $userDescription$);
}else if (personData != null){
	attributes.put('cn', $personName$);
//	attrs.put('uid', $personID$+'@'+$personOrganisationID$);
	attributes.put('description', $personComment$);
}

// Person fields
if (personData != null){
	attributes.put('sn', $personName$);
	attributes.put('displayName', $personDisplayName$);
	attributes.put('fax', $personFax$);
	attributes.put('gn', $personFirstName$);
//	attributes.put('photo', $personPhoto$);
	attributes.put('localityName', $personCity$);
	attributes.put('labeledURI', $personHomepage$);
	attributes.put('mail', $personEMail$);
	attributes.put('organizationName', $personCompany$);
	attributes.put('postalAddress', $personAddress$ + ' ' + $personCity$ + ' ' + $personPostCode$ + ' ' + $personCity$ + ' ' + $personRegion$ + ' ' + $personCountry$);
	attributes.put('postalCode', $personPostCode$);
	attributes.put('preferredLanguage', $personLocaleLanguage$);
	attributes.put('stateOrProvinceName', $personRegion$);
	attributes.put('street', $personAddress$);
	attributes.put('telephoneNumber', $personPhonePrimary$);
	attributes.put('title', $personTitle$);
}
attributes;