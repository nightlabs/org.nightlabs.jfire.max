/**
 * This is initial script which is used when new LDAPServer and corresponding LDAPScriptSet are created. 
 * All changes in this particular file WILL NOT be reflected in existing LDAPServers but only in new ones.
 * 
 * This script is used for storing data into JFire objects (User and/or Person) during synchronization when Samba LDAPServer is a leading system.
 * 
 * It makes use of several java objects passed to evaluating ScriptContext: <code>allAtributes</code> - Map<String, Object[]> with all attributes
 * of entry to be synchronized, <code>pm</code> - PersistenceManager, <code>organisationID</code> - the ID of JFire organisation, 
 * <code>newPersonID</code> - value returned by IDGenerator.nextID(PropertySet.class) used when new Person object is created,
 * <code>logger</code> - org.slf4j.Logger for debug purposes.  
 * 
 * Returns persisted object (either User or Person).
 *  
 */
importClass(org.nightlabs.jfire.prop.PropertySet);
importClass(org.nightlabs.jfire.security.User);
importClass(org.nightlabs.jfire.person.Person);
importClass(org.nightlabs.jfire.person.PersonStruct);

var user = null;
var person = null;

var uid = allAttributes.get('uid');	// should NOT be changed in LDAP directory
if (uid != null && uid.length > 0){	// assume were dealing with User object
	logger.debug("UID is "+ uid[0]);
	try{
		user = User.getUser(pm, organisationID, uid[0]);
	}catch(e){
		// user not found - create new one
		logger.debug("Creating new user...");
		user = new User(organisationID, uid[0]);
		
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

if (user != null){
	logger.debug("User != null");
	person = user.getPerson();
	if (person == null){ // 
		logger.debug("Create new person");
		person = new Person(organisationID, newPersonID);
		user.setPerson(person);
	}
}else{	// assume we are synchronizing just Person
	// try to find Person object by displayName or create a new one

	logger.debug("User is null, looking for person...");
	
	var f = new Packages.org.nightlabs.jfire.person.PersonSearchFilter(Packages.org.nightlabs.jdo.search.SearchFilter.CONJUNCTION_AND);
	f.addSearchFilterItem(new Packages.org.nightlabs.jfire.prop.search.DisplayNameSearchFilterItem(Packages.org.nightlabs.jdo.search.MatchType.EQUALS), allAttributes.get('displayName')[0]);
	var results = f.getResult();
	if (results.size() > 0){	// what if size greater than 1?
		person = results.iterator().next();
	}else{
		logger.debug("Person not found, creating new one...");
		person = new Person(organisationID, newPersonID);
	}
	
}

logger.debug("Init data attributes...");


function getAttributeValue(name){
	// getting first atribute value, can be rewritten to gain multiple values
	return allAttributes.get(name)!=null?allAttributes.get(name)[0]:null;
}

var description = getAttributeValue('description');

// set attributes to JFire objects
if (user != null){
	logger.debug("set name and description to user");
	user.setName(getAttributeValue('cn'));
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
	// comment
	person.getDataField(PersonStruct.COMMENT_COMMENT).setData(description);
	
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