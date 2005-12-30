/*
 * Created on Feb 21, 2005
 */
package org.nightlabs.ipanema.simpletrade.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.inheritance.FieldInheriter;
import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.inheritance.Inheritable;
import org.nightlabs.inheritance.InheritableFieldInheriter;
import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.store.NestedProductType;
import org.nightlabs.ipanema.store.ProductLocator;
import org.nightlabs.ipanema.store.ProductType;
import org.nightlabs.ipanema.store.Repository;
import org.nightlabs.ipanema.store.Store;
import org.nightlabs.ipanema.store.id.ProductTypeID;
import org.nightlabs.ipanema.trade.LegalEntity;
import org.nightlabs.ipanema.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.store.ProductType"
 *		detachable="true"
 *		table="JFireSimpleTrade_SimpleProductType"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.query
 * 		global="false"
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
 * 
 * @jdo.fetch-group name="SimpleProductType.this" fetch-groups="default, ProductType.this" fields="name"
 *
 * @jdo.fetch-group name="ProductType.name" fields="name"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="name"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="name"
 */
public class SimpleProductType extends ProductType
{
	public static final String FETCH_GROUP_THIS_SIMPLE_PRODUCT_TYPE = "SimpleProductType.this";

	/**
	 * Note, that this method does only return instances of {@link SimpleProductType} while
	 * the same-named method {@link ProductType#getChildProductTypes(PersistenceManager, ProductTypeID)}
	 * returns all types inherited from {@link ProductType}.
	 *
	 * @param pm The <tt>PersistenceManager</tt> that should be used to access the datastore.
	 * @param parentProductTypeID The <tt>ProductType</tt> of which to find all children or <tt>null</tt> to find all top-level-<tt>SimpleProductType</tt>s.
	 * @return Returns instances of <tt>SimpleProductType</tt>.
	 */
	public static Collection getChildProductTypes(PersistenceManager pm, ProductTypeID parentProductTypeID)
	{
		if (parentProductTypeID == null) {
			Query q = pm.newNamedQuery(SimpleProductType.class, "getChildProductTypes_topLevel");
			return (Collection)q.execute();
		}

		Query q = pm.newNamedQuery(SimpleProductType.class, "getChildProductTypes_hasParent");
		return (Collection) q.execute(
			parentProductTypeID.organisationID, parentProductTypeID.productTypeID);
	}

	public static Repository getDefaultHome(PersistenceManager pm, SimpleProductType simpleProductType)
	{
		Store store = Store.getStore(pm);
		if (store.getOrganisationID().equals(simpleProductType.getOrganisationID()))
			return getDefaultLocalHome(pm, store);
		else
			return getDefaultForeignHome(pm, store);
	}

	private static AnchorID localHomeID = null;

	protected static Repository getDefaultLocalHome(PersistenceManager pm, Store store)
	{
		if (localHomeID == null) {
			localHomeID = AnchorID.create(
					store.getOrganisationID(),
					Repository.ANCHOR_TYPE_ID_HOME,
					ANCHOR_ID_REPOSITORY_HOME_LOCAL);
		}

		pm.getExtent(Repository.class);
		Repository home;
		try {
			home = (Repository) pm.getObjectById(localHomeID);
		} catch (JDOObjectNotFoundException x) {
			home = new Repository(
					localHomeID.organisationID,
					localHomeID.anchorTypeID,
					localHomeID.anchorID,
					store.getMandator(), false);

			pm.makePersistent(home);
		}

		return home;
	}

	private static AnchorID foreignHomeID = null;

	protected static Repository getDefaultForeignHome(PersistenceManager pm, Store store)
	{
		if (foreignHomeID == null) {
			foreignHomeID = AnchorID.create(
					store.getOrganisationID(),
					Repository.ANCHOR_TYPE_ID_HOME,
					ANCHOR_ID_REPOSITORY_HOME_FOREIGN);
		}

		pm.getExtent(Repository.class);
		Repository home;
		try {
			home = (Repository) pm.getObjectById(foreignHomeID);
		} catch (JDOObjectNotFoundException x) {
			home = new Repository(
					foreignHomeID.organisationID,
					foreignHomeID.anchorTypeID,
					foreignHomeID.anchorID,
					store.getMandator(), false);

			pm.makePersistent(home);
		}

		return home;
	}

	/**
	 * @deprecated Constructor only for JDO!
	 */
	protected SimpleProductType() { }

