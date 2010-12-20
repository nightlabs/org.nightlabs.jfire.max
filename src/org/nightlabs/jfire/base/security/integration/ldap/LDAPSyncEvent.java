package org.nightlabs.jfire.base.security.integration.ldap;

import java.io.Serializable;

/**
 * Instances of this class are used for managing the synchronization process by passing them 
 * to {@link LDAPServer} synchronize() method. 
 * They indicate what to synchronize (via {@code completeUserId}) and in which direction
 * (via {@code eventType}).
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
	 * A complete user Id, returned by {@link org.nightlabs.jfire.security.User}.getCompleteUserID();
	 * 
	 * TODO: it should be changed to an objectId (or smth else) because we'll also sync a Person object.
	 * But if a Person is never stored separately from User we still can use a user ID here. Although
	 * I would go for a more general approach and not limit SyncEvent to a particular object type.
	 */
	private String completeUserId;
	
	/**
	 * Constructs a new LDAPSyncEvent to be passed to {@link LDAPServer} synchronize() method.
	 * 
	 * @param type
	 * @param completeUserId
	 */
	public LDAPSyncEvent(LDAPSyncEventType type, String completeUserId){
		this.eventType = type;
		this.completeUserId = completeUserId;
	}

	/**
	 * 
	 * @return a complete user id
	 */
	public String getCompleteUserId() {
		return completeUserId;
	}
	
	/**
	 * 
	 * @return an event type
	 */
	public LDAPSyncEventType getType() {
		return eventType;
	}
	
}
