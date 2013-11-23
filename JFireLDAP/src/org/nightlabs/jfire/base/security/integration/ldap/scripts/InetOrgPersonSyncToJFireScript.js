/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script is used for storing data into JFire objects (User and/or Person) during synchronization when InetOrgPerson LDAPServer is a leading system.
 * 
 * It makes use of several java objects passed to evaluating ScriptContext: <code>allAtributes</code> - LDAPAttributeSet with all attributes
 * of entry to be synchronized, <code>pm</code> - PersistenceManager, <code>organisationID</code> - the ID of JFire organisation, 
 * <code>logger</code> - org.slf4j.Logger for debug purposes.  
 * 
 * Returns stored object (either User or Person) or nothing if removing was called.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *  
 */
importClass(org.nightlabs.jfire.base.security.integration.ldap.scripts.LDAPScriptUtil);
importClass(org.nightlabs.jfire.person.PersonStruct);

var completeID = LDAPScriptUtil.getAttributeValue(allAttributes, 'uid', 'userid');
var user = null;
if (entryName != null 
		&& (entryName.toLowerCase().indexOf("cn") == 0 || entryName.toLowerCase().indexOf("commonname") == 0)){	// assume it's a User
	user = LDAPScriptUtil.getUser(pm, LDAPScriptUtil.getOrganisationID(completeID), LDAPScriptUtil.getAttributeValue(allAttributes, 'cn', 'commonName'));
}
var person = LDAPScriptUtil.getPerson(pm, user, LDAPScriptUtil.getOrganisationID(completeID), LDAPScriptUtil.getAttributeValue(allAttributes, 'sn', 'surname'));

if (removeJFireObjects){
	if (person != null){
		pm.deletePersistent(person);
	}
	if (user != null){
		LDAPScriptUtil.deleteUser(pm, user);
	}
}else{
	// set attributes to JFire objects
	if (user != null){
		logger.debug("set name and description to user");
		user.setName(LDAPScriptUtil.getAttributeValue(allAttributes, 'cn', 'commonName'));
		user.setDescription(LDAPScriptUtil.getAttributeValue(allAttributes, 'description', null));
	}

	if (person != null){

		logger.debug("inflating person...");
		person.inflate(LDAPScriptUtil.getPersonStructLocal(pm, person));

		logger.debug("setting data to data fields...");
		// personal data
		person.getDataField(PersonStruct.PERSONALDATA_COMPANY).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'o', 'organizationName'));
		person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'sn', 'surname')!=null?LDAPScriptUtil.getAttributeValue(allAttributes, 'sn', 'surname'):LDAPScriptUtil.getAttributeValue(allAttributes, 'cn', 'commonName'));
		person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'gn', 'givenName'));
		person.getDataField(PersonStruct.PERSONALDATA_TITLE).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'title', null));
		var photo = LDAPScriptUtil.getAttributeValue(allAttributes, 'photo', null);
		if (photo == null){
			photo = LDAPScriptUtil.getAttributeValue(allAttributes, 'jpegPhoto', null);
		}
		person.getDataField(PersonStruct.PERSONALDATA_PHOTO).setData(photo);
		
		// postadress
		person.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'street', 'streetAddress'));
		person.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'postalCode', null));
		person.getDataField(PersonStruct.POSTADDRESS_CITY).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'l', 'localityName'));
		person.getDataField(PersonStruct.POSTADDRESS_REGION).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'st', 'stateOrProvinceName'));

		// internet
		person.getDataField(PersonStruct.INTERNET_EMAIL).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'mail', 'rfc822Mailbox'));
		person.getDataField(PersonStruct.INTERNET_HOMEPAGE).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'labeledURI', null));
		
		// phone
		person.getDataField(PersonStruct.PHONE_PRIMARY).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'telephoneNumber', null));
		person.getDataField(PersonStruct.FAX).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'fax', 'facsimileTelephoneNumber'));
		
		// comment
		person.getDataField(PersonStruct.COMMENT_COMMENT).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'description', null));
		
		var language = LDAPScriptUtil.getAttributeValue(allAttributes, 'preferredLanguage', null);
		if (language != null && language != ''){
			person.setLocale(new java.util.Locale(language));
		}
		
		logger.debug("deflating person...");
		person.deflate();
	}

	var objectToStore = null;
	if (user != null){
		objectToStore = user;
	}else if (person != null){
		objectToStore = person;
	}
	pm.makePersistent(objectToStore);
}