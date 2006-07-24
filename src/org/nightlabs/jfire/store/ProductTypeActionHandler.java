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

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.accounting.Accounting;
import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Trader;
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
	 * @param productType TODO
	 * @param nestedProductType This will be <code>null</code> if the top-level product shall be found/created.
	 * @param productLocator A specialized Object defining for YOUR implementation of <code>ProductType</code> which <code>Product</code> to find.
	 * @return Return either <code>null</code> if no suitable <code>Product</code>s can be allocated or a <code>Collection</code> of <code>Product</code>.
	 */
	public abstract Collection<Product> findProducts(
			User user, ProductType productType, NestedProductType nestedProductType, ProductLocator productLocator);

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
	public void onAllocateArticlesBegin(User user, Trader trader, Collection<Article> articles)
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
	public void onAllocateArticlesEnd(User user, Trader trader, Collection<Article> articles)
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
	public void onReleaseArticlesBegin(User user, Trader trader, Collection<Article> articles)
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
	public void onReleaseArticlesEnd(User user, Trader trader, Collection<Article> articles)
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
	public void onAddArticlesToInvoice(User user, Accounting accounting, Invoice invoice, Collection<Article> articles)
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
	public void onAddArticlesToDeliveryNote(User user, Store store, DeliveryNote deliveryNote, Collection<Article> articles)
	{
	}

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
