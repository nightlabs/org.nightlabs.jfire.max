package org.nightlabs.jfire.accounting.tariffuserset;

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
public class SecurityChangeListenerTariffUserSet extends SecurityChangeListener
{
	private static final long serialVersionUID = 1L;

	public static void register(PersistenceManager pm)
	{
		pm.getExtent(SecurityChangeListenerTariffUserSet.class);
		SecurityChangeListenerID id = SecurityChangeListenerID.create(Organisation.DEV_ORGANISATION_ID, SecurityChangeListenerTariffUserSet.class.getName());
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			pm.makePersistent(new SecurityChangeListenerTariffUserSet(id.organisationID, id.securityChangeListenerID));
		}
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SecurityChangeListenerTariffUserSet() { }

	protected SecurityChangeListenerTariffUserSet(String organisationID, String securityChangeListenerID)
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
		for (AuthorizedObjectRef groupAuthorizedObjectRef : AuthorizedObjectRef.getAuthorizedObjectRefs(pm, (AuthorizedObjectID) JDOHelper.getObjectId(userSecurityGroup))) {
			TariffUserSet tariffUserSet = groupAuthorizedObjectRef.getTariffUserSet();
			AuthorizedObjectRef memberAuthorizedObjectRef = tariffUserSet.createAuthorizedObjectRef((AuthorizedObjectID) JDOHelper.getObjectId(member));
			memberAuthorizedObjectRef.incReferenceCount();
			for (TariffRef groupTariffRef : groupAuthorizedObjectRef.getTariffRefs()) {
				TariffRef memberTariffRef = memberAuthorizedObjectRef.createTariffRef(groupTariffRef.getTariff());
				memberTariffRef.incReferenceCount(groupTariffRef.getReferenceCount());
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
		for (AuthorizedObjectRef groupAuthorizedObjectRef : AuthorizedObjectRef.getAuthorizedObjectRefs(pm, (AuthorizedObjectID) JDOHelper.getObjectId(userSecurityGroup))) {
			TariffUserSet tariffUserSet = groupAuthorizedObjectRef.getTariffUserSet();
			AuthorizedObjectRef memberAuthorizedObjectRef = tariffUserSet.getAuthorizedObjectRef((AuthorizedObjectID) JDOHelper.getObjectId(member));
			if (memberAuthorizedObjectRef != null) {
				for (TariffRef groupTariffRef : groupAuthorizedObjectRef.getTariffRefs()) {
					TariffRef memberTariffRef = memberAuthorizedObjectRef.getTariffRef(groupTariffRef.getTariff());
					if (memberTariffRef != null) {
						int newRefCount = memberTariffRef.decReferenceCount(groupTariffRef.getReferenceCount());
						if (newRefCount == 0) {
							memberAuthorizedObjectRef.internalRemoveTariffRef(memberTariffRef);
						}
					}
				}

				int newRefCount = memberAuthorizedObjectRef.decReferenceCount();
				if (newRefCount == 0)
					tariffUserSet.internalRemoveAuthorizedObjectRef(memberAuthorizedObjectRef);
			} // if (memberAuthorizedObjectRef != null) {
		}
	}

	@Override
	public void on_SecurityChangeController_endChanging() {
		super.on_SecurityChangeController_endChanging();
	}
}
