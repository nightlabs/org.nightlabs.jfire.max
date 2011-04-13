/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script is used for storing data into JFire objects (User and/or Person) during synchronization when InetOrgPerson LDAPServer is a leading system.
 * 
 * It makes use of several java objects passed to evaluating ScriptContext: <code>allAtributes</code> - LDAPAttributeSet with all attributes
 * of entry to be synchronized, <code>pm</code> - PersistenceManager, <code>organisationID</code> - the ID of JFire organisation, 
 * <code>newPersonID</code> - value returned by IDGenerator.nextID(PropertySet.class) used when new Person object is created,
 * <code>logger</code> - org.slf4j.Logger for debug purposes.  
 * 
 * Returns persisted object (either User or Person).
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *  
 */
importClass(org.nightlabs.jfire.prop.PropertySet);
importClass(org.nightlabs.jfire.security.User);
importClass(org.nightlabs.jfire.person.Person);
importClass(org.nightlabs.jfire.person.PersonStruct);

/**
 * Functions block: start
 * TODO: Consider move these code to a separate file as it's the same both for InetOrgPerson and Samba schemes. For now they use several
 * global variables, so do not forget to add corresponding parameters to functions.
 */

function getUser(uid){
	logger.debug("UID is "+ uid);
	var user = null;
	if (uid != null){	// assume were dealing with User object
		try{
			user = User.getUser(pm, organisationID, uid);
		}catch(e){
			// user not found - create new one
			logger.debug("Creating new user...");
			user = new User(organisationID, uid);
			
			logger.debug("Creating new user local...");
			new Packages.org.nightlabs.jfire.security.UserLocal(user);
			
			user = pm.makePersistent(user);
			var userLocal = user.getUserLocal();
			
			logger.debug("configuring security for new user...");
			var successful = false;
			try{
	
				var authorityID = Packages.org.nightlabs.jfire.security.id.AuthorityID.create(
						organisationID, Packages.org.nightlabs.jfire.security.Authority.AUTHORITY_ID_ORGANISATION
						);
				var logUserRoleGroupID  = Packages.org.nightlabs.jfire.security.id.RoleGroupID.create(
						'org.nightlabs.jfire.workstation.loginWithoutWorkstation'
						);
	
				logger.debug("begin changing...");
				Packages.org.nightlabs.jfire.security.listener.SecurityChangeController.beginChanging();
				
				logger.debug("getting authority...");
				var authority = pm.getObjectById(authorityID);
				logger.debug("creating AuthorizedObjectRef...");
				var userRef = authority.createAuthorizedObjectRef(userLocal);
				
				logger.debug("getting role group by id...");
				var roleGroup = pm.getObjectById(logUserRoleGroupID);
				logger.debug("creating and adding RoleGroupRef to userRef...");
				userRef.addRoleGroupRef(authority.createRoleGroupRef(roleGroup));
				
				successful = true;
	
				logger.debug("security configuration done.");
			}finally{
				logger.debug("end changing: " + successful);
				Packages.org.nightlabs.jfire.security.listener.SecurityChangeController.endChanging(successful);
			}
			
			logger.debug("user creation done.");
		}
	}
	return user;
}

function getAttributeValue(name, canonicalName){
	if (name != null && allAttributes.getAttribute(name) != null){
		if (allAttributes.getAttribute(name).hasSingleValue()){
			return allAttributes.getAttributeValue(name);
		}else{
			return allAttributes.getAttributeValues(name);
		}
	}else if (canonicalName != null && allAttributes.getAttribute(canonicalName)){
		if (allAttributes.getAttribute(canonicalName).hasSingleValue()){
			return allAttributes.getAttributeValue(canonicalName);
		}else{
			return allAttributes.getAttributeValues(canonicalName);
		}
	}
	return null;
}

