// REV: Alex: These are the scripts that will be used initially 
// when a LDAP-Server (LDAPScriptSet) is created, so maybe there 
// should be a comment on top of every script telling the administrator 
// what this script is for, when it is executed and which 
// variables are published into it when it is executed.
// Additionally the comment should tell, whether the script is 
// supposed to return a value and for what this value is used

importClass(org.nightlabs.jfire.prop.PropertySet);
importClass(org.nightlabs.jfire.security.User);
importClass(org.nightlabs.jfire.person.Person);
importClass(org.nightlabs.jfire.person.PersonStruct);

var user = null;
var person = null;

var uid = allAttributes.get('uid');	// should not be changed in LDAP directory
if (uid != null && uid.length > 0){	// assume were dealing with User object
	logger.info("UID is "+ uid[0]);
	try{
		user = User.getUser(pm, organisationID, uid[0]);
	}catch(e){
		// user not found - create new one
		logger.info("Creating new user...");
		user = new User(organisationID, uid[0]);
		
		logger.info("Creating new user local...");
		new Packages.org.nightlabs.jfire.security.UserLocal(user);
		
		user = pm.makePersistent(user);
		var userLocal = user.getUserLocal();
		
		logger.info("configuring security for new user...");
		var successful = false;
		try{

			var authorityID = Packages.org.nightlabs.jfire.security.id.AuthorityID.create(
					organisationID, Packages.org.nightlabs.jfire.security.Authority.AUTHORITY_ID_ORGANISATION
					);
			var logUserRoleGroupID  = Packages.org.nightlabs.jfire.security.id.RoleGroupID.create(
					'org.nightlabs.jfire.workstation.loginWithoutWorkstation'
					);

			logger.info("begin changing...");
			Packages.org.nightlabs.jfire.security.listener.SecurityChangeController.beginChanging();
			
			logger.info("getting authority...");
			var authority = pm.getObjectById(authorityID);
			logger.info("creating AuthorizedObjectRef...");
			var userRef = authority.createAuthorizedObjectRef(userLocal);
			
			logger.info("getting role group by id...");
			var roleGroup = pm.getObjectById(logUserRoleGroupID);
			logger.info("creating and adding RoleGroupRef to userRef...");
			userRef.addRoleGroupRef(authority.createRoleGroupRef(roleGroup));
			
			successful = true;

			logger.info("security configuration done.");
		}finally{
			logger.info("end changing: " + successful);
			Packages.org.nightlabs.jfire.security.listener.SecurityChangeController.endChanging(successful);
		}
		
		logger.info("user creation done.");
	}
}

if (user != null){
	logger.info("User != null, creating new person...");
	person = new Person(organisationID, newPersonID);
	user.setPerson(person);
	person = user.getPerson();
}else{	// assume we are synchronizing just Person
	// try to find Person object by displayName or create a new one

	logger.info("User is null, looking for person...");
	
	var f = new Packages.org.nightlabs.jfire.person.PersonSearchFilter(Packages.org.nightlabs.jdo.search.SearchFilter.CONJUNCTION_AND);
	f.addSearchFilterItem(new Packages.org.nightlabs.jfire.prop.search.DisplayNameSearchFilterItem(Packages.org.nightlabs.jdo.search.MatchType.EQUALS), allAttributes.get('displayName')[0]);
	var results = f.getResult();
	if (results.size() > 0){	// what if size greater than 1?
		person = results.iterator().next();
	}else{
		logger.info("Person not found, creating new one...");
		person = new Person(organisationID, newPersonID);
	}
	
}

logger.info("Init data attributes...");

// getting first atribute value, can be rewritten to gain multiple values
var cn = allAttributes.get('cn')!=null?allAttributes.get('cn')[0]:null;
var description = allAttributes.get('description')!=null?allAttributes.get('description')[0]:null;
var organizationName = allAttributes.get('organizationName')!=null?allAttributes.get('organizationName')[0]:null;
var sn = allAttributes.get('sn')!=null?allAttributes.get('sn')[0]:null;
var gn = allAttributes.get('gn')!=null?allAttributes.get('gn')[0]:null;
var title = allAttributes.get('title')!=null?allAttributes.get('title')[0]:null;
var photo = allAttributes.get('photo')!=null?allAttributes.get('photo')[0]:null;
var street = allAttributes.get('street')!=null?allAttributes.get('street')[0]:null;
var postalCode = allAttributes.get('postalCode')!=null?allAttributes.get('postalCode')[0]:null;
var localityName = allAttributes.get('localityName')!=null?allAttributes.get('localityName')[0]:null;
var stateOrProvinceName = allAttributes.get('stateOrProvinceName')!=null?allAttributes.get('stateOrProvinceName')[0]:null;
var mail = allAttributes.get('mail')!=null?allAttributes.get('mail')[0]:null;
var labeledURI = allAttributes.get('labeledURI')!=null?allAttributes.get('labeledURI')[0]:null;
var telephoneNumber = allAttributes.get('telephoneNumber')!=null?allAttributes.get('telephoneNumber')[0]:null;
var fax = allAttributes.get('fax')!=null?allAttributes.get('fax')[0]:null;
var preferredLanguage = allAttributes.get('preferredLanguage')!=null?allAttributes.get('preferredLanguage')[0]:null;

// set attributes to JFire objects
if (user != null){
	logger.info("set name and description to user");
	user.setName(cn);
	user.setDescription(description);
}

if (person != null){

	logger.info("setting person data...");

	var structLocalId = person.getStructLocalObjectID();
	logger.info("loading person struct...");
	var ps = Packages.org.nightlabs.jfire.prop.StructLocal.getStructLocal(
			pm, Packages.org.nightlabs.jfire.organisation.Organisation.DEV_ORGANISATION_ID, structLocalId.linkClass, structLocalId.structScope, structLocalId.structLocalScope
	);
	
	logger.info("inflating person...");
	person.inflate(ps);

	logger.info("setting data to data fields...");

	// personal data
	person.getDataField(PersonStruct.PERSONALDATA_COMPANY).setData(organizationName);
	person.getDataField(PersonStruct.PERSONALDATA_NAME).setData(sn!=null?sn:cn);
	person.getDataField(PersonStruct.PERSONALDATA_FIRSTNAME).setData(gn);
	person.getDataField(PersonStruct.PERSONALDATA_TITLE).setData(title);
	person.getDataField(PersonStruct.PERSONALDATA_PHOTO).setData(photo);
	
	// postadress
	person.getDataField(PersonStruct.POSTADDRESS_ADDRESS).setData(street);
	person.getDataField(PersonStruct.POSTADDRESS_POSTCODE).setData(postalCode);
	person.getDataField(PersonStruct.POSTADDRESS_CITY).setData(localityName);
	person.getDataField(PersonStruct.POSTADDRESS_REGION).setData(stateOrProvinceName);

	// internet
	person.getDataField(PersonStruct.INTERNET_EMAIL).setData(mail);
	person.getDataField(PersonStruct.INTERNET_HOMEPAGE).setData(labeledURI);
	
	// phone
	person.getDataField(PersonStruct.PHONE_PRIMARY).setData(telephoneNumber);
	person.getDataField(PersonStruct.FAX).setData(fax);
	
	// comment
	person.getDataField(PersonStruct.COMMENT_COMMENT).setData(description);
	
	logger.info("setting locale to person...");
	person.setLocale(new java.util.Locale(preferredLanguage));

	logger.info("deflating person...");
	person.deflate();
}

var returnObject = null;
if (user != null){
	returnObject = user;
}else if (person != null){
	returnObject = person;
}
pm.makePersistent(returnObject);