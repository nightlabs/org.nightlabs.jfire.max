package org.nightlabs.jfire.store;

import java.io.Serializable;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.Authority;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.id.ProductTypePermissionFlagSetID;
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
 *
 * @jdo.create-objectid-class
 *		field-order="productTypeOrganisationID, productTypeID, userOrganisationID, userID"
 *		include-body="id/ProductTypePermissionFlagSetID.body.inc"
 */
public class ProductTypePermissionFlagSet
implements Serializable
{
	private static final long serialVersionUID = 1L;

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
	 * If such an instance does not exist, it is created and persisted.
	 *
	 * @param pm the door to the datastore.
	 * @param productType the {@link ProductType} in question.
	 * @param user the {@link User} in question.
	 * @param throwExceptionIfNotExisting throw a {@link JDOObjectNotFoundException} instead of creating it.
	 * @return an instance of <code>ProductTypePermissionFlagSet</code> for the specified arguments.
	 */
	public static ProductTypePermissionFlagSet getProductTypePermissionFlagSet(PersistenceManager pm, ProductType productType, User user, boolean throwExceptionIfNotExisting)
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
			if (throwExceptionIfNotExisting)
				throw x;

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

	public int getFlagsSeeProductType() {
		return flagsSeeProductType;
	}
	public void setFlagsSeeProductType(int flagSeeProductType) {
		this.flagsSeeProductType = flagSeeProductType;
	}

	public int getFlagsSellProductType() {
		return flagsSellProductType;
	}
	public void setFlagsSellProductType(int flagSellProductType) {
		this.flagsSellProductType = flagSellProductType;
	}

	public int getFlagsReverseProductType() {
		return flagsReverseProductType;
	}
	public void setFlagsReverseProductType(int flagReverseProductType) {
		this.flagsReverseProductType = flagReverseProductType;
	}

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