function getPerson(user){
	if (user != null){
		logger.debug("User != null");
		person = user.getPerson();
		if (person == null){ // 
			logger.debug("Create new person");
			person = new Person(organisationID, newPersonID);
			user.setPerson(person);
		}
	}else if (getAttributeValue('displayName', null) != null){	// assume we are synchronizing just Person
		// try to find Person object by displayName or create a new one

		logger.debug("User is null, looking for person...");
		
		var f = new Packages.org.nightlabs.jfire.person.PersonSearchFilter(Packages.org.nightlabs.jdo.search.SearchFilter.CONJUNCTION_AND);
		f.addSearchFilterItem(new Packages.org.nightlabs.jfire.prop.search.DisplayNameSearchFilterItem(Packages.org.nightlabs.jdo.search.MatchType.EQUALS, getAttributeValue('displayName', null)));
		var results = f.getResult();
		if (results.size() > 0){	// what if size greater than 1?
			person = results.iterator().next();
		}else{
			logger.debug("Person not found, creating new one...");
			person = new Person(organisationID, newPersonID);
		}
		
	}
}

/**
 * Functions block: end 
 */

var user = getUser(getAttributeValue('uid', 'userid'));
var person = getPerson(user);

var cn = getAttributeValue('cn', 'commonName');
var description = getAttributeValue('description', null);
var sn = getAttributeValue('sn', 'surname');

// set attributes to JFire objects
if (user != null){
	logger.debug("set name and description to user");
	user.setName(cn);
	user.setDescription(description);
}

if (person != null){

	logger.debug("setting person data...");

	var structLocalId = person.getStructLocalObjectID();
	logger.debug("loading person struct...");
	var ps = Packages.org.nightlabs.jfire.prop.StructLocal.getStructLocal(
			pm, Packages.org.nightlabs.jfire.organisation.Organisation.DEV_ORGANISATION_ID, structLocalId.linkClass, structLocalId.structScope, structLocalId.structLocalScope
	);
	
	logger.debug("inflating person...");
	person.inflate(ps);

	logger.debug("setting data to data fields...");

	// personal data
	person.getDataField(PersonStruct.PERSONALDATA_COMPANY).setData(getAttributeValue('o', 'organizationName'));
	person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(sn!=null?sn:cn);
	person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(getAttributeValue('gn', 'givenName'));
	person.getDataField(PersonStruct.PERSONALDATA_TITLE).setData(getAttributeValue('title', null));
	var photo = getAttributeValue('photo', null);
	if (photo == null){
		photo = getAttributeValue('jpegPhoto', null);
	}
	person.getDataField(PersonStruct.PERSONALDATA_PHOTO).setData(photo);
	
	// postadress
	person.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(getAttributeValue('street', 'streetAddress'));
	person.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(getAttributeValue('postalCode', null));
	person.getDataField(PersonStruct.POSTADDRESS_CITY).setData(getAttributeValue('l', 'localityName'));
	person.getDataField(PersonStruct.POSTADDRESS_REGION).setData(getAttributeValue('st', 'stateOrProvinceName', null));

	// internet
	person.getDataField(PersonStruct.INTERNET_EMAIL).setData(getAttributeValue('mail', 'rfc822Mailbox'));
	person.getDataField(PersonStruct.INTERNET_HOMEPAGE).setData(getAttributeValue('labeledURI', null));
	
	// phone
	person.getDataField(PersonStruct.PHONE_PRIMARY).setData(getAttributeValue('telephoneNumber', null));
	person.getDataField(PersonStruct.FAX).setData(getAttributeValue('fax', 'facsimileTelephoneNumber'));
	
	// comment
	person.getDataField(PersonStruct.COMMENT_COMMENT).setData(description);
	
	logger.debug("setting locale to person...");
	person.setLocale(new java.util.Locale(getAttributeValue('preferredLanguage')));

	logger.debug("deflating person...");
	person.deflate();
}

var returnObject = null;
if (user != null){
	returnObject = user;
}else if (person != null){
	returnObject = person;
}
pm.makePersistent(returnObject);