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

import java.util.Collection;
import java.util.Hashtable;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.base.JFirePrincipal;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.DeliveryData;
import org.nightlabs.jfire.store.deliver.DeliveryHelperBean;
import org.nightlabs.jfire.store.deliver.id.DeliveryDataID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Order;
import org.nightlabs.jfire.trade.Segment;
import org.nightlabs.jfire.trade.SegmentType;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.Trader;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.SegmentID;
import org.nightlabs.util.Utils;

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
		return Utils.hashCode(organisationID) ^ Utils.hashCode(productTypeActionHandlerID);
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
				Utils.equals(this.organisationID, other.organisationID) &&
				Utils.equals(this.productTypeActionHandlerID, other.productTypeActionHandlerID);
	}

}
