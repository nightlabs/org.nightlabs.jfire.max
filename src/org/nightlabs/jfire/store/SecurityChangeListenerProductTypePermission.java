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
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.AuthorizedObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.UserLocal;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.security.id.UserLocalID;
import org.nightlabs.jfire.security.listener.SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef;
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
	private Map<AuthorizedObject, Map<Authority, Map<RoleID, Boolean>>> roleGrantedBackupMap = new HashMap<AuthorizedObject, Map<Authority,Map<RoleID,Boolean>>>();

	private static final Set<RoleID> interestingRoleIDs;
	static {
		Set<RoleID> set = new HashSet<RoleID>();

		set.add(org.nightlabs.jfire.store.RoleConstants.seeProductType);
		set.add(org.nightlabs.jfire.trade.RoleConstants.sellProductType);
		set.add(org.nightlabs.jfire.trade.RoleConstants.reverseProductType);

		interestingRoleIDs = Collections.unmodifiableSet(set);
	}

	private void backupRoleGranted(AuthorizedObject authorizedObject, Authority authority, RoleID roleID)
	{
		if (authorizedObject == null)
			throw new IllegalArgumentException("authorizedObject == null");

		if (authority == null)
			throw new IllegalArgumentException("authority == null");

		if (roleID == null)
			throw new IllegalArgumentException("roleID == null");

		if (!(authorizedObject instanceof UserLocal)) {
			// changes to UserSecurityGroups are reflected via ...addRole(...) calls to the UserLocals immediately
			// and cause this method to be triggered for the users directly. So we can ignore changes to user-security-groups.
			return;
		}

		UserLocal userLocal = (UserLocal) authorizedObject;
		UserID userID = (UserID) JDOHelper.getObjectId(userLocal.getUser());
		if (userID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(userLocal.getUser()) returned null!");

		if (!interestingRoleIDs.contains(roleID))
			return;

		// ignore foreign users (e.g. the own UserLocal at a remote organisation is usually synced for self-information - see JFireSecurityManagerBean.getAuthoritiesSelfInformation(...))
		String localOrganisationID = LocalOrganisation.getLocalOrganisation(getPersistenceManager()).getOrganisationID();
		if (!localOrganisationID.equals(userLocal.getOrganisationID()))
			return;

		if (User.USER_ID_OTHER.equals(userLocal.getUserID())) {
			for (UserLocal userLocalManagedViaOther : authority.getUserLocalsManagedViaOther())
				backupRoleGranted(userLocalManagedViaOther, authority, roleID);
		}
		else if (!User.USER_ID_SYSTEM.equals(userLocal.getUserID())) {
			Map<Authority, Map<RoleID, Boolean>> m1 = roleGrantedBackupMap.get(authorizedObject);
			if (m1 == null) {
				m1 = new HashMap<Authority, Map<RoleID,Boolean>>();
				roleGrantedBackupMap.put(authorizedObject, m1);
			}

			Map<RoleID, Boolean> m2 = m1.get(authority);
			if (m2 == null) {
				m2 = new HashMap<RoleID, Boolean>();
				m1.put(authority, m2);
			}

			Boolean grantedOld = m2.get(roleID);
			if (grantedOld == null) {
				boolean granted = authority.containsRoleRef(userID, roleID);
				m2.put(roleID, granted);
			}
		}
	}

	private void backupRoleGranted(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event)
	{
		Authority authority = event.getAuthorizedObjectRef().getAuthority();
		AuthorizedObject authorizedObject = event.getAuthorizedObjectRef().getAuthorizedObject();
		if (!(authorizedObject instanceof UserLocal)) {
			// changes to UserSecurityGroups are reflected via ...addRole(...) calls to the UserLocals immediately
			// and cause this method to be triggered for the users directly. So we can ignore changes to user-security-groups.
			return;
		}

		RoleID roleID = (RoleID) JDOHelper.getObjectId(event.getRole());
		backupRoleGranted(authorizedObject, authority, roleID);
	}

	private void backupRoleGranted(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event)
	{
		Authority authority = event.getAuthority();
		AuthorizedObject authorizedObject = event.getAuthorizedObject();
		if (!(authorizedObject instanceof UserLocal)) {
			// changes to UserSecurityGroups are reflected via ...addRole(...) calls to the UserLocals immediately
			// and cause this method to be triggered for the users directly. So we can ignore changes to user-security-groups.
			return;
		}

		for (RoleID roleID : interestingRoleIDs)
			backupRoleGranted(authorizedObject, authority, roleID);
	}

	@Override
	public void pre_Authority_createAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event) {
		backupRoleGranted(event);
	}

	@Override
	public void pre_Authority_destroyAuthorizedObjectRef(SecurityChangeEvent_Authority_createDestroyAuthorizedObjectRef event) {
		backupRoleGranted(event);
	}

	@Override
	public void pre_AuthorizedObjectRef_addRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event) {
		backupRoleGranted(event);
	}

	@Override
	public void pre_AuthorizedObjectRef_removeRole(SecurityChangeEvent_AuthorizedObjectRef_addRemoveRole event) {
		backupRoleGranted(event);
	}

	private Map<UserLocalID, Set<AuthorityID>> getAffectedUserLocalIDs(RoleID roleID)
	{
		Map<UserLocalID, Set<AuthorityID>> res = new HashMap<UserLocalID, Set<AuthorityID>>();

		for (Map.Entry<AuthorizedObject, Map<Authority, Map<RoleID, Boolean>>> me1 : roleGrantedBackupMap.entrySet()) {
			AuthorizedObject authorizedObject = me1.getKey();
			UserLocal userLocal = (UserLocal) authorizedObject;

			UserLocalID userLocalID = (UserLocalID) JDOHelper.getObjectId(userLocal);
			UserID userID = UserID.create(userLocalID);

			for (Map.Entry<Authority, Map<RoleID, Boolean>> me2 : me1.getValue().entrySet()) {
				Authority authority = me2.getKey();

				Boolean oldGranted = me2.getValue().get(roleID);
				if (oldGranted != null) {
					boolean newGranted = authority.containsRoleRef(userID, roleID);
					if (oldGranted.booleanValue() != newGranted) {
						Set<AuthorityID> authorityIDSet = res.get(userLocalID);
						if (authorityIDSet == null) {
							authorityIDSet = new HashSet<AuthorityID>();
							res.put(userLocalID, authorityIDSet);
						}
						authorityIDSet.add((AuthorityID) JDOHelper.getObjectId(authority));
					}
				}
			}
		}

		return res;
	}

	@Override
	public void on_SecurityChangeController_endChanging() {
		try {
			CalculateProductTypePermissionFlagSetsInvocation invocation = new CalculateProductTypePermissionFlagSetsInvocation(
					getAffectedUserLocalIDs(org.nightlabs.jfire.store.RoleConstants.seeProductType),
					getAffectedUserLocalIDs(org.nightlabs.jfire.trade.RoleConstants.sellProductType),
					getAffectedUserLocalIDs(org.nightlabs.jfire.trade.RoleConstants.reverseProductType)
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
			synchronized (ProductTypePermissionFlagSet.getMutex(getOrganisationID())) {

				PersistenceManager pm = getPersistenceManager();
				try {
					NLJDOHelper.enableTransactionSerializeReadObjects(pm);
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
						NLJDOHelper.disableTransactionSerializeReadObjects(pm);
					}
				} finally {
					pm.close();
				}
				return null;

			} // synchronized (ProductTypePermissionFlagSet.getMutex(getOrganisationID())) {
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
