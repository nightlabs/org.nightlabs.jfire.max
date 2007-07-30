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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.ModuleException;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.base.Lookup;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.book.PartnerStorekeeper;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryHelperBean;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.OfferLocal;
import org.nightlabs.jfire.trade.OfferRequirement;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.OrderRequirement;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.TradeSide;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.jfire.trade.jbpm.ProcessDefinitionAssignment;
import org.nightlabs.jfire.trade.jbpm.id.ProcessDefinitionAssignmentID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.util.Util;

/**
 * <p>
 * This class provides logic about how certain {@link ProductType}-related operations
 * are performed. Therefore, the {@link Store} and the {@link Accounting} as well as
 * the {@link Trader} delegate work to an instance of this class.
 * </p>
 * <p>
 * If you need special logic for your {@link ProductType}s or if you intend to interface
 * to an external system, you need to extend this class and register an instance via
 * your project's <code>datastoreinit.xml</code>.
 * </p>
 * <p>
 * Note, that you should not call methods here directly (except some static methods)! You should always call methods
 * in {@link Trader}, {@link Store}, {@link Accounting} etc.
 * </p>
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductTypeActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_ProductTypeActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, productTypeActionHandlerID"
 *
 * @jdo.query name="getProductTypeActionHandlerByProductTypeClassName" query="
 *		SELECT UNIQUE
 *		WHERE this.productTypeClassName == pProductTypeClassName
 *		PARAMETERS String pProductTypeClassName
 *		import java.lang.String"
 */
public abstract class ProductTypeActionHandler
{
	/**
	 * This method finds the right handler for the given class (which must extend {@link ProductType}).
	 * Therefore, the method traverses the inheritance and searches for all parent classes and for
	 * all interfaces.
	 * <p>
	 * The search order is like this:
	 * <ul>
	 * <li>class</li>
	 * <li>interfaces in declaration order</li>
	 * <li>superclass</li>
	 * <li>interfaces of superclass in declaration order</li>
	 * <li>...and so on for all super-super-[...]-classes...</li>
	 * </ul>
	 * </p>
	 *
	 * @param pm The <code>PersistenceManager</code> to be used for accessing the datastore.
	 * @param productTypeClass The class (must be an inheritent of {@link ProductType}) for which to find a handler.
	 * @return Returns an instance of {@link ProductTypeActionHandler}. Never returns <code>null</code>. 
	 * @throws ProductTypeActionHandlerNotFoundException If no handler is registered for the given class or one of its
	 *		parent classes.
	 */
	public static ProductTypeActionHandler getProductTypeActionHandler(PersistenceManager pm, Class productTypeClass)
	throws ProductTypeActionHandlerNotFoundException
	{
		Class searchClass = productTypeClass;
		while (searchClass != null) {
			ProductTypeActionHandler res = _getProductTypeActionHandler(pm, searchClass);
			if (res != null)
				return res;

			searchClass = searchClass.getSuperclass();
		}

		throw new ProductTypeActionHandlerNotFoundException(productTypeClass, "There is no handler registered for " + productTypeClass.getName());
	}

	/**
	 * Checks only one class (no superclasses), but including all interfaces implemented in this class.
	 * This method is used by {@link #getProductTypeActionHandler(PersistenceManager, Class) }.
	 */
	private static ProductTypeActionHandler _getProductTypeActionHandler(PersistenceManager pm, Class searchClass)
	{
		Query q = pm.newNamedQuery(ProductTypeActionHandler.class, "getProductTypeActionHandlerByProductTypeClassName");
		ProductTypeActionHandler res = (ProductTypeActionHandler) q.execute(searchClass.getName());
		if (res != null)
			return res;

		Class[] interfaces = searchClass.getInterfaces();
		if (interfaces.length > 1) {
			for (int i = 0; i < interfaces.length; i++) {
				Class intf = interfaces[i];
				res = (ProductTypeActionHandler) q.execute(intf.getName());
				if (res != null)
					return res;
			}
		}

		return null;
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
	private String productTypeActionHandlerID;

	/**
	 * Class or interface. If it's a class, it must extend {@link ProductType}.
	 *
	 * @jdo.field persistence-modifier="persistent" unique="true"
	 */
	private String productTypeClassName;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductTypeActionHandler() { }

	/**
	 * @param organisationID First part of primary key: The identifier of that organisation which defined this handler.
	 *		Use {@link Organisation#DEVIL_ORGANISATION_ID} if you contribute directly to a JFire project and your own
	 *		organisation's unique identifier (i.e. your domain), if you write an own project.
	 * @param productTypeActionHandlerID The ID within the scope of the <code>organisationID</code> 
	 * @param productTypeClass The class for which this handler shall be responsible. It will apply to all
	 *		inherited classes as well, except if there is another handler registered for the extended type.
	 */
	public ProductTypeActionHandler(
			String organisationID, String productTypeActionHandlerID,
			Class productTypeClass)
	{
		ObjectIDUtil.assertValidIDString(organisationID, "organisationID");
		ObjectIDUtil.assertValidIDString(organisationID, "productTypeActionHandlerID");

		if (productTypeClass == null)
			throw new IllegalArgumentException("productTypeClass must not be null!");

		if (!productTypeClass.isInterface()) {
			if (!ProductType.class.isAssignableFrom(productTypeClass))
				throw new IllegalArgumentException("productTypeClass is a class, but does not extend " + ProductType.class.getName() + "!");
		}

		this.organisationID = organisationID;
		this.productTypeActionHandlerID = productTypeActionHandlerID;
		this.productTypeClassName = productTypeClass.getName();
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public String getProductTypeActionHandlerID()
	{
		return productTypeActionHandlerID;
	}
	public String getProductTypeClassName()
	{
		return productTypeClassName;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	/**
	 * Implement this method to find suitable {@link Product}s for the given
	 * {@link ProductLocator}.
	 * <p>
	 * Do NOT call this method directly!
	 * Use {@link Store#findProducts(User, NestedProductType, ProductLocator)} instead! This is
	 * necessary for interception.
	 * <p>
	 * You should return the same number of <code>Product</code>s as defined in <code>nestedProductType.quantity</code>! Otherwise,
	 * it is handled as if you return <code>null</code>.
	 *
	 * @param user The <code>User</code> who is responsible for this creation.
	 * @param productType If <code>nestedProductType</code> is defined, it is the same as {@link NestedProductType#getInnerProductType()}. This is passed, because
	 *		<code>nestedProductType</code> might be <code>null</code> for top-level products.
	 * @param nestedProductType This will be <code>null</code> if the top-level product shall be found/created.
	 * @param productLocator A specialized Object defining for YOUR implementation of <code>ProductType</code> which <code>Product</code> to find.
	 * @return Return either <code>null</code> if no suitable <code>Product</code>s can be allocated or a <code>Collection</code> of <code>Product</code>.
	 */
	public abstract Collection<? extends Product> findProducts(
			User user, ProductType productType, NestedProductType nestedProductType, ProductLocator productLocator);

	/**
	 * This method is called on the client-side (i.e. the reseller-side) and needs to create {@link Article}s
	 * (or specific decendents of the class {@link Article}) on the vendor-side. It must not persist them on
	 * the client-side or do anything else except for creating them on the vendor-side and returning them.
	 *
	 * @param user The user who is creating the package-article and thus responsible for creating the nested (remote) <code>Product</code>s.
	 * @param localPackageProduct The {@link Product} into which the nested products are put after having been obtained from the vendor.
	 * @param localArticle The local {@link Article} which is a convenience (could be read from {@link Product#getProductLocal()} and
	 *		{@link ProductLocal#getArticle()}).
	 * @param partnerOrganisationID The ID of the vendor-organisation.
	 * @param partnerInitialContextProperties This parameter is passed for convenience and performance reasons. This <code>Hashtable</code> contains
	 *		initial-context-properties for communication with the vendor-organisation and can be passed to xdoclet-generated Util-classes - for
	 *		example {@link TradeManagerUtil#getHome(Hashtable)}.
	 * @param partnerOffer The vendor's (= business partner's) {@link Offer} into which the new {@link Article}s shall be created.
	 * @param partnerOfferID This parameter is passed for convenience and performance reasons. It's the result of
	 *		{@link JDOHelper#getObjectId(Object)} applied to <code>partnerOffer</code>.
	 * @param partnerSegmentID For every {@link SegmentType} of the local {@link Order}, a Segment is created in the vendor's {@link Order}. This is the ID
	 *		of the {@link Segment} in the partner's Order that has been created for the <code>SegmentType</code> returned by <code>localArticle.getSegment().getSegmentType()</code>.
	 * @param nestedProductType The {@link NestedProductType}s passed as parameter <code>nestedProductTypes</code> are grouped by {@link ProductType} as returned
	 *		by {@link NestedProductType#getInnerProductType()}. This instance is then passed to this method here.
	 * @param nestedProductTypes The {@link NestedProductType}s for whose inner-{@link ProductType}s the {@link Article}s shall be created.
	 * @return the {@link Article}s which have been created by the vendor and detached from the vendor's organisation's datastore. They must not yet be
	 *		attached locally - this is done by the framework.
	 * @throws Exception As this method communicates with a remote server and calls implementation-specific methods, it can throw a wide variety of exceptions.
	 */
	public abstract Collection<? extends Article> createCrossTradeArticles(
			User user, Product localPackageProduct, Article localArticle,
			String partnerOrganisationID, Hashtable partnerInitialContextProperties,
			Offer partnerOffer, OfferID partnerOfferID, SegmentID partnerSegmentID,
			ProductType nestedProductType, Collection<NestedProductType> nestedProductTypes) throws Exception;


	/**
	 * This method is called by {@link Trader#allocateArticlesEnd(User user, Collection articles)} and
	 * must ensure that the product is fully allocated and assembled.
	 * <p>
	 * This method DOES NOT set the allocated status in {@link ProductLocal} - this
	 * is done by the {@link Trader}.
	 * </p>
	 *
	 * @param user The user who is responsible.
	 * @param product The product to be assembled.
	 * @throws ModuleException
	 */
	public void assembleProduct(User user, ProductTypeActionHandlerCache productTypeActionHandlerCache, Product product)
	throws ModuleException
	{
		ProductLocal productLocal = product.getProductLocal();
		if (productLocal.isAssembled())
			return; // nothing to do

		Trader trader = Trader.getTrader(getPersistenceManager());
		Store store = trader.getStore();

		if (!product.getOrganisationID().equals(trader.getOrganisationID())) {
			// remote product => this should never happen, because every foreign product should arrive here in assembled form
			throw new UnsupportedOperationException("Foreign product should always be assembled!");
		}

		// we assemble the Product recursively

		// key: Anchor home
		// value: Set<Product> products
		Map nestedProductsByHome = new HashMap();

		// All nestedProductTypes that come from a partner-organisation are collected and grouped in this map in order
		// to import them more efficiently
//		Map<String, List<NestedProductType>> organisationID2partnerNestedProductType = new HashMap<String, List<NestedProductType>>();
		Map organisationID2partnerNestedProductType = null; // lazy creation
// The above generic notation causes the class Product to be destroyed during enhancement with BCEL + JPOX-Enhancer. This results
// in the following exception when afterwards enhancing JFireSimpleTrade (which extends the class Product):
//		[jpoxenhancer] Exception in thread "main" java.lang.ClassFormatError: LVTT entry for 'me' in class file org/nightlabs/jfire/store/Product does not match any LVT entry
//		[jpoxenhancer] at java.lang.ClassLoader.defineClass1(Native Method)
//		[jpoxenhancer] at java.lang.ClassLoader.defineClass(ClassLoader.java:620)
//		[jpoxenhancer] at java.security.SecureClassLoader.defineClass(SecureClassLoader.java:124)
//		[jpoxenhancer] at java.net.URLClassLoader.defineClass(URLClassLoader.java:260)
//		[jpoxenhancer] at java.net.URLClassLoader.access$100(URLClassLoader.java:56)
//		[jpoxenhancer] at java.net.URLClassLoader$1.run(URLClassLoader.java:195)
//		[jpoxenhancer] at java.security.AccessController.doPrivileged(Native Method)
//		[jpoxenhancer] at java.net.URLClassLoader.findClass(URLClassLoader.java:188)

		// local product => create/find nested products
		ProductType productType = product.getProductType();
		for (Iterator itNPT = productType.getNestedProductTypes().iterator(); itNPT.hasNext(); ) {
			NestedProductType nestedProductType = (NestedProductType) itNPT.next();

			if (product.getOrganisationID().equals(nestedProductType.getInnerProductTypeOrganisationID())) {
				ProductLocator productLocator = product.getProductLocator(user, nestedProductType);

				// nested productType is our own, so we can just package it without buying it from someone else.
				Collection nestedProducts = store.findProducts(user, null, nestedProductType, productLocator);
				if (nestedProducts == null || nestedProducts.size() != nestedProductType.getQuantity())
					throw new NotAvailableException("The product '"+product.getPrimaryKey()+"' cannot be assembled, because the nested ProductType '"+nestedProductType.getInnerProductTypePrimaryKey()+"' could not find available products!");

				for (Iterator itNestedProducts = nestedProducts.iterator(); itNestedProducts.hasNext(); ) {
					Product nestedProduct = (Product)itNestedProducts.next();
					ProductLocal nestedProductLocal = nestedProduct.getProductLocal();
//					productLocal.addNestedProductLocal(nestedProductLocal);
					productTypeActionHandlerCache.getProductTypeActionHandler(nestedProduct).assembleProduct(user, productTypeActionHandlerCache, nestedProduct);
//					nestedProduct.assemble(user);
					productLocal.addNestedProductLocal(nestedProductLocal);
					nestedProductLocal.decQuantity();

					// We need to transfer the nested product from wherever it is to the same repository as the
					// package-product and then update productLocal.quantity
					// To reduce transfers, we group them by source-repository (dest is the same for all nested products)
					// dest: product.productType.productTypeLocal.home
					// source: nestedProduct.productType.productTypeLocal.home
					Anchor nestedProductHome = nestedProduct.getProductType().getProductTypeLocal().getHome();
					Set nestedProductSet = (Set) nestedProductsByHome.get(nestedProductHome);
					if (nestedProductSet == null) {
						nestedProductSet = new HashSet();
						nestedProductsByHome.put(nestedProductHome, nestedProductSet);
					}
					nestedProductSet.add(nestedProduct);
				}
			}
			else {
				if (organisationID2partnerNestedProductType == null)
					organisationID2partnerNestedProductType = new HashMap();
				
				// nested productType is coming from a remote organisation and must be acquired from there
				// this means: an Offer must be created (or a previously created one used) and an Article be added
				// we group them in order to make it more efficient
				List partnerNestedProductTypes = (List) organisationID2partnerNestedProductType.get(nestedProductType.getInnerProductTypeOrganisationID());
				if (partnerNestedProductTypes == null) {
					partnerNestedProductTypes = new ArrayList<NestedProductType>();
					organisationID2partnerNestedProductType.put(nestedProductType.getInnerProductTypeOrganisationID(), partnerNestedProductTypes);
				}
				partnerNestedProductTypes.add(nestedProductType);
			}
		} // for (Iterator itNPT = productType.getNestedProductTypes().iterator(); itNPT.hasNext(); ) {

//		for (Iterator<Map.Entry<String, List<NestedProductType>>> itPNPT = organisationID2partnerNestedProductType.entrySet().iterator(); itPNPT.hasNext(); ) {
		if (organisationID2partnerNestedProductType != null) {
			for (Iterator itPNPT = organisationID2partnerNestedProductType.entrySet().iterator(); itPNPT.hasNext(); ) {
	//			Map.Entry<String, List<NestedProductType>> me = (Entry<String, List<NestedProductType>>) itPNPT.next();
				Map.Entry me = (Map.Entry) itPNPT.next();
				String organisationID = (String) me.getKey();
				List nestedProductTypes = (List) me.getValue();
				Collection articlesWithNestedProducts = importCrossTradeNestedProducts(user, productTypeActionHandlerCache, product, organisationID, nestedProductTypes);
//				(user, this, organisationID, nestedProductTypes);

				// dest: product.productType.productTypeLocal.home
				// source: nestedProduct.productType.productTypeLocal.home
				for (Iterator itArticle = articlesWithNestedProducts.iterator(); itArticle.hasNext(); ) {
					Article articleWithNestedProduct = (Article) itArticle.next();
					Product nestedProduct = articleWithNestedProduct.getProduct();
					ProductLocal nestedProductLocal = nestedProduct.getProductLocal();

					productLocal.addNestedProductLocal(nestedProductLocal);
					nestedProductLocal.decQuantity();

					Anchor nestedProductHome = nestedProduct.getProductType().getProductTypeLocal().getHome();
					Set nestedProductSet = (Set) nestedProductsByHome.get(nestedProductHome);
					if (nestedProductSet == null) {
						nestedProductSet = new HashSet();
						nestedProductsByHome.put(nestedProductHome, nestedProductSet);
					}
					nestedProductSet.add(nestedProduct);
				}
			}
		}
		// TODO are the ProductTransfers for the foreign products already created correctly?

		PersistenceManager pm = getPersistenceManager();

		// create the ProductTransfers for the grouped nested products
		Set involvedAnchors = new HashSet();
		LinkedList productTransfers = new LinkedList();
		boolean failed = true;
		try {
			Anchor thisProductHome = productType.getProductTypeLocal().getHome();
			for (Iterator it = nestedProductsByHome.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry me = (Map.Entry) it.next();
				Anchor nestedProductHome = (Anchor) me.getKey();
				Set nestedProducts = (Set) me.getValue();
				// transfer from nested to this
				if (!thisProductHome.getPrimaryKey().equals(nestedProductHome.getPrimaryKey())) {
					ProductTransfer productTransfer = new ProductTransfer(null, user, nestedProductHome, thisProductHome, nestedProducts);
					productTransfer = (ProductTransfer) pm.makePersistent(productTransfer);
					productTransfer.bookTransfer(user, involvedAnchors);
					productTransfers.add(productTransfer);
				}
			}

			// and finally check the integrity of the involved anchors after all the transfers
			Anchor.checkIntegrity(productTransfers, involvedAnchors);

			failed = false;
		} finally {
			if (failed)
				Anchor.resetIntegrity(productTransfers, involvedAnchors);
		}

		productLocal.setAssembled(true);
	}

	public Collection<? extends Article> importCrossTradeNestedProducts(
			User user, ProductTypeActionHandlerCache productTypeActionHandlerCache, Product packageProduct,
			String partnerOrganisationID, Collection<NestedProductType> partnerNestedProductTypes)
	{
		try {
			PersistenceManager pm = getPersistenceManager();
			Article localArticle = packageProduct.getProductLocal().getArticle();
			Order localOrder = localArticle.getOrder();
			Offer localOffer = localArticle.getOffer();
			SegmentType segmentType = localArticle.getSegment().getSegmentType();

			Set segmentTypeIDsWithTheCurrentInstanceOnly = new HashSet();
			segmentTypeIDsWithTheCurrentInstanceOnly.add(JDOHelper.getObjectId(segmentType));

			// TODO we should remove the anchorTypeID from the following method's parameter list
			OrganisationLegalEntity partner = OrganisationLegalEntity.getOrganisationLegalEntity(pm, partnerOrganisationID, OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION, true);

			Hashtable initialContextProperties = Lookup.getInitialContextProperties(pm, partnerOrganisationID);
			TradeManager tradeManager = TradeManagerUtil.getHome(initialContextProperties).create();

//			Set segmentTypeIDs = Segment.getSegmentTypeIDs(pm, localOrder);

			// for the current order, we create/find an instance of OrderRequirement
			OrderRequirement orderRequirement = OrderRequirement.getOrderRequirement(pm, localOrder);
			Order partnerOrder = orderRequirement.getPartnerOrder(partner);
			OrderID partnerOrderID;
			SegmentID partnerSegmentID;
			if (partnerOrder == null) {
				Order order = tradeManager.createCrossTradeOrder(null, // TODO should we somehow configure the orderIDPrefix on this side? I don't think so. Marco.
						localOrder.getCurrency().getCurrencyID(),
						null, // TODO we should find out and pass the CustomerGroupID
//						segmentTypeIDs);
						segmentTypeIDsWithTheCurrentInstanceOnly);
				partnerOrder = (Order) pm.makePersistent(order);
				orderRequirement.addPartnerOrder(partnerOrder);
				partnerOrderID = (OrderID) JDOHelper.getObjectId(partnerOrder);
				partnerSegmentID = Segment.getSegmentIDs(pm, partnerOrder, segmentType).iterator().next();
			}
			else {
				partnerOrderID = (OrderID) JDOHelper.getObjectId(partnerOrder);
				Set segmentIDs = Segment.getSegmentIDs(pm, partnerOrder, segmentType);
				if (segmentIDs.isEmpty()) {
					Collection segments = tradeManager.createCrossTradeSegments(partnerOrderID, segmentTypeIDsWithTheCurrentInstanceOnly);
					segments = pm.makePersistentAll(segments);
					segmentIDs = NLJDOHelper.getObjectIDSet(segments);
				}
				partnerSegmentID = (SegmentID) segmentIDs.iterator().next();
//				Set partnerSegmentTypeIDs = Segment.getSegmentTypeIDs(pm, partnerOrder);
//				if (!segmentTypeIDs.equals(partnerSegmentTypeIDs))
//					tradeManager.createCrossTradeSegments(partnerOrderID, segmentTypeIDs);
			}

			// for the current offer, we create/find an instance of OfferRequirement
			OfferRequirement offerRequirement = OfferRequirement.createOfferRequirement(pm, localOffer);
			Offer partnerOffer = offerRequirement.getPartnerOffer(partner);
			if (partnerOffer == null) {
				{
					Offer offer = tradeManager.createCrossTradeOffer(partnerOrderID, null); // we don't pass the offerIDPrefix - or should we?
					new OfferLocal(offer);
					partnerOffer = (Offer) pm.makePersistent(offer);
					offerRequirement.addPartnerOffer(partnerOffer);
				}

				ProcessDefinitionAssignment processDefinitionAssignment = (ProcessDefinitionAssignment) getPersistenceManager().getObjectById(
						ProcessDefinitionAssignmentID.create(Offer.class, TradeSide.customer));
				processDefinitionAssignment.createProcessInstance(null, user, partnerOffer);
			}
			OfferID partnerOfferID = (OfferID) JDOHelper.getObjectId(partnerOffer);

//			ProductTypeID[] productTypeIDs = new ProductTypeID[partnerNestedProductTypes.size()];
//			int[] quantities = new int[partnerNestedProductTypes.size()];
//			ProductLocator[] productLocators = new ProductLocator[partnerNestedProductTypes.size()];
//
//			int idx = 0;
//			for (NestedProductType partnerNestedProductType : partnerNestedProductTypes) {
//				ProductLocator productLocator = packageProduct.getProductLocator(user, partnerNestedProductType);
//				productTypeIDs[idx] = (ProductTypeID) JDOHelper.getObjectId(partnerNestedProductType.getInnerProductType());
//				quantities[idx] = partnerNestedProductType.getQuantity();
//				productLocators[idx] = productLocator;
//				++idx;
//			}
//
//			Map<Integer, Collection<? extends Article>> articleMap = tradeManager.createCrossTradeArticles(partnerOfferID, productTypeIDs, quantities, productLocators);

			Map productType2NestedProductTypes = new HashMap();
			for (NestedProductType partnerNestedProductType : partnerNestedProductTypes) {
				Collection nestedProductTypes = (Collection) productType2NestedProductTypes.get(partnerNestedProductType.getInnerProductType());
				if (nestedProductTypes == null) {
					nestedProductTypes = new ArrayList();
					productType2NestedProductTypes.put(partnerNestedProductType.getInnerProductType(), nestedProductTypes);
				}
				nestedProductTypes.add(partnerNestedProductType);
			}

			Collection resultArticles = null;
			Store store = Store.getStore(pm);

			for (Iterator itME = productType2NestedProductTypes.entrySet().iterator(); itME.hasNext();) {
				Map.Entry me = (Map.Entry) itME.next();
				ProductType productType = (ProductType) me.getKey();
				Collection nestedProductTypes = (Collection) me.getValue();
//				ProductTypeActionHandler productTypeActionHandler = ProductTypeActionHandler.getProductTypeActionHandler(pm, productType.getClass());

				ProductTypeActionHandler productTypeActionHandler = productTypeActionHandlerCache.getProductTypeActionHandler(productType);
				Collection articles = productTypeActionHandler.createCrossTradeArticles(
						user, packageProduct, localArticle,
						partnerOrganisationID, initialContextProperties,
						partnerOffer, partnerOfferID, partnerSegmentID,
						productType, nestedProductTypes);

				articles = pm.makePersistentAll(articles);

				if (resultArticles == null)
					resultArticles = new ArrayList(articles);
				else
					resultArticles.addAll(articles);

				for (Iterator itA = articles.iterator(); itA.hasNext();) {
					Article article = (Article) itA.next();
					article.createArticleLocal(user);

					ProductType articleProductType = article.getProductType();
					if (articleProductType == null)
						throw new IllegalStateException("article.getProductType() == null for imported Article: " + article.getPrimaryKey());

					ProductTypeLocal articleProductTypeLocal = articleProductType.getProductTypeLocal();
					if (articleProductTypeLocal == null)
						throw new IllegalStateException("article.getProductType().getProductTypeLocal() == null for imported Article (" + article.getPrimaryKey() + "). ProductType: " + articleProductType.getPrimaryKey());

//					Product product = store.addProduct(user, article.getProduct(), articleProductTypeLocal.getHome());
					Product product = store.addProduct(user, article.getProduct());
					product.getProductLocal().setAssembled(true); // since we receive it from another organisation, we always assume that it's assembled.
//					product.getProductLocal().setArticle(article); // pointing back to the article which delivers it to us - NO! we do not point back, because the same product[Local] can be in multiple Articles (when being reversed - and maybe even resold and again reversed and so on) - it's cleaner to find the Article by a query - see the static method in class Article!
				}
			}

			pm.flush();

			if (resultArticles == null) // can this ever happen?!
				resultArticles = new ArrayList(0);

			return resultArticles;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param user The responsible user
	 * @param onRelease Whether this method is called during the release method. Hence, a Product
	 *		might decide not to disassemble, if this is <code>true</code>.
	 */
	public void disassembleProduct(User user, ProductTypeActionHandlerCache productTypeActionHandlerCache, Product product, boolean onRelease)
	{
		ProductLocal productLocal = product.getProductLocal();
		if (!productLocal.isAssembled())
			return; // nothing to do

		PersistenceManager pm = getPersistenceManager();

		Trader trader = Trader.getTrader(getPersistenceManager());

		if (!product.getOrganisationID().equals(trader.getOrganisationID())) {
			// remote product => cannot be disassembled, but must be returned instead
			throw new UnsupportedOperationException("Foreign product cannot be disassembled!");
		}

		// key: Anchor home
		// value: Set<Product> products
		Map nestedProductsByHome = new HashMap();

		for (Iterator it = product.getProductLocal().getNestedProductLocals().iterator(); it.hasNext(); ) {
			ProductLocal nestedProductLocal = (ProductLocal) it.next();
			Product nestedProduct = nestedProductLocal.getProduct();

			if (product.getOrganisationID().equals(nestedProduct.getOrganisationID())) {
				// local nested product => disassemble
				nestedProductLocal.incQuantity();
				productTypeActionHandlerCache.getProductTypeActionHandler(nestedProduct).disassembleProduct(user, productTypeActionHandlerCache, nestedProduct, onRelease);
//				nestedProduct.disassemble(user, onRelease);
			}
			else {
				// remote nested product => return to the source organisation

				// TODO allow remote products - delegate to Trader!
				throw new UnsupportedOperationException("NYI");
			}

			// We need to transfer the nested product back to its home repositories and update productLocal.quantity
			// To reduce transfers, we group them by dest-repository (source is the same for all nested products)
			// source: product.productType.productTypeLocal.home
			// dest nestedProduct.productType.productTypeLocal.home
			Anchor nestedProductHome = nestedProduct.getProductType().getProductTypeLocal().getHome();
			Set nestedProducts = (Set) nestedProductsByHome.get(nestedProductHome);
			if (nestedProducts == null) {
				nestedProducts = new HashSet();
				nestedProductsByHome.put(nestedProductHome, nestedProducts);
			}
			nestedProducts.add(nestedProduct);
		}

		// create the ProductTransfers for the grouped nested products
		Set involvedAnchors = new HashSet();
		LinkedList productTransfers = new LinkedList();
		boolean failed = true;
		try {
	
			Anchor thisProductHome = product.getProductType().getProductTypeLocal().getHome();
			for (Iterator it = nestedProductsByHome.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry me = (Map.Entry) it.next();
				Anchor nestedProductHome = (Anchor) me.getKey();
				Set nestedProducts = (Set) me.getValue();
				// transfer from this to nested
				if (!thisProductHome.getPrimaryKey().equals(nestedProductHome.getPrimaryKey())) {
					ProductTransfer productTransfer = new ProductTransfer(null, user, thisProductHome, nestedProductHome, nestedProducts);
					productTransfer = (ProductTransfer) pm.makePersistent(productTransfer);
					productTransfer.bookTransfer(user, involvedAnchors);
					productTransfers.add(productTransfer);
				}
			}

			// and finally check the integrity of the involved anchors after all the transfers
			Anchor.checkIntegrity(productTransfers, involvedAnchors);

			failed = false;
		} finally {
			if (failed)
				Anchor.resetIntegrity(productTransfers, involvedAnchors);
		}

		productLocal.removeAllNestedProductLocals();
		productLocal.setAssembled(false);
	}

	/**
	 * This method is called by {@link Trader#allocateArticlesBegin(User, Collection)}
	 * when an Article is allocated. You can extend this method
	 * in order to do sth. when allocation starts. The basic implementation in
	 * <code>ProductTypeActionHandler</code> is a noop.
	 * <p>
	 * Note, that you better implement your work in {@link #onAllocateArticlesEnd(User, Trader, Collection)}, because
	 * that method is normally called asynchronously (and thus, expensive work is not such a problem).
	 * </p>
	 * @param user The user who initiated this action.
	 * @param trader The trader.
	 * @param articles The {@link Article}s that are being allocated.
	 */
	public void onAllocateArticlesBegin(User user, Trader trader, Collection<? extends Article> articles)
	{
	}

	/**
	 * This method is called by {@link Trader#allocateArticlesEnd(User, Collection)}
	 * when an Article is allocated. You can extend this method
	 * in order to do sth. when allocation ends. The basic implementation in
	 * <code>ProductTypeActionHandler</code> is a noop.
	 * <p>
	 * As {@link Trader#allocateArticlesEnd(User, Collection)} is normally called asynchronously,
	 * you should do expensive things here (and not in {@link #onAllocateArticlesBegin(User, Trader, Collection)}).
	 * But still, you should try to get your work done as fast as possible ;-)
	 * </p>
	 * @param user The user who initiated this action.
	 * @param trader The trader.
	 * @param articles The {@link Article}s that are being allocated.
	 */
	public void onAllocateArticlesEnd(User user, Trader trader, Collection<? extends Article> articles)
	{
	}

	/**
	 * This method is called by {@link Trader#releaseArticlesBegin(User, Collection)}.
	 * The basic implementation in
	 * <code>ProductTypeActionHandler</code> is a noop.
	 *
	 * @param user The user who initiated this action.
	 * @param trader The trader.
	 * @param articles The {@link Article}s that are being released.
	 */
	public void onReleaseArticlesBegin(User user, Trader trader, Collection<? extends Article> articles)
	{
	}

	/**
	 * This method is called by {@link Trader#releaseArticlesEnd(User, Collection)}.
	 * The basic implementation in
	 * <code>ProductTypeActionHandler</code> is a noop.
	 *
	 * @param user The user who initiated this action.
	 * @param trader The trader.
	 * @param articles The {@link Article}s that are being released.
	 */
	public void onReleaseArticlesEnd(User user, Trader trader, Collection<? extends Article> articles)
	{
	}

	/**
	 * This method is called by {@link Accounting#addArticlesToInvoice(User, org.nightlabs.jfire.accounting.Invoice, Collection)}. 
	 *
	 * @param user The user who initiated this action.
	 * @param accounting The Accounting instance.
	 * @param invoice The invoice to which articles are added. This is the same as {@link Article#getInvoice()}.
	 * @param articles The {@link Article}s that are being added to the invoice.
	 */
	public void onAddArticlesToInvoice(User user, Accounting accounting, Invoice invoice, Collection<? extends Article> articles)
	{		
	}

	/**
	 * This method is called by {@link Store#addArticlesToDeliveryNote(User, DeliveryNote, Collection)}.
	 *
	 * @param user The user who initiated this action.
	 * @param store The Store instance.
	 * @param deliveryNote The delivery note to which articles are added. This is the same as {@link Article#getDeliveryNote()}.
	 * @param articles The {@link Article}s that are added.
	 */
	public void onAddArticlesToDeliveryNote(User user, Store store, DeliveryNote deliveryNote, Collection<? extends Article> articles)
	{
	}

	/**
	 * This method is called by {@link DeliveryHelperBean#deliverBegin_storeDeliverBeginServerResult(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean, String[], int)}
	 * at the end of its action. You should not cause any exception here as this will cause the <code>DeliveryResult</code> not to be written and
	 * this situation is not handled.
	 *
	 * @param principal The user who initiated this action. If you need a {@link User} instance, call {@link User#getUser(PersistenceManager, org.nightlabs.jfire.base.JFireBasePrincipal)}.
	 * @param delivery The currently performed delivery. If you need the {@link DeliveryData}, you can use {@link PersistenceManager#getObjectById(Object)} with {@link DeliveryDataID#create(org.nightlabs.jfire.store.deliver.id.DeliveryID)}
	 * @param articles The articles containing {@link ProductType} implementations that are linked to this <code>ProductTypeActionHandler</code>.
	 */
	public void onDeliverBegin_storeDeliverBeginServerResult(JFirePrincipal principal, Delivery delivery, Set<? extends Article> articles)
	{
	}
	/**
	 * This method is called by {@link DeliveryHelperBean#deliverDoWork_storeDeliverDoWorkServerResult(org.nightlabs.jfire.store.deliver.id.DeliveryID, org.nightlabs.jfire.store.deliver.DeliveryResult, boolean, String[], int)} at the end of its action.
	 *
	 * @param principal The user who initiated this action. If you need a {@link User} instance, call {@link User#getUser(PersistenceManager, org.nightlabs.jfire.base.JFireBasePrincipal)}.
	 * @param delivery The currently performed delivery. If you need the {@link DeliveryData}, you can use {@link PersistenceManager#getObjectById(Object)} with {@link DeliveryDataID#create(org.nightlabs.jfire.store.deliver.id.DeliveryID)}
	 * @param articles The articles containing {@link ProductType} implementations that are linked to this <code>ProductTypeActionHandler</code>.
	 */
	public void onDeliverDoWork_storeDeliverDoWorkServerResult(JFirePrincipal principal, Delivery delivery, Set<? extends Article> articles)
	{
	}
	/**
	 * This method is called by {@link Store#deliverEnd(User, DeliveryData)} at the end of its action.
	 *
	 * @param principal The user who initiated this action. If you need a {@link User} instance, call {@link User#getUser(PersistenceManager, org.nightlabs.jfire.base.JFireBasePrincipal)}.
	 * @param delivery The currently performed delivery. If you need the {@link DeliveryData}, you can use {@link PersistenceManager#getObjectById(Object)} with {@link DeliveryDataID#create(org.nightlabs.jfire.store.deliver.id.DeliveryID)}
	 * @param articles The articles containing {@link ProductType} implementations that are linked to this <code>ProductTypeActionHandler</code>.
	 */
	public void onDeliverEnd_storeDeliverEndServerResult(JFirePrincipal principal, Delivery delivery, Set<? extends Article> articles)
	{
	}

	/**
	 * Create/return an existing repository which is used to put a newly created product into it.
	 *
	 * @param product The new product before it is added to the store (it has no {@link ProductLocal} assigned yet).
	 * @return the repository
	 */
	public Repository getInitialRepository(Product product)
	{
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);

		if (store.getOrganisationID().equals(product.getOrganisationID()))
			return getInitialLocalRepository(product);
		else
			return getInitialForeignRepository(product);
	}

	protected String getAnchorIDForLocalHomeRepository(ProductType productType)
	{
		return productType.getClass().getName() + ".home";  
	}

	protected String getAnchorIDForForeignHomeRepository(ProductType productType)
	{
		return productType.getClass().getName() + ".home#" + productType.getOrganisationID();  
	}

	protected Repository getInitialLocalRepository(Product product)
	{
		return getDefaultHomeRepository(product.getProductType());
	}

	/**
	 * This method is called by {@link Store#addProductType(User, ProductType)} in order to assign the default
	 * value for {@link ProductTypeLocal#getHome()}.
	 * <p>
	 * Furthermore, it is called by {@link #getInitialLocalRepository(Product)} since the initial repository
	 * for local products is the same as the home (while it is different for foreign products, which first need to
	 * be delivered from their initial repository to their home).
	 * </p>
	 *
	 * @param productType The <code>ProductType</code> for which to determine the default home.
	 * @return the home repository
	 */
	public Repository getDefaultHomeRepository(ProductType productType)
	{
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);

		return Repository.createRepository(
				pm,
				store.getOrganisationID(),
				Repository.ANCHOR_TYPE_ID_HOME,
				store.getOrganisationID().equals(productType.getOrganisationID()) ?
						getAnchorIDForLocalHomeRepository(productType) : getAnchorIDForForeignHomeRepository(productType),
				store.getMandator(), false);
	}

	protected Repository getInitialForeignRepository(Product product)
	{
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);

		LegalEntity repositoryOwner = OrganisationLegalEntity.getOrganisationLegalEntity(
				pm, product.getOrganisationID(), OrganisationLegalEntity.ANCHOR_TYPE_ID_ORGANISATION, true);

		return PartnerStorekeeper.createPartnerOutsideRepository(pm, store.getOrganisationID(), repositoryOwner);
//		Store store = Store.getStore(pm);
//
//		// local (i.e. produced here)
//		return Repository.createRepository(
//				pm,
//				store.getOrganisationID(),
//				Repository.ANCHOR_TYPE_,
//				ANCHOR_ID_REPOSITORY_HOME_LOCAL,
//				store.getMandator(), true);		
	}



//	public CrossTradeDeliveryCoordinator getCrossTradeDeliveryCoordinator()
//	{
//		PersistenceManager pm = getPersistenceManager();
//
//		CrossTradeDeliveryCoordinatorID id = CrossTradeDeliveryCoordinatorID.create(Organisation.DEVIL_ORGANISATION_ID, CrossTradeDeliveryCoordinator.class.getName());
//		try {
//			CrossTradeDeliveryCoordinator ctdc = (CrossTradeDeliveryCoordinator) pm.getObjectById(id);
//			ctdc.getModeOfDeliveryFlavour();
//			return ctdc;
//		} catch (JDOObjectNotFoundException x) {
//			CrossTradeDeliveryCoordinator ctdc = new CrossTradeDeliveryCoordinator(id.organisationID, id.crossTradeDeliveryCoordinatorID);
//
//			ModeOfDeliveryFlavour modeOfDeliveryFlavour = (ModeOfDeliveryFlavour) pm.getObjectById(ModeOfDeliveryConst.MODE_OF_DELIVERY_FLAVOUR_ID_JFIRE);
//			ctdc.setModeOfDeliveryFlavour(modeOfDeliveryFlavour);
//
//			ServerDeliveryProcessor serverDeliveryProcessor = ServerDeliveryProcessorJFire.getServerDeliveryProcessorJFire(pm);
//			ctdc.setServerDeliveryProcessor(serverDeliveryProcessor);
//
//			ctdc = (CrossTradeDeliveryCoordinator) pm.makePersistent(ctdc);
//			return ctdc;
//		}
//	}

//	/**
//	 * This method is called by {@link PaymentHelperBean#payBegin_storePayBeginServerResult(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean, String[], int)}
//	 * at the end of its action. You should not cause any exception here as this will cause the <code>PaymentResult</code> not to be written and
//	 * this situation is not handled.
//	 * <p>
//	 * Note, that payments are only very loosely coupled to the articles as you can do many payments for one or more {@link Invoice}s
//	 * (thus allowing partial payments or instalment sales).
//	 * </p>
//	 *
//	 * @param principal The user who initiated this action. If you need a {@link User} instance, call {@link User#getUser(PersistenceManager, org.nightlabs.jfire.base.JFireBasePrincipal)}.
//	 * @param payment The currently performed payment. If you need the {@link PaymentData}, you can use {@link PersistenceManager#getObjectById(Object)} with {@link PaymentDataID#create(org.nightlabs.jfire.accounting.pay.id.PaymentID)}
//	 * @param articles The articles containing {@link ProductType} implementations that are linked to this <code>ProductTypeActionHandler</code>.
//	 */
//	public void onPayBegin_storePayBeginServerResult(JFirePrincipal principal, Payment payment, Set<? extends Article> articles)
//	{
//	}
//	/**
//	 * This method is called by {@link PaymentHelperBean#payDoWork_storePayDoWorkServerResult(org.nightlabs.jfire.accounting.pay.id.PaymentID, org.nightlabs.jfire.accounting.pay.PaymentResult, boolean, String[], int)} at the end of its action.
//	 *
//	 * @param principal The user who initiated this action. If you need a {@link User} instance, call {@link User#getUser(PersistenceManager, org.nightlabs.jfire.base.JFireBasePrincipal)}.
//	 * @param payment The currently performed payment. If you need the {@link PaymentData}, you can use {@link PersistenceManager#getObjectById(Object)} with {@link PaymentDataID#create(org.nightlabs.jfire.accounting.pay.id.PaymentID)}
//	 * @param articles The articles containing {@link ProductType} implementations that are linked to this <code>ProductTypeActionHandler</code>.
//	 */
//	public void onPayDoWork_storePayDoWorkServerResult(JFirePrincipal principal, Payment payment, Set<? extends Article> articles)
//	{
//	}
//	/**
//	 * This method is called by {@link Store#payEnd(User, PaymentData)} at the end of its action.
//	 *
//	 * @param principal The user who initiated this action. If you need a {@link User} instance, call {@link User#getUser(PersistenceManager, org.nightlabs.jfire.base.JFireBasePrincipal)}.
//	 * @param payment The currently performed payment. If you need the {@link PaymentData}, you can use {@link PersistenceManager#getObjectById(Object)} with {@link PaymentDataID#create(org.nightlabs.jfire.accounting.pay.id.PaymentID)}
//	 * @param articles The articles containing {@link ProductType} implementations that are linked to this <code>ProductTypeActionHandler</code>.
//	 */
//	public void onPayEnd_storePayEndServerResult(JFirePrincipal principal, Payment payment, Set<? extends Article> articles)
//	{
//	}

	@Override
	public int hashCode()
	{
		return Util.hashCode(organisationID) ^ Util.hashCode(productTypeActionHandlerID);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;

		if (!(obj instanceof ProductTypeActionHandler))
			return false;

		ProductTypeActionHandler other = (ProductTypeActionHandler) obj;

		return
				Util.equals(this.organisationID, other.organisationID) &&
				Util.equals(this.productTypeActionHandlerID, other.productTypeActionHandlerID);
	}

}
