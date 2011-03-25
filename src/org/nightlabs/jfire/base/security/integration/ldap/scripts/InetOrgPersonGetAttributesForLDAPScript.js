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

var attributes = new java.util.HashMap();

if (isNewEntry){
	attributes.put('objectClass', 'InetOrgPerson');
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