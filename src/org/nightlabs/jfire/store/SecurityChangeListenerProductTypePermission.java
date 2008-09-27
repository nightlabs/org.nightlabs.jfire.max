package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.AuthorizedObjectRef;
import org.nightlabs.jfire.security.RoleRef;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.listener.SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole;
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
public class SecurityChangeListenerProductTypePermission
extends SecurityChangeListener
{
	private static final Logger logger = Logger.getLogger(SecurityChangeListenerProductTypePermission.class);

	public static void register(PersistenceManager pm)
	{
		pm.getExtent(SecurityChangeListenerProductTypePermission.class);
		SecurityChangeListenerID id = SecurityChangeListenerID.create(Organisation.DEV_ORGANISATION_ID, SecurityChangeListenerProductTypePermission.class.getName());
		try {
			pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			pm.makePersistent(new SecurityChangeListenerProductTypePermission(id.organisationID, id.securityChangeListenerID));
		}
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SecurityChangeListenerProductTypePermission() { }

	protected SecurityChangeListenerProductTypePermission(String organisationID, String securityChangeListenerID) {
		super(organisationID, securityChangeListenerID);
	}

	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private Map<AuthorizedObject, Map<Authority, Integer>> seeRefCountBefore = new HashMap<AuthorizedObject, Map<Authority,Integer>>();

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private Map<AuthorizedObject, Map<Authority, Integer>> sellRefCountBefore = new HashMap<AuthorizedObject, Map<Authority,Integer>>();

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private Map<AuthorizedObject, Map<Authority, Integer>> reverseRefCountBefore = new HashMap<AuthorizedObject, Map<Authority,Integer>>();

	private static void addFirstEntryToRoleRefCountMap(Map<AuthorizedObject, Map<Authority, Integer>> authorizedObject2authority2roleRefCount, SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event, RoleID roleID)
	{
		if (!roleID.roleID.equals(event.getRole().getRoleID())) {
			// silently return
			return;
		}

		Map<Authority, Integer> authority2roleRefCount = authorizedObject2authority2roleRefCount.get(event.getAuthorizedObjectRef().getAuthorizedObject());
		if (authority2roleRefCount == null) {
			authority2roleRefCount = new HashMap<Authority, Integer>();
			authorizedObject2authority2roleRefCount.put(event.getAuthorizedObjectRef().getAuthorizedObject(), authority2roleRefCount);
		}

		Integer c = authority2roleRefCount.get(event.getAuthorizedObjectRef().getAuthority());
		if (c == null) {
			RoleRef roleRef = event.getAuthorizedObjectRef().getRoleRef(roleID);
			if (roleRef == null)
				authority2roleRefCount.put(event.getAuthorizedObjectRef().getAuthority(), 0);
			else
				authority2roleRefCount.put(event.getAuthorizedObjectRef().getAuthority(), roleRef.getReferenceCount());
		}
	}

	@Override
	public void pre_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event)
	{
		if (!(event.getAuthorizedObjectRef().getAuthorizedObject() instanceof UserLocal)) {
			// changes to UserSecurityGroups are reflected via ...addRole(...) calls to the UserLocals immediately
			// and cause this method to be triggered for the users directly. So we can ignore changes to user-security-groups.
			return;
		}

		addFirstEntryToRoleRefCountMap(seeRefCountBefore, event, org.nightlabs.jfire.store.RoleConstants.seeProductType);
		addFirstEntryToRoleRefCountMap(sellRefCountBefore, event, org.nightlabs.jfire.trade.RoleConstants.sellProductType);
		addFirstEntryToRoleRefCountMap(reverseRefCountBefore, event, org.nightlabs.jfire.trade.RoleConstants.reverseProductType);
	}

	@Override
	public void pre_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event) {
		if (!(event.getAuthorizedObjectRef().getAuthorizedObject() instanceof UserLocal)) {
			// changes to UserSecurityGroups are reflected via ...addRole(...) calls to the UserLocals immediately
			// and cause this method to be triggered for the users directly. So we can ignore changes to user-security-groups.
			return;
		}

		addFirstEntryToRoleRefCountMap(seeRefCountBefore, event, org.nightlabs.jfire.store.RoleConstants.seeProductType);
		addFirstEntryToRoleRefCountMap(sellRefCountBefore, event, org.nightlabs.jfire.trade.RoleConstants.sellProductType);
		addFirstEntryToRoleRefCountMap(reverseRefCountBefore, event, org.nightlabs.jfire.trade.RoleConstants.reverseProductType);
	}

	private static Map<UserLocalID, Set<AuthorityID>> getAffectedUserLocalIDs(Map<AuthorizedObject, Map<Authority, Integer>> authorizedObject2authority2roleRefCount, RoleID roleID)
	{
		Map<UserLocalID, Set<AuthorityID>> res = new HashMap<UserLocalID, Set<AuthorityID>>();

		for (Map.Entry<AuthorizedObject, Map<Authority, Integer>> me1 : authorizedObject2authority2roleRefCount.entrySet()) {
			AuthorizedObject authorizedObject = me1.getKey();
			UserLocal userLocal = (UserLocal) authorizedObject;
			UserLocalID userLocalID = (UserLocalID) JDOHelper.getObjectId(userLocal);

			for (Map.Entry<Authority, Integer> me2 : me1.getValue().entrySet()) {
				Authority authority = me2.getKey();
				int roleRefCountOld = me2.getValue().intValue();
				int roleRefCountNew;

				AuthorizedObjectRef authorizedObjectRef = authority.getAuthorizedObjectRef(authorizedObject);
				// Because the AuthorizedObjectRef might have been removed from the Authority (and deleted from the datastore),
				// authorizedObjectRef might be null now.
				if (authorizedObjectRef == null) {
					// The user is not managed directly or indirectly via UserSecurityGroups => Check _Other_
					if (User.USER_ID_OTHER.equals(userLocal.getUserID())) {
						// The user is _Other_ himself.
						roleRefCountNew = 0;
					}
					else {
						// the current user is *NOT* itself _Other_
						AuthorizedObjectID otherID = UserLocalID.create(userLocal.getOrganisationID(), User.USER_ID_OTHER, userLocal.getLocalOrganisationID());
						AuthorizedObjectRef otherAuthorizedObjectRef = authority.getAuthorizedObjectRef(otherID);
						if (otherAuthorizedObjectRef != null) {
							// _Other_ is not in the Authority, either
							roleRefCountNew = 0;
						}
						else {
							// access maybe granted indirectly by _Other_
							RoleRef roleRef = otherAuthorizedObjectRef.getRoleRef(roleID);
							if (roleRef == null)
								roleRefCountNew = 0;
							else
								roleRefCountNew = roleRef.getReferenceCount();
						}
					} // if (!User.USER_ID_OTHER.equals(userLocal.getUserID())) {
				}
				else {
					RoleRef roleRef = authorizedObjectRef.getRoleRef(roleID);
					if (roleRef == null)
						roleRefCountNew = 0;
					else
						roleRefCountNew = roleRef.getReferenceCount();
				}

				boolean grantedOld = roleRefCountOld > 0;
				boolean grantedNew = roleRefCountNew > 0;

				if (grantedOld != grantedNew) {
					Set<AuthorityID> authorityIDSet = res.get(userLocalID);
					if (authorityIDSet == null) {
						authorityIDSet = new HashSet<AuthorityID>();
						res.put(userLocalID, authorityIDSet);
					}
					authorityIDSet.add((AuthorityID) JDOHelper.getObjectId(authority));
				}
			}
		}

		return res;
	}


	@Override
	public void on_SecurityChangeController_endChanging() {
		try {
			CalculateProductTypePermissionFlagSetsInvocation invocation = new CalculateProductTypePermissionFlagSetsInvocation(
					getAffectedUserLocalIDs(seeRefCountBefore, org.nightlabs.jfire.store.RoleConstants.seeProductType),
					getAffectedUserLocalIDs(sellRefCountBefore, org.nightlabs.jfire.trade.RoleConstants.sellProductType),
					getAffectedUserLocalIDs(reverseRefCountBefore, org.nightlabs.jfire.trade.RoleConstants.reverseProductType)
			);
			if (!invocation.isEmpty()) {
				AsyncInvoke.exec(
						invocation,
						true
				);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class CalculateProductTypePermissionFlagSetsInvocation
	extends Invocation
	{
		private static final long serialVersionUID = 1L;
//		private static final Set<ProductType> variableAllNonClosedProductTypes = Collections.emptySet();
//		private static final Set<ProductType> variableAllNonExpiredProductTypes = Collections.emptySet();
		private static final Set<ProductType> variableAllAffectableProductTypes = Collections.emptySet();

		private Map<UserLocalID, Set<AuthorityID>> seeProductTypeGrantChanged;
		private Map<UserLocalID, Set<AuthorityID>> sellProductTypeGrantChanged;
		private Map<UserLocalID, Set<AuthorityID>> reverseProductTypeGrantChanged;

		public CalculateProductTypePermissionFlagSetsInvocation(
				Map<UserLocalID, Set<AuthorityID>> seeProductTypeGrantChanged,
				Map<UserLocalID, Set<AuthorityID>> sellProductTypeGrantChanged,
				Map<UserLocalID, Set<AuthorityID>> reverseProductTypeGrantChanged
		)
		{
			if (seeProductTypeGrantChanged == null)
				throw new IllegalArgumentException("seeProductTypeGrantChanged == null");

			if (sellProductTypeGrantChanged == null)
				throw new IllegalArgumentException("sellProductTypeGrantChanged == null");

			if (reverseProductTypeGrantChanged == null)
				throw new IllegalArgumentException("reverseProductTypeGrantChanged == null");

			this.seeProductTypeGrantChanged = seeProductTypeGrantChanged;
			this.sellProductTypeGrantChanged = sellProductTypeGrantChanged;
			this.reverseProductTypeGrantChanged = reverseProductTypeGrantChanged;

			if (logger.isDebugEnabled()) {
				logDebug("seeProductTypeGrantChanged:", seeProductTypeGrantChanged);
				logDebug("sellProductTypeGrantChanged:", sellProductTypeGrantChanged);
				logDebug("reverseProductTypeGrantChanged:", reverseProductTypeGrantChanged);
			}
		}

		private boolean isEmpty() {
			return seeProductTypeGrantChanged.isEmpty() && sellProductTypeGrantChanged.isEmpty() && reverseProductTypeGrantChanged.isEmpty();
		}

		private void logDebug(String msg, Map<UserLocalID, Set<AuthorityID>> map) {
			logger.debug(msg);
			if (map.isEmpty())
				logger.debug("  * {EMPTY}");

			for (Map.Entry<UserLocalID, Set<AuthorityID>> me : map.entrySet()) {
				logger.debug("  * userLocal: " + me.getKey().organisationID + "/" + me.getKey().userID + "/" + me.getKey().localOrganisationID);

				if (me.getValue().isEmpty())
					logger.debug("    - {EMPTY}");

				for (AuthorityID authorityID : me.getValue()) {
					logger.debug("    - authority: " + authorityID.organisationID + "/" + authorityID.authorityID);
				}
			}
		}

		@Override
		public Serializable invoke() throws Exception
		{


			return null;
		}
	}

//	/**
//	 * @jdo.field persistence-modifier="none"
//	 */
//	private Set<User> affectedUsersAllEvents = new HashSet<User>();

	private Set<ProductType> getAffectedProductTypes(Authority changedAuthority)
	{
		Query q = getPersistenceManager().newQuery(ProductType.class);
		q.setFilter("this.productTypeLocal.securingAuthorityID == :securingAuthorityID");

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("securingAuthorityID", JDOHelper.getObjectId(changedAuthority).toString());
		Collection<ProductType> c = CollectionUtil.castCollection((Collection<?>) q.executeWithMap(params));
		return new HashSet<ProductType>(c);
	}

//	@Override
//	public void processEvents(List<SecurityChangeEvent> events) {
//		PersistenceManager pm = getPersistenceManager();
//		Authority organisationAuthority = null;
//
//		Set<User> affectedUsersAllProductTypes = new HashSet<User>();
//		Map<User, Set<ProductType>> affectedUsersSomeProductTypes = new HashMap<User, Set<ProductType>>();
//
//		for (SecurityChangeEvent event : events) {
//			if (event instanceof SecurityChangeEvent_AuthorizedObjectRef_addRoleGroupRef) {
//				SecurityChangeEvent_AuthorizedObjectRef_addRoleGroupRef evt = (SecurityChangeEvent_AuthorizedObjectRef_addRoleGroupRef) event;
//				RoleGroup roleGroup = evt.getRoleGroupRef().getRoleGroup();
//				if (
//						roleGroup.containsRole(org.nightlabs.jfire.store.RoleConstants.seeProductType) ||
//						roleGroup.containsRole(org.nightlabs.jfire.trade.RoleConstants.sellProductType) ||
//						roleGroup.containsRole(org.nightlabs.jfire.trade.RoleConstants.reverseProductType)
//				)
//				{
//					if (organisationAuthority == null)
//						organisationAuthority = Authority.getOrganisationAuthority(pm);
//
//					if (evt.getAuthorizedObjectRef().getAuthorizedObject() instanceof UserSecurityGroup) {
//						UserSecurityGroup userSecurityGroup = (UserSecurityGroup) evt.getAuthorizedObjectRef().getAuthorizedObject();
//						// affects all members
//						for (AuthorizedObject member : userSecurityGroup.getMembers()) {
//							User user = ((UserLocal)member).getUser();
//							if (organisationAuthority.equals(evt.getAuthorizedObjectRef().getAuthority()))
//								affectedUsersAllProductTypes.add(user);
//							else
//								// TODO affectedUsersSomeProductTypes
//								;
//						}
//					}
//					else {
//						User user = ((UserLocal)evt.getAuthorizedObjectRef().getAuthorizedObject()).getUser();
//						if (organisationAuthority.equals(evt.getAuthorizedObjectRef().getAuthority()))
//							affectedUsersAllProductTypes.add(user);
//						else
//							// TODO affectedUsersSomeProductTypes
//							;
//
//					}
//				}
//			}
//			else if (event instanceof SecurityChangeEvent_AuthorizedObjectRef_removeRoleGroupRef) {
//				// TODO
//			}
//		}
//	}

}
