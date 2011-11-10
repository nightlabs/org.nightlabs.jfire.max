package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.io.Serializable;
import java.util.Collection;

import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent;

/**
 * Implementation of a {@link UserManagementSystemSyncEvent} for {@link LDAPServer}. 
 * Indicates what to synchronize (via {@code #sendEventTypeDataUnits} and {@code #fetchEventTypeDataUnits}) 
 * and in which direction (via {@code eventType}).
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
public class LDAPSyncEvent implements UserManagementSystemSyncEvent, Serializable{
	
	/**
	 * The serial version of this class.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Simple class for holding data for synchronization of one JFire object. Event type: SEND_DELETE
	 * This class is introduced to help with proper usage of {@link LDAPSyncEvent} class.
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	public static class SendEventTypeDataUnit implements Serializable{
		
		private static final long serialVersionUID = 1L;
		private Object jfireObjectId;
		private String ldapEntryId;
		
		public SendEventTypeDataUnit(Object jfireObjectId) {
			this(jfireObjectId, null);
		}
		
		public SendEventTypeDataUnit(Object jfireObjectId, String ldapEntryId) {
			if (jfireObjectId == null){
				throw new IllegalArgumentException("jfireObjectId could not be null!");
			}
			this.jfireObjectId = jfireObjectId;
			this.ldapEntryId = ldapEntryId;
		}

		/**
		 * Get JFire object ID. Could NOT be <code>null</code>.
		 * 
		 * @return ID of JFire object to be sent to {@link LDAPServer}
		 */
		public Object getJfireObjectId() {
			return jfireObjectId;
		}
		
		/**
		 * Get LDAP entry name which corresponds to JFire object with {@link #jfireObjectId}.
		 * Could be not set and therefore this method could return <code>null</code>.
		 * 
		 * @return name of LDAP entry or <code>null</code> if not set
		 */
		public String getLdapEntryId() {
			return ldapEntryId;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((jfireObjectId == null) ? 0 : jfireObjectId.hashCode());
			result = prime * result
					+ ((ldapEntryId == null) ? 0 : ldapEntryId.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SendEventTypeDataUnit other = (SendEventTypeDataUnit) obj;
			if (jfireObjectId == null) {
				if (other.jfireObjectId != null)
					return false;
			} else if (!jfireObjectId.equals(other.jfireObjectId))
				return false;
			if (ldapEntryId == null) {
				if (other.ldapEntryId != null)
					return false;
			} else if (!ldapEntryId.equals(other.ldapEntryId))
				return false;
			return true;
		}
	}
	
	/**
	 * Simple class for holding data for synchronization of one LDAP entity. Event type: FETCH_DELETE
	 * This class is introduced to help with proper usage of {@link LDAPSyncEvent} class.
	 * 
	 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
	 *
	 */
	public static class FetchEventTypeDataUnit implements Serializable{
		
		private static final long serialVersionUID = 1L;
		private String ldapEntryName;

		public FetchEventTypeDataUnit(String ldapEntryName) {
			if (ldapEntryName == null || "".equals(ldapEntryName)){
				throw new IllegalArgumentException("ldapEntryName could not be null or empty!");
			}
			this.ldapEntryName = ldapEntryName;
		}
		
		/**
		 * Get name of LDAP entry to be fetched from directory. Could NOT be <code>null</code> or empty.
		 * 
		 * @return name of LDAP entry to be fetched from LDAP directory
		 */
		public String getLdapEntryName() {
			return ldapEntryName;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((ldapEntryName == null) ? 0 : ldapEntryName.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FetchEventTypeDataUnit other = (FetchEventTypeDataUnit) obj;
			if (ldapEntryName == null) {
				if (other.ldapEntryName != null)
					return false;
			} else if (!ldapEntryName.equals(other.ldapEntryName))
				return false;
			return true;
		}
	}

	/**
	 * Event type defines the synchronization direction. Either fetch data from {@link LDAPServer)
	 * and store it in JFire or send (perhaps changed) data to the {@link LDAPServer).
	 */
	private SyncEventType eventType;
	
	/**
	 * ID of organisation where sync is performed
	 */
	private String organisationID;
	
	/**
	 * {@link Collection} of entities describing data objects being send to {@link LDAPServer}
	 */
	private Collection<SendEventTypeDataUnit> sendEventTypeDataUnits;
	
	/**
	 * {@link Collection} of entities describing data objects being fetched from {@link LDAPServer} and stored to JFire
	 */
	private Collection<FetchEventTypeDataUnit> fetchEventTypeDataUnits;
	
	
	/**
	 * Constructs a new LDAPSyncEvent to be passed to {@link LDAPServer#synchronize(UserManagementSystemSyncEvent)} method.
	 * 
	 * @param type
	 * @param completeUserId
	 */
	public LDAPSyncEvent(SyncEventType type){
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
	 * Get entites describing objects to be sent to {@link LDAPServer}
	 * 
	 * @return {@link Collection} of {@link SendEventTypeDataUnit} objects
	 */
	public Collection<SendEventTypeDataUnit> getSendEventTypeDataUnits() {
		return sendEventTypeDataUnits;
	}
	
	/**
	 * Provide collection of entities describing objects for synchronization of user data from JFire to LDAP.
	 *  
	 * @param sendEventTypeDataUnits
	 */
	public void setSendEventTypeDataUnits(Collection<SendEventTypeDataUnit> sendEventTypeDataUnits) {
		this.sendEventTypeDataUnits = sendEventTypeDataUnits;
	}
	
	/**
	 * Get entites describing objects to be fetched from {@link LDAPServer}
	 * 
	 * @return {@link Collection} of {@link FetchEventTypeDataUnit} objects
	 */
	public Collection<FetchEventTypeDataUnit> getFetchEventTypeDataUnits() {
		return fetchEventTypeDataUnits;
	}
	
	/**
	 * Provide collection of entities describing synchronization of user data from LDAP to JFire.
	 *  
	 * @param fetchEventTypeDataUnits
	 */
	public void setFetchEventTypeDataUnits(Collection<FetchEventTypeDataUnit> fetchEventTypeDataUnits) {
		this.fetchEventTypeDataUnits = fetchEventTypeDataUnits;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SyncEventType getEventType() {
		return eventType;
	}
	
}
