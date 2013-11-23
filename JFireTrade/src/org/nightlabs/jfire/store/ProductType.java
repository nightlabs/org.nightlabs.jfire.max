/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Discriminator;
import javax.jdo.annotations.DiscriminatorStrategy;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Value;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.AttachCallback;
import javax.jdo.listener.DetachCallback;
import javax.jdo.listener.StoreCallback;

import org.apache.log4j.Logger;
import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.InheritanceCallbacks;
import org.nightlabs.inheritance.InheritanceManager;
import org.nightlabs.inheritance.StaticFieldMetaData;
import org.nightlabs.jdo.FetchPlanBackup;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jdo.inheritance.JDOInheritableFieldInheriter;
import org.nightlabs.jdo.inheritance.JDOInheritanceManager;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.accounting.tariffuserset.TariffUserSet;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.IndirectlySecuredObject;
import org.nightlabs.jfire.security.SecuredObject;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.endcustomer.EndCustomerReplicationPolicy;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.NLLocale;
import org.nightlabs.util.Util;

/**
 * <p>
 * This is the base class for all product-types in the ERP. You subclass it in order to implement
 * your own specialized saleable goods.
 * </p>
 * <p>
 * {@link Product}s for a <code>ProductType</code> are created via the assigned {@link ProductTypeActionHandler}.
 * </p>
 * <p>
 * <code>ProductType</code>s are organized in a data-inheritance-tree, where each child can inherit
 * properties from the parent (or override some of them). Usually, there is one root-<code>ProductType</code>
 * ({@link #getExtendedProductType()}<code> == null</code>) for each implementation of <code>ProductType</code>
 * and each {@link Organisation} (the local one and every business partner).
 * </p>
 * <p>
 * It's important to
 * understand the difference between a {@link Product} and a <code>ProductType</code>:
 * While the <code>Product</code> is one concrete thing that can only be at one
 * location and owned by one person at a time, a <code>ProductType</code> specifies
 * the common properties of all its <code>Product</code>s.
 * </p>
 * <p>
 * Imagine the following example: You want to sell balls. Each type of ball - say
 * specified by diameter and color - would be represented by a <code>BallType</code>.
 * If your store has enough space for 100 green balls with a diameter of 30 cm and
 * 200 red balls with a diameter of 90 cm, then you would probably have two instances of
 * <code>BallType</code> and 300 instances of <code>Ball</code> in your datastore.
 * <code>BallType</code> would extend <code>ProductType</code> and <code>Ball</code> would
 * be an implementation of {@link Product}.
 * </p>
 *
 * @author Niklas Schiffler <nick@nightlabs.de>
 * @author marco schulze - marco at nightlabs dot de
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeID"
 *		detachable="true"
 *		table="JFireTrade_ProductType"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class
 *		field-order="organisationID, productTypeID"
 *		include-body="id/ProductTypeID.body.inc"
 *
 * @jdo.fetch-group name="ProductType.extendedProductType" fields="extendedProductType"
 * @jdo.fetch-group name="ProductType.extendedProductType[2]" fields="extendedProductType[2]"
 * @jdo.fetch-group name="ProductType.extendedProductType[-1]" fields="extendedProductType[-1]"
 * @jdo.fetch-group name="ProductType.fieldMetaDataMap" fields="fieldMetaDataMap"
 * @jdo.fetch-group name="ProductType.innerPriceConfig" fields="innerPriceConfig"
 * @jdo.fetch-group name="ProductType.packagePriceConfig" fields="packagePriceConfig"
 * @jdo.fetch-group name="ProductType.owner" fields="owner"
 * @jdo.fetch-group name="ProductType.vendor" fields="vendor"
 * @jdo.fetch-group name="ProductType.deliveryConfiguration" fields="deliveryConfiguration"
 * @jdo.fetch-group name="ProductType.productTypeGroups" fields="productTypeGroups"
 * @jdo.fetch-group name="ProductType.managedProductTypeGroup" fields="managedProductTypeGroup"
 * @jdo.fetch-group name="ProductType.productTypeLocal" fields="productTypeLocal"
 * @jdo.fetch-group name="ProductType.name" fields="name"
 * @jdo.fetch-group name="ProductType.tariffUserSet" fields="tariffUserSet"
 * @jdo.fetch-group name="ProductType.endCustomerReplicationPolicy" fields="endCustomerReplicationPolicy"
 * @jdo.fetch-group name="ProductType.this" fetch-groups="default" fields="deliveryConfiguration, extendedProductType, fieldMetaDataMap, innerPriceConfig, managedProductTypeGroup, name, owner, packagePriceConfig, productTypeGroups, productTypeLocal"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="name, vendor"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="name, vendor"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="name, vendor"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="name, vendor"
 *
 * @!jdo.fetch-group name="FetchGroupsCrossTrade.ProductTypeForReseller" fetch-groups="default" fields="deliveryConfiguration, extendedProductType, fieldMetaDataMap, innerPriceConfig, localAccountantDelegate, localStorekeeperDelegate, managedProductTypeGroup, name, owner, packagePriceConfig, productTypeGroups"
 *
 * @jdo.query name="getProductTypesOfProductTypeGroup" query="
 *		SELECT
 *		WHERE
 *			this.productTypeGroups.containsValue(productTypeGroup) &&
 *			productTypeGroup.organisationID == paramOrganisationID &&
 *			productTypeGroup.productTypeGroupID == paramProductTypeGroupID
 *		VARIABLES ProductTypeGroup productTypeGroup
 *		PARAMETERS String paramOrganisationID, String paramProductTypeGroupID
 *		import java.lang.String; import org.nightlabs.jfire.store.ProductTypeGroup"
 *
 * @jdo.query
 *		name="getChildProductTypes_topLevel"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE this.extendedProductType == null"
 *
 * @jdo.query
 *		name="getChildProductTypes_hasParent"
 *		language="javax.jdo.query.JDOQL"
 *		query="SELECT
 *		  WHERE
 *		    this.extendedProductType.organisationID == parentProductTypeOrganisationID &&
 *		    this.extendedProductType.productTypeID == parentProductTypeProductTypeID
 *		  PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID
 *		  import java.lang.String"
 *
 * @jdo.query
 *		name="getProductTypesNestingThis"
 *		query="SELECT
 *			WHERE
 *				this.productTypeLocal.nestedProductTypeLocals.containsValue(nestedProductTypeLocal) &&
 *				nestedProductTypeLocal.innerProductTypeLocal.productType == :productType
 *				VARIABLES org.nightlabs.jfire.store.NestedProductTypeLocal nestedProductTypeLocal"
 *
 */
@PersistenceCapable(
	objectIdClass=ProductTypeID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ProductType")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name=ProductType.FETCH_GROUP_EXTENDED_PRODUCT_TYPE,
		members=@Persistent(name="extendedProductType")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_EXTENDED_PRODUCT_TYPE_2,
		members=@Persistent(
			name="extendedProductType",
			recursionDepth=2)),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_EXTENDED_PRODUCT_TYPE_NO_LIMIT,
		members=@Persistent(
			name="extendedProductType",
			recursionDepth=-1)),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_FIELD_METADATA_MAP,
		members=@Persistent(name="fieldMetaDataMap")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_INNER_PRICE_CONFIG,
		members=@Persistent(name="innerPriceConfig")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_PACKAGE_PRICE_CONFIG,
		members=@Persistent(name="packagePriceConfig")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_OWNER,
		members=@Persistent(name="owner")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_VENDOR,
		members=@Persistent(name="vendor")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_DELIVERY_CONFIGURATION,
		members=@Persistent(name="deliveryConfiguration")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_PRODUCT_TYPE_GROUPS,
		members=@Persistent(name="productTypeGroups")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_MANAGED_PRODUCT_TYPE_GROUP,
		members=@Persistent(name="managedProductTypeGroup")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_PRODUCT_TYPE_LOCAL,
		members=@Persistent(name="productTypeLocal")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_TARIFF_USER_SET,
		members=@Persistent(name="tariffUserSet")),
	@FetchGroup(
		name=ProductType.FETCH_GROUP_END_CUSTOMER_TRANSFER_POLICY,
		members=@Persistent(name="endCustomerReplicationPolicy")),
	@FetchGroup(
		fetchGroups={"default"},
		name=ProductType.FETCH_GROUP_THIS_PRODUCT_TYPE,
		members={@Persistent(name="deliveryConfiguration"), @Persistent(name="extendedProductType"), @Persistent(name="fieldMetaDataMap"), @Persistent(name="innerPriceConfig"), @Persistent(name="managedProductTypeGroup"), @Persistent(name="name"), @Persistent(name="owner"), @Persistent(name="packagePriceConfig"), @Persistent(name="productTypeGroups"), @Persistent(name="productTypeLocal")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOrderEditor",
		members={@Persistent(name="name"), @Persistent(name="vendor")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOfferEditor",
		members={@Persistent(name="name"), @Persistent(name="vendor")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members={@Persistent(name="name"), @Persistent(name="vendor")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members={@Persistent(name="name"), @Persistent(name="vendor")})
})
@Discriminator(strategy=DiscriminatorStrategy.CLASS_NAME)
@Queries({
	@javax.jdo.annotations.Query(
			name="getProductTypesOfProductTypeGroup",
			value="SELECT WHERE this.productTypeGroups.containsValue(productTypeGroup) && productTypeGroup.organisationID == paramOrganisationID && productTypeGroup.productTypeGroupID == paramProductTypeGroupID VARIABLES ProductTypeGroup productTypeGroup PARAMETERS String paramOrganisationID, String paramProductTypeGroupID import java.lang.String; import org.nightlabs.jfire.store.ProductTypeGroup"
	),
	@javax.jdo.annotations.Query(
			name="getChildProductTypes_topLevel",
			value="SELECT WHERE this.extendedProductType == null",
			language="javax.jdo.query.JDOQL"
	),
//TODO DataNucleus WORKAROUND: Sorting in JDOQL causes objects to be skipped (not found) when they do not have a name!
//	@javax.jdo.annotations.Query(
//			name="getChildProductTypes_hasParent",
//			value="SELECT " +
//					"WHERE" +
//					"  this.extendedProductType.organisationID == parentProductTypeOrganisationID && " +
//					"  this.extendedProductType.productTypeID == parentProductTypeProductTypeID " +
//					"PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID, String languageID " +
//					"import java.lang.String " +
//					"ORDER BY this.name.names.get(languageID) ASCENDING ",
//			language="javax.jdo.query.JDOQL"
//	),
	@javax.jdo.annotations.Query(
			name="getChildProductTypes_hasParent",
			value="SELECT " +
					"WHERE" +
					"  this.extendedProductType.organisationID == parentProductTypeOrganisationID && " +
					"  this.extendedProductType.productTypeID == parentProductTypeProductTypeID " +
					"PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID " +
					"import java.lang.String",
			language="javax.jdo.query.JDOQL"
	),
	@javax.jdo.annotations.Query(
			name="getChildProductTypeCount_topLevel",
			value="SELECT count(this) WHERE this.extendedProductType == null"
	),
	@javax.jdo.annotations.Query(
			name="getChildProductTypeCount_hasParent",
			value="SELECT count(this) WHERE this.extendedProductType.organisationID == parentProductTypeOrganisationID && this.extendedProductType.productTypeID == parentProductTypeProductTypeID PARAMETERS String parentProductTypeOrganisationID, String parentProductTypeProductTypeID import java.lang.String"
	),
	@javax.jdo.annotations.Query(
			name="getProductTypesNestingThis",
			value="SELECT WHERE this.productTypeLocal.nestedProductTypeLocals.containsValue(nestedProductTypeLocal) && nestedProductTypeLocal.innerProductTypeLocal.productType == :productType VARIABLES org.nightlabs.jfire.store.NestedProductTypeLocal nestedProductTypeLocal"
	)
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public abstract class ProductType
implements
		Inheritable,
		InheritanceCallbacks,
		StoreCallback,
		Serializable,
		DetachCallback,
		AttachCallback,
		IndirectlySecuredObject
{
	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger.getLogger(ProductType.class);

	/**
	 * This class defines constants for the field names of implementation of
	 * {@link Inheritable}, to avoid the use of "hardcoded" Strings for retrieving
	 * {@link FieldMetaData} or {@link FieldInheriter}.
	 * In the future the JFire project will probably autogenerate this class,
	 * but until then you should implement it manually.
	 */
	public static final class FieldName
	{
		public static final String closeTimestamp = "closeTimestamp";
		public static final String confirmed = "confirmed";
		public static final String deliveryConfiguration = "deliveryConfiguration";
		public static final String extendedProductType = "extendedProductType";
		public static final String extendedProductTypeID = "extendedProductTypeID";
		public static final String extendedProductTypeID_detached = "extendedProductTypeID_detached";
		public static final String fieldMetaDataMap = "fieldMetaDataMap";
		public static final String inheritanceNature = "inheritanceNature";
		public static final String innerPriceConfig = "innerPriceConfig";
		public static final String managedProductTypeGroup = "managedProductTypeGroup";
		public static final String name = "name";
		public static final String organisationID = "organisationID";
		public static final String owner = "owner";
		public static final String packageNature = "packageNature";
		public static final String packagePriceConfig = "packagePriceConfig";
		public static final String productAvailable = "productAvailable";
		public static final String productTypeGroups = "productTypeGroups";
		public static final String productTypeID = "productTypeID";
		public static final String productTypeLocal = "productTypeLocal";
		public static final String published = "published";
		public static final String saleable = "saleable";
		public static final String vendor = "vendor";
		public static final String tariffUserSet = "tariffUserSet";
		public static final String endCustomerReplicationPolicy = "endCustomerReplicationPolicy";
	};

	/**
	 * This fetch-group (named "ProductType.name") must be used in all descendents of this class
	 * to ensure that the method {@link #getName()} can be used.
	 */
	public static final String FETCH_GROUP_NAME = "ProductType.name";

	/**
	 * This is a virtual fetch-group which is processed in the <code>DetachCallback</code> method <code>jdoPostDetach</code>.
	 */
	public static final String FETCH_GROUP_EXTENDED_PRODUCT_TYPE_ID = "ProductType.extendedProductTypeID";
	public static final String FETCH_GROUP_EXTENDED_PRODUCT_TYPE = "ProductType.extendedProductType";
	public static final String FETCH_GROUP_EXTENDED_PRODUCT_TYPE_2 = "ProductType.extendedProductType[2]";
	public static final String FETCH_GROUP_EXTENDED_PRODUCT_TYPE_NO_LIMIT = "ProductType.extendedProductType[-1]";
	/**
	 * Needed for inheritance.
	 */
	public static final String FETCH_GROUP_FIELD_METADATA_MAP = "ProductType.fieldMetaDataMap";
	public static final String FETCH_GROUP_INNER_PRICE_CONFIG = "ProductType.innerPriceConfig";
	public static final String FETCH_GROUP_PACKAGE_PRICE_CONFIG = "ProductType.packagePriceConfig";
	public static final String FETCH_GROUP_OWNER = "ProductType.owner";
	public static final String FETCH_GROUP_VENDOR = "ProductType.vendor";
	public static final String FETCH_GROUP_DELIVERY_CONFIGURATION = "ProductType.deliveryConfiguration";
	public static final String FETCH_GROUP_PRODUCT_TYPE_GROUPS = "ProductType.productTypeGroups";
	public static final String FETCH_GROUP_MANAGED_PRODUCT_TYPE_GROUP = "ProductType.managedProductTypeGroup";
//	public static final String FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE = "ProductType.localAccountantDelegate";
//	public static final String FETCH_GROUP_LOCAL_STOREKEEPER_DELEGATE = "ProductType.localStorekeeperDelegate";
	public static final String FETCH_GROUP_PRODUCT_TYPE_LOCAL = "ProductType.productTypeLocal";
	public static final String FETCH_GROUP_TARIFF_USER_SET = "ProductType.tariffUserSet";
	public static final String FETCH_GROUP_END_CUSTOMER_TRANSFER_POLICY = "ProductType.endCustomerReplicationPolicy";

	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon!
	 */
	@Deprecated
	public static final String FETCH_GROUP_THIS_PRODUCT_TYPE = "ProductType.this";

	public static final String CANNOT_MAKE_SALEABLE_REASON_NOT_PUBLISHED = "ProductType.cannotMakeSaleable.notPublished";
	public static final String CANNOT_MAKE_SALEABLE_REASON_NOT_CONFIRMED = "ProductType.cannotMakeSaleable.notConfirmed";
	public static final String CANNOT_MAKE_SALEABLE_REASON_ALREADY_CLOSED = "ProductType.cannotMakeSaleable.alreadyClosed";
	public static final String CANNOT_MAKE_SALEABLE_REASON_NO_PRICECONFIG = "ProductType.cannotMakeSaleable.noPriceConfig";
//	public static final String CANNOT_MAKE_SALEABLE_REASON_NO_ACCOUNTANT_DELEGATE = "ProductType.cannotMakeSaleable.noAccountantDelegate";

	public static final String CANNOT_CONFIRM_REASON_IMMUTABLE = "ProductType.cannotConfirm.immutable";
	public static final String CANNOT_CONFIRM_REASON_INHERITANCE_BRANCH = "ProductType.cannotConfirm.inheritanceBranch"; // only leafs can be confirmed

	public static final String CANNOT_PUBLISH_REASON_IMMUTABLE = "ProductType.cannotPublish.immutable";
	public static final String CANNOT_PUBLISH_REASON_PARENT_NOT_PUBLISHED = "ProductType.cannotPublish.parentNotPublished";

	public static Collection<ProductType> getProductTypesNestingOneOfTheseProductTypeIDs(PersistenceManager pm, Set<ProductTypeID> productTypeIDs)
	{
		Set<ProductType> productTypes = NLJDOHelper.getObjectIDSet(productTypeIDs);
		return getProductTypesNestingOneOfTheseProductTypes(pm, productTypes);
	}
	public static Collection<ProductType> getProductTypesNestingOneOfTheseProductTypes(PersistenceManager pm, Set<ProductType> productTypes)
	{
		// TODO optimize this to use one single query instead of iteration. Marco.
		Set<ProductType> res = null;
		for (ProductType productType : productTypes) {
			Collection<ProductType> c = getProductTypesNestingThis(pm, productType);
			if (res == null)
				res = new HashSet<ProductType>(c);
			else
				res.addAll(c);
		}
		return res;
	}

	public static Collection<ProductType> getProductTypesNestingThis(PersistenceManager pm, ProductTypeID productTypeID)
	{
		pm.getExtent(ProductType.class);
		ProductType productType = (ProductType) pm.getObjectById(productTypeID);
		return getProductTypesNestingThis(pm, productType);
	}

	public static Collection<ProductType> getProductTypesNestingThis(PersistenceManager pm, ProductType productType)
	{
		Query q = pm.newNamedQuery(ProductType.class, "getProductTypesNestingThis");
		return CollectionUtil.castCollection((Collection<?>)q.execute(productType));
	}

	/**
	 * @param pm The <code>PersistenceManager</code> that should be used to access the datastore.
	 * @param parentProductTypeID The <code>ProductType</code> of which to find all children or <code>null</code> to find all top-level-<code>ProductType</code>s.
	 * @return Returns instances of <code>ProductType</code>.
	 */
	public static Collection<? extends ProductType> getChildProductTypes(PersistenceManager pm, ProductTypeID parentProductTypeID)
	{
		final String languageID = NLLocale.getDefault().getLanguage();

		Collection<ProductType> result;
		if (parentProductTypeID == null) {
			Query q = pm.newNamedQuery(ProductType.class, "getChildProductTypes_topLevel");
			result = CollectionUtil.castCollection((Collection<?>)q.execute());
		}
		else {
			Query q = pm.newNamedQuery(ProductType.class, "getChildProductTypes_hasParent");

			result = CollectionUtil.castCollection(
					(Collection<?>) q.execute(
							parentProductTypeID.organisationID, parentProductTypeID.productTypeID
					)
			);
		}

		// TODO DataNucleus WORKAROUND: Sorting in JDOQL causes objects to be skipped (not found) when they do not have a name!
		// sort
		long loadStart = System.currentTimeMillis();
		result = new ArrayList<ProductType>(result);
		long loadDuration = System.currentTimeMillis() - loadStart;

		long sortStart = System.currentTimeMillis();
		Collections.sort((List<ProductType>)result, new Comparator<ProductType>() {
			@Override
			public int compare(ProductType o1, ProductType o2) {
				return o1.getName().getText(languageID).compareTo(o2.getName().getText(languageID));
			}
		});
		if (logger.isDebugEnabled())
			logger.debug("getChildProductTypes: Loading " + result.size() + " product types took " + loadDuration + " msec and sorting with languageID=" + languageID + " took " + (System.currentTimeMillis() - sortStart) + " msec.");

		return result;
	}

	public static long getChildProductTypeCount(PersistenceManager pm, ProductTypeID parentProductTypeID)
	{
		if (parentProductTypeID == null) {
			Query q = pm.newNamedQuery(ProductType.class, "getChildProductTypeCount_topLevel");
			return ((Long)q.execute()).longValue();
		}

		Query q = pm.newNamedQuery(ProductType.class, "getChildProductTypeCount_hasParent");
		return ((Long) q.execute(parentProductTypeID.organisationID, parentProductTypeID.productTypeID)).longValue();
	}

	@SuppressWarnings("unchecked")
	public static Collection<? extends ProductType> getRootProductTypes(PersistenceManager pm, Class<? extends ProductType> productTypeClass, boolean subclasses)
	{
		Query q = pm.newQuery(pm.getExtent(productTypeClass, subclasses));
		q.setFilter(FieldName.extendedProductType + " == null");
		return (Collection<? extends ProductType>) q.execute();
	}

	public static long getRootProductTypeCount(PersistenceManager pm, Class<? extends ProductType> productTypeClass, boolean subclasses)
	{
		Query q = pm.newQuery(pm.getExtent(productTypeClass, subclasses));
		q.setResult("count(this)");
		q.setFilter(FieldName.extendedProductType + " == null");
		return ((Long) q.execute()).longValue();
	}

	public static String createProductTypeID()
	{
		return ObjectIDUtil.longObjectIDFieldToString(IDGenerator.nextID(ProductType.class));
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String productTypeID;

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 * @jdo.column length="201"
//	 */
//	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * // TODO JPOX WORKAROUND should be null-value="exception", but causes problems in replication
	 */
@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity owner;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * // TODO JPOX WORKAROUND should be null-value="exception", but causes problems in replication
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity vendor;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductType extendedProductType;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private ProductTypeID extendedProductTypeID = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean extendedProductTypeID_detached = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PriceConfig innerPriceConfig = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private PriceConfig packagePriceConfig = null;

// These fields are in ProductTypeLocal now.
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private LocalAccountantDelegate localAccountantDelegate = null;
//
//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private LocalStorekeeperDelegate localStorekeeperDelegate = null;

	/**
	 * key: String productTypeGroupPK (see {@link ProductTypeGroup#getPrimaryKey()})<br/>
	 * value: {@link ProductTypeGroup} productTypeGroup
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="String"
	 *		value-type="ProductTypeGroup"
	 *		table="JFireTrade_ProductType_productTypeGroups"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
@Join
@Persistent(
	nullValue=NullValue.EXCEPTION,
	table="JFireTrade_ProductType_productTypeGroups",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<String, ProductTypeGroup> productTypeGroups;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="managedByProductType"
	 */
	@Persistent(
		mappedBy="managedByProductType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductTypeGroup managedProductTypeGroup = null;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="productType"
	 */
	@Persistent(
		mappedBy="productType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductTypeLocal productTypeLocal;

//	public static final String NATURE_TYPE = "type";
//	public static final String NATURE_INSTANCE = "instance";
//
//	/**
//	 * Products use inheritence. This means, there is a tree of which Types form the branches
//	 * and instances are the leafs. It is not possible to extend a ProductInstance.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private String nature;

	// IMHO The base class of ProductType should not have a multilingual name. It should not have
	// a name at all, because this causes heavy load and most products (e.g. seats) do not need
	// it. They need a row and a column which is sth. different.
//	/**
//	 * key: String languageID<br/>
//	 * value: String productName
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="java.lang.String"
//	 *		dependent="true"
//	 *
//	 * @jdo.join
//	 *
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 100"
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="value-length" value="max 255"
//	 */
//	protected Map names = new HashMap();

//	/**
//	 * key: String propertyName<br/>
//	 * value: String propertyValue
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="map"
//	 *		key-type="java.lang.String"
//	 *		value-type="java.lang.String"
//	 *
//	 * @jdo.join
//	 *
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="clear-on-delete" value="true"
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 200"
//	 * @jdo.map-vendor-extension vendor-name="jpox" key="value-length" value="max 255"
//	 */
//	protected Map properties = new HashMap();

//	/**
//	 * @see #isPackage()
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 * @jdo.column name="package"
//	 */
//	private boolean _package;

//	/**
//	 * If the <code>ProductType</code> has this <code>flavour</code>, it cannot create
//	 * <code>Product</code> instances and can therefore not be sold. Instead it is
//	 * used as node (<b>not</b> leaf) within the <code>ProductType</code>
//	 * data-inheritance-tree to categorize <code>ProductType</code>s.
//	 */
//	public static String FLAVOUR_CATEGORY = "category";
//
//	/**
//	 * If the <code>ProductType</code> has this <code>flavour</code>, it cannot be
//	 * extended anymore - means it is a leaf in the tree. Being a package,
//	 * it can contain other <code>ProductType</code>s (see {@link #getNestedProductTypes()})
//	 * and it can virtually package itself. Even though being a package, it
//	 * can still be packaged within another <code>ProductType</code>, but
//	 * it is not able to make its price dependent on its siblings within
//	 * this other package. A package always has a definite price, which is the sum
//	 * of all its nested <code>ProductType</code>s.
//	 */
//	public static String FLAVOUR_PACKAGE = "package";
//
//	/**
//	 * If the <code>ProductType</code> has this <code>flavour</code>, it cannot be
//	 * extended anymore - means it is a leaf in the tree. Being inner - means
//	 * not a package itself, it is not able to package other <code>ProductType</code>s.
//	 * The price is dependent on its siblings within the package. Hence, the price
//	 * of an inner <code>ProductType</code> varies from package to package.
//	 */
//	public static String FLAVOUR_INNER = "inner";
//
//	/**
//	 * @see #FLAVOUR_CATEGORY
//	 * @see #FLAVOUR_PACKAGE
//	 * @see #FLAVOUR_INNER
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private String flavour;

	/**
	 * If the <code>ProductType</code> has this <code>inheritanceNature</code>, it cannot
	 * create <code>Product</code> instances and can therefore not be sold. Instead
	 * it is used as node within the <code>ProductType</code>
	 * data-inheritance-tree to categorize <code>ProductType</code>s.
	 */
	public static final byte INHERITANCE_NATURE_BRANCH = 11;

	/**
	 * If the <code>ProductType</code> has this <code>inheritanceNature</code>, it cannot be
	 * extended anymore - means it is a leaf in the tree. Only with this
	 * <code>inheritanceNature</code>, it can instantiate {@link Product} instances.
	 */
	public static final byte INHERITANCE_NATURE_LEAF = 12;

	private static final String[] inheritanceNatureStrings = new String[] { "INHERITANCE_NATURE_BRANCH", "INHERITANCE_NATURE_LEAF" };

	/**
	 * @jdo.field persistence-modifier="persistent"
	 *
	 * @see #INHERITANCE_NATURE_BRANCH
	 * @see #INHERITANCE_NATURE_LEAF
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private byte inheritanceNature;

	/**
	 * If the <code>ProductType</code> has this <code>packageNature</code>, it is an inner part
	 * of a package. This means, it is NOT able to package itself virtually - but it
	 * is able to nest other <code>ProductType</code>s (see {@link #getNestedProductTypeLocals()})!
	 * <p>
	 * The price is dependent on its siblings within the package. Hence, the price
	 * of an inner <code>ProductType</code> varies from package to package.
	 * </p>
	 * <p>
	 * If a ProductType of this nature does package other product types, their price
	 * can only be dependent on the price of this ProductType. This means, a special
	 * price config is required for this multi-level-nesting
	 * (the {@link org.nightlabs.jfire.accounting.gridpriceconfig.GridPriceConfig}
	 * cannot be used for the second [and 3rd...] level of nesting).
	 * </p>
	 */
	public static final byte PACKAGE_NATURE_INNER = 21;

	/**
	 * If the <code>ProductType</code> has this <code>packageNature</code>, it can not only
	 * contain other <code>ProductType</code>s (see {@link #getNestedProductTypeLocals()}),
	 * but even virtually package itself. Additionally, it
	 * can still be packaged within another <code>ProductType</code>, but
	 * it is not able to make its price dependent on its siblings within
	 * this other package. A package always has a definite price, which is the sum
	 * of all its nested <code>ProductType</code>s.
	 */
	public static final byte PACKAGE_NATURE_OUTER = 22;

	private static final String[] packageNatureStrings = new String[] { "PACKAGE_NATURE_INNER", "PACKAGE_NATURE_OUTER" };

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private byte packageNature;

	/**
	 * @see #isPublished()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean published = false;

	/**
	 * @see #isConfirmed()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean confirmed = false;

	/**
	 * @see #isSaleable()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean saleable = false;

	/**
	 * @see #isClosed()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Date closeTimestamp = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private DeliveryConfiguration deliveryConfiguration = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private TariffUserSet tariffUserSet = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private EndCustomerReplicationPolicy endCustomerReplicationPolicy = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductType() {}

	/**
	 * @param organisationID This must not be null. This reflects the owner organisation which is issuing this <code>ProductType</code>.
	 * @param productTypeID The local ID within the namespace of <code>organisationID</code>.
	 * @param extendedProductType The "parent" <code>ProductType</code> in the
	 *		data-inheritance-tree. If this is <code>null</code> it will be considered being
	 *		a root node.
	 * @!param owner The owner of this ProductType because an Organisation might
	 *		sell <code>ProductType</code>s for another <code>LegalEntity</code> which has
	 *		no own Organisation in the system. This can be <code>null</code>, if
	 *		<code>extendedProductType</code> is defined. In this case, it will be set to
	 *		<code>extendedProductType.</code>{@link #getOwner()}.
	 * @param flavour What is this <code>ProductType</code> used for? Must be one of {@link #FLAVOUR_CATEGORY}, {@link #FLAVOUR_PACKAGE}, {@link #FLAVOUR_INNER}
	 */
	public ProductType(
			String organisationID, String productTypeID,
			ProductType extendedProductType,
//			LegalEntity owner,
			byte inheritanceNature,
			byte packageNature)
	{
		Organisation.assertValidOrganisationID(organisationID);
		ObjectIDUtil.assertValidIDString(productTypeID, "productTypeID");

		if (INHERITANCE_NATURE_BRANCH != inheritanceNature &&
				INHERITANCE_NATURE_LEAF != inheritanceNature)
			throw new IllegalArgumentException("inheritanceNature \""+inheritanceNature+"\" is invalid! Must be INHERITANCE_NATURE_BRANCH or INHERITANCE_NATURE_LEAF!");

		if (PACKAGE_NATURE_INNER != packageNature &&
				PACKAGE_NATURE_OUTER != packageNature)
			throw new IllegalArgumentException("packageNature \""+packageNature+"\" is invalid! Must be PACKAGE_NATURE_INNER or PACKAGE_NATURE_OUTER!");

		this.organisationID = organisationID;
		this.productTypeID = productTypeID;
//		this.primaryKey = getPrimaryKey(organisationID, productTypeID);
//		setNature(nature);
		setExtendedProductType(extendedProductType);

//		if (owner == null) {
//			if (extendedProductType == null)
//				throw new IllegalStateException("owner and extendedProductType are both null! At least one of them must be defined!");
//
//			this.owner = extendedProductType.getOwner();
//		}
//		else
//			this.owner = owner;

		if (extendedProductType != null) {
			this.deliveryConfiguration = extendedProductType.getDeliveryConfiguration();
			this.owner = extendedProductType.getOwner();
			this.vendor = extendedProductType.getVendor();
		}

		this.inheritanceNature = inheritanceNature;
		this.packageNature = packageNature;

		productTypeGroups = new HashMap<String, ProductTypeGroup>();
		fieldMetaDataMap = new HashMap<String, ProductTypeFieldMetaData>();

		this.name = new ProductTypeName(this);
		FieldMetaData fmd = getFieldMetaData(FieldName.name);
		if (fmd != null)
			fmd.setValueInherited(false);
	}

	/**
	 * Get the JDO object id.
	 * @return the JDO object id.
	 */
	public ProductTypeID getObjectId()
	{
		return (ProductTypeID)JDOHelper.getObjectId(this);
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}

//	/**
//	 * @param productTypeID The productTypeID to set.
//	 */
//	protected void setProductID(String productTypeID)
//	{
//		BaseObjectID.assertValidIDString(productTypeID, "productTypeID");
//		this.productID = productTypeID;
//	}
	public String getProductTypeID()
	{
		return productTypeID;
	}

	public static String getPrimaryKey(String organisationID, String productTypeID)
	{
		if (organisationID == null)
			throw new IllegalArgumentException("organisationID must not be null!");
		if (productTypeID == null)
			throw new IllegalArgumentException("productTypeID must not be null!");
		return organisationID + '/' + productTypeID;
	}

	public static ProductTypeID primaryKeyToProductTypeID(String primaryKey) {
		String[] parts = primaryKey.split("/");
		if (parts.length != 2)
			throw new IllegalArgumentException("The given productTypePK "+primaryKey+" is illegal (more than one /)");
		return ProductTypeID.create(parts[0], parts[1]);
	}

	public String getPrimaryKey()
	{
		return getPrimaryKey(organisationID, productTypeID);
//		return primaryKey;
	}

	/**
	 * An organisation might sell products for someone else who does not have a JFire
	 * organisation connected to the network. Hence, this organisation would create
	 * and manage the <code>ProductType</code>, but still is not the owner.
	 * The real owner can therefore be assigned as property of this
	 * <code>ProductType</code>.
	 *
	 * @return the owner. If not managed for someone else, this is the {@link LocalOrganisation#getOrganisation() local organisation}
	 * @see #getVendor()
	 */
	public LegalEntity getOwner()
	{
		return owner;
	}
	/**
	 * @param owner The owner to set.
	 * @see #getOwner()
	 */
	public void setOwner(LegalEntity owner)
	{
		this.owner = owner;
	}

	/**
	 * Get the vendor of this <code>ProductType</code>.
	 * <p>
	 * If an organisation sells products for someone else, there are two possibilities: Either it first buys them
	 * from the other legal entity and then sells it wrapped in its own reseller-ProductType or it directly sells
	 * it without buying it first. In the latter case the owner (see {@link #getOwner()}) would still be the other
	 * legal entity, but the vendor is the local organisation. In this case the local organisation acts as a trustee
	 * and represents the real vendor. This might be desired to reduce the load on the JFire system or if the
	 * act of buying the products from the other legal-entity should not be managed within JFire for other reasons.
	 * </p>
	 * <p>
	 * In other words:
	 * <ul>
	 * <li>
	 * If the <code>vendor</code> is not the local organisation (usually the <code>vendor</code> will be
	 * the same as the <code>owner</code> in this case), the local organisation must first
	 * buy the products (i.e. create a purchase-<code>Offer</code> and go through the complete purchase-workflow), before
	 * it can sell them wrapped within an own reseller-product (in the reseller-product-type, the local organisation
	 * will be the vendor).
	 * </li>
	 * <li>
	 * If the <code>vendor</code> is the local organisation, the local organisation does directly sell the
	 * products to customers. It is impossible in this case, to create a purchase-<code>Offer</code> with the local
	 * organisation being the customer for the products.
	 * </li>
	 * </ul>
	 * </p>
	 *
	 * @return the vendor.
	 * @see #getOwner()
	 */
	public LegalEntity getVendor()
	{
		return vendor;
	}
	/**
	 * @param vendor The vendor to set.
	 * @see #getVendor()
	 */
	public void setVendor(LegalEntity vendor)
	{
		if (Util.equals(this.vendor, vendor))
			return;

		if (this.confirmed)
			throw new IllegalStateException("Cannot assign a vendor to a confirmed ProductType! " + this);

		if (vendor instanceof OrganisationLegalEntity) {
			OrganisationLegalEntity orgaVendor = (OrganisationLegalEntity) vendor;
			if (!this.organisationID.equals(orgaVendor.getOrganisationID()))
				throw new IllegalStateException("Cannot assign a foreign OrganisationLegalEntity as vendor of a ProductType! " + this + " " + vendor);
		}

		this.vendor = vendor;
	}

	/**
	 * @return Returns the extendedProductType, which means the parent of this
	 *		<code>ProductType</code> in the data inheritance tree. This may be
	 *		<code>null</code> if this <code>ProductType</code> is a root element in the
	 *		inheritance tree.
	 */
	public ProductType getExtendedProductType()
	{
		return extendedProductType;
	}

	public ProductTypeID getExtendedProductTypeID()
	{
		if (!extendedProductTypeID_detached) {
			extendedProductTypeID = extendedProductType == null ? null : extendedProductType.getObjectId();
			extendedProductTypeID_detached = true;
		}
		return extendedProductTypeID;
	}

	/**
	 * @param extendedProductType The extendedProductType to set. This method should not be called
	 *		from outside. Currently it is not yet possible to change the structure of the inheritance
	 *		tree (it's fixed after creation)!!!
	 * @see #getExtendedProductType()
	 *
	 * @deprecated Currently it is not yet possible to change the structure of the inheritance
	 *		tree!!! You must not call this method! Even after we implemented
	 *		the possibility to restructure the product type tree, you will use another API
	 *		(via the {@link Store})!!!
	 */
	@Deprecated
	protected void setExtendedProductType(ProductType extendedProductType)
	{
		if (extendedProductType != null) {
			if (!(extendedProductType.getClass().isInstance(this)))
				throw new IllegalArgumentException("This ProductType (\""+this.getPrimaryKey()+"\") is of type " + this.getClass().getName() + " and cannot extend the ProductType \""+extendedProductType.getPrimaryKey()+"\" which is of type "+extendedProductType.getClass().getName() + "! Correct Java-inheritance is essential!");

			if (INHERITANCE_NATURE_LEAF == extendedProductType.getInheritanceNature())
				throw new IllegalArgumentException("The extended ProductType (\""+extendedProductType.getPrimaryKey()+"\") has the inheritanceNature \""+extendedProductType.getInheritanceNatureString()+"\" and therefore cannot be extended!");
		}

		this.extendedProductType = extendedProductType;
		this.extendedProductTypeID_detached = false;
		this.extendedProductTypeID = null;
	}

	/**
	 * @return Returns the innerPriceConfig.
	 */
	public IInnerPriceConfig getInnerPriceConfig()
	{
		return (IInnerPriceConfig)innerPriceConfig;
	}
	/**
	 * @param innerPriceConfig The innerPriceConfig to set.
	 */
	public void setInnerPriceConfig(IInnerPriceConfig priceConfig)
	{
		if (priceConfig != null && !(priceConfig instanceof PriceConfig))
			throw new ClassCastException("Every IPriceConfig must be extended from PriceConfig, because this is the JDO base object!");

		this.innerPriceConfig = (PriceConfig) priceConfig;
	}

	/**
	 * @return Returns the packagePriceConfig.
	 */
	public IPackagePriceConfig getPackagePriceConfig()
	{
		return (IPackagePriceConfig)packagePriceConfig;
	}
	/**
	 * @param packagePriceConfig The packagePriceConfig to set.
	 */
	public void setPackagePriceConfig(IPackagePriceConfig priceConfig)
	{
		if (priceConfig != null && !(priceConfig instanceof PriceConfig))
			throw new ClassCastException("Every IPriceConfig must be extended from PriceConfig, because this is the JDO base object!");

		this.packagePriceConfig = (PriceConfig)priceConfig;
	}

// these methods are in ProductTypeLocal, now
//	/**
//	 * The LocalAccountantDelegate is in charge of booking money to different
//	 * account for the productType it is assigned to and its packaged types
//	 * when an invoice is booked.
//	 */
//	public LocalAccountantDelegate getLocalAccountantDelegate() {
//		return localAccountantDelegate;
//	}
//
//	/**
//	 * Set the LocalAccountantDelegate.
//	 */
//	public void setLocalAccountantDelegate(LocalAccountantDelegate localAccountantDelegate) {
//		this.localAccountantDelegate = localAccountantDelegate;
//	}
//
//	/**
//	 * @return Returns the localStorekeeperDelegate.
//	 */
//	public LocalStorekeeperDelegate getLocalStorekeeperDelegate()
//	{
//		return localStorekeeperDelegate;
//	}
//
//	/**
//	 * @param localStorekeeperDelegate The localStorekeeperDelegate to set.
//	 */
//	public void setLocalStorekeeperDelegate(
//			LocalStorekeeperDelegate localStorekeeperDelegate)
//	{
//		this.localStorekeeperDelegate = localStorekeeperDelegate;
//	}

	// *********************************
	// /// *** begin inheritance *** ///

	private static Set<String> nonInheritableFields = new HashSet<String>();

	/**
	 * value: ProductTypeFieldMetaData fieldMetaData
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="ProductTypeFieldMetaData"
	 *		dependent-value="true"
	 *		mapped-by="productType"
	 *
	 * @jdo.key mapped-by="fieldName"
	 */
	@Persistent(
		mappedBy="productType",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	@Key(mappedBy="fieldName")
	@Value(dependent="true")
	protected Map<String, ProductTypeFieldMetaData> fieldMetaDataMap;

	/**
	 * <p>
	 * Note, that the following fields cause a <code>null</code> result (i.e. no inheritance).
	 * If you want to inherit them anyway, you can extend this method and handle them before
	 * calling the super method.
	 * </p>
	 * <p>
	 * Fields:
	 * <ul>
	 *   <li>jdo* (you must never inherit an internal jdo field!)</li>
	 *   <li>organisationID (you must never inherit a primary key field!)</li>
	 *   <li>productTypeID (primary key field)</li>
	 *   <li>primaryKey (concatenated primary key - must never be inherited as well)</li>
	 *   <li>closed</li>
	 *   <li>confirmed</li>
	 *   <li>fieldMetaDataMap</li>
	 *   <li>extendedProductType</li>
	 *   <li>inheritanceNature</li>
	 *   <li>managedProductTypeGroup</li>
	 *   <li>packageNature</li>
	 *   <li>productAvailable</li>
	 *   <li>productTypeGroups</li>
	 *   <li>published</li>
	 *   <li>saleable</li>
	 *   <li>selfForVirtualSelfPackaging (transient and a must-not-inherit, too)</li>
	 *   <li>packagePriceConfig (usually stores results of price calculations, as normally the innerPriceConfig is formula based - in rare cases this might different, but you can override the behaviour if you want to)</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note, that the above list might not be up-to-date. Check the source code if you have problems!
	 * And please update the javadoc, if you encounter differences!
	 * </p>
	 */
	@Override
	public final org.nightlabs.inheritance.FieldMetaData getFieldMetaData(String fieldName)
	{
		return getFieldMetaData(fieldName, true);
	}

	public org.nightlabs.inheritance.FieldMetaData getFieldMetaData(String fieldName, boolean createMissingMetaData)
	{
		if (isClosed()) {
			StaticFieldMetaData fmd = new StaticFieldMetaData(fieldName);
			fmd.setValueInherited(false);
			fmd.setWritable(false);
			return fmd;
		}

		if (fieldName.startsWith("jdo"))
			return null;

		if (fieldName.startsWith("tmpInherit"))
			return null;

// TODO the below checks for localAccountantDelegate and localStorekeeperDelegate should be removed after a few months transition time.
		if (ProductTypeLocal.FieldName.localAccountantDelegate.equals(fieldName))
			throw new IllegalArgumentException("The field 'localAccountantDelegate' has been moved to ProductTypeLocal!");
		if (ProductTypeLocal.FieldName.localStorekeeperDelegate.equals(fieldName))
			throw new IllegalArgumentException("The field 'localStorekeeperDelegate' has been moved to ProductTypeLocal!");
// END to do

// TODO the below checks for nestedProductTypes should be removed after a few months transition time.
		if ("nestedProductTypes".equals(fieldName))
			throw new IllegalArgumentException("The field 'nestedProductTypes' has been moved to ProductTypeLocal and renamed to 'nestedProductTypeLocals'!");
		if ("nestedProductTypeLocals".equals(fieldName))
			throw new IllegalArgumentException("The field 'nestedProductTypeLocals' is in class ProductTypeLocal!");
// END to do

		if (FieldName.productTypeLocal.equals(fieldName))
			return new StaticFieldMetaData(FieldName.productTypeLocal);

		synchronized (nonInheritableFields) {
			if (nonInheritableFields.isEmpty()) {
				// PK fields
				nonInheritableFields.add(FieldName.organisationID);
				nonInheritableFields.add(FieldName.productTypeID);

				// other fields
				nonInheritableFields.add(FieldName.closeTimestamp);
				nonInheritableFields.add(FieldName.confirmed);
				nonInheritableFields.add(FieldName.fieldMetaDataMap);
				nonInheritableFields.add(FieldName.extendedProductType);
				nonInheritableFields.add(FieldName.inheritanceNature);
				nonInheritableFields.add(FieldName.managedProductTypeGroup);
				nonInheritableFields.add(FieldName.packageNature);
				nonInheritableFields.add(FieldName.productAvailable);
				nonInheritableFields.add(FieldName.productTypeGroups);
//				nonInheritableFields.add("productTypeLocal");
				nonInheritableFields.add(FieldName.published);
				nonInheritableFields.add(FieldName.saleable);
				nonInheritableFields.add(FieldName.extendedProductTypeID);
				nonInheritableFields.add(FieldName.extendedProductTypeID_detached);
//				nonInheritableFields.add("packagePriceConfig");
			}

			if (nonInheritableFields.contains(fieldName))
				return null;
		}

		ProductTypeFieldMetaData fmd = fieldMetaDataMap.get(fieldName);
		if (fmd == null && createMissingMetaData) {
			fmd = new ProductTypeFieldMetaData(this, fieldName);

			fieldMetaDataMap.put(fieldName, fmd);
		} // if (fmd == null) {

		// ensure that the vendor is not changed anymore after a ProductType has been confirmed.
		if (confirmed && FieldName.vendor.equals(fieldName) && fmd != null && fmd.isValueInherited())
			fmd.setValueInherited(false);

		return fmd;
	}

	@Override
	public FieldInheriter getFieldInheriter(String fieldName)
	{
		if (FieldName.productTypeLocal.equals(fieldName))
			return new JDOInheritableFieldInheriter();

		if (FieldName.name.equals(fieldName))
			return new JDOInheritableFieldInheriter();

		return new JDOSimpleFieldInheriter();
	}

	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient PriceConfigID tmpInherit_innerPriceConfigID = null;

	@Override
	public void preInherit(Inheritable mother, Inheritable child)
	{
		// JDOInheritanceManager uses PersistenceManager.retrieve, which works often,
		// but not reliably => we need this! :-( Marco.

		// access all non-simple fields in order to ensure, they're loaded by JDO
		if (productTypeLocal == null);
		if (deliveryConfiguration == null);
		if (innerPriceConfig == null);
		if (packagePriceConfig == null);
		if (owner == null);
		if (vendor == null);
		name.getI18nMap();
		if (endCustomerReplicationPolicy == null);
		if (tariffUserSet == null);

		tmpInherit_innerPriceConfigID = (PriceConfigID) JDOHelper.getObjectId(getInnerPriceConfig());
	}

	protected PriceConfigID getTmpInherit_innerPriceConfigID() {
		return tmpInherit_innerPriceConfigID;
	}

	@Override
	public void postInherit(Inheritable mother, Inheritable child) {
		// nothing to do
	}

	/**
	 * The default implementation of this method returns <code>true</code> if it is {@link #PACKAGE_NATURE_OUTER}
	 * and the {@link #packagePriceConfig} is assigned (i.e. not <code>null</code>).
	 *
	 * @see #calculatePrices()
	 * @see #postInherit(Inheritable, Inheritable)
	 */
	protected boolean isResponsibleForPriceCalculation()
	{
		return ProductType.PACKAGE_NATURE_OUTER == this.getPackageNature() && this.getPackagePriceConfig() != null;
	}

	/**
	 * This method is called, when this ProductType should recalculate its prices and {@link #isResponsibleForPriceCalculation()}
	 * returned <code>true</code>. This happens for example during inheritance application, if the packaging
	 * (i.e. {@link #nestedProductTypeLocals}) changed or an inner price config changed.
	 * <p>
	 * This method is never be called when the object is detached. It assumes that it is currently persistent and thus
	 * has a {@link PersistenceManager} assigned (which can be obtained via {@link #getPersistenceManager()}).
	 * </p>
	 */
	protected abstract void calculatePrices();

	/**
	 * This method causes all settings of this instance to be passed to all children
	 * (recursively).
	 * <p>
	 * This method should never be called when the object is detached. It assumes that it is currently persistent and thus
	 * has a {@link PersistenceManager} assigned (which can be obtained via {@link #getPersistenceManager()}).
	 * </p>
	 * <p>
	 * Alternative wordings: bequeath, devise, entail<br/>
	 * Note: it is the opposite of inherit (=receive), because calling this
	 * method causes this object to GIVE its data to all the children. Before
	 * passing the inheritance to the children, it updates of course its own data from
	 * the extendedProductType (if existing).
	 * </p>
	 */
	public void applyInheritance()
	{
		PersistenceManager pm = getPersistenceManager();

		// https://www.jfire.org/modules/bugs/view.php?id=1192
		// Since we delegate to the ProductTypeActionHandler and implementors
		// might modify the fetch-plan there, it is a good idea to
		// backup and restore the fetch-plan.
		FetchPlanBackup fetchPlanBackup = NLJDOHelper.backupFetchPlan(pm.getFetchPlan());

		InheritanceManager im = new JDOInheritanceManager();
		ProductType extendedProductType = getExtendedProductType();
		if (extendedProductType != null)
			im.inheritAllFields(extendedProductType, this);

		applyInheritance(pm, im);

		ProductTypeActionHandler.getProductTypeActionHandler(pm, this.getClass()).postApplyInheritance(this);

		NLJDOHelper.restoreFetchPlan(pm.getFetchPlan(), fetchPlanBackup);
	}
	private void applyInheritance(PersistenceManager pm, InheritanceManager im)
	{
		Collection<? extends ProductType> children = getChildProductTypes(pm, (ProductTypeID) JDOHelper.getObjectId(this));
		for (Iterator<? extends ProductType> it = children.iterator(); it.hasNext();) {
			ProductType child = it.next();

			// before inheriting, we need to check, whether the following things change:
			// - a priceConfig
			// - the NestedProductTypes
			// if one of these will change due to the inheritance, we must update (recalculate) the price configs

			im.inheritAllFields(this, child);
			child.applyInheritance(pm, im);
		}
	}

	// /// *** end inheritance *** ///
	// *********************************

//	/**
//	 * Whether or not this implementation is able to create <code>Product</code>s automatically,
//	 * when needed. This is e.g. the case if a packaged fee should be included in a product.
//	 *
//	 * @see #createProduct()
//	 */
//	public abstract boolean isProductFactory();
//
//	/**
//	 * If you decided to make this <code>ProductType</code> a product factory
//	 * by returning <code>true</code> in <code>isProductFactory()</code>, then you must
//	 * implement this method - otherwise just throw an <code>UnsupportedOperationException</code>.
//	 *
//	 * In your implementation of this method, you must create an instance
//	 * of <code>Product</code>, assign a primary key to it and return this instance.
//	 *
//	 * @return Returns a newly created instance of <code>Product</code>.
//	 *
//	 * @see #isProductFactory()
//	 */
//	public abstract Product createProduct();

//	/**
//	 * This member is automatically set to the result of <code>isProductProvider()</code>.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean productProvider;

//	/**
//	 * Whether or not this <code>ProductType</code> is able to find/create available <code>Product</code>s.
//	 * A <code>ProductType</code> can only be packaged within another one having <code>productProvider==true</code>, if this
//	 * is <code>true</code>, too. If the <code>Product</code>s of the package are created manually, the
//	 * result of this method does not matter.
//	 *
//	 * @see #findProducts(User, NestedProductTypeLocal, ProductLocator)
//	 * @!see #provideAvailableProduct()
//	 */
//	public abstract boolean isProductProvider();

//	/**
//	 * If <code>isProductProvider()</code> is true, this method might be called and must
//	 * either return <code>null</code> if there is no <code>Product</code> available or an instance
//	 * that can be sold.
//	 * <p>
//	 * The implementation of <code>ProductType</code> might choose to create a new instance
//	 * of <code>Product</code> when this method is called.
//	 * <p>
//	 * This method is only called while this <code>ProductType</code> is persistent.
//	 * <p>
//	 * ???? !!!! ????
//	 * Marco: IMHO this method should allocate the product immediately, to avoid someone
//	 * else from "stealing" it. Therefore, we probably need the parameter <code>allocationKey</code>.
//	 * ???? !!!! ????
//	 *
//	 * @return Returns <code>null</code> or a <code>Product</code> that can be sold.
//	 */
//	public abstract Product provideAvailableProduct();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean productAvailable = true;

	public boolean isProductAvailable() {
		return productAvailable;
	}

//	/**
//	 * Your implementation of this method should check whether it can
//	 * return a <code>Product</code> in <code>provideAvailableProduct()</code>
//	 * or whether there's none available anymore.
//	 * <p>
//	 * If there is no <code>Product</code> available anymore, the <code>ProductType</code>
//	 * will be listed as not available (or even filtered out) in sale lists.
//	 * <p>
//	 * This method is only called while this <code>ProductType</code> is persistent.
//	 */
//	protected abstract boolean _checkProductAvailability();
//
//	public boolean checkProductAvailability()
//	{
//		this.productAvailable = _checkProductAvailability();
//		return productAvailable;
//	}

	/**
	 * @return Returns the inheritance nature: {@link #INHERITANCE_NATURE_BRANCH} or {@link #INHERITANCE_NATURE_LEAF}
	 */
	public byte getInheritanceNature()
	{
		return inheritanceNature;
	}
	public String getInheritanceNatureString()
	{
		return inheritanceNatureStrings[inheritanceNature - INHERITANCE_NATURE_BRANCH];
	}
	public String getInheritanceNatureString(byte inheritanceNature)
	{
		return inheritanceNatureStrings[inheritanceNature - INHERITANCE_NATURE_BRANCH];
	}

	/**
	 * @return Returns the package nature: {@link #PACKAGE_NATURE_INNER} or {@link #PACKAGE_NATURE_OUTER}
	 */
	public byte getPackageNature()
	{
		return packageNature;
	}
	public String getPackageNatureString()
	{
		return packageNatureStrings[packageNature - PACKAGE_NATURE_INNER];
	}
	public static String getPackageNatureString(byte packageNature)
	{
		return packageNatureStrings[packageNature - PACKAGE_NATURE_INNER];
	}

	/**
	 * @return Returns <code>true</code>, if <code>packageNature == </code>{@link #PACKAGE_NATURE_OUTER}.
	 */
	public boolean isPackageOuter()
	{
		return PACKAGE_NATURE_OUTER == packageNature;
	}
	/**
	 * @return Returns <code>true</code>, if <code>packageNature == </code>{@link #PACKAGE_NATURE_INNER}.
	 */
	public boolean isPackageInner()
	{
		return PACKAGE_NATURE_INNER == packageNature;
	}

	/**
	 * @return Returns <code>true</code> if <code>inheritanceNature == </code>{@link #INHERITANCE_NATURE_BRANCH}.
	 */
	public boolean isInheritanceBranch()
	{
		return INHERITANCE_NATURE_BRANCH == inheritanceNature;
	}

	/**
	 * @return Returns <code>true</code> if <code>inheritanceNature == </code>{@link #INHERITANCE_NATURE_LEAF}.
	 */
	public boolean isInheritanceLeaf()
	{
		return INHERITANCE_NATURE_LEAF == inheritanceNature;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * If this is true, it means the world knows about this ProductType.
	 * Once published, this flag is immutable, because as soon as sth.
	 * is known to the whole wide world, noone can erase knowledge
	 * anymore :-)
	 *
	 * @return Whether this ProductType is published
	 */
	public boolean isPublished() {
		return published;
	}

	/**
	 * Sets whether this ProductType is published.  This flag is immutable after once set to true.
	 *
	 * @param published Weather the ProductType should be published.
	 * @throws CannotPublishProductTypeException If the ProductType cannot be published.
	 */
	protected void setPublished(boolean published) throws CannotPublishProductTypeException {
		getPersistenceManager();

		if (published == false && this.published == true)
			throw new CannotPublishProductTypeException(CANNOT_PUBLISH_REASON_IMMUTABLE, "The published flag of a ProductType is immutable after once set to true");

		this.published = published;
	}

	/**
	 * Checks if the ProductType can be confirmed.
	 * This implementation checks whether the parent ProductType (the productType this extends) is published.
	 * <p>
	 * This method might be overridden, however the super implementation should be invoked then.
	 * </p>
	 *
	 * @throws CannotPublishProductTypeException If the ProductType cannot be published.
	 */
	protected void checkCanPublish() throws CannotPublishProductTypeException {
		if (extendedProductType != null && !extendedProductType.isPublished())
			throw new CannotPublishProductTypeException(CANNOT_PUBLISH_REASON_PARENT_NOT_PUBLISHED, "The ProductType \""+getPrimaryKey()+"\" cannot be published, because it extends \""+extendedProductType.getPrimaryKey()+"\", which is not yet published! Publish the parent first!");
	}

	/**
	 * If this true, it is possible that instances of the corresponding
	 * Product have been sold. When set to true, this flag is immutable for
	 * a ProductType.
	 *
	 * @return
	 */
	public boolean isConfirmed()
	{
		return confirmed;
	}

	/**
	 * Confirmed ProductTypes can be sold. Once it is confirmed, the flag cannot be cleared
	 * again, because ProductTypes might already be sold and certain settings are frozen
	 * when confirmed. You can {@link #setSaleable(boolean)} however.
	 *
	 * @param confirmed
	 * @throws CannotConfirmProductTypeException If the ProductType cannot be confirmed.
	 */
	protected void setConfirmed(boolean confirmed) throws CannotConfirmProductTypeException
	{
		getPersistenceManager();

		if (this.confirmed == confirmed)
			return;

		if (confirmed == false && this.confirmed == true)
			throw new CannotConfirmProductTypeException(CANNOT_CONFIRM_REASON_IMMUTABLE, "The confirmed flag of a ProductType is immutable after once set to true");

		if (confirmed)
			checkCanConfirm();

		this.confirmed = confirmed;
	}

	/**
	 * Checks if the ProductType can be confirmed. This implementation does nothing.
	 * <p>
	 * This method might be overridden, however the super implementation should be invoked then.
	 * </p>
	 *
	 * @throws CannotConfirmProductTypeException If the ProductType cannot be confirmed.
	 */
	protected void checkCanConfirm() throws CannotConfirmProductTypeException {
		if (!isInheritanceLeaf())
			throw new CannotConfirmProductTypeException(CANNOT_CONFIRM_REASON_INHERITANCE_BRANCH, "The confirmed flag of a ProductType can only be set for leafs in the inheritance tree");
	}

	/**
	 * A ProductType can only be sold when saleable and published are true.
	 * This method only returns the saleable flag, which can be used for filtering.
	 *
	 * @return Weather this ProductType is saleable
	 */
	public boolean isSaleable() {
		return saleable;
	}

	/**
	 * Sets if this ProductType can be sold. If a product-type is saleable but not published,
	 * it cannot be sold by resellers, though - only within the local organisation.
	 * This is used as a filter flag.
	 *
	 * @param saleable Wheather this ProductType is saleable
	 * @throws CannotMakeProductTypeSaleableException If the ProductType cannot be made saleable.
	 */
	protected void setSaleable(boolean saleable) throws CannotMakeProductTypeSaleableException
	{
		getPersistenceManager();

		if (this.saleable == saleable)
			return;

		if (saleable)
			checkCanMakeSaleable();

		this.saleable = saleable;
	}

	/**
	 * Checks if the productType can be made saleable (saleable can be set to <code>true</code>).
	 * <p>
	 * This implementation checks if the productType:
	 * <ul>
	 *   <li>is confirmed</li>
	 *   <li>is not closed</li>
	 *   <li>has a {@link LocalAccountantDelegate} assigned</li>
	 *   <li>has a correct {@link IPriceConfig} assigned</li>
	 * </ul>
	 * </p>
	 * <p>
	 * This method might be overridden, however the super implementation should be invoked then.
	 * </p>
	 * @throws CannotMakeProductTypeSaleableException If the ProductType cannot be made saleable.
	 */
	protected void checkCanMakeSaleable() throws CannotMakeProductTypeSaleableException {
		if (!confirmed)
			throw new CannotMakeProductTypeSaleableException(CANNOT_MAKE_SALEABLE_REASON_NOT_CONFIRMED, "Cannot make ProductType \"" + getPrimaryKey() + "\" saleable, because it is not yet confirmed!");

		if (isClosed())
			throw new CannotMakeProductTypeSaleableException(CANNOT_MAKE_SALEABLE_REASON_ALREADY_CLOSED, "Cannot make ProductType \"" + getPrimaryKey() + "\" saleable, because it is already closed!");

//		if (localAccountantDelegate == null)
//			throw new CannotMakeProductTypeSaleableException(CANNOT_MAKE_SALEABLE_REASON_NO_ACCOUNTANT_DELEGATE, "Cannot make ProductType \"" + getPrimaryKey() + "\" saleable, because it has no LocalAccountantDelegate assigned! instance=" + System.identityHashCode(this));

		if (getPriceConfigInPackage(this.getPrimaryKey()) == null)
			throw new CannotMakeProductTypeSaleableException(CANNOT_MAKE_SALEABLE_REASON_NO_PRICECONFIG, "Cannot make ProductType \"" + getPrimaryKey() + "\" saleable, because it has no PriceConfig assigned!");
	}


	/**
	 * A ProductType can be closed. This is irreversile.
	 *
	 * @return Returns whether this ProductType is closed.
	 */
	public boolean isClosed()
	{
		return closeTimestamp != null;
	}

	public Date getCloseTimestamp() {
		return closeTimestamp;
	}

	protected void setClosed(boolean closed)
	{
		getPersistenceManager();

		if (saleable && closed)
			throw new IllegalStateException("Cannot close ProductType \"" + getPrimaryKey() + "\", because it is still saleable!");

		if (isClosed() == closed)
			return;

		if (isClosed() && !closed)
			throw new IllegalArgumentException("The closed flag of a ProductType is immutable after once set to true");

		this.closeTimestamp = new Date();
	}

	/**
	 * @return Returns the deliveryConfiguration.
	 */
	public DeliveryConfiguration getDeliveryConfiguration()
	{
		return deliveryConfiguration;
	}

	/**
	 * @param deliveryConfiguration The deliveryConfiguration to set.
	 */
	public void setDeliveryConfiguration(DeliveryConfiguration deliveryConfiguration)
	{
		this.deliveryConfiguration = deliveryConfiguration;
	}

	public void addProductTypeGroup(ProductTypeGroup productTypeGroup)
	{
		if (managedProductTypeGroup != null)
			throw new IllegalStateException("managedProductTypeGroup != null ==> Cannot manage productTypeGroups manually as they are managed by the system!");

		_addProductTypeGroup(productTypeGroup);
	}

	protected void _addProductTypeGroup(ProductTypeGroup productTypeGroup)
	{
		String pk = productTypeGroup.getPrimaryKey();
		if (productTypeGroups.containsKey(pk))
			return;

		productTypeGroups.put(pk, productTypeGroup);
	}

	public void removeProductTypeGroup(ProductTypeGroupID productTypeGroupID)
	{
		removeProductTypeGroup(productTypeGroupID.organisationID, productTypeGroupID.productTypeGroupID);
	}
	public void removeProductTypeGroup(ProductTypeGroup productTypeGroup)
	{
		if (managedProductTypeGroup != null)
			throw new IllegalStateException("managedProductTypeGroup != null ==> Cannot manage productTypeGroups manually as they are managed by the system!");

		productTypeGroups.remove(productTypeGroup.getPrimaryKey());
	}
	public void removeProductTypeGroup(String organisationID, String productTypeGroupID)
	{
		if (managedProductTypeGroup != null)
			throw new IllegalStateException("managedProductTypeGroup != null ==> Cannot manage productTypeGroups manually as they are managed by the system!");

		productTypeGroups.remove(ProductType.getPrimaryKey(organisationID, productTypeID));
	}

	public Collection<ProductTypeGroup> getProductTypeGroups()
	{
		return productTypeGroups.values();
	}

	/**
	 * This method returns the inner price config, if <code>packageProductTypePK</code>
	 * equals this pk (means we use <code>this</code> as virtual inner product type) or
	 * if this is not a package.
	 * If it is a package (and <code>packageProductTypePK</code> does not point back
	 * to <code>this</code>), the package price config will be returned.
	 * <p>
	 * The sense in this behaviour is to automatically return the correct price config
	 * depending on the situation.
	 *
	 * @param packageProductTypePK The primary key of the <code>ProductType</code>
	 * (obtained via {@link ProductType#getPrimaryKey()) in which this
	 * <code>ProductType</code> is packaged. This parameter can be <code>null</code>.
	 * @return Returns either the result of {@link #getInnerPriceConfig()} or
	 * of {@link #getPackagePriceConfig()}.
	 */
	public IPriceConfig getPriceConfigInPackage(String packageProductTypePK)
	{
		if (this.isPackageOuter() && !this.getPrimaryKey().equals(packageProductTypePK))
			return this.getPackagePriceConfig();
		else
			return this.getInnerPriceConfig();
	}

//public abstract I18nText getName();

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="productType" dependent="true"
	 */
@Persistent(
	dependent="true",
	mappedBy="productType",
	persistenceModifier=PersistenceModifier.PERSISTENT)
	private ProductTypeName name;
	/**
	 * return the multilanguage capable name of the productType
	 *
	 * @return the {@link I18nText} which stores the name of the productType
	 * in a  {@link ProductTypeName}
	 */
	public ProductTypeName getName() {
		return name;
	}

	/**
	 * @return Returns either <code>null</code> or the ProductTypeGroup
	 *		which is managed by this ProductType.
	 */
	public ProductTypeGroup getManagedProductTypeGroup()
	{
		return managedProductTypeGroup;
	}

	/**
	 *  A {@link ProductTypeGroup} can be automatically managed by the system. This means:
	 * <ul>
	 *	<li>
	 *		If a ProductType with flavour {@link ProductType#FLAVOUR_CATEGORY} is created,
	 *		a ProductTypeGroup will automatically be created as well and assigned.
	 *	</li>
	 *	<li>
	 *		If a ProductType with another flavour is created, it will inherit the ProductTypeGroup.
	 *	</li>
	 *	<li>
	 *		If the name of the ProductType for which the managed ProductTypeGroup has been created,
	 *		is changed, the ProductTypeGroup's name will be updated.
	 *	</li>
	 * </ul>
	 * Note, that you have to implement all the above requirements yourself in your
	 * concrete implementation of JFireTrade.
	 *
	 * @param managedProductTypeGroup The ProductTypeGroup that shall be managed by this ProductType.
	 *
	 * @see ProductTypeGroup#setManagedByProductType(ProductType)
	 */
	public void setManagedProductTypeGroup(ProductTypeGroup manageProductTypeGroup)
	{
		if (manageProductTypeGroup == null) {
			if (this.managedProductTypeGroup != null)
				productTypeGroups.clear();

			this.managedProductTypeGroup = null;
		}
		else {
			if (this.managedProductTypeGroup == null && !productTypeGroups.isEmpty())
				throw new IllegalStateException("There are already ProductTypeGroups manually assigned to the ProductType \"" + getPrimaryKey() + "\"! Cannot assign managedProductTypeGroup=\"" + manageProductTypeGroup.getPrimaryKey() + "\"");

			productTypeGroups.clear();
			this.managedProductTypeGroup = manageProductTypeGroup;
			manageProductTypeGroup._setManagedByProductType(this);
			_addProductTypeGroup(manageProductTypeGroup);
		}
	}

	/**
	 * This method is called on creation of a ProductType that should inherit the managed ProductTypeGroup.
	 * Later inheritance stuff is managed by the inheritance framework.
	 * <p>
	 * TODO !!!Warning: inheritance related parts of the api are likely to change!!!
	 * </p>
	 */
	public void initInheritedManagedProductTypeGroup()
	{
		ProductType parent = getExtendedProductType();
		productTypeGroups.clear();
		if (parent.getManagedProductTypeGroup() != null)
			_addProductTypeGroup(parent.getManagedProductTypeGroup());
	}

	public ProductTypeLocal getProductTypeLocal()
	{
		return productTypeLocal;
	}
	@Override
	public SecuredObject getSecuredObject() {
		return productTypeLocal;
	}
	protected void setProductTypeLocal(ProductTypeLocal productTypeLocal)
	{
		this.productTypeLocal = productTypeLocal;
	}

	protected ProductTypeLocal createProductTypeLocal(User user)
	{
		return new ProductTypeLocal(user, this); // self-registering
	}

	/**
	 * Get the {@link TariffUserSet} that holds the information which user is allowed to access which
	 * {@link Tariff}s (i.e. sell products with this tariff). If this is not assigned, the current policy
	 * is that all {@code Tariff}s (those that are used in the assigned price configuration) are available.
	 * This is likely to change later! The future policy will probably fallback to a default-{@code TariffUserSet}.
	 *
	 * @return the assigned {@link TariffUserSet} or <code>null</code>, if there is none assigned.
	 * @see #setTariffUserSet(TariffUserSet)
	 */
	public TariffUserSet getTariffUserSet() {
		return tariffUserSet;
	}
	/**
	 * Assign a {@link TariffUserSet} or <code>null</code>. Note, that this property is subject to data-inheritance
	 * and you might need to disable inheritance (otherwise your changes might be overwritten).
	 *
	 * @param tariffUserSet the new {@link TariffUserSet} or <code>null</code>.
	 * @see #getTariffUserSet()
	 * @see FieldName#tariffUserSet
	 */
	public void setTariffUserSet(TariffUserSet tariffUserSet) {
		if (Util.equals(this.tariffUserSet, tariffUserSet))
			return;

		this.tariffUserSet = tariffUserSet;
	}

	public EndCustomerReplicationPolicy getEndCustomerReplicationPolicy() {
		return endCustomerReplicationPolicy;
	}
	public void setEndCustomerReplicationPolicy(EndCustomerReplicationPolicy endCustomerReplicationPolicy) {
		this.endCustomerReplicationPolicy = endCustomerReplicationPolicy;
	}

	@Override
	public void jdoPreStore() { }

	@Override
	public void jdoPreDetach() { }

	@Override
	public void jdoPreAttach() {
		PersistenceManager pm = NLJDOHelper.getThreadPersistenceManager();
		ProductType persistentProductType;
		try {
			persistentProductType = (ProductType) pm.getObjectById(JDOHelper.getObjectId(this));
		} catch (JDOObjectNotFoundException x) {
			// seems we are in replication (from one datastore to another) => silently return
			return;
		}

		try {
			this.saleable = persistentProductType.saleable;
		} catch (JDODetachedFieldAccessException x) { } // ignore - no need to restore a non-detached field
		try {
			this.confirmed = persistentProductType.confirmed;
		} catch (JDODetachedFieldAccessException x) { } // ignore - no need to restore a non-detached field
		try {
			this.closeTimestamp = persistentProductType.closeTimestamp;
		} catch (JDODetachedFieldAccessException x) { } // ignore - no need to restore a non-detached field
		try {
			this.published = persistentProductType.published;
		} catch (JDODetachedFieldAccessException x) { } // ignore - no need to restore a non-detached field
	}

	@Override
	public void jdoPostAttach(Object attached) { }

	@SuppressWarnings("unchecked")
	public void jdoPostDetach(Object o)
	{
		ProductType attached = (ProductType) o;
		ProductType detached = this;
		PersistenceManager pm = attached.getPersistenceManager();
		Set fetchGroups = pm.getFetchPlan().getGroups();
		if (fetchGroups.contains(FETCH_GROUP_EXTENDED_PRODUCT_TYPE_ID)) {
			detached.extendedProductTypeID = attached.getExtendedProductTypeID();
			detached.extendedProductTypeID_detached = true;
		}
	}

	@Override
	public int hashCode()
	{
		return (31 * Util.hashCode(organisationID)) ^ Util.hashCode(productTypeID);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (this.getClass() != obj.getClass()) return false;

		ProductType o = (ProductType) obj;
		return (
				Util.equals(this.organisationID, o.organisationID) &&
				Util.equals(this.productTypeID, o.productTypeID)
		);
	}

	@Override
	public String toString() {
		return this.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + organisationID + ',' + productTypeID + ']';
	}
}
