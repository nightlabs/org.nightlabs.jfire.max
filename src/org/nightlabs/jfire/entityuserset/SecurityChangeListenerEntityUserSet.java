package org.nightlabs.jfire.entityuserset;

import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.IAuthorizedObject;
import org.nightlabs.jfire.security.UserSecurityGroup;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.listener.SecurityChangeEvent_UserSecurityGroup_addRemoveMember;
import org.nightlabs.jfire.security.listener.SecurityChangeListener;
import org.nightlabs.jfire.security.listener.id.SecurityChangeListenerID;
import org.nightlabs.util.CollectionUtil;

/**
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class SecurityChangeListenerEntityUserSet extends SecurityChangeListener
{
	private static final long serialVersionUID = 1L;

	public static void register(PersistenceManager pm)
	{
		pm.getExtent(SecurityChangeListenerEntityUserSet.class);
		SecurityChangeListenerID id = SecurityChangeListenerID.create(Organisation.DEV_ORGANISATION_ID, SecurityChangeListenerEntityUserSet.class.getName());
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			pm.makePersistent(new SecurityChangeListenerEntityUserSet(id.organisationID, id.securityChangeListenerID));
		}
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SecurityChangeListenerEntityUserSet() { }

	protected SecurityChangeListenerEntityUserSet(String organisationID, String securityChangeListenerID)
	{
		super(organisationID, securityChangeListenerID);
	}

	@Override
	public void pre_UserSecurityGroup_addMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event) {
		super.pre_UserSecurityGroup_addMember(event);
	}

	@Override
	public void post_UserSecurityGroup_addMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event) {
		super.post_UserSecurityGroup_addMember(event);
		PersistenceManager pm = getPersistenceManager();

		UserSecurityGroup userSecurityGroup = event.getUserSecurityGroup();
		IAuthorizedObject member = event.getMember();
		Collection<AuthorizedObjectRef<Object>> c = CollectionUtil.castCollection(AuthorizedObjectRef.getAuthorizedObjectRefs(pm, (AuthorizedObjectID) JDOHelper.getObjectId(userSecurityGroup)));
		for (AuthorizedObjectRef<Object> groupAuthorizedObjectRef : c) {
			EntityUserSet<Object> entityUserSet = groupAuthorizedObjectRef.getEntityUserSet();
			AuthorizedObjectRef<Object> memberAuthorizedObjectRef = entityUserSet.createOrGetAuthorizedObjectRef((AuthorizedObjectID) JDOHelper.getObjectId(member));
			memberAuthorizedObjectRef.incReferenceCount();
			for (EntityRef<Object> groupEntityRef : groupAuthorizedObjectRef.getEntityRefs()) {
				EntityRef<Object> memberEntityRef = memberAuthorizedObjectRef.createEntityRef(groupEntityRef.getEntity());
				memberEntityRef.incReferenceCount(groupEntityRef.getReferenceCount());
			}
		}
	}

	@Override
	public void pre_UserSecurityGroup_removeMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event) {
		super.pre_UserSecurityGroup_removeMember(event);
	}

	@Override
	public void post_UserSecurityGroup_removeMember(SecurityChangeEvent_UserSecurityGroup_addRemoveMember event) {
		super.post_UserSecurityGroup_removeMember(event);

		PersistenceManager pm = getPersistenceManager();

		UserSecurityGroup userSecurityGroup = event.getUserSecurityGroup();
		IAuthorizedObject member = event.getMember();
		Collection<AuthorizedObjectRef<Object>> c = CollectionUtil.castCollection(AuthorizedObjectRef.getAuthorizedObjectRefs(pm, (AuthorizedObjectID) JDOHelper.getObjectId(userSecurityGroup)));
		for (AuthorizedObjectRef<Object> groupAuthorizedObjectRef : c) {
			EntityUserSet<Object> entityUserSet = groupAuthorizedObjectRef.getEntityUserSet();
			AuthorizedObjectRef<Object> memberAuthorizedObjectRef = entityUserSet.getAuthorizedObjectRef((AuthorizedObjectID) JDOHelper.getObjectId(member));
			if (memberAuthorizedObjectRef != null) {
				for (EntityRef<Object> groupEntityRef : groupAuthorizedObjectRef.getEntityRefs()) {
					EntityRef<Object> memberEntityRef = memberAuthorizedObjectRef.getEntityRef(groupEntityRef.getEntity());
					if (memberEntityRef != null) {
						int newRefCount = memberEntityRef.decReferenceCount(groupEntityRef.getReferenceCount());
						if (newRefCount == 0) {
							memberAuthorizedObjectRef.internalRemoveEntityRef(memberEntityRef);
						}
					}
				}

				int newRefCount = memberAuthorizedObjectRef.decReferenceCount();
				if (newRefCount == 0)
					entityUserSet.internalRemoveAuthorizedObjectRef(memberAuthorizedObjectRef);
			} // if (memberAuthorizedObjectRef != null) {
		}
	}

	@Override
	public void on_SecurityChangeController_endChanging() {
		super.on_SecurityChangeController_endChanging();
	}
}