	/**
	 * @see ProductType#ProductType(String, String, ProductType, LegalEntity, String)
	 */
	public SimpleProductType(String organisationID, String productTypeID,
			ProductType extendedProductType, LegalEntity owner,
			byte inheritanceNature, byte packageNature)
	{
		super(organisationID, productTypeID, extendedProductType, owner, inheritanceNature, packageNature);

		this.name = new SimpleProductTypeName(this);
		getFieldMetaData("name").setValueInherited(false);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private SimpleProductTypeName name;

	/**
	 * If <tt>maxProductCount</tt> has a value <tt>&gt;=0</tt>, this is the maximum number of
	 * <tt>Product</tt>s that can be created and sold, To have an unlimited amount of
	 * <tt>Product</tt>s available, set this to <tt>-1</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long maxProductCount = -1;
	
	/**
	 * Keeps track, how many <tt>Product</tt>s have already been created. If this number
	 * reaches <tt>maxProductCount</tt> and <tt>maxProductCount</tt> is a positive number,
	 * this <tt>ProductType</tt> will stop to create new <tt>SimpleProduct</tt>s.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long createdProductCount = 0;

	/**
	 * @return Returns the name.
	 */
	public I18nText getName()
	{
		return name;
	}

//	/**
//	 * @see org.nightlabs.ipanema.store.ProductType#isProductProvider()
//	 */
//	public boolean isProductProvider()
//	{
//		return true;
//	}

//	/**
//	 * @see org.nightlabs.ipanema.store.ProductType#provideAvailableProduct()
//	 */
//	public Product provideAvailableProduct()
//	{
//		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
//		if (pm == null)
//			throw new IllegalStateException("This instance of SimpleProductType is currently not persistent!");
//
//		if (maxProductCount >= 0) {
//			if (createdProductCount >= maxProductCount)
//				return null;
//		}
//		
//		Store store = Store.getStore(pm);
//		if (!store.getOrganisationID().equals(this.getOrganisationID()))
//			throw new IllegalStateException("Cannot create a Product in a foreign datastore! this.organisationID=\""+getOrganisationID()+"\" != store.organisationID=\""+store.getOrganisationID()+"\"");
//
//		createdProductCount = createdProductCount + 1;
//
//		SimpleProduct newProduct = new SimpleProduct(this, store.createProductID());
//		return newProduct;
//	}

	/**
	 * @see org.nightlabs.ipanema.store.ProductType#_checkProductAvailability()
	 */
	protected boolean _checkProductAvailability()
	{
		if (maxProductCount >= 0) {
			if (createdProductCount >= maxProductCount)
				return false;
		}

		return true;
	}
	/**
	 * WORKAROUND Because of a JPOX bug, we have to re-set the member extendedProductType in the EJBean.
	 * 
	 * @see org.nightlabs.ipanema.store.ProductType#setExtendedProductType(org.nightlabs.ipanema.store.ProductType)
	 */
	public void setExtendedProductType(ProductType extendedProductType)
	{
		super.setExtendedProductType(extendedProductType);
	}
	/**
	 * @return Returns the createdProductCount.
	 */
	public long getCreatedProductCount()
	{
		return createdProductCount;
	}
	/**
	 * @param createdProductCount The createdProductCount to set.
	 */
	public void setCreatedProductCount(long createdProductCount)
	{
		this.createdProductCount = createdProductCount;
	}
	/**
	 * @return Returns the maxProductCount.
	 */
	public long getMaxProductCount()
	{
		return maxProductCount;
	}
	/**
	 * @param maxProductCount The maxProductCount to set.
	 */
	public void setMaxProductCount(long maxProductCount)
	{
		this.maxProductCount = maxProductCount;
	}

	/**
	 * This is the {@link org.nightlabs.ipanema.transfer.Anchor#getAnchorID()} of
	 * the {@link Repository} which becomes the factory-output-repository for all
	 * newly created {@link SimpleProduct}s.
	 */
	public static final String ANCHOR_ID_REPOSITORY_HOME_LOCAL = SimpleProductType.class.getName() + ".local";

	/**
	 * This is the {@link org.nightlabs.ipanema.transfer.Anchor#getAnchorID()} of
	 * the {@link Repository} which is used for products that are bought from a foreign organisation.
	 */
	public static final String ANCHOR_ID_REPOSITORY_HOME_FOREIGN = SimpleProductType.class.getName() + ".foreign";

	protected Collection findProducts(User user, NestedProductType nestedProductType, ProductLocator productLocator)
	{
		int qty = nestedProductType == null ? 1 : nestedProductType.getQuantity();
		PersistenceManager pm = getPersistenceManager();

		Store store = Store.getStore(pm);
		// search for an available product
		Query q = pm.newQuery(SimpleProduct.class);
		q.setFilter("productType == pProductType && productLocal.available");
		q.declareParameters("SimpleProductType pProductType");
		q.declareImports("import " + SimpleProductType.class.getName());
		Collection availableProducts = (Collection) q.execute(this); // Product.getProducts(pm, this, ProductStatus.STATUS_AVAILABLE);
		ArrayList res = new ArrayList();
		Iterator iteratorAvailableProducts = availableProducts.iterator();
		for (int i = 0; i < qty; ++i) {
			SimpleProduct product = null;
			if (iteratorAvailableProducts.hasNext()) {
				product = (SimpleProduct) iteratorAvailableProducts.next();
				res.add(product);
			}
			else {
				// create products only if this product type is ours
				if (this.getOrganisationID().equals(store.getOrganisationID())) {
					long createdProductCount = getCreatedProductCount();
					if (getMaxProductCount() < 0 || createdProductCount + 1 <= getMaxProductCount()) {
						product = new SimpleProduct(this, store.createProductID());
						setCreatedProductCount(createdProductCount + 1);

						store.addProduct(user, product, (Repository)getProductTypeLocal().getHome());
						res.add(product);
					}
				} // This productType is factored by this organisation
			}
		}
		return res;
	}


	// ******************************
	// /// *** begin inheritance *** ///
	public FieldMetaData getFieldMetaData(String fieldName)
	{
		if ("createdProductCount".equals(fieldName))
			return null;

		return super.getFieldMetaData(fieldName);
	}

	public FieldInheriter getFieldInheriter(String fieldName)
	{
		if ("name".equals(fieldName))
			return new InheritableFieldInheriter();

		return super.getFieldInheriter(fieldName);
	}

	public void preInherit(Inheritable mother, Inheritable child)
	{
		super.preInherit(mother, child);
		name.getI18nMap();
	}
	// /// *** end inheritance *** ///
	// ******************************
}
