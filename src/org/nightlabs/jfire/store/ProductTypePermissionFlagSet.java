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

import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.security.id.AuthorityID;
import org.nightlabs.jfire.security.id.RoleID;
import org.nightlabs.jfire.security.id.UserID;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.Util;

/**
 * Holds a set of flags that give quick information about access rights a certain
 * {@link User} has to a certain {@link ProductType}.
 *
 * @author marco schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypePermissionFlagSet"
 *
 * @jdo.create-objectid-class
 *		field-order="productTypeOrganisationID, productTypeID, userOrganisationID, userID"
 *		include-body="id/ProductTypePermissionFlagSetID.body.inc"
 */
public class ProductTypePermissionFlagSet
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Map<String, Object> organisationID2mutex = new HashMap<String, Object>();
	public synchronized static Object getMutex(String organisationID)
	{
		Object mutex = organisationID2mutex.get(organisationID);
		if (mutex == null) {
			mutex = new Object();
			organisationID2mutex.put(organisationID, mutex);
		}
		return mutex;
	}

	/**
	 * Find all {@link ProductType}s for which - in combination with the specified {@link User} - there
	 * is no <code>ProductTypePermissionFlagSet</code> existing in the datastore.
	 *
	 * @param pm the door to the datastore.
	 * @param userID the user for which to check the presence of link <code>ProductTypePermissionFlagSet</code>s
	 * @return the product types that are missing a <code>ProductTypePermissionFlagSet</code>.
	 */
	public static Collection<ProductType> getProductTypesMissingProductTypePermissionFlagSet(PersistenceManager pm, UserID userID)
	{
		Query qSub1 = pm.newQuery(ProductTypePermissionFlagSet.class);
		qSub1.setResult("count(this.userID)");
		qSub1.setFilter(
				"this.productTypeOrganisationID == :subParamProductTypeOrganisationID && " +
				"this.productTypeID == :subParamProductTypeID && " +
				"this.userOrganisationID == \"" + userID.organisationID + "\" && " + // this didn't work with parameters, so I hardcode these arguments
				"this.userID == \"" + userID.userID + "\""
		);

		Query q = pm.newQuery(
				ProductType.class,
				"this.organisationID == :localOrganisationID && " +
				"this.inheritanceNature == " + ProductType.INHERITANCE_NATURE_LEAF + " && " +
				"1 > ptpfsCount"
		);
		q.declareVariables("long ptpfsCount");
		Map<String, Object> linkParams = new HashMap<String, Object>();
		linkParams.put("subParamProductTypeOrganisationID", "this.organisationID");
		linkParams.put("subParamProductTypeID", "this.productTypeID");
		q.addSubquery(qSub1, "long ptpfsCount", null, linkParams);

		Collection<ProductType> c = CollectionUtil.castCollection((Collection<?>) q.execute(
				LocalOrganisation.getLocalOrganisation(pm).getOrganisationID())
		);
		return c;
	}

	public static void updateFlags(PersistenceManager pm, Collection<ProductType> productTypes)
	{
		for (User user : getRelevantUsers(pm)) {
			updateFlags(pm, productTypes, user);
		}
	}

	public static Collection<? extends User> getRelevantUsers(PersistenceManager pm)
	{
		Query qUsers = pm.newQuery(User.class);
		qUsers.setFilter(
				"this.organisationID == :localOrganisationID && " +
				"this.userID != :systemUserID && " +
				"this.userID != :otherUserID"
		);

		Collection<?> users = (Collection<?>) qUsers.execute(
				LocalOrganisation.getLocalOrganisation(pm).getOrganisationID(),
				User.USER_ID_SYSTEM,
				User.USER_ID_OTHER
		);
		return CollectionUtil.castCollection(users);
	}

	public static void updateFlags(PersistenceManager pm, Collection<ProductType> productTypes, User user)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		if (productTypes == null)
			throw new IllegalArgumentException("productTypes must not be null!");

		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		updateFlagsSeeProductType(pm, productTypes, user);
		updateFlagsSellReverseProductType(pm, productTypes, user);
	}

	public static void updateFlagsSellReverseProductType(PersistenceManager pm, Collection<ProductType> productTypes, User user)
	{
		_updateFlagsSellReverseProductType(pm, productTypes, user);
	}
	private static void _updateFlagsSellReverseProductType(PersistenceManager pm, Collection<ProductType> productTypes, User user)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		if (productTypes == null)
			throw new IllegalArgumentException("productTypes must not be null!");

		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		UserID userID = (UserID) JDOHelper.getObjectId(user);
		if (userID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(user) returned null for " + user);

		Authority organisationAuthority = Authority.getOrganisationAuthority(pm);

		boolean sellMissingInOrganisationAuthority = !organisationAuthority.containsRoleRef(
				userID,
				org.nightlabs.jfire.trade.RoleConstants.sellProductType
		);

		boolean reverseMissingInOrganisationAuthority = !organisationAuthority.containsRoleRef(
				userID,
				org.nightlabs.jfire.trade.RoleConstants.reverseProductType
		);

		Set<ProductType> nestingProductTypes = new HashSet<ProductType>();

		Map<AuthorityID, Integer> authorityID2sellFlags = new HashMap<AuthorityID, Integer>();
		Map<AuthorityID, Integer> authorityID2reverseFlags = new HashMap<AuthorityID, Integer>();
		for (ProductType productType : productTypes) {
			if (productType.getInheritanceNature() != ProductType.INHERITANCE_NATURE_LEAF)
				continue; // silently ignore this productType and continue with the next one

			nestingProductTypes.addAll(ProductType.getProductTypesNestingThis(pm, productType));
			ProductTypePermissionFlagSet flagSet = getProductTypePermissionFlagSet(pm, productType, user, true, false);

			flagSet.setClosed(productType.isClosed());
			if (productType.isClosed()) {
				flagSet.setExpired(System.currentTimeMillis() - productType.getCloseTimestamp().getTime() > expireDurationMSec);
				flagSet.setFlags(org.nightlabs.jfire.trade.RoleConstants.sellProductType, FlagSellProductType.PRODUCT_TYPE_CLOSED);
				flagSet.setFlags(org.nightlabs.jfire.trade.RoleConstants.reverseProductType, FlagReverseProductType.PRODUCT_TYPE_CLOSED);
				continue; // the closed flag is alone - see javadoc
			}
			else
				flagSet.setExpired(false);

			AuthorityID securingAuthorityID = productType.getProductTypeLocal().getSecuringAuthorityID();

			Integer sellFlagsWithoutNestedData = authorityID2sellFlags.get(securingAuthorityID);
			if (sellFlagsWithoutNestedData == null) {
				sellFlagsWithoutNestedData = sellMissingInOrganisationAuthority ? FlagSellProductType.SELL_MISSING_IN_ORGANISATION_AUTHORITY : 0;
				if (securingAuthorityID != null) {
					Authority securingAuthority = (Authority) pm.getObjectById(securingAuthorityID);
					boolean missing = !securingAuthority.containsRoleRef(userID, org.nightlabs.jfire.trade.RoleConstants.sellProductType);
					sellFlagsWithoutNestedData |= missing ? FlagSellProductType.SELL_MISSING_IN_SECURING_AUTHORITY : 0;
				}
				authorityID2sellFlags.put(securingAuthorityID, sellFlagsWithoutNestedData);
			}

			Integer reverseFlagsWithoutNestedData = authorityID2reverseFlags.get(securingAuthorityID);
			if (reverseFlagsWithoutNestedData == null) {
				reverseFlagsWithoutNestedData = reverseMissingInOrganisationAuthority ? FlagReverseProductType.REVERSE_MISSING_IN_ORGANISATION_AUTHORITY : 0;
				if (securingAuthorityID != null) {
					Authority securingAuthority = (Authority) pm.getObjectById(securingAuthorityID);
					boolean missing = !securingAuthority.containsRoleRef(userID, org.nightlabs.jfire.trade.RoleConstants.reverseProductType);
					reverseFlagsWithoutNestedData |= missing ? FlagReverseProductType.REVERSE_MISSING_IN_SECURING_AUTHORITY : 0;
				}
				authorityID2reverseFlags.put(securingAuthorityID, reverseFlagsWithoutNestedData);
			}

			int sellFlagsOnlyNestedData = 0;
			int reverseFlagsOnlyNestedData = 0;
			for (NestedProductTypeLocal	nestedProductTypeLocal : productType.getProductTypeLocal().getNestedProductTypeLocals()) {
				ProductTypePermissionFlagSet nestedFlagSet = ProductTypePermissionFlagSet.getProductTypePermissionFlagSet(
						pm,
						nestedProductTypeLocal.getInnerProductTypeLocal().getProductType(),
						user,
						true,
						false
				);
				int sellNestedFlags = nestedFlagSet.getFlags(org.nightlabs.jfire.trade.RoleConstants.sellProductType);
				int reverseNestedFlags = nestedFlagSet.getFlags(org.nightlabs.jfire.trade.RoleConstants.reverseProductType);

				if ((sellNestedFlags & FlagSellProductType.SELL_MISSING_IN_ORGANISATION_AUTHORITY) != 0 ||
						(sellNestedFlags & FlagSellProductType.SELL_MISSING_FOR_INNER_PRODUCT_TYPE_IN_ORGANISATION_AUTHORITY) != 0)
					sellFlagsOnlyNestedData |= FlagSellProductType.SELL_MISSING_FOR_INNER_PRODUCT_TYPE_IN_ORGANISATION_AUTHORITY;

				if ((sellNestedFlags & FlagSellProductType.SELL_MISSING_IN_SECURING_AUTHORITY) != 0 ||
						(sellNestedFlags & FlagSellProductType.SELL_MISSING_FOR_INNER_PRODUCT_TYPE_IN_SECURING_AUTHORITY) != 0)
					sellFlagsOnlyNestedData |= FlagSellProductType.SELL_MISSING_FOR_INNER_PRODUCT_TYPE_IN_SECURING_AUTHORITY;

				if ((reverseNestedFlags & FlagReverseProductType.REVERSE_MISSING_IN_ORGANISATION_AUTHORITY) != 0 ||
						(reverseNestedFlags & FlagReverseProductType.REVERSE_MISSING_FOR_INNER_PRODUCT_TYPE_IN_ORGANISATION_AUTHORITY) != 0)
					reverseFlagsOnlyNestedData |= FlagReverseProductType.REVERSE_MISSING_FOR_INNER_PRODUCT_TYPE_IN_ORGANISATION_AUTHORITY;

				if ((reverseNestedFlags & FlagReverseProductType.REVERSE_MISSING_IN_SECURING_AUTHORITY) != 0 ||
						(reverseNestedFlags & FlagReverseProductType.REVERSE_MISSING_FOR_INNER_PRODUCT_TYPE_IN_SECURING_AUTHORITY) != 0)
					reverseFlagsOnlyNestedData |= FlagReverseProductType.REVERSE_MISSING_FOR_INNER_PRODUCT_TYPE_IN_SECURING_AUTHORITY;


				if ((sellNestedFlags & FlagSellProductType.PRODUCT_TYPE_CLOSED) != 0 ||
						(sellNestedFlags & FlagSellProductType.INNER_PRODUCT_TYPE_CLOSED) != 0)
					sellFlagsOnlyNestedData |= FlagSellProductType.INNER_PRODUCT_TYPE_CLOSED;

				if ((sellNestedFlags & FlagSellProductType.PRODUCT_TYPE_NOT_SALEABLE) != 0 ||
						(sellNestedFlags & FlagSellProductType.INNER_PRODUCT_TYPE_NOT_SALEABLE) != 0)
					sellFlagsOnlyNestedData |= FlagSellProductType.INNER_PRODUCT_TYPE_NOT_SALEABLE;


				if ((reverseNestedFlags & FlagReverseProductType.PRODUCT_TYPE_CLOSED) != 0 ||
						(reverseNestedFlags & FlagReverseProductType.INNER_PRODUCT_TYPE_CLOSED) != 0)
					reverseFlagsOnlyNestedData |= FlagReverseProductType.INNER_PRODUCT_TYPE_CLOSED;
			}

			int sellFlagSaleable = productType.isSaleable() ? 0 : FlagSellProductType.PRODUCT_TYPE_NOT_SALEABLE;

			flagSet.setFlags(org.nightlabs.jfire.trade.RoleConstants.sellProductType, sellFlagsWithoutNestedData | sellFlagsOnlyNestedData | sellFlagSaleable);
			flagSet.setFlags(org.nightlabs.jfire.trade.RoleConstants.reverseProductType, reverseFlagsWithoutNestedData | reverseFlagsOnlyNestedData);
		}

		// recursion up the nesting hierarchy
		if (!nestingProductTypes.isEmpty())
			_updateFlagsSellReverseProductType(pm, nestingProductTypes, user);
	}

	public static void updateFlagsSeeProductType(PersistenceManager pm, Collection<ProductType> productTypes, User user)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm must not be null!");

		if (productTypes == null)
			throw new IllegalArgumentException("productTypes must not be null!");

		if (user == null)
			throw new IllegalArgumentException("user must not be null!");

		UserID userID = (UserID) JDOHelper.getObjectId(user);
		if (userID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(user) returned null for " + user);

		if (User.USER_ID_SYSTEM.equals(userID.userID))
			throw new IllegalArgumentException("user must not be internal *system* user!");

		if (User.USER_ID_OTHER.equals(userID.userID))
			throw new IllegalArgumentException("user must not be internal *other* user!");

		boolean missingInOrganisationAuthority = !Authority.getOrganisationAuthority(pm).containsRoleRef(
				userID,
				org.nightlabs.jfire.store.RoleConstants.seeProductType
		);

		String localOrganisationID = LocalOrganisation.getLocalOrganisation(pm).getOrganisationID();

		Set<ProductType> nestingProductTypes = new HashSet<ProductType>();

		Map<AuthorityID, Integer> authorityID2flags = new HashMap<AuthorityID, Integer>();
		for (ProductType productType : productTypes) {
			if (!localOrganisationID.equals(productType.getOrganisationID()))
				throw new IllegalArgumentException("productType.organisationID does not reference the local organisation! " + productType);

			if (productType.getInheritanceNature() != ProductType.INHERITANCE_NATURE_LEAF)
				continue; // silently ignore this productType and continue with the next one
//				throw new IllegalArgumentException("productType.inheritanceNature is not ProductType.INHERITANCE_NATURE_LEAF! " + productType);

			nestingProductTypes.addAll(ProductType.getProductTypesNestingThis(pm, productType));
			ProductTypePermissionFlagSet flagSet = getProductTypePermissionFlagSet(pm, productType, user, true, false);

			flagSet.setClosed(productType.isClosed());
			if (productType.isClosed())
				flagSet.setExpired(System.currentTimeMillis() - productType.getCloseTimestamp().getTime() > expireDurationMSec);
			else
				flagSet.setExpired(false);

			AuthorityID securingAuthorityID = productType.getProductTypeLocal().getSecuringAuthorityID();
			Integer flagsWithoutNestedData = authorityID2flags.get(securingAuthorityID); // see is not escalating the nesting structure!
			if (flagsWithoutNestedData == null) {
				flagsWithoutNestedData = missingInOrganisationAuthority ? FlagSeeProductType.SEE_MISSING_IN_ORGANISATION_AUTHORITY : 0;
				if (securingAuthorityID != null) {
					Authority securingAuthority = (Authority)
					pm.getObjectById(securingAuthorityID);
					boolean missing = !securingAuthority.containsRoleRef(userID, org.nightlabs.jfire.store.RoleConstants.seeProductType);
					flagsWithoutNestedData |= missing ? FlagSeeProductType.SEE_MISSING_IN_SECURING_AUTHORITY : 0;
				}
				authorityID2flags.put(securingAuthorityID, flagsWithoutNestedData);
			}

			flagSet.setFlags(org.nightlabs.jfire.store.RoleConstants.seeProductType, flagsWithoutNestedData);
		}
	}

	private static final long expireDurationMSec = 1000L * 60L * 60L * 24L * 365L * 3L;

	public static final class FlagSeeProductType {
		/**
		 * The user has not the necessary access rights to see the ProductType in the global
		 * organisation's {@link Authority} (see {@link Authority#AUTHORITY_ID_ORGANISATION}).
		 */
		public static final int SEE_MISSING_IN_ORGANISATION_AUTHORITY = 001; // octal (leading "0")!

		/**
		 * The user has not the necessary access rights to see the ProductType in the {@link Authority}
		 * that's assigned to the {@link SecuredObject} (see {@link SecuredObject#getSecuringAuthorityID()}).
		 */
		public static final int SEE_MISSING_IN_SECURING_AUTHORITY = 002; // octal (leading "0")!

		/**
		 * A configurable time after a ProductType has been closed (usually 3 years after the close time-stamp),
		 * this flag is set causing the ProductType to disappear (not being listed anymore).
		 * <p>
		 * This flag is set alone - i.e. no other flag in this bitmask is set additionally -, because
		 * a ProductType with this flag is not tracked anymore to reduce system load.
		 * </p>
		 */
		public static final int PRODUCT_TYPE_CLOSED_AND_EXPIRED = 004; // octal (leading "0")!
	}

	public static final class FlagSellProductType {
		/**
		 * The user has not the necessary access rights to sell the ProductType in the global
		 * organisation's {@link Authority} (see {@link Authority#AUTHORITY_ID_ORGANISATION}).
		 */
		public static final int SELL_MISSING_IN_ORGANISATION_AUTHORITY = 001; // octal (leading "0")!

		/**
		 * The user has not the necessary access rights to sell the ProductType in the {@link Authority}
		 * that's assigned to the {@link SecuredObject} (see {@link SecuredObject#getSecuringAuthorityID()}).
		 */
		public static final int SELL_MISSING_IN_SECURING_AUTHORITY = 002; // octal (leading "0")!

		public static final int SELL_MISSING_FOR_INNER_PRODUCT_TYPE_IN_ORGANISATION_AUTHORITY = 004; // octal (leading "0")!

		public static final int SELL_MISSING_FOR_INNER_PRODUCT_TYPE_IN_SECURING_AUTHORITY = 010; // octal (leading "0")!

		/**
		 * The product type is closed. This is an irreversable state for the <code>ProductType</code>.
		 * <p>
		 * This flag is set alone - i.e. no other flag in this bitmask is set additionally -, because
		 * changes affecting this bitmask of a ProductType with this flag are not tracked anymore to reduce system load.
		 * </p>
		 */
		public static final int PRODUCT_TYPE_CLOSED = 020; // octal (leading "0")!

		public static final int INNER_PRODUCT_TYPE_CLOSED = 040; // octal (leading "0")!

		public static final int PRODUCT_TYPE_NOT_SALEABLE = 0100; // octal (leading "0")!

		public static final int INNER_PRODUCT_TYPE_NOT_SALEABLE = 0200; // octal (leading "0")!
	}

	public static final class FlagReverseProductType {
		/**
		 * The user has not the necessary access rights to sell the ProductType in the global
		 * organisation's {@link Authority} (see {@link Authority#AUTHORITY_ID_ORGANISATION}).
		 */
		public static final int REVERSE_MISSING_IN_ORGANISATION_AUTHORITY = 001; // octal (leading "0")!

		/**
		 * The user has not the necessary access rights to sell the ProductType in the {@link Authority}
		 * that's assigned to the {@link SecuredObject} (see {@link SecuredObject#getSecuringAuthorityID()}).
		 */
		public static final int REVERSE_MISSING_IN_SECURING_AUTHORITY = 002; // octal (leading "0")!

		public static final int REVERSE_MISSING_FOR_INNER_PRODUCT_TYPE_IN_ORGANISATION_AUTHORITY = 004; // octal (leading "0")!

		public static final int REVERSE_MISSING_FOR_INNER_PRODUCT_TYPE_IN_SECURING_AUTHORITY = 010; // octal (leading "0")!

		/**
		 * The product type is closed. This is an irreversable state for the <code>ProductType</code>.
		 * <p>
		 * This flag is set alone - i.e. no other flag in this bitmask is set additionally -, because
		 * changes affecting this bitmask of a ProductType with this flag are not tracked anymore to reduce system load.
		 * </p>
		 */
		public static final int PRODUCT_TYPE_CLOSED = 020; // octal (leading "0")!

		public static final int INNER_PRODUCT_TYPE_CLOSED = 040; // octal (leading "0")!
	}

	/**
	 * Get the <code>ProductTypePermissionFlagSet</code> for a certain combination of {@link ProductType} and {@link User}.
	 * If such an instance does not exist, either throw an exception or return <code>null</code>.
	 *
	 * @param pm the door to the datastore.
	 * @param productType the {@link ProductType} in question.
	 * @param user the {@link User} in question.
	 * @param throwExceptionIfNotExisting throw a {@link JDOObjectNotFoundException} instead of returning <code>null</code>.
	 * @return an instance of <code>ProductTypePermissionFlagSet</code> for the specified arguments or <code>null</code>.
	 */
	public static ProductTypePermissionFlagSet getProductTypePermissionFlagSet(PersistenceManager pm, ProductType productType, User user, boolean throwExceptionIfNotExisting)
	{
		return getProductTypePermissionFlagSet(pm, productType, user, false, throwExceptionIfNotExisting);
	}

	/**
	 * Get the <code>ProductTypePermissionFlagSet</code> for a certain combination of {@link ProductType} and {@link User}.
	 * If such an instance does not exist, it is created and persisted.
	 *
	 * @param pm the door to the datastore.
	 * @param productType the {@link ProductType} in question.
	 * @param user the {@link User} in question.
	 * @param createIfNotExisting if <code>true</code>, an instance is created if not existing (and <code>throwExceptionIfNotExisting</code> is ignored in this case).
	 * @param throwExceptionIfNotExisting throw a {@link JDOObjectNotFoundException} instead of returning <code>null</code>.
	 * @return an instance of <code>ProductTypePermissionFlagSet</code> for the specified arguments.
	 */
	private static ProductTypePermissionFlagSet getProductTypePermissionFlagSet(PersistenceManager pm, ProductType productType, User user, boolean createIfNotExisting, boolean throwExceptionIfNotExisting)
	{
		ProductTypePermissionFlagSet res;
		try {
			res =(ProductTypePermissionFlagSet) pm.getObjectById(ProductTypePermissionFlagSetID.create(productType, user));
			// if it has been deleted, re-persist it!
			if (JDOHelper.isDeleted(res)) {
				if (throwExceptionIfNotExisting)
					throw new JDOObjectNotFoundException("The object has been deleted! " + res);

				res = pm.makePersistent(res);
			}
		} catch (JDOObjectNotFoundException x) {
			if (!createIfNotExisting) {
				if (throwExceptionIfNotExisting)
					throw x;

				return null;
			}

			// does not yet exist => create it.
			res = new ProductTypePermissionFlagSet(productType, user);
			res = pm.makePersistent(res);
		}
		return res;
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productTypeOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productTypeID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String userOrganisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String userID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private User user;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int flagsSeeProductType;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int flagsSellProductType;
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int flagsReverseProductType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean closed;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean expired;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTypePermissionFlagSet() { }

	public ProductTypePermissionFlagSet(ProductType productType, User user)
	{
		this.productType = productType;
		this.productTypeOrganisationID = productType.getOrganisationID();
		this.productTypeID = productType.getProductTypeID();

		this.user = user;
		this.userOrganisationID = user.getOrganisationID();
		this.userID = user.getUserID();
	}

	public String getProductTypeOrganisationID() {
		return productTypeOrganisationID;
	}
	public String getProductTypeID() {
		return productTypeID;
	}
	public String getUserOrganisationID() {
		return userOrganisationID;
	}
	public String getUserID() {
		return userID;
	}

	public ProductType getProductType() {
		return productType;
	}
	public User getUser() {
		return user;
	}

	public int getFlags(RoleID roleID)
	{
		if (org.nightlabs.jfire.store.RoleConstants.seeProductType.equals(roleID))
			return flagsSeeProductType;
		else if (org.nightlabs.jfire.trade.RoleConstants.sellProductType.equals(roleID))
			return flagsSellProductType;
		else if (org.nightlabs.jfire.trade.RoleConstants.reverseProductType.equals(roleID))
			return flagsReverseProductType;
		else
			throw new IllegalArgumentException("Unsupported roleID: " + roleID);
	}

	private void setFlags(RoleID roleID, int flags)
	{
		if (org.nightlabs.jfire.store.RoleConstants.seeProductType.equals(roleID)) {
			if (this.flagsSeeProductType != flags)
				this.flagsSeeProductType = flags;
		}
		else if (org.nightlabs.jfire.trade.RoleConstants.sellProductType.equals(roleID)) {
			if (this.flagsSellProductType != flags)
				this.flagsSellProductType = flags;
		}
		else if (org.nightlabs.jfire.trade.RoleConstants.reverseProductType.equals(roleID)) {
			if (this.flagsReverseProductType != flags)
				this.flagsReverseProductType = flags;
		}
		else
			throw new IllegalArgumentException("Unsupported roleID: " + roleID);
	}

	public boolean isClosed() {
		return closed;
	}
	public boolean isExpired() {
		return expired;
	}

	private void setClosed(boolean closed)
	{
		if (this.closed == closed)
			return;

		this.closed = closed;

		if (closed) {
			this.flagsSellProductType |= FlagSellProductType.PRODUCT_TYPE_CLOSED;
			this.flagsReverseProductType |= FlagSellProductType.PRODUCT_TYPE_CLOSED;
		}
		else {
			updateFlagsSellReverseProductType(JDOHelper.getPersistenceManager(this), Collections.singleton(productType), user);
		}
	}

	private void setExpired(boolean expired)
	{
		if (this.expired == expired)
			return;

		this.expired = expired;

		if (expired) {
			this.flagsSeeProductType |= FlagSeeProductType.PRODUCT_TYPE_CLOSED_AND_EXPIRED;
		}
		else {
			updateFlagsSeeProductType(JDOHelper.getPersistenceManager(this), Collections.singleton(productType), user);
		}
	}

//	public int getFlagsSeeProductType() {
//		return flagsSeeProductType;
//	}
//	public void setFlagsSeeProductType(int flagSeeProductType) {
//		this.flagsSeeProductType = flagSeeProductType;
//	}
//
//	public int getFlagsSellProductType() {
//		return flagsSellProductType;
//	}
//	public void setFlagsSellProductType(int flagSellProductType) {
//		this.flagsSellProductType = flagSellProductType;
//	}
//
//	public int getFlagsReverseProductType() {
//		return flagsReverseProductType;
//	}
//	public void setFlagsReverseProductType(int flagReverseProductType) {
//		this.flagsReverseProductType = flagReverseProductType;
//	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((productTypeID == null) ? 0 : productTypeID.hashCode());
		result = prime * result + ((productTypeOrganisationID == null) ? 0 : productTypeOrganisationID.hashCode());
		result = prime * result + ((userID == null) ? 0 : userID.hashCode());
		result = prime * result + ((userOrganisationID == null) ? 0 : userOrganisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		final ProductTypePermissionFlagSet other = (ProductTypePermissionFlagSet) obj;
		return (
				Util.equals(this.productTypeOrganisationID, other.productTypeOrganisationID) &&
				Util.equals(this.productTypeID, other.productTypeID) &&
				Util.equals(this.userOrganisationID, other.userOrganisationID) &&
				Util.equals(this.userID, other.userID)
		);
	}

	@Override
	public String toString() {
		return getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + productTypeOrganisationID + ',' + productTypeID + ',' + userOrganisationID + ',' + userID + ']';
	}
}
