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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.nightlabs.jdo.ObjectIDUtil;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.transfer.Anchor;

/**
 * An instance of this class holds data related to the product, but different in each
 * datastore. 
 *
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.store.id.ProductLocalID"
 *		detachable="true"
 *		table="JFireTrade_ProductLocal"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, productID"
 *
 * @jdo.fetch-group name="ProductLocal.saleArticle" fields="saleArticle"
 */
public class ProductLocal
implements Serializable
{
	private static final long serialVersionUID = 1L;

	// TODO create fetch-groups for all object-fields
	public static final String FETCH_GROUP_ARTICLE = "ProductLocal.saleArticle";

//	/**
//	 * This method creates a new instance of <code>ProductLocal</code> if it is necessesary. If there exists
//	 * already one for the given <code>product</code>, the previously created one will be returned.
//	 *
//	 * @param pm The PersistenceManager for accessing the datastore.
//	 * @param product The Product
//	 * @param initialRepository The anchor in which the Product arrives first.
//	 * @return
//	 */
//	public static ProductLocal createProductLocal(PersistenceManager pm, User user, Product product, Repository initialRepository)
//	{
//		Store store = Store.getStore(pm);
//		String organisationID = store.getOrganisationID();
//		ProductLocal res = getProductLocal(pm, organisationID, product, false);
//		if (res == null) {
//			res = new ProductLocal(user, store, product, initialRepository);
//			pm.makePersistent(res);
//		}
//		return res;
//	}

//	public static ProductLocal getProductLocal(PersistenceManager pm, Product product, boolean throwExceptionIfNotFound)
//	{
//		return getProductLocal(pm, product, throwExceptionIfNotFound);
//	}
//
//	protected static ProductLocal getProductLocal(PersistenceManager pm, String organisationID, Product product, boolean throwExceptionIfNotFound)
//	{
//		pm.getExtent(ProductLocal.class);
//		try {
//			return (ProductLocal) pm.getObjectById(ProductLocalID.create(product.getOrganisationID(), product.getProductID()));
//		} catch (JDOObjectNotFoundException x) {
//			if (throwExceptionIfNotFound)
//				throw x;
//		}
//		return null;
//	}

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long productID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Product product;

	/**
	 * A <tt>Product</tt> can only be allocated in one saleArticle. But all Articles hold
	 * a reference of the <tt>Product</tt> even after they have been released (to allow
	 * re-allocation).
	 * <p>
	 * When <code>saleArticle</code> is set, {@link #allocationPending} is set to <code>true</code>.
	 * </p>
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Article saleArticle = null;

	/**
	 * This field references the article by which a product has been purchased. It therefore references
	 * the same article as {@link #saleArticle} does in the {@link ProductLocal} on the vendor-side.
	 */
	private Article purchaseArticle = null;

	/**
	 * @see #isAvailable()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean available = false;

	/**
	 * @see #isAllocationPending()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean allocationPending = false;

	/**
	 * @see #isReleasePending()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean releasePending = false;

	/**
	 * A <tt>Product</tt> is allocated when all packaged products are either already in
	 * the local store or allocated in backhand orders and therefore themselves
	 * ready for being sold in the package of this <tt>Product</tt>. In short: a Product is
	 * always allocated, if the {@link Article} referenced by {@link #saleArticle} is allocated.
	 * <p>
	 * Note, that assembling a Product can be done asynchronously. Therefore a Product
	 * can have the state {@link #allocationPending}.
	 * </p>
	 *
	 * @see #isAllocated()
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean allocated = false;

	/**
	 * A Product needs to be assembled before it can be put into a {@link DeliveryNote}
	 * or {@link org.nightlabs.jfire.accounting.Invoice}.
	 * <p>
	 * The assembling can be done asynchronously. It is started while allocating.
	 * </p>
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean assembled = false;

	/**
	 * This is used for the {@link #nestedProductLocals} as mapped-by, which means
	 * that this is null if this ProductLocal is not nested within another or
	 * it points to the one in which it is nested.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private ProductLocal packageProductLocal = null;

	/**
	 * This is <code>true</code>, if {@link #packageProductLocal}<code> != null</code>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean nested = false;

	/**
	 * key: String primaryKey(organisationID + '/' + productTypeID)<br/>
	 * value: Product product
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="ProductLocal"
	 *		mapped-by="packageProductLocal"
	 *
	 * @!jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="java.lang.String"
	 *		value-type="Product"
	 *		table="JFireTrade_ProductLocal_nestedProductLocals"
	 *
	 * @!jdo.join
	 */
	private Set<ProductLocal> nestedProductLocals = new HashSet<ProductLocal>();

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductLocal() { }

	public ProductLocal(User user, Product _product, Repository initialRepository)
	{
		this.organisationID = _product.getOrganisationID();
		this.productID = _product.getProductID();
		this.product = _product;

		this.setAnchor(initialRepository);
		if (initialRepository.getRepositoryType().isOutside())
			this.quantity = 0;
		else
			this.quantity = 1;

		product.setProductLocal(this);
		updateAvailable();
//		this.hollow = !initialRepository.getOwner().getPrimaryKey().equals(
//				store.getMandator().getPrimaryKey());

//		this.setCurrentStatus(
//				new ProductStatus(this, createStatusID(), user,
//						ProductStatus.STATUS_BLOCKED));
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getProductID()
	{
		return productID;
	}
	public Product getProduct()
	{
		return product;
	}

	/**
	 * Specifies, where the real product currently is. If the real product has not been delivered yet,
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Anchor anchor;

	/**
	 * A product having a quantity <= 0 means that the product here in this store is only virtual and
	 * basically means the same as if it's not here. This is necessary, because we need to have
	 * the object for satisfying links in orders, offers etc. already before the product is really here
	 * and after it has been reversed.
	 * <p>
	 * It is a counter instead of a flag, because a not-yet-here product might even be delivered already
	 * (asynchronously, before it arrives). In this case, the quantity might even become -1. If
	 * quantity == 1, the product is really in this store.
	 * </p>
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private int quantity = 0;

	/**
	 * @return Returns usually a {@link Repository}, but during a transaction, this might be a
	 *		{@link org.nightlabs.jfire.trade.LegalEntity}, too.
	 */
	public Anchor getAnchor()
	{
		return anchor;
	}
	public void setAnchor(Anchor anchor)
	{
		if (anchor == null)
			throw new IllegalArgumentException("anchor must not be null!");

		this.anchor = anchor;
//		this.hollow = (anchor instanceof Repository) && ((Repository)anchor).isOutside();
	}

	public int getQuantity()
	{
		return quantity;
	}
	public int incQuantity(int val)
	{
		quantity = quantity + val;
		return quantity;
	}
	public int incQuantity()
	{
		quantity = quantity + 1;
		return quantity;
	}
	public int decQuantity()
	{
		quantity = quantity - 1;
		return quantity;
	}

//	/**
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private ProductStatus currentStatus;

//	/**
//	 * List of ProductStatus. The last item is always identical with currentStatus.
//	 *
//	 * @jdo.field
//	 *		persistence-modifier="persistent"
//	 *		collection-type="collection"
//	 *		element-type="ProductStatus"
//	 *		mapped-by="productStatusTracker"
//	 *		dependent="true"
//	 */
//	protected List statusHistory = new ArrayList();

	public Collection<ProductLocal> getNestedProductLocals()
	{
		return Collections.unmodifiableCollection(nestedProductLocals);
	}

	public Collection<ProductLocal> getNestedProductLocals(boolean includeSelfForVirtualSelfPackaging)
	{
		if (!includeSelfForVirtualSelfPackaging || getProduct().getProductType().getInnerPriceConfig() == null)
			return getNestedProductLocals();
		else {
			HashSet<ProductLocal> res = new HashSet<ProductLocal>(nestedProductLocals);
			res.add(this);
			return Collections.unmodifiableCollection(res);
		}
	}

	protected String getPrimaryKey()
	{
		return organisationID + '/' + ObjectIDUtil.longObjectIDFieldToString(productID);
	}

	public void addNestedProductLocal(ProductLocal productLocal)
	{
		if (productLocal.getPackageProductLocal() != null)
			throw new IllegalArgumentException("The ProductLocal \""+productLocal.getPrimaryKey()+"\" is already nested within \""+productLocal.packageProductLocal.getPrimaryKey()+"\"!");

		productLocal.decQuantity();
		productLocal.setPackageProductLocal(this);
		nestedProductLocals.add(productLocal);
	}

	public void removeAllNestedProductLocals()
	{
		for (Iterator<ProductLocal> it = nestedProductLocals.iterator(); it.hasNext(); ) {
			ProductLocal productLocal = (ProductLocal) it.next();
			productLocal.setPackageProductLocal(null);
			productLocal.incQuantity();
			it.remove();
		}
	}

	public void setPackageProductLocal(ProductLocal packageProductLocal)
	{
		this.packageProductLocal = packageProductLocal;
		this.setNested(packageProductLocal != null);
	}

	protected void setNested(boolean nested)
	{
		this.nested = nested;
		updateAvailable();
	}

	/**
	 * A product is only available, if it is neither {@link #nested} nor {@link #allocated}
	 * nor {@link #allocationPending}. Nested means that another Product has "consumed" it
	 * while being assembled.
	 *
	 * @return Returns <code>true</code>, if this <code>ProductLocal</code> is nested within another one. This means,
	 *		it is equal to {@link #packageProductLocal}<code> != null</code>.
	 *
	 * @see #getPackageProductLocal()
	 * @see #isAllocated()
	 */
	public boolean isNested()
	{
		return nested;
	}

	/**
	 * @return Returns <code>null</code>, if this ProductLocal is stand-alone. If it is nested, this method
	 *		returns the <code>ProductLocal</code>, in which it is nested.
	 *
	 * @see #isNested()
	 */
	public ProductLocal getPackageProductLocal()
	{
		return packageProductLocal;
	}

//	public Collection getNestedProducts()
//	{
//		return Collections.unmodifiableCollection(nestedProducts.values());
//	}
//
//	public Collection getNestedProducts(boolean includeSelfForVirtualSelfPackaging)
//	{
//		if (!includeSelfForVirtualSelfPackaging || getProduct().getProductType().getInnerPriceConfig() == null)
//			return getNestedProducts();
//		else {
//			HashSet res = new HashSet(nestedProducts.values());
//			res.add(this);
//			return Collections.unmodifiableCollection(res);
//		}
//	}
//
//	public void addNestedProduct(Product product)
//	{
//		nestedProducts.put(product.getPrimaryKey(), product);
//	}
//
//	public void removeNestedProduct(Product product)
//	{
//		nestedProducts.remove(product.getPrimaryKey());
//	}
//
//	public void removeAllNestedProducts()
//	{
//		nestedProducts.clear();
//	}

	/**
	 * Get the currently assigned <code>Article</code>. This is always a "normal" saleArticle - never a reversing one.
	 * <p>
	 * </p>
	 *
	 * @return Returns the saleArticle or <code>null</code>.
	 * @see #setSaleArticle(Article)
	 */
	public Article getSaleArticle()
	{
		return saleArticle;
	}
	/**
	 * This method simply sets the field {@link #saleArticle}. It does not do any complex
	 * logic (like iterating nested products or contacting other organisations).
	 *
	 * @param saleArticle The saleArticle to set. This is always a "normal" saleArticle - never a reversing saleArticle.
	 * @see #getSaleArticle()
	 */
	public void setSaleArticle(Article saleArticle)
	{
		if (saleArticle != null && saleArticle.isReversing())
			throw new IllegalArgumentException("saleArticle must not be reversing!");

		this.saleArticle = saleArticle;
	}

	public Article getPurchaseArticle()
	{
		return purchaseArticle;
	}
	public void setPurchaseArticle(Article purchaseArticle)
	{
		if (purchaseArticle != null && purchaseArticle.isReversing())
			throw new IllegalArgumentException("purchaseArticle must not be reversing!");

		this.purchaseArticle = purchaseArticle;
	}

	public boolean isAssembled()
	{
		return assembled;
	}
	public void setAssembled(boolean assembled)
	{
		this.assembled = assembled;
	}
	/**
	 * @return Returns <code>true</code>, if the Product is not yet allocated
	 *		({@link #isAllocated()}<code> == false</code>), but the user
	 *		has already performed the first phase of the allocation process via
	 *		{@link org.nightlabs.jfire.trade.Trader#allocateArticleBegin(User, Article)}.
	 *
	 * @see #isAllocated()
	 */
	public boolean isAllocationPending()
	{
		return allocationPending;
	}
	public void setAllocationPending(boolean allocationPending)
	{
		if (allocationPending && allocated)
			throw new IllegalStateException("This ProductLocal ("+getPrimaryKey()+") is already allocated! Cannot set allocationPending = true!");

		if (allocationPending && releasePending)
			throw new IllegalStateException("This ProductLocal ("+getPrimaryKey()+") has status releasePending! Cannot set allocationPending = true!");

		this.allocationPending = allocationPending;

		updateAvailable();
	}
	/**
	 * @return Returns whether this <code>Product</code> is currently allocated within an {@link Article}.
	 *		Only top-level-<code>Product</code>s are allocated; the inner products are <code>nested</code>. They
	 *		can only be <code>allocated</code>, if they come from another organisation. But in this case, they
	 *		are allocated within another <code>Article</code>: The one which was used to aquire the
	 *		foreign <code>Product</code>.
	 *
	 * @see #isAllocationPending()
	 * @see #isReleasePending()
	 * @see #isNested()
	 */
	public boolean isAllocated()
	{
		return allocated;
	}

	/**
	 * This method is called indirectly by {@link Product#setAllocated(boolean)}.
	 */
	public void setAllocated(boolean allocated)
	{
		this.allocated = allocated;

		if (allocated && allocationPending)
			setAllocationPending(false);

		if (!allocated && releasePending)
			setReleasePending(false);

		updateAvailable();
	}

	protected void updateAvailable()
	{
		available = !allocated && !allocationPending && !nested && quantity > 0;
	}

	/**
	 * @return Returns <code>true</code>, if the product is neither
	 *		{@link #allocated} nor {@link #allocationPending} nor {@link nested}.
	 */
	public boolean isAvailable()
	{
		return available;
	}

	/**
	 * @return Returns <code>true</code>, if the Product is not yet released
	 *		({@link #isAllocated()}<code> == true</code>), but the user
	 *		has already performed the first phase of the release process via
	 *		{@link org.nightlabs.jfire.trade.Trader#releaseArticleBegin(User, Article)}.
	 *
	 * @see #isAllocated()
	 */
	public boolean isReleasePending()
	{
		return releasePending;
	}
	public void setReleasePending(boolean releasePending)
	{
		this.releasePending = releasePending;
	}

}
