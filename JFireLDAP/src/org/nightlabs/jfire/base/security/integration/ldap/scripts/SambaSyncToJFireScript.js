/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script is used for storing data into JFire objects (User and/or Person) during synchronization when Samba LDAPServer is a leading system.
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

var user = null;
var completeID = LDAPScriptUtil.getAttributeValue(allAttributes, 'uid', 'userid');
var displayName = LDAPScriptUtil.getAttributeValue(allAttributes, 'displayName', null);
if (displayName == null || displayName == ''){	// assume it's not a Person
	user = LDAPScriptUtil.getUser(pm, LDAPScriptUtil.getOrganisationID(completeID), LDAPScriptUtil.getAttributeValue(allAttributes, 'cn', 'commonName'));
}
var person = LDAPScriptUtil.getPerson(pm, user, LDAPScriptUtil.getOrganisationID(completeID), LDAPScriptUtil.getAttributeValue(allAttributes, 'cn', 'commonName'));

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
		person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'cn', 'commonName'));
		person.getDataField(PersonStruct.COMMENT_COMMENT).setData(LDAPScriptUtil.getAttributeValue(allAttributes, 'description', null));
		
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