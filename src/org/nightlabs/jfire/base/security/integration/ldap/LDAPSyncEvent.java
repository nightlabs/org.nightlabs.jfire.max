package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.Serializable;
import java.util.Collection;

/**
 * <p>Instances of this class are used for managing the synchronization process by passing them 
 * to {@link LDAPServer} synchronize() method. 
 * Instance of this class indicates what to synchronize (via {@code jfireObjectsIds} 
 * and {@code ldapUsersIds}) and in which direction (via {@code eventType}).</p>
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPSyncEvent implements Serializable{
	
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Event types for LDAPSyncEvent
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	public enum LDAPSyncEventType{
		FETCH,
		SEND
	}

	/**
	 * Event type defines the synchronization direction. Either fetch data from {@link LDAPServer)
	 * and store it in JFire or send (perhaps changed) data to the {@link LDAPServer).
	 */
	private LDAPSyncEventType eventType;
	
	/**
	 * ID of organisation where sync is performed
	 */
	private String organisationID;
	
	/**
	 * Collection of Strings containing DNs of users in LDAP directory
	 */
	private Collection<String> ldapUsersIds;

	/**
	 * Collection of JFire objects IDs, i.e. UserID
	 */
	private Collection<Object> jfireObjectsIds;

	
	/**
	 * Constructs a new LDAPSyncEvent to be passed to {@link LDAPServer} synchronize() method.
	 * 
	 * @param type
	 * @param completeUserId
	 */
	public LDAPSyncEvent(LDAPSyncEventType type){
		this.eventType = type;
	}

	/**
	 * 
	 * @return organisationID
	 */
	public String getOrganisationID() {
		return organisationID;
	}
	
	/**
	 * Sets organisationID
	 * @param organisationID
	 */
	public void setOrganisationID(String organisationID) {
		this.organisationID = organisationID;
	}
	
	/**
	 * 
	 * @return collection of JFire objects IDs
	 */
	public Collection<Object> getJFireObjectsIds() {
		return jfireObjectsIds;
	}
	
	/**
	 * Provide collection of JFire objects IDs for synchronization user data from JFire to LDAP. 
	 * @param jfireObjectsIds
	 */
	public void setJFireObjectsIds(Collection<Object> jfireObjectsIds) {
		this.jfireObjectsIds = jfireObjectsIds;
	}
	
	/**
	 * 
	 * @return collection of user DNs in LDAP directory 
	 */
	public Collection<String> getLdapUsersIds() {
		return ldapUsersIds;
	}
	
	/**
	 * Provide collection of DNs for synchronization user data from LDAP to JFire. 
	 * @param ldapUsersIds
	 */
	public void setLdapUsersIds(Collection<String> ldapUsersIds) {
		this.ldapUsersIds = ldapUsersIds;
	}
	
	/**
	 * 
	 * @return an event type
	 */
	public LDAPSyncEventType getEventType() {
		return eventType;
	}

}
