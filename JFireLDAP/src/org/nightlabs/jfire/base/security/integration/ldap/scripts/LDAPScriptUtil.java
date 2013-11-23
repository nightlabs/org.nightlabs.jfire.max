package org.nightlabs.jfire.base.security.integration.ldap.scripts;

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jdo.search.MatchType;
import org.nightlabs.jdo.search.SearchFilter;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPScriptSet;
import org.nightlabs.jfire.base.security.integration.ldap.attributes.LDAPAttributeSet;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.person.Person;
import org.nightlabs.jfire.person.PersonSearchFilter;
import org.nightlabs.jfire.person.PersonStruct;
import org.nightlabs.jfire.prop.PropertySet;
import org.nightlabs.jfire.prop.StructLocal;
import org.nightlabs.jfire.prop.id.StructFieldID;
import org.nightlabs.jfire.prop.id.StructLocalID;
import org.nightlabs.jfire.prop.search.TextStructFieldSearchFilterItem;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.GlobalSecurityReflector;
import org.nightlabs.jfire.security.RoleGroup;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleGroupID;
import org.nightlabs.jfire.security.listener.SecurityChangeController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains static methods only which could be called from {@link LDAPScriptSet}'s scripts.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPScriptUtil {

	private static final Logger logger = LoggerFactory.getLogger(LDAPScriptUtil.class);
	
	public static final String ORGANISATION_SEPARATOR = new String(new char[]{User.SEPARATOR_BETWEEN_USER_ID_AND_ORGANISATION_ID});
	
	/**
	 * Either gets JFire {@link User} by given uid or creates a new one.
	 * 
	 * @param pm
	 * @param uid
	 * @return {@link User} (loaded or created)
	 */
	public static User getUser(PersistenceManager pm, String organisationID, String uid){
		logger.debug("UID is "+ uid);
		User user = null;
		if (uid != null){	// assume were dealing with User object
			try{
				user = User.getUser(pm, organisationID, uid);
			}catch(JDOObjectNotFoundException e){
				// user not found - create new one
				logger.debug("Creating new user...");
				user = new User(organisationID, uid);
				
				logger.debug("Creating new user local...");
				new UserLocal(user);
				
				user = pm.makePersistent(user);
				UserLocal userLocal = user.getUserLocal();
				
				logger.debug("configuring security for new user...");
				boolean successful = false;
				try{
		
					AuthorityID authorityID = AuthorityID.create(organisationID, Authority.AUTHORITY_ID_ORGANISATION);
					RoleGroupID logUserRoleGroupID  = RoleGroupID.create("org.nightlabs.jfire.workstation.loginWithoutWorkstation");
		
					logger.debug("begin changing...");
					SecurityChangeController.beginChanging();
					
					logger.debug("getting authority...");
					Authority authority = (Authority) pm.getObjectById(authorityID);
					logger.debug("creating AuthorizedObjectRef...");
					AuthorizedObjectRef userRef = authority.createAuthorizedObjectRef(userLocal);
					
					logger.debug("getting role group by id...");
					RoleGroup roleGroup = (RoleGroup) pm.getObjectById(logUserRoleGroupID);
					logger.debug("creating and adding RoleGroupRef to userRef...");
					userRef.addRoleGroupRef(authority.createRoleGroupRef(roleGroup));
					
					successful = true;
		
					logger.debug("security configuration done.");
				}finally{
					logger.debug("end changing: " + successful);
					SecurityChangeController.endChanging(successful);
				}
				
				logger.debug("user creation done.");
			}
		}
		return user;
	}
	
	/**
	 * Deletes given {@link User}.
	 * 
	 * FIXME: could not delete User with assigned role
	 * 
	 * @param pm
	 * @param user
	 */
	public static void deleteUser(PersistenceManager pm, User user) {
		if (user != null){
			try{
				UserLocal uLocal = user.getUserLocal();
				if (uLocal != null){
					Authority authority = null;
					try{
						authority = (Authority) pm.getObjectById(AuthorityID.create(user.getOrganisationID(), Authority.AUTHORITY_ID_ORGANISATION));
					}catch(Exception e){
						logger.error(e.getMessage());
						authority = null;
					}
					if (authority != null){
						SecurityChangeController.beginChanging();
						boolean sucessful = false;
						try{
							authority.destroyAuthorizedObjectRef(uLocal);
							sucessful = true;
						}finally{
							SecurityChangeController.endChanging(sucessful);
						}
					}
				}
				user.setPerson(null);
				pm.deletePersistent(user);
				if (uLocal != null){
					pm.deletePersistent(uLocal);
				}
			}catch(Exception e){
				logger.error("Error in deleteUser!", e);
			}
		}
	}
	
	/**
	 * Gets a {@link Person} from given {@link User} or creates a new one if {@link User} is null.
	 * 
	 * @param pm
	 * @param user
	 * @param organisationID
	 * @param personName
	 * @return
	 */
	public static Person getPerson(PersistenceManager pm, User user, String organisationID, String personName){
		if (user != null){
			logger.debug("User != null");
			Person person = user.getPerson();
			if (person == null){ // 
				logger.debug("Create new person");
				person = new Person(organisationID, IDGenerator.nextID(PropertySet.class));
				user.setPerson(person);
			}
			return person;
		}else if (personName != null){	// assume we are synchronizing just Person
			// try to find Person object by displayName or create a new one

			logger.debug("User is null, looking for person...");
			
			PersonSearchFilter f = new PersonSearchFilter(SearchFilter.CONJUNCTION_AND);
			f.setPersistenceManager(pm);
			Collection<StructFieldID> structFiledIDs = new ArrayList<StructFieldID>();
			structFiledIDs.add(PersonStruct.PERSONALDATA_NAME);
			f.addSearchFilterItem(
					new TextStructFieldSearchFilterItem(
							structFiledIDs, MatchType.EQUALS, personName));
			Collection<?> results = f.getResult();
			if (results.size() > 0){	// what if size greater than 1?
				return (Person) results.iterator().next();
			}else{
				logger.debug("Person not found, creating new one...");
				return new Person(organisationID, IDGenerator.nextID(PropertySet.class));
			}
			
		}
		logger.debug("No Person found or created!");
		return null;
	}

	/**
	 * Gets {@link StructLocal} by given {@link Person}.
	 * 
	 * @param pm
	 * @param person
	 * @return
	 */
	public static StructLocal getPersonStructLocal(PersistenceManager pm, Person person){
		StructLocalID structLocalId = person.getStructLocalObjectID();
		return StructLocal.getStructLocal(
				pm, Organisation.DEV_ORGANISATION_ID, structLocalId.linkClass, structLocalId.structScope, structLocalId.structLocalScope
		);
	}

	/**
	 * Returns attribute value from given {@link LDAPAttributeSet} by attribute name (or canonical name).
	 * The return value should be null-checked where appropriate.
	 *  
	 * @param allAttributes
	 * @param name
	 * @param canonicalName
	 * @return attribute value or <code>null</code>
	 */
	public static Object getAttributeValue(LDAPAttributeSet allAttributes, String name, String canonicalName){
		if (name != null && allAttributes.getAttribute(name) != null){
			if (allAttributes.getAttribute(name).hasSingleValue()){
				return allAttributes.getAttributeValue(name);
			}else{
				return allAttributes.getAttribute(name).getValues();
			}
		}else if (canonicalName != null && allAttributes.getAttribute(canonicalName) != null){
			if (allAttributes.getAttribute(canonicalName).hasSingleValue()){
				return allAttributes.getAttributeValue(canonicalName);
			}else{
				return allAttributes.getAttribute(canonicalName).getValues();
			}
		}
		return null;
	}
	
	/**
	 * Get organisation ID by given completeID of JFire object.
	 * 
	 * @param completeID The complete ID of a JFire object, i.e "username@organisationID"
	 * @return organisationID from completeID or a local organisationID
	 */
	public static String getOrganisationID(String completeID){
		String organisationID = GlobalSecurityReflector.sharedInstance().getUserDescriptor().getOrganisationID();
		if (completeID != null && completeID.indexOf(ORGANISATION_SEPARATOR) > -1){
			String[] idParts = completeID.split(ORGANISATION_SEPARATOR);
			if (idParts.length == 2
					&& !idParts[1].isEmpty()){
				return idParts[1];
			}
		}
		return organisationID;
	}
}
