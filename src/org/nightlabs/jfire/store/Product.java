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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductID"
 *		detachable="true"
 *		table="JFireTrade_Product"
 *
 * @jdo.inheritance strategy="new-table"
 * @!jdo.inheritance-discriminator strategy="class-name"
 * @!jdo.inheritance strategy="subclass-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, productID"
 *
 * @jdo.fetch-group name="Product.productLocal" fields="productLocal"
 * @jdo.fetch-group name="Product.productType" fields="productType"
 */
public abstract class Product
implements Serializable
{
	public static final String FETCH_GROUP_PRODUCT_LOCAL = "Product.productLocal";
	public static final String FETCH_GROUP_PRODUCT_TYPE = "Product.productType";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long productID;

	public static long createProductID()
	{
		return IDGenerator.nextID(Product.class);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 * @jdo.column length="120"
	 */
	private String primaryKey;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductType productType;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="product"
	 * @! mapped-by="product" // TODO this mapped-by should be here, but there seems to be a bug in JPOX
	 */
	private ProductLocal productLocal = null;

	/**
	 * @deprecated This default constructor exists only for JDO. When inheriting
	 * this class, please make your default constructor protected and
	 * use only the second constructor!
	 */
	protected Product()
	{
	}

	/**
	 * @param productType The <tt>ProductType</tt> of which this <tt>Product</tt> is an instance.
	 * @param productID The unique ID of this <tt>Product</tt> within the owner organisation. The owner organisation is the same as the one of the <tt>ProductType</tt>.
	 */
	public Product(ProductType productType, long productID)
	{
		if (productID < 0)
			throw new IllegalArgumentException("productID < 0");
		if (productType == null)
			throw new NullPointerException("productType");

		this.productType = productType;
		this.organisationID = productType.getOrganisationID();
		this.productID = productID;
		this.primaryKey = getPrimaryKey(organisationID, productID);
	}

	protected void setProductLocal(ProductLocal productLocal)
	{
		if (this.productLocal != null)
			throw new IllegalStateException("A ProductLocal is already assigned! Cannot change!");

		if (productLocal.getProduct() != this)
			throw new IllegalArgumentException("productLocal.getProduct() != this");

		this.productLocal = productLocal;
	}
	public ProductLocal getProductLocal()
	{
		return productLocal;
	}

	/**
	 * @return Returns the organisationID.
	 */
	public String getOrganisationID()
	{
		return organisationID;
	}
	/**
	 * @return Returns the productID.
	 */
	public long getProductID()
	{
		return productID;
	}
	public static String getPrimaryKey(String organisationID, long productID)
	{
		return organisationID + '/' + Long.toHexString(productID);
	}
	/**
	 * @return Returns the primaryKey.
	 */
	public String getPrimaryKey()
	{
		return primaryKey;
	}
	/**
	 * @return Returns the productType.
	 */
	public ProductType getProductType()
	{
		return productType;
	}

//	public Collection getNestedProducts(NestedProductType nestedProductType)
//	{
//		Collection res = new LinkedList();
//		String innerProductTypePK = nestedProductType.getInnerProductTypePrimaryKey();
//		for (Iterator it = nestedProducts.values().iterator(); it.hasNext(); ) {
//			Product nestedProduct = (Product) it.next();
//			if (innerProductTypePK.equals(nestedProduct.getProductType().getPrimaryKey()))
//				res.add(nestedProduct);
//		}
//		return res;
//	}

	/**
	 * This method is called during the allocation-process.
	 * It must return a <code>ProductLocator</code> that is suitable for
	 * {@link ProductType#findProducts(User, ProductType, NestedProductType, ProductLocator)}
	 * of the <code>nestedProductType</code>s. This method assumes that a wrapping
	 * <code>ProductType</code> knows the <code>ProductType</code>s it is packaging
	 * (which is usually the case - not the other way around).
	 * You can obtain the {@link Article} in which this <code>Product</code> is wrapped
	 * by {@link #getProductLocal()} and {@link ProductLocal#getArticle()}, in case you're interested.
	 *
	 * @param user The <code>User</code> who is responsible for this action.
	 * @param nestedProductType The <code>NestedProductType</code> for which to create a <code>ProductLocator</code>.
	 * @return Return <tt>null</tt> or an object that identifies one or more nested <tt>Product</tt>s
	 * 		in order to allocate them.
	 */
	public abstract ProductLocator getProductLocator(User user, NestedProductType nestedProductType);

//	/**
//	 * This method is called by {@link Trader#allocateArticlesEnd(User user, Collection articles)} and
//	 * must ensure that this product is fully allocated and assembled.
//	 * <p>
//	 * This method DOES NOT set the allocated status in {@link ProductLocal} - this
//	 * is done by the {@link Trader}.
//	 * </p>
//	 *
//	 * @param user
//	 * @throws ModuleException
//	 */
//	public void assemble(User user)
//	throws ModuleException
//	{
//		ProductLocal productLocal = getProductLocal();
//		if (productLocal.isAssembled())
//			return; // nothing to do
//
//		Trader trader = Trader.getTrader(getPersistenceManager());
//		Store store = trader.getStore();
//
//		if (!this.organisationID.equals(trader.getOrganisationID())) {
//			// remote product => this should never happen, because every foreign product should arrive here in assembled form
//			throw new UnsupportedOperationException("Foreign product should always be assembled!");
//		}
//
//		// we assemble the Product recursively
//
//		// key: Anchor home
//		// value: Set<Product> products
//		Map nestedProductsByHome = new HashMap();
//
//		// All nestedProductTypes that come from a partner-organisation are collected and grouped in this map in order
//		// to import them more efficiently
////		Map<String, List<NestedProductType>> organisationID2partnerNestedProductType = new HashMap<String, List<NestedProductType>>();
//		Map organisationID2partnerNestedProductType = null; // lazy creation
//// The above generic notation causes the class Product to be destroyed during enhancement with BCEL + JPOX-Enhancer. This results
//// in the following exception when afterwards enhancing JFireSimpleTrade (which extends the class Product):
////		[jpoxenhancer] Exception in thread "main" java.lang.ClassFormatError: LVTT entry for 'me' in class file org/nightlabs/jfire/store/Product does not match any LVT entry
////		[jpoxenhancer] at java.lang.ClassLoader.defineClass1(Native Method)
////		[jpoxenhancer] at java.lang.ClassLoader.defineClass(ClassLoader.java:620)
////		[jpoxenhancer] at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:124)
////		[jpoxenhancer] at java.net.URLClassLoader.defineClass(URLClassLoader.java:260)
////		[jpoxenhancer] at java.net.URLClassLoader.access$100(URLClassLoader.java:56)
////		[jpoxenhancer] at java.net.URLClassLoader$1.run(URLClassLoader.java:195)
////		[jpoxenhancer] at java.security.AccessController.doPrivileged(Native Method)
////		[jpoxenhancer] at java.net.URLClassLoader.findClass(URLClassLoader.java:188)
//
//		// local product => create/find nested products
//		ProductType productType = this.getProductType();
//		for (Iterator itNPT = productType.getNestedProductTypes().iterator(); itNPT.hasNext(); ) {
//			NestedProductType nestedProductType = (NestedProductType) itNPT.next();
//
//			if (this.organisationID.equals(nestedProductType.getInnerProductTypeOrganisationID())) {
//				ProductLocator productLocator = this.getProductLocator(user, nestedProductType);
//
//				// nested productType is our own, so we can just package it without buying it from someone else.
//				Collection nestedProducts = store.findProducts(user, null, nestedProductType, productLocator);
//				if (nestedProducts == null || nestedProducts.size() != nestedProductType.getQuantity())
//					throw new NotAvailableException("The product '"+getPrimaryKey()+"' cannot be assembled, because the nested ProductType '"+nestedProductType.getInnerProductTypePrimaryKey()+"' could not find available products!");
//
//				for (Iterator itNestedProducts = nestedProducts.iterator(); itNestedProducts.hasNext(); ) {
//					Product nestedProduct = (Product)itNestedProducts.next();
//					ProductLocal nestedProductLocal = nestedProduct.getProductLocal();
//					productLocal.addNestedProductLocal(nestedProductLocal);
//					nestedProduct.assemble(user);
//					nestedProductLocal.decQuantity();
//
//					// We need to transfer the nested product from wherever it is to the same repository as the
//					// package-product and then update productLocal.quantity
//					// To reduce transfers, we group them by source-repository (dest is the same for all nested products)
//					// dest: this.productType.productTypeLocal.home
//					// source: nestedProduct.productType.productTypeLocal.home
//					Anchor nestedProductHome = nestedProduct.getProductType().getProductTypeLocal().getHome();
//					Set nestedProductSet = (Set) nestedProductsByHome.get(nestedProductHome);
//					if (nestedProductSet == null) {
//						nestedProductSet = new HashSet();
//						nestedProductsByHome.put(nestedProductHome, nestedProductSet);
//					}
//					nestedProductSet.add(nestedProduct);
//				}
//			}
//			else {
//				if (organisationID2partnerNestedProductType == null)
//					organisationID2partnerNestedProductType = new HashMap();
//				
//				// nested productType is coming from a remote organisation and must be acquired from there
//				// this means: an Offer must be created (or a previously created one used) and an Article be added
//				// we group them in order to make it more efficient
//				List partnerNestedProductTypes = (List) organisationID2partnerNestedProductType.get(nestedProductType.getInnerProductTypeOrganisationID());
//				if (partnerNestedProductTypes == null) {
//					partnerNestedProductTypes = new ArrayList<NestedProductType>();
//					organisationID2partnerNestedProductType.put(nestedProductType.getInnerProductTypeOrganisationID(), partnerNestedProductTypes);
//				}
//				partnerNestedProductTypes.add(nestedProductType);
//			}
//		} // for (Iterator itNPT = productType.getNestedProductTypes().iterator(); itNPT.hasNext(); ) {
//
////		for (Iterator<Map.Entry<String, List<NestedProductType>>> itPNPT = organisationID2partnerNestedProductType.entrySet().iterator(); itPNPT.hasNext(); ) {
//		if (organisationID2partnerNestedProductType != null) {
//			for (Iterator itPNPT = organisationID2partnerNestedProductType.entrySet().iterator(); itPNPT.hasNext(); ) {
//	//			Map.Entry<String, List<NestedProductType>> me = (Entry<String, List<NestedProductType>>) itPNPT.next();
//				Map.Entry me = (Map.Entry) itPNPT.next();
//				String organisationID = (String) me.getKey();
//				List nestedProductTypes = (List) me.getValue();
//				Collection articlesWithNestedProducts = trader.onProductAssemble_importNestedProduct(user, this, organisationID, nestedProductTypes);
//
//				// dest: this.productType.productTypeLocal.home
//				// source: nestedProduct.productType.productTypeLocal.home
//				for (Iterator itArticle = articlesWithNestedProducts.iterator(); itArticle.hasNext(); ) {
//					Article articleWithNestedProduct = (Article) itArticle.next();
//					Product nestedProduct = articleWithNestedProduct.getProduct();
//					Anchor nestedProductHome = nestedProduct.getProductType().getProductTypeLocal().getHome();
//					Set nestedProductSet = (Set) nestedProductsByHome.get(nestedProductHome);
//					if (nestedProductSet == null) {
//						nestedProductSet = new HashSet();
//						nestedProductsByHome.put(nestedProductHome, nestedProductSet);
//					}
//					nestedProductSet.add(nestedProduct);
//				}
//			}
//		}
//		// TODO are the ProductTransfers for the foreign products already created correctly?
//
//		// create the ProductTransfers for the grouped nested products
//		Map<String, Anchor> involvedAnchors = new HashMap<String, Anchor>();
//		LinkedList productTransfers = new LinkedList();
//		boolean failed = true;
//		try {
//			Anchor thisProductHome = getProductType().getProductTypeLocal().getHome();
//			for (Iterator it = nestedProductsByHome.entrySet().iterator(); it.hasNext(); ) {
//				Map.Entry me = (Map.Entry) it.next();
//				Anchor nestedProductHome = (Anchor) me.getKey();
//				Set nestedProducts = (Set) me.getValue();
//				// transfer from nested to this
//				if (!thisProductHome.getPrimaryKey().equals(nestedProductHome.getPrimaryKey())) {
//					ProductTransfer productTransfer = new ProductTransfer(store, null, user, nestedProductHome, thisProductHome, nestedProducts);
//					productTransfer.bookTransfer(user, involvedAnchors);
//					productTransfers.add(productTransfer);
//				}
//			}
//
//			// and finally check the integrity of the involved anchors after all the transfers
//			Anchor.checkIntegrity(productTransfers, involvedAnchors);
//
//			failed = false;
//		} finally {
//			if (failed)
//				Anchor.resetIntegrity(productTransfers, involvedAnchors);
//		}
//
//		productLocal.setAssembled(true);
//	}

//	/**
//	 * @param user The responsible user
//	 * @param onRelease Whether this method is called during the release method. Hence, a Product
//	 *		might decide not to disassemble, if this is <code>true</code>.
//	 */
//	public void disassemble(User user, boolean onRelease)
//	{
//		ProductLocal productLocal = getProductLocal();
//		if (!productLocal.isAssembled())
//			return; // nothing to do
//
//		Trader trader = Trader.getTrader(getPersistenceManager());
//		Store store = trader.getStore();
//
//		if (!this.organisationID.equals(trader.getOrganisationID())) {
//			// remote product => cannot be disassembled, but must be returned instead
//			throw new UnsupportedOperationException("Foreign product cannot be disassembled!");
//		}
//
//		// key: Anchor home
//		// value: Set<Product> products
//		Map nestedProductsByHome = new HashMap();
//
//		for (Iterator it = getProductLocal().getNestedProductLocals().iterator(); it.hasNext(); ) {
//			ProductLocal nestedProductLocal = (ProductLocal) it.next();
//			Product nestedProduct = nestedProductLocal.getProduct();
//
//			if (this.organisationID.equals(nestedProduct.getOrganisationID())) {
//				// local nested product => disassemble
//				nestedProductLocal.incQuantity();
//				nestedProduct.disassemble(user, onRelease);
//			}
//			else {
//				// remote nested product => return to the source organisation
//
//				// TODO allow remote products - delegate to Trader!
//				throw new UnsupportedOperationException("NYI");
//			}
//
//			// We need to transfer the nested product back to its home repositories and update productLocal.quantity
//			// To reduce transfers, we group them by dest-repository (source is the same for all nested products)
//			// source: this.productType.productTypeLocal.home
//			// dest nestedProduct.productType.productTypeLocal.home
//			Anchor nestedProductHome = nestedProduct.getProductType().getProductTypeLocal().getHome();
//			Set nestedProducts = (Set) nestedProductsByHome.get(nestedProductHome);
//			if (nestedProducts == null) {
//				nestedProducts = new HashSet();
//				nestedProductsByHome.put(nestedProductHome, nestedProducts);
//			}
//			nestedProducts.add(nestedProduct);
//		}
//
//		// create the ProductTransfers for the grouped nested products
//		Map<String, Anchor> involvedAnchors = new HashMap<String, Anchor>();
//		LinkedList productTransfers = new LinkedList();
//		boolean failed = true;
//		try {
//	
//			Anchor thisProductHome = getProductType().getProductTypeLocal().getHome();
//			for (Iterator it = nestedProductsByHome.entrySet().iterator(); it.hasNext(); ) {
//				Map.Entry me = (Map.Entry) it.next();
//				Anchor nestedProductHome = (Anchor) me.getKey();
//				Set nestedProducts = (Set) me.getValue();
//				// transfer from this to nested
//				if (!thisProductHome.getPrimaryKey().equals(nestedProductHome.getPrimaryKey())) {
//					ProductTransfer productTransfer = new ProductTransfer(store, null, user, thisProductHome, nestedProductHome, nestedProducts);
//					productTransfer.bookTransfer(user, involvedAnchors);
//					productTransfers.add(productTransfer);
//				}
//			}
//
//			// and finally check the integrity of the involved anchors after all the transfers
//			Anchor.checkIntegrity(productTransfers, involvedAnchors);
//
//			failed = false;
//		} finally {
//			if (failed)
//				Anchor.resetIntegrity(productTransfers, involvedAnchors);
//		}
//
//		productLocal.removeAllNestedProductLocals();
//		productLocal.setAssembled(false);
//	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Cannot obtain PersistenceManager! This Product is currently not attached to a datastore!");
		return pm;
	}

	public String toString()
	{
		return this.getClass().getName() + '{' + getPrimaryKey() + '}';
	}

	/**
	 * This method is called by {@link Store#addProduct(User, Product, Repository)} on the new product.
	 * Override it, if you need a specialized descendant of {@link ProductLocal}.
	 */
	protected ProductLocal createProductLocal(User user, Repository initialRepository)
	{
		return new ProductLocal(user, this, initialRepository); // self-registering - no pm.makePersistent(...) and no product.setProductLocal necessary
	}
}
