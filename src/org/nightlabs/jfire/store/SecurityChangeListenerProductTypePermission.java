package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.Collection;
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
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.AuthorizedObjectID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;
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

		addFirstEntryToRoleRefCountMap(
				authorizedObject2authority2roleRefCount,
				event.getAuthorizedObjectRef().getAuthority(),
				event.getAuthorizedObjectRef().getAuthorizedObject(),
				roleID
		);
	}

	private static void addFirstEntryToRoleRefCountMap(
			Map<AuthorizedObject, Map<Authority, Integer>> authorizedObject2authority2roleRefCount,
			Authority authority,
			AuthorizedObject authorizedObject,
			RoleID roleID)
	{
		// if the user "other" is modified, we need to add entries for the indirectly affected users (that are the others)
		if (authorizedObject instanceof UserLocal) {
			UserLocal userLocal = (UserLocal) authorizedObject;
			if (userLocal.getUserID().equals(User.USER_ID_OTHER)) {
				for (UserLocal userLocalMangedViaOther : authority.getUserLocalsManagedViaOther()) {
					addFirstEntryToRoleRefCountMap(
							authorizedObject2authority2roleRefCount,
							authority,
							userLocalMangedViaOther,
							roleID
					);
				}
			}
		}

		Map<Authority, Integer> authority2roleRefCount = authorizedObject2authority2roleRefCount.get(authorizedObject);
		if (authority2roleRefCount == null) {
			authority2roleRefCount = new HashMap<Authority, Integer>();
			authorizedObject2authority2roleRefCount.put(authorizedObject, authority2roleRefCount);
		}

		Integer c = authority2roleRefCount.get(authority);
		if (c == null) {
			int roleRefCount = getRoleRefCount(authority, authorizedObject, roleID);
			authority2roleRefCount.put(authority, new Integer(roleRefCount));

//			RoleRef roleRef = authorizedObjectRef.getRoleRef(roleID);
//			if (roleRef == null)
//				authority2roleRefCount.put(authorizedObjectRef.getAuthority(), 0);
//			else
//				authority2roleRefCount.put(authorizedObjectRef.getAuthority(), roleRef.getReferenceCount());
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

	private static int getRoleRefCount(Authority authority, AuthorizedObject authorizedObject, RoleID roleID)
	{
		AuthorizedObjectID authorizedObjectID = (AuthorizedObjectID) JDOHelper.getObjectId(authorizedObject);
		String localOrganisationID = SecurityReflector.getUserDescriptor().getOrganisationID();
		AuthorizedObjectID otherUserLocalID = UserLocalID.create(localOrganisationID, User.USER_ID_OTHER, localOrganisationID);

		int res;
		AuthorizedObjectRef authorizedObjectRef = authority.getAuthorizedObjectRef(authorizedObject);
		// Because the AuthorizedObjectRef might have been removed from the Authority (and deleted from the datastore),
		// authorizedObjectRef might be null now.
		if (authorizedObjectRef == null) {
			// The user is not managed directly or indirectly via UserSecurityGroups => Check _Other_
			if (authorizedObjectID.equals(otherUserLocalID)) {
				// The user is _Other_ himself.
				res = 0;
			}
			else {
				// the current user is *NOT* itself _Other_
				AuthorizedObjectRef otherAuthorizedObjectRef = authority.getAuthorizedObjectRef(otherUserLocalID);
				if (otherAuthorizedObjectRef == null) {
					// _Other_ is not in the Authority, either
					res = 0;
				}
				else {
					// access maybe granted indirectly by _Other_
					RoleRef roleRef = otherAuthorizedObjectRef.getRoleRef(roleID);
					if (roleRef == null)
						res = 0;
					else
						res = roleRef.getReferenceCount();
				}
			} // if (!User.USER_ID_OTHER.equals(userLocal.getUserID())) {
		}
		else {
			RoleRef roleRef = authorizedObjectRef.getRoleRef(roleID);
			if (roleRef == null)
				res = 0;
			else
				res = roleRef.getReferenceCount();
		}
		return res;
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
//				int roleRefCountNew;
//
//				AuthorizedObjectRef authorizedObjectRef = authority.getAuthorizedObjectRef(authorizedObject);
//				// Because the AuthorizedObjectRef might have been removed from the Authority (and deleted from the datastore),
//				// authorizedObjectRef might be null now.
//				if (authorizedObjectRef == null) {
//					// The user is not managed directly or indirectly via UserSecurityGroups => Check _Other_
//					if (User.USER_ID_OTHER.equals(userLocal.getUserID())) {
//						// The user is _Other_ himself.
//						roleRefCountNew = 0;
//					}
//					else {
//						// the current user is *NOT* itself _Other_
//						AuthorizedObjectID otherID = UserLocalID.create(userLocal.getOrganisationID(), User.USER_ID_OTHER, userLocal.getLocalOrganisationID());
//						AuthorizedObjectRef otherAuthorizedObjectRef = authority.getAuthorizedObjectRef(otherID);
//						if (otherAuthorizedObjectRef != null) {
//							// _Other_ is not in the Authority, either
//							roleRefCountNew = 0;
//						}
//						else {
//							// access maybe granted indirectly by _Other_
//							RoleRef roleRef = otherAuthorizedObjectRef.getRoleRef(roleID);
//							if (roleRef == null)
//								roleRefCountNew = 0;
//							else
//								roleRefCountNew = roleRef.getReferenceCount();
//						}
//					} // if (!User.USER_ID_OTHER.equals(userLocal.getUserID())) {
//				}
//				else {
//					RoleRef roleRef = authorizedObjectRef.getRoleRef(roleID);
//					if (roleRef == null)
//						roleRefCountNew = 0;
//					else
//						roleRefCountNew = roleRef.getReferenceCount();
//				}

				int roleRefCountNew = getRoleRefCount(authority, authorizedObject, roleID);

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

		private static void populateUserLocalID2ProductTypeSetMap(
				Map<UserLocalID, Set<ProductType>> destination,
				UserLocalID userLocalID,
				Collection<ProductType> productTypes
		)
		{
			Set<ProductType> collectedProductTypes = destination.get(userLocalID);
			if (collectedProductTypes == null) {
				collectedProductTypes = new HashSet<ProductType>(Math.max(10, productTypes.size()));
				destination.put(userLocalID, collectedProductTypes);
			}
			collectedProductTypes.addAll(productTypes);
		}

		private static void populateUserLocalID2AuthorityIDMap(Map<UserLocalID, Set<AuthorityID>> destination, Map<UserLocalID, Set<AuthorityID>> source)
		{
			for (Map.Entry<UserLocalID, Set<AuthorityID>> me : source.entrySet()) {
				Set<AuthorityID> authorityIDs = destination.get(me.getKey());
				if (authorityIDs == null) {
					authorityIDs = new HashSet<AuthorityID>();
					destination.put(me.getKey(), authorityIDs);
				}
				authorityIDs.addAll(me.getValue());
			}
		}

		@Override
		public Serializable invoke() throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				Set<UserLocalID> userLocalIDs = new HashSet<UserLocalID>(
						// prevent exception when max expected size has an overflow (theoretical max: 3 * Integer.MAX_VALUE) and becomes negative
						Math.max(
								10,
								seeProductTypeGrantChanged.size() + sellProductTypeGrantChanged.size() + reverseProductTypeGrantChanged.size()
						)
				);
				userLocalIDs.addAll(seeProductTypeGrantChanged.keySet());
				userLocalIDs.addAll(sellProductTypeGrantChanged.keySet());
				userLocalIDs.addAll(reverseProductTypeGrantChanged.keySet());

				Map<UserLocalID, Set<ProductType>> userLocalID2productTypes_see = new HashMap<UserLocalID, Set<ProductType>>();
				Map<UserLocalID, Set<ProductType>> userLocalID2productTypes_sellReverse = new HashMap<UserLocalID, Set<ProductType>>();

				for (UserLocalID userLocalID : userLocalIDs) {
					UserID userID = UserID.create(userLocalID);
					Collection<ProductType> productTypes = ProductTypePermissionFlagSet.getProductTypesMissingProductTypePermissionFlagSet(pm, userID);
					if (logger.isDebugEnabled()) {
						logger.debug("productTypesMissingProductTypePermissionFlagSet.size=" + productTypes.size());
						for (ProductType productType : productTypes) {
							logger.debug("* productType missing permissionFlagSet: " + productType);
						}
					}

					populateUserLocalID2ProductTypeSetMap(userLocalID2productTypes_see, userLocalID, productTypes);
					populateUserLocalID2ProductTypeSetMap(userLocalID2productTypes_sellReverse, userLocalID, productTypes);
				}

				for (Map.Entry<UserLocalID, Set<AuthorityID>> me : seeProductTypeGrantChanged.entrySet()) {
					UserLocalID userLocalID = me.getKey();
					Set<AuthorityID> authorityIDs = me.getValue();
					Set<ProductType> affectedProductTypes = getDirectlyAffectedProductTypes(pm, userLocalID, authorityIDs, true);
					populateUserLocalID2ProductTypeSetMap(userLocalID2productTypes_see, userLocalID, affectedProductTypes);
				}

				Map<UserLocalID, Set<AuthorityID>> sellOrReverseProductTypeGrantChanged = new HashMap<UserLocalID, Set<AuthorityID>>();
				populateUserLocalID2AuthorityIDMap(sellOrReverseProductTypeGrantChanged, sellProductTypeGrantChanged);
				populateUserLocalID2AuthorityIDMap(sellOrReverseProductTypeGrantChanged, reverseProductTypeGrantChanged);

				for (Map.Entry<UserLocalID, Set<AuthorityID>> me : sellOrReverseProductTypeGrantChanged.entrySet()) {
					UserLocalID userLocalID = me.getKey();
					Set<AuthorityID> authorityIDs = me.getValue();
					Set<ProductType> affectedProductTypes = getDirectlyAffectedProductTypes(pm, userLocalID, authorityIDs, false);
					populateUserLocalID2ProductTypeSetMap(userLocalID2productTypes_sellReverse, userLocalID, affectedProductTypes);
				}

				for (Map.Entry<UserLocalID, Set<ProductType>> me : userLocalID2productTypes_see.entrySet()) {
					UserLocalID userLocalID = me.getKey();
					Set<ProductType> productTypes = me.getValue();
					UserLocal userLocal = (UserLocal) pm.getObjectById(userLocalID);

					ProductTypePermissionFlagSet.updateFlagsSeeProductType(pm, productTypes, userLocal.getUser());
				}

				for (Map.Entry<UserLocalID, Set<ProductType>> me : userLocalID2productTypes_sellReverse.entrySet()) {
					UserLocalID userLocalID = me.getKey();
					Set<ProductType> productTypes = me.getValue();
					UserLocal userLocal = (UserLocal) pm.getObjectById(userLocalID);

					ProductTypePermissionFlagSet.updateFlagsSellReverseProductType(pm, productTypes, userLocal.getUser());
				}
			} finally {
				pm.close();
			}
			return null;
		}
	}

	private static Set<ProductType> getDirectlyAffectedProductTypes(
			PersistenceManager pm,
			UserLocalID userLocalID,
			Set<AuthorityID> authorityIDs,
			boolean includeClosedButNotYetExpired
	)
	{
		UserID userID = UserID.create(userLocalID);

		Set<String> securingAuthorityIDAsStringSet = new HashSet<String>(authorityIDs.size());
		boolean organisationAuthorityContained = false;
		for (AuthorityID authorityID : authorityIDs) {
			if (Authority.AUTHORITY_ID_ORGANISATION.equals(authorityID.authorityID))
				organisationAuthorityContained = true;
			securingAuthorityIDAsStringSet.add(authorityID.toString());
		}

//		Set<Authority> authorities = NLJDOHelper.getObjectSet(pm, authorityIDs, Authority.class);

		Query q = pm.newQuery(ProductType.class);
		StringBuilder filter = new StringBuilder();

		q.declareVariables(ProductTypePermissionFlagSet.class.getName() + " productTypePermissionFlagSet");
		filter.append("productTypePermissionFlagSet.productType == this");

		filter.append(" && this.inheritanceNature == "); filter.append(ProductType.INHERITANCE_NATURE_LEAF);

		if (includeClosedButNotYetExpired)
			filter.append(" && !productTypePermissionFlagSet.expired");
		else
			filter.append(" && !productTypePermissionFlagSet.closed");

		if (!organisationAuthorityContained) // if this authority is contained in the list, all product-types are affected - no matter what securing-authority they have configured
			filter.append(" && :securingAuthorityIDs.contains(this.productTypeLocal.securingAuthorityID)");

		q.setFilter(filter.toString());

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("securingAuthorityIDs", securingAuthorityIDAsStringSet);
		params.put("userID", userID);
		Collection<ProductType> c = CollectionUtil.castCollection((Collection<?>) q.executeWithMap(params));
		return new HashSet<ProductType>(c);
	}

}
