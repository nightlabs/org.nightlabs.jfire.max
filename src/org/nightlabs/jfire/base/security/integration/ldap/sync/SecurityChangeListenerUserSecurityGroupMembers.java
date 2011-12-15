package org.nightlabs.jfire.base.security.integration.ldap.sync;

import java.util.Collection;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.security.integration.ldap.LDAPServer;
import org.nightlabs.jfire.base.security.integration.ldap.sync.LDAPSyncEvent.SendEventTypeDataUnit;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.UserSecurityGroupID;
import org.nightlabs.jfire.security.integration.UserManagementSystem;
import org.nightlabs.jfire.security.integration.UserManagementSystemSyncEvent.SyncEventGenericType;
import org.nightlabs.jfire.security.listener.SecurityChangeEvent_UserSecurityGroup_addRemoveMember;
import org.nightlabs.jfire.security.listener.SecurityChangeListener;
import org.nightlabs.jfire.security.listener.id.SecurityChangeListenerID;
import org.nightlabs.util.CollectionUtil;

/**
 * {@link SecurityChangeListener} which listens to addition and removal of members of a {@link UserSecurityGroup}.
 * When modifications to {@link UserSecurityGroup} are over and {@link #on_SecurityChangeController_endChanging()}
 * is called then it performs synchronization of authorization related data to LDAP when JFire is a leading system.
 * 
 * @author Denis Dudnik <deniska.dudnik[at]gmail{dot}com>
 *
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class SecurityChangeListenerUserSecurityGroupMembers extends SecurityChangeListener{
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger logger = Logger.getLogger(SecurityChangeListenerUserSecurityGroupMembers.class);

	/**
	 * Flag for enabling/disabling changing {@link UserSecurityGroup} membership. It affects external modification which
	 * triggered this listener.	For example it is set when synchronization is running.
	 */
	private static ThreadLocal<Boolean> isChangeGroupMembersEnabledTL = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() {
			return true;
		};
	};

	/**
	 * Flag for enabling/disabling this listener, so it could do nothing itself. It does NOT affect external modification
	 * which triggered this listener.
	 */
	private static ThreadLocal<Boolean> isListenerEnabledTL = new ThreadLocal<Boolean>(){
		protected Boolean initialValue() {
			return true;
		};
	};

	/**
	 * Enable/disable this listener. If it's disabled than addition/removing of {@link UserSecurityGroup}s members will NOT be processed.
	 * 
	 * IMPORTANT! When you want to disable listener and then persist your objects you SHOULD always call to
	 * pm.flush() BEFORE enabling listener back. Otherwise there's a big chance that pm will be flushed 
	 * somewhere else AFTER you enable listener and it will be triggered causing unexpected behaviour.  
	 * 
	 * @param isEnabled
	 */
	public static void setEnabled(boolean isEnabled) {
		isListenerEnabledTL.set(isEnabled);
	}
	
	/**
	 * 
	 * @return if this listener is enabled and addition/removal of Group memebers will be processed
	 */
	public static boolean isEnabled(){
		return isListenerEnabledTL.get();
	}

	/**
	 * If it's disabled than it will not allow adding or removing {@link UserSecurityGroup}s members.
	 * 
	 * @param isEnabled
	 */
	public static void setChangeGroupMembersEnabled(boolean isEnabled) {
		isChangeGroupMembersEnabledTL.set(isEnabled);
	}
	
	/**
	 * 
	 * @return if this listener is enabled and will allow changing members of {@link UserSecurityGroup}s
	 */
	public static boolean isChangeGroupMembersEnabled(){
		return isChangeGroupMembersEnabledTL.get();
	}

	public static void register(PersistenceManager pm) {
		pm.getExtent(SecurityChangeListenerUserSecurityGroupMembers.class);
		SecurityChangeListenerID id = SecurityChangeListenerID.create(Organisation.DEV_ORGANISATION_ID, "JFireLDAP."+SecurityChangeListenerUserSecurityGroupMembers.class.getSimpleName());
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			pm.makePersistent(new SecurityChangeListenerUserSecurityGroupMembers(id.organisationID, id.securityChangeListenerID));
		}
	}

	public static void unregister(PersistenceManager pm) {
		pm.getExtent(SecurityChangeListenerUserSecurityGroupMembers.class);
		SecurityChangeListenerID id = SecurityChangeListenerID.create(Organisation.DEV_ORGANISATION_ID, "JFireLDAP."+SecurityChangeListenerUserSecurityGroupMembers.class.getSimpleName());
		try {
			SecurityChangeListener listener = (SecurityChangeListener) pm.getObjectById(id);
			pm.deletePersistent(listener);
		} catch (JDOObjectNotFoundException x) {
			// do nothing because already unregistered
		}
	}
	
	/**
	 * {@link UserSecurityGroup} which members were changed and thus needs to be synchronizaed to LDAP
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private UserSecurityGroup userSecurityGroup;
	
	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SecurityChangeListenerUserSecurityGroupMembers() { }

	protected SecurityChangeListenerUserSecurityGroupMembers(String organisationID, String securityChangeListenerID) {
		super(organisationID, securityChangeListenerID);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void post_UserSecurityGroup_addMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event) {
		if (!isEnabled()){
			return;
		}
		if (!isChangeGroupMembersEnabled()){
			throw new RuntimeException("Adding new members to UserSecurityGroup is disalowed because of running synchronization to LDAP!");
		}
		userSecurityGroup = event.getUserSecurityGroup();
		super.post_UserSecurityGroup_addMember(event);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void post_UserSecurityGroup_removeMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event) {
		if (!isEnabled()){
			return;
		}
		if (!isChangeGroupMembersEnabled()){
			throw new RuntimeException("Removing members from UserSecurityGroup is disalowed because of running synchronization to LDAP!");
		}
		userSecurityGroup = event.getUserSecurityGroup();
		super.post_UserSecurityGroup_removeMember(event);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void on_SecurityChangeController_endChanging() {
		if (!isEnabled()){
			return;
		}
		syncUserSecurityGroup();
		super.on_SecurityChangeController_endChanging();
	}
	
	private void syncUserSecurityGroup(){
		if (userSecurityGroup == null){
			logger.warn("Can't proceed with because UserSecurityGroup is null!");
			return;
		}

		PersistenceManager pm = getPersistenceManager();
		Collection<LDAPServer> ldapServers = UserManagementSystem.getUserManagementSystemsByLeading(pm, false, LDAPServer.class);

		Throwable lastThrowable = null;
		for (LDAPServer ldapServer : ldapServers){
			try{
				
				LDAPSyncEvent syncEvent = new LDAPSyncEvent(SyncEventGenericType.SEND_AUTHORIZATION);
				SendEventTypeDataUnit dataUnit = new SendEventTypeDataUnit(
						UserSecurityGroupID.create(userSecurityGroup.getOrganisationID(), userSecurityGroup.getUserSecurityGroupID()));
				syncEvent.setSendEventTypeDataUnits(CollectionUtil.createArrayList(dataUnit));
				
				LDAPSyncInvocation.executeWithPreservedLDAPConnection(
						pm, new LDAPSyncInvocation(ldapServer.getUserManagementSystemObjectID(), syncEvent), ldapServer);
				
			}catch(Exception e){
				lastThrowable = e;
				logger.error(
						String.format(
								"Exception occured while trying to sync UserSecurityGroup with id %s to LDAPServer at %s:%s! Will continue with next LDAPServer.", 
								userSecurityGroup.getUserSecurityGroupID(), ldapServer.getHost(), ldapServer.getPort()), e);
			}
		}
		userSecurityGroup = null;
		if (lastThrowable != null){
			throw new RuntimeException(
					"Exception(s) occured during UserSecurityGroup sync to LDAP! Please see server log for details. Last one was " + lastThrowable.getMessage(), lastThrowable);
		}
	}
}