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