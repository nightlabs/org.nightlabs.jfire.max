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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.InheritanceCallbacks;
import org.nightlabs.inheritance.InheritanceManager;
import org.nightlabs.inheritance.MapFieldInheriter;
import org.nightlabs.jdo.inheritance.JDOSimpleFieldInheriter;
import org.nightlabs.jfire.accounting.book.LocalAccountantDelegate;
import org.nightlabs.jfire.accounting.priceconfig.IInnerPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.organisation.LocalOrganisation;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.book.LocalStorekeeperDelegate;
import org.nightlabs.jfire.store.deliver.DeliveryConfiguration;
import org.nightlabs.jfire.store.id.ProductTypeGroupID;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * <p>
 * This is the base class for all product-types in the ERP. You subclass it in order to implement
 * your own specialized saleable goods.
 * </p>
 * <p>
 * A <code>ProductType</code> has the possibility to create {@link Product}s (on the fly if necessary).
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
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeID"
 *
 * @jdo.fetch-group name="ProductType.extendedProductType" fields="extendedProductType"
 * @jdo.fetch-group name="ProductType.fieldMetaDataMap" fields="fieldMetaDataMap"
 * @jdo.fetch-group name="ProductType.innerPriceConfig" fields="innerPriceConfig"
 * @jdo.fetch-group name="ProductType.nestedProductTypes" fields="nestedProductTypes"
 * @jdo.fetch-group name="ProductType.packagePriceConfig" fields="packagePriceConfig"
 * @jdo.fetch-group name="ProductType.owner" fields="owner"
 * @jdo.fetch-group name="ProductType.deliveryConfiguration" fields="deliveryConfiguration"
 * @jdo.fetch-group name="ProductType.productTypeGroups" fields="productTypeGroups"
 * @jdo.fetch-group name="ProductType.managedProductTypeGroup" fields="managedProductTypeGroup"
 * @jdo.fetch-group name="ProductType.localAccountantDelegate" fields="localAccountantDelegate"
 * @jdo.fetch-group name="ProductType.localStorekeeperDelegate" fields="localStorekeeperDelegate"
 * @jdo.fetch-group name="ProductType.this" fetch-groups="default" fields="nonInheritableFields, extendedProductType, fieldMetaDataMap, innerPriceConfig, nestedProductTypes, packagePriceConfig, localAccountantDelegate, localStorekeeperDelegate"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default"
 *
 * @jdo.query name="getProductTypesOfProductTypeGroup" query="
 *		SELECT
 *		WHERE
 *			this.productTypeGroups.containsValue(productTypeGroup) &&
 *			productTypeGroup.organisationID == paramOrganisationID &&
 *			productTypeGroup.productTypeGroupID == paramProductTypeGroupID
 *		VARIABLES ProductTypeGroup productTypeGroup
 *		PARAMETERS String paramOrganisationID, String paramProductTypeGroupID
 *		IMPORTS import java.lang.String; import org.nightlabs.jfire.store.ProductTypeGroup"
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
 *		  IMPORTS import java.lang.String"
 **/
public abstract class ProductType
implements
		Inheritable,
		InheritanceCallbacks,
		Serializable
{
	/**
	 * This fetch-group (named "ProductType.name") must be used in all descendents of this class
	 * to ensure that the method {@link #getName()} can be used. 
	 */
	public static final String FETCH_GROUP_NAME = "ProductType.name";

	public static final String FETCH_GROUP_EXTENDED_PRODUCT_TYPE = "ProductType.extendedProductType";
	/**
	 * Needed for inheritance.
	 */
	public static final String FETCH_GROUP_FIELD_METADATA_MAP = "ProductType.fieldMetaDataMap";
	public static final String FETCH_GROUP_INNER_PRICE_CONFIG = "ProductType.innerPriceConfig";
	public static final String FETCH_GROUP_NESTED_PRODUCT_TYPES = "ProductType.nestedProductTypes";
	public static final String FETCH_GROUP_PACKAGE_PRICE_CONFIG = "ProductType.packagePriceConfig";
	public static final String FETCH_GROUP_OWNER = "ProductType.owner";
	public static final String FETCH_GROUP_DELIVERY_CONFIGURATION = "ProductType.deliveryConfiguration";
	public static final String FETCH_GROUP_PRODUCT_TYPE_GROUPS = "ProductType.productTypeGroups";
	public static final String FETCH_GROUP_MANAGED_PRODUCT_TYPE_GROUP = "ProductType.managedProductTypeGroup";
	public static final String FETCH_GROUP_LOCAL_ACCOUNTANT_DELEGATE = "ProductType.localAccountantDelegate";
	public static final String FETCH_GROUP_LOCAL_STOREKEEPER_DELEGATE = "ProductType.localStorekeeperDelegate";
	public static final String FETCH_GROUP_THIS_PRODUCT_TYPE = "ProductType.this";

	/**
	 * @param pm The <tt>PersistenceManager</tt> that should be used to access the datastore.
	 * @param parentProductTypeID The <tt>ProductType</tt> of which to find all children or <tt>null</tt> to find all top-level-<tt>ProductType</tt>s.
	 * @return Returns instances of <tt>ProductType</tt>.
	 */
	public static Collection getChildProductTypes(PersistenceManager pm, ProductTypeID parentProductTypeID)
	{
		if (parentProductTypeID == null) {
			Query q = pm.newNamedQuery(ProductType.class, "getChildProductTypes_topLevel");
			return (Collection)q.execute();
		}

		Query q = pm.newNamedQuery(ProductType.class, "getChildProductTypes_hasParent");
		return (Collection) q.execute(
			parentProductTypeID.organisationID, parentProductTypeID.productTypeID);
	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String productTypeID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="201"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity owner;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType extendedProductType;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig innerPriceConfig = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private PriceConfig packagePriceConfig = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalAccountantDelegate localAccountantDelegate = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LocalStorekeeperDelegate localStorekeeperDelegate = null;

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
	 *
	 * @jdo.join
	 */
	private Map<String, ProductTypeGroup> productTypeGroups;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="managedByProductType"
	 */
	private ProductTypeGroup managedProductTypeGroup = null;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
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

	/**
	 * key: String primaryKey(organisationID + '/' + productTypeID)<br/>
	 * value: {@link NestedProductType} nestedProductType
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="NestedProductType"
	 *		mapped-by="packageProductType"
	 *
	 * @jdo.key mapped-by="innerProductTypePrimaryKey"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-field" value="innerProductTypePrimaryKey"
	 * @!jdo.join
	 */
	private Map<String, NestedProductType> nestedProductTypes;

//	/**
//	 * @see #isPackage()
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 * @jdo.column name="package"
//	 */
//	private boolean _package;

//	/**
//	 * If the <tt>ProductType</tt> has this <tt>flavour</tt>, it cannot create
//	 * <tt>Product</tt> instances and can therefore not be sold. Instead it is
//	 * used as node (<b>not</b> leaf) within the <tt>ProductType</tt>
//	 * data-inheritance-tree to categorize <tt>ProductType</tt>s.
//	 */
//	public static String FLAVOUR_CATEGORY = "category";
//
//	/**
//	 * If the <tt>ProductType</tt> has this <tt>flavour</tt>, it cannot be
//	 * extended anymore - means it is a leaf in the tree. Being a package,
//	 * it can contain other <tt>ProductType<tt>s (see {@link #getNestedProductTypes()})
//	 * and it can virtually package itself. Even though being a package, it
//	 * can still be packaged within another <tt>ProductType</tt>, but
//	 * it is not able to make its price dependent on its siblings within
//	 * this other package. A package always has a definite price, which is the sum
//	 * of all its nested <tt>ProductType</tt>s.
//	 */
//	public static String FLAVOUR_PACKAGE = "package";
//
//	/**
//	 * If the <tt>ProductType</tt> has this <tt>flavour</tt>, it cannot be
//	 * extended anymore - means it is a leaf in the tree. Being inner - means
//	 * not a package itself, it is not able to package other <tt>ProductType</tt>s.
//	 * The price is dependent on its siblings within the package. Hence, the price
//	 * of an inner <tt>ProductType</tt> varies from package to package.
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
	 * If the <tt>ProductType</tt> has this <tt>inheritanceNature</tt>, it cannot
	 * create <tt>Product</tt> instances and can therefore not be sold. Instead
	 * it is used as node within the <tt>ProductType</tt>
	 * data-inheritance-tree to categorize <tt>ProductType</tt>s.
	 */
	public static final byte INHERITANCE_NATURE_BRANCH = 11;

	/**
	 * If the <tt>ProductType</tt> has this <tt>inheritanceNature</tt>, it cannot be
	 * extended anymore - means it is a leaf in the tree. Only in this
	 * <tt>inheritanceNature</tt>, it can instantiate {@link Product} instances.
	 */
	public static final byte INHERITANCE_NATURE_LEAF = 12;

	private static final String[] inheritanceNatureStrings = new String[] { "INHERITANCE_NATURE_BRANCH", "INHERITANCE_NATURE_LEAF" };

	/**
	 * @jdo.field persistence-modifier="persistent"
	 *
	 * @see #INHERITANCE_NATURE_BRANCH
	 * @see #INHERITANCE_NATURE_LEAF
	 */
	private byte inheritanceNature;

	/**
	 * If the <tt>ProductType</tt> has this <tt>packageNature</tt>, it is an inner part
	 * of a package. This means, it is NOT able to package itself virtually - but it
	 * is able to nest other <tt>ProductType</tt>s (see {@link #getNestedProductTypes()})!
	 * <p>
	 * The price is dependent on its siblings within the package. Hence, the price
	 * of an inner <tt>ProductType</tt> varies from package to package.
	 * </p>
	 * <p>
	 * If a ProductType of this nature does package other product types, their price
	 * can only be dependent on the price of this ProductType. This means, a special
	 * price config is required for this multi-level-nesting
	 * (the {@link org.nightlabs.jfire.accounting.tariffpriceconfig.TariffPriceConfig}
	 * cannot be used for the second [and 3rd...] level of nesting).
	 * </p>
	 */
	public static final byte PACKAGE_NATURE_INNER = 21;

	/**
	 * If the <tt>ProductType</tt> has this <tt>packageNature</tt>, it can not only
	 * contain other <tt>ProductType<tt>s (see {@link #getNestedProductTypes()}),
	 * but even virtually package itself. Additionally, it
	 * can still be packaged within another <tt>ProductType</tt>, but
	 * it is not able to make its price dependent on its siblings within
	 * this other package. A package always has a definite price, which is the sum
	 * of all its nested <tt>ProductType</tt>s.
	 */
	public static final byte PACKAGE_NATURE_OUTER = 22;

	private static final String[] packageNatureStrings = new String[] { "PACKAGE_NATURE_INNER", "PACKAGE_NATURE_OUTER" };

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private byte packageNature;

	/**
	 * @see #isPublished()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean published = false;

	/**
	 * @see #isConfirmed()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean confirmed = false;

	/**
	 * @see #isSaleable()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean saleable = false;

	/**
	 * @see #isClosed()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean closed = false;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DeliveryConfiguration deliveryConfiguration = null;

	protected static String EMPTYSTRING = "";

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductType() {} 

	/**
	 * @param organisationID This must not be null. This reflects the owner organisation which is issuing this <code>ProductType</code>.
	 * @param productTypeID The local ID within the namespace of <code>organisationID</code>.
	 * @param extendedProductType The "parent" <tt>ProductType</tt> in the
	 *		data-inheritance-tree. If this is <tt>null</tt> it will be considered being
	 *		a root node.
	 * @param owner The owner of this ProductType because an Organisation might
	 *		sell <tt>ProductType</tt>s for another <tt>LegalEntity</tt> which has
	 *		no own Organisation in the system. This can be <tt>null</tt>, if
	 *		<tt>extendedProductType</tt> is defined. In this case, it will be set to
	 *		<tt>extendedProductType.</tt>{@link #getOwner()}.
	 * @param flavour What is this <tt>ProductType</tt> used for? Must be one of {@link #FLAVOUR_CATEGORY}, {@link #FLAVOUR_PACKAGE}, {@link #FLAVOUR_INNER}
	 */
	public ProductType(
			String organisationID, String productTypeID,
			ProductType extendedProductType,
			LegalEntity owner,
			byte inheritanceNature,
			byte packageNature)
	{
		if (organisationID == null)
			throw new NullPointerException("organisationID");

		if (productTypeID == null)
			throw new IllegalArgumentException("productTypeID == null!");

		if (INHERITANCE_NATURE_BRANCH != inheritanceNature &&
				INHERITANCE_NATURE_LEAF != inheritanceNature)
			throw new IllegalArgumentException("inheritanceNature \""+inheritanceNature+"\" is invalid! Must be INHERITANCE_NATURE_BRANCH or INHERITANCE_NATURE_LEAF!");

		if (PACKAGE_NATURE_INNER != packageNature &&
				PACKAGE_NATURE_OUTER != packageNature)
			throw new IllegalArgumentException("packageNature \""+packageNature+"\" is invalid! Must be PACKAGE_NATURE_INNER or PACKAGE_NATURE_OUTER!");

		this.organisationID = organisationID;
		this.productTypeID = productTypeID;
		this.primaryKey = getPrimaryKey(organisationID, productTypeID);
//		setNature(nature);
		setExtendedProductType(extendedProductType);

		if (owner == null) {
			if (extendedProductType == null)
				throw new IllegalStateException("owner and extendedProductType are both null! At least one of them must be defined!");

			this.owner = extendedProductType.getOwner();
		}
		else
			this.owner = owner;

		if (extendedProductType != null)
			this.deliveryConfiguration = extendedProductType.getDeliveryConfiguration();

		this.inheritanceNature = inheritanceNature;
		this.packageNature = packageNature;

		productTypeGroups = new HashMap<String, ProductTypeGroup>();
		fieldMetaDataMap = new HashMap<String, ProductTypeFieldMetaData>();
		nestedProductTypes = new HashMap<String, NestedProductType>();
//	initFieldMetaData();
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
			throw new NullPointerException("organisationID must not be null!");
		if (productTypeID == null)
			throw new NullPointerException("productTypeID must not be null!");
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
		return primaryKey;
	}

	/**
	 * An organisation might sell products for someone else who does not have a JFire
	 * organisation connected to the network. Hence, this organisation would create
	 * and manage the <code>ProductType</code>, but still is not the owner.
	 * The real owner can therefore be assigned as property of this
	 * <code>ProductType</code>. 
	 *
	 * @return Returns the owner. If not managed for someone else, this is the {@link LocalOrganisation#getOrganisation() local organisation}
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
	 * @return Returns the extendedProductType, which means the parent of this
	 *		<code>ProductType</code> in the data inheritance tree. This may be
	 *		<code>null</code> if this <code>ProductType</code> is a root element in the
	 *		inheritance tree.
	 */
	public ProductType getExtendedProductType()
	{
		return extendedProductType;
	}

	/**
	 * @param extendedProductType The extendedProductType to set. This method should not be called
	 *		from outside. Currently it is not yet possible to change the structure of the inheritance
	 *		tree (it's fixed after creation).
	 * @see #getExtendedProductType()
	 */
	protected void setExtendedProductType(ProductType extendedProductType)
	{
		if (extendedProductType != null) {
			if (!(extendedProductType.getClass().isInstance(this)))
				throw new IllegalArgumentException("This ProductType (\""+this.getPrimaryKey()+"\") is of type " + this.getClass().getName() + " and cannot extend the ProductType \""+extendedProductType.getPrimaryKey()+"\" which is of type "+extendedProductType.getClass().getName() + "! Correct Java-inheritance is essential!");

			if (INHERITANCE_NATURE_LEAF == extendedProductType.getInheritanceNature())
				throw new IllegalArgumentException("The extended ProductType (\""+extendedProductType.getPrimaryKey()+"\") has the inheritanceNature \""+extendedProductType.getInheritanceNatureString()+"\" and therefore cannot be extended!");
		}

		this.extendedProductType = extendedProductType;
	}

	/**
	 * A <code>ProductType</code> can contain others (e.g. a car would contain 4 wheels,
	 * the engine and much more). This method allows to package another <code>ProductType</code>
	 * within this one.
	 *
	 * @param productType The <code>ProductType</code> which will be packaged within this one. 
	 * @return Returns the {@link NestedProductType} which functions as glue-with-metadata between
	 *		the package and its content. You can change the quantity (and later other properties)
	 *		manipulating the <code>NestedProductType</code>.
	 */
	public NestedProductType createNestedProductType(ProductType productType)
	{
		// it MUST be possible to continue nesting indefinitely
//		if (this.isPackageInner())
//			throw new IllegalStateException("This ProductType ("+getPrimaryKey()+") is marked as package-inner and can therefore not contain nested product types!");

		NestedProductType packagedProductType = (NestedProductType)nestedProductTypes.get(productType.getPrimaryKey());
		if (packagedProductType == null) {
			packagedProductType = new NestedProductType(this, productType);
			nestedProductTypes.put(productType.getPrimaryKey(), packagedProductType);
		}
		return packagedProductType;
	}

	/**
	 * @return Returns a <tt>Collection</tt> of {@link NestedProductType}. These are all
	 *		other <code>ProductType</code>s that have been added to this package. It does <b>not</b>
	 *		contain itself, even if this <code>ProductType</code> does virtual-self-packaging.
	 *
	 * @see #getNestedProductTypes(boolean)
	 */
	public Collection<NestedProductType> getNestedProductTypes()
	{
		return Collections.unmodifiableCollection(nestedProductTypes.values());
	}

	/**
	 * This method can be used instead of {@link #getNestedProductTypes()} and provides
	 * the possibility to take the virtual-self-packaging into account.
	 *
	 * @param includeSelfForVirtualSelfPackaging Whether or not to add <code>this</code>
	 *		to the result, if this ProductType virtually packages itself (i.e. has an {@link #getInnerPriceConfig() inner}
	 *		<b>and</b> a {@link #getPackagePriceConfig() package price config} assigned).
	 *
	 * @return Returns either the same as {@link #getNestedProductTypes()} or adds <code>this</code> to the result.
	 */
	public Collection getNestedProductTypes(boolean includeSelfForVirtualSelfPackaging)
	{
		if (!includeSelfForVirtualSelfPackaging || isPackageInner() || getInnerPriceConfig() == null)
			return getNestedProductTypes();
		else {
			HashSet<NestedProductType> res = new HashSet<NestedProductType>(nestedProductTypes.values());
			res.add(getSelfForVirtualSelfPackaging());
			return Collections.unmodifiableCollection(res);
		}
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	protected transient NestedProductType selfForVirtualSelfPackaging = null;

	/**
	 * This method is used internally (e.g. by {@link #getNestedProductTypes(boolean)}).
	 *
	 * @return Returns a transient, temporary instance of {@link NestedProductType} linking <code>this</code>
	 *		to itself.
	 */
	protected NestedProductType getSelfForVirtualSelfPackaging()
	{
		if (selfForVirtualSelfPackaging == null)
			selfForVirtualSelfPackaging = new NestedProductType(this, this);
		return selfForVirtualSelfPackaging;
	}

	/**
	 * @param productTypePK The composite primary key returned by {@link #getPrimaryKey()}.
	 * @return Returns a packaged productType or <tt>null</tt> if none with the given ID exists.
	 */
	public NestedProductType getNestedProductType(String productTypePK, boolean throwExceptionIfNotExistent)
	{
		if (this.primaryKey.equals(productTypePK))
			return getSelfForVirtualSelfPackaging();

		NestedProductType res = (NestedProductType)nestedProductTypes.get(productTypePK);
		if (res == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No NestedProductType existing with productTypePK=\""+productTypePK+"\"!");
		return res;
	}
	/**
	 * @param organisationID The organisation part of the primary key as returned by {@link #getOrganisationID()}.
	 * @param productTypeID The local id part of the primary key as returned by {@link #getProductTypeID()}.
	 * @return Returns a packaged product or <tt>null</tt> if none with the given ID exists.
	 */
	public NestedProductType getNestedProductType(String organisationID, String productTypeID, boolean throwExceptionIfNotExistent)
	{
		if (this.organisationID.equals(organisationID) && this.productTypeID.equals(productTypeID))
			return getSelfForVirtualSelfPackaging();

		NestedProductType res = (NestedProductType)nestedProductTypes.get(getPrimaryKey(organisationID, productTypeID));
		if (res == null && throwExceptionIfNotExistent)
			throw new IllegalArgumentException("No NestedProductType existing with organisationID=\""+organisationID+"\", productTypeID=\""+productTypeID+"\"!");
		return res;
	}
	/**
	 * Removes a packaged product from this package.
	 *
	 * @param organisationID The organisation part of the primary key as returned by {@link #getOrganisationID()}.
	 * @param productTypeID The local id part of the primary key as returned by {@link #getProductTypeID()}.
	 */
	public void removeNestedProductType(String organisationID, String productTypeID)
	{
		nestedProductTypes.remove(getPrimaryKey(organisationID, productTypeID));
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

	/**
	 * The LocalAccountantDelegate is in charge of booking money to different
	 * account for the productType it is assigned to and its packaged types
	 * when an invoice is booked. 
	 */
	public LocalAccountantDelegate getLocalAccountantDelegate() {
		return localAccountantDelegate;
	}

	/**
	 * Set the LocalAccountantDelegate.
	 */
	public void setLocalAccountantDelegate(LocalAccountantDelegate localAccountantDelegate) {
		this.localAccountantDelegate = localAccountantDelegate;
	}

	/**
	 * @return Returns the localStorekeeperDelegate.
	 */
	public LocalStorekeeperDelegate getLocalStorekeeperDelegate()
	{
		return localStorekeeperDelegate;
	}

	/**
	 * @param localStorekeeperDelegate The localStorekeeperDelegate to set.
	 */
	public void setLocalStorekeeperDelegate(
			LocalStorekeeperDelegate localStorekeeperDelegate)
	{
		this.localStorekeeperDelegate = localStorekeeperDelegate;
	}

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
	 *		dependent="true"
	 *		mapped-by="productType"
	 *
	 * @jdo.key mapped-by="fieldName"
	 */
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
	 *   <li>jdo* (you must never inherit an internal jdo field!!!)</li>
	 *   <li>organisationID  (you must never inherit a primary key field!!!)</li>
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
	 *   <li>productTypeLocal</li>
	 *   <li>published</li>
	 *   <li>saleable</li>
	 *   <li>selfForVirtualSelfPackaging</li>
	 *   <li>innerPriceConfig</li>
	 *   <li>packagePriceConfig</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note, that the above list might not be up-to-date. Check the source code if you have problems!
	 * And please update the javadoc, if you encounter differences!
	 * </p>
	 */
	public org.nightlabs.inheritance.FieldMetaData getFieldMetaData(String fieldName)
	{
		if (fieldName.startsWith("jdo"))
			return null;

		synchronized (nonInheritableFields) {
			if (nonInheritableFields.isEmpty()) {
				// PK fields
				nonInheritableFields.add("organisationID");
				nonInheritableFields.add("productTypeID");
				nonInheritableFields.add("primaryKey");

				// other fields
				nonInheritableFields.add("closed");
				nonInheritableFields.add("confirmed");
				nonInheritableFields.add("fieldMetaDataMap");
				nonInheritableFields.add("extendedProductType");
				nonInheritableFields.add("inheritanceNature");
				nonInheritableFields.add("managedProductTypeGroup");
				nonInheritableFields.add("packageNature");
				nonInheritableFields.add("productAvailable");
				nonInheritableFields.add("productTypeGroups");
				nonInheritableFields.add("productTypeLocal");
				nonInheritableFields.add("published");
				nonInheritableFields.add("saleable");
				nonInheritableFields.add("selfForVirtualSelfPackaging");
				nonInheritableFields.add("innerPriceConfig");
				nonInheritableFields.add("packagePriceConfig");
			}

			if (nonInheritableFields.contains(fieldName))
				return null;
		}

		ProductTypeFieldMetaData fmd = (ProductTypeFieldMetaData)fieldMetaDataMap.get(fieldName);
		if (fmd == null) {
			if ("nestedProductTypes".equals(fieldName))
				fmd = new ProductTypeMapFieldMetaData(this, fieldName);
			else
				fmd = new ProductTypeFieldMetaData(this, fieldName);

			fieldMetaDataMap.put(fieldName, fmd);
		} // if (fmd == null) {

		return fmd;
	}

	public FieldInheriter getFieldInheriter(String fieldName)
	{
		if ("nestedProductTypes".equals(fieldName)) {		
			return new MapFieldInheriter();
		}

		return new JDOSimpleFieldInheriter();
	}
	
	public void preInherit(Inheritable mother, Inheritable child)
	{
		// access all non-simple fields in order to ensure, they're loaded by JDO
		if (deliveryConfiguration == null);
		if (innerPriceConfig == null);
		if (localAccountantDelegate == null);
		if (localStorekeeperDelegate == null);
		nestedProductTypes.size();
		if (packagePriceConfig == null);
		if (owner == null);
	}

	public void postInherit(Inheritable mother, Inheritable child) { }

	/**
	 * This method causes all settings of this instance to be passed to all children
	 * (recursively).
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
		InheritanceManager im = new InheritanceManager();
		ProductType extendedProductType = getExtendedProductType();
		if (extendedProductType != null)
			im.inheritAllFields(extendedProductType, this);

		applyInheritance(pm, im);
	}
	private void applyInheritance(PersistenceManager pm, InheritanceManager im)
	{
		Collection children = getChildProductTypes(pm, (ProductTypeID) JDOHelper.getObjectId(this));
		for (Iterator it = children.iterator(); it.hasNext();) {
			ProductType child = (ProductType) it.next();
			im.inheritAllFields(this, child);
			child.applyInheritance(pm, im);
		}
	}

	// /// *** end inheritance *** ///
	// *********************************

//	/**
//	 * Whether or not this implementation is able to create <tt>Product</tt>s automatically,
//	 * when needed. This is e.g. the case if a packaged fee should be included in a product.
//	 *
//	 * @see #createProduct()
//	 */
//	public abstract boolean isProductFactory();
//
//	/**
//	 * If you decided to make this <tt>ProductType</tt> a product factory
//	 * by returning <tt>true</tt> in <tt>isProductFactory()</tt>, then you must
//	 * implement this method - otherwise just throw an <tt>UnsupportedOperationException</tt>.
//	 *
//	 * In your implementation of this method, you must create an instance
//	 * of <tt>Product</tt>, assign a primary key to it and return this instance.
//	 *
//	 * @return Returns a newly created instance of <tt>Product</tt>.
//	 *
//	 * @see #isProductFactory()
//	 */
//	public abstract Product createProduct();

//	/**
//	 * This member is automatically set to the result of <tt>isProductProvider()</tt>.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean productProvider;

//	/**
//	 * Whether or not this <tt>ProductType</tt> is able to find/create available <tt>Product</tt>s.
//	 * A <tt>ProductType</tt> can only be packaged within another one having <tt>productProvider==true</tt>, if this
//	 * is <tt>true</tt>, too. If the <tt>Product</tt>s of the package are created manually, the
//	 * result of this method does not matter.
//	 *
//	 * @see #findProducts(User, NestedProductType, ProductLocator)
//	 * @!see #provideAvailableProduct()
//	 */
//	public abstract boolean isProductProvider();

//	/**
//	 * If <tt>isProductProvider()</tt> is true, this method might be called and must
//	 * either return <tt>null</tt> if there is no <tt>Product</tt> available or an instance
//	 * that can be sold.
//	 * <p>
//	 * The implementation of <tt>ProductType</tt> might choose to create a new instance
//	 * of <tt>Product</tt> when this method is called.
//	 * <p>
//	 * This method is only called while this <tt>ProductType</tt> is persistent.
//	 * <p>
//	 * ???? !!!! ????
//	 * Marco: IMHO this method should allocate the product immediately, to avoid someone
//	 * else from "stealing" it. Therefore, we probably need the parameter <tt>allocationKey</tt>.
//	 * ???? !!!! ????
//	 *
//	 * @return Returns <tt>null</tt> or a <tt>Product</tt> that can be sold.
//	 */
//	public abstract Product provideAvailableProduct();

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean productAvailable = true;

	public boolean isProductAvailable() {
		return productAvailable;
	}

	/**
	 * Your implementation of this method should check whether it can
	 * return a <tt>Product</tt> in <tt>provideAvailableProduct()</tt>
	 * or whether there's none available anymore.
	 * <p>
	 * If there is no <tt>Product</tt> available anymore, the <tt>ProductType</tt>
	 * will be listed as not available (or even filtered out) in sale lists.
	 * <p>
	 * This method is only called while this <tt>ProductType</tt> is persistent. 
	 */
	protected abstract boolean _checkProductAvailability();

	public boolean checkProductAvailability()
	{
		this.productAvailable = _checkProductAvailability();
		return productAvailable;
	}

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
	 * @return Returns <tt>true</tt>, if <tt>packageNature == </tt>{@link #PACKAGE_NATURE_OUTER}.
	 */
	public boolean isPackageOuter()
	{
		return PACKAGE_NATURE_OUTER == packageNature;
	}
	/**
	 * @return Returns <tt>true</tt>, if <tt>packageNature == </tt>{@link #PACKAGE_NATURE_INNER}.
	 */
	public boolean isPackageInner()
	{
		return PACKAGE_NATURE_INNER == packageNature;
	}

	/**
	 * @return Returns <tt>true</tt> if <tt>inheritanceNature == </tt>{@link #INHERITANCE_NATURE_BRANCH}.
	 */
	public boolean isInheritanceBranch()
	{
		return INHERITANCE_NATURE_BRANCH == inheritanceNature;
	}

	/**
	 * @return Returns <tt>true</tt> if <tt>inheritanceNature == </tt>{@link #INHERITANCE_NATURE_LEAF}.
	 */
	public boolean isInheritanceLeaf()
	{
		return INHERITANCE_NATURE_LEAF == inheritanceNature;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of ProductType is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

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
	 */
	protected void setPublished(boolean published) {
		getPersistenceManager();

		if (published == false && this.published == true)
			throw new IllegalArgumentException("The published flag of a ProductType is immutable after once set to true");

		if (extendedProductType != null && !extendedProductType.isPublished())
			throw new IllegalStateException("The ProductType \""+getPrimaryKey()+"\" cannot be published, because it extends \""+extendedProductType.getPrimaryKey()+"\", which is not yet published! Publish the parent first!");

		this.published = published;
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
	 */
	protected void setConfirmed(boolean confirmed)
	{
		getPersistenceManager();

		if (confirmed == false && this.confirmed == true)
			throw new IllegalArgumentException("The confirmed flag of a ProductType is immutable after once set to true");

		this.confirmed = confirmed;
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
	 * Sets if this ProductType can be sold. This only takes effect when 
	 * published is true. This is used as a filter flag.
	 * 
	 * @param saleable Wheather this ProductType is saleable
	 */
	protected void setSaleable(boolean saleable)
	{
		getPersistenceManager();

		if (saleable && !confirmed)
			throw new IllegalStateException("Cannot make ProductType \"" + getPrimaryKey() + "\" saleable, because it is not yet confirmed!");

		if (saleable && closed)
			throw new IllegalStateException("Cannot make ProductType \"" + getPrimaryKey() + "\" saleable, because it is already closed!");

		this.saleable = saleable;
	}

	/**
	 * A ProductType can be closed. This is irreversile.
	 *
	 * @return Returns whether this ProductType is closed.
	 */
	public boolean isClosed()
	{
		return closed;
	}

	protected void setClosed(boolean closed)
	{
		getPersistenceManager();

		if (saleable && closed)
			throw new IllegalStateException("Cannot close ProductType \"" + getPrimaryKey() + "\", because it is still saleable!");

		if (this.closed && !closed)
			throw new IllegalArgumentException("The closed flag of a ProductType is immutable after once set to true"); 

		this.closed = closed;
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

	public Collection getProductTypeGroups()
	{
		return productTypeGroups.values();
	}

	/**
	 * This method returns the inner price config, if <tt>packageProductTypePK</tt>
	 * equals this pk (means we use <tt>this</tt> as virtual inner product type) or
	 * if this is not a package.
	 * If it is a package (and <tt>packageProductTypePK</tt> does not point back
	 * to <tt>this</tt>), the package price config will be returned.
	 * <p>
	 * The sense in this behaviour is to automatically return the correct price config
	 * depending on the situation.
	 *
	 * @param packageProductTypePK The primary key of the <tt>ProductType</tt>
	 * (obtained via {@link ProductType#getPrimaryKey()) in which this
	 * <tt>ProductType</tt> is packaged. This parameter can be <tt>null</tt>.
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

	/**
	 * Implement this method to find a suitable <tt>Product</tt> for the given
	 * <tt>ProductLocator</tt>.
	 * <p>
	 * Do NOT call this method directly!
	 * Use {@link Store#findProducts(User, NestedProductType, ProductLocator)} instead! This is
	 * necessary for interception.
	 * <p>
	 * You should return the same number of <tt>Product</tt>s as defined in <tt>nestedProductType.quantity</tt>! Otherwise,
	 * it is handled as if you return <tt>null</tt>.
	 *
	 * @param user The <tt>User</tt> who is responsible for this creation.
	 * @param nestedProductType This will be <tt>null</tt> if the top-level product shall be found/created.
	 * @param productLocator A specialized Object defining for YOUR implementation of <tt>ProductType</tt> which <tt>Product</tt> to find.
	 * @return Return either <tt>null</tt> if no suitable <tt>Product</tt>s can be allocated or a <tt>Collection</tt> of <tt>Product</tt>.
	 *
	 * @see #isProductProvider()
	 */
	protected abstract Collection findProducts(User user, NestedProductType nestedProductType, ProductLocator productLocator);

	public abstract I18nText getName();

	/**
	 * @return Returns either <tt>null</tt> or the ProductTypeGroup
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
	protected void setProductTypeLocal(ProductTypeLocal productTypeLocal)
	{
		this.productTypeLocal = productTypeLocal;
	}

	public String toString()
	{
		return this.getClass().getName() + '{' + getPrimaryKey() + '}';
	}

	protected ProductTypeLocal createProductTypeLocal(User user, Anchor home)
	{
		return new ProductTypeLocal(this, home); // self-registering
	}
}
