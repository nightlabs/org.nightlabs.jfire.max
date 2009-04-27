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
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.transfer.Transfer"
 *		detachable="true"
 *		table="JFireTrade_ProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="ProductTransfer.products" fields="products"
 *
 * @jdo.query
 *		name="getProductType2productCount"
 *		query="
 *				SELECT product.productType, count(product)
 *				WHERE this == :productTransfer && this.products.contains(product)
 *				VARIABLES org.nightlabs.jfire.store.Product product
 *				GROUP BY product.productType
 *		"
 *
 * @jdo.query
 *		name="WORKAROUND_getProductTypesForProducts"
 *		query="
 *				SELECT product.productType
 *				WHERE this == :productTransfer && this.products.contains(product)
 *				VARIABLES org.nightlabs.jfire.store.Product product
 *		"
 * @jdo.query
 *		name="WORKAROUND_getProductCountForProductType"
 *		query="
 *				SELECT count(product.productID)
 *				WHERE
 *						this == :productTransfer && this.products.contains(product) &&
 *						product.productType == :productType
 *				VARIABLES org.nightlabs.jfire.store.Product product
 *		"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ProductTransfer")
@FetchGroups(
	@FetchGroup(
		name=ProductTransfer.FETCH_GROUP_PRODUCTS,
		members=@Persistent(name="products"))
)
@Queries({
	@Query(
		name="getProductType2productCount",
		value=" SELECT product.productType, count(product) WHERE this == :productTransfer && this.products.contains(product) VARIABLES org.nightlabs.jfire.store.Product product GROUP BY product.productType "),
	@Query(
		name="WORKAROUND_getProductTypesForProducts",
		value=" SELECT product.productType WHERE this == :productTransfer && this.products.contains(product) VARIABLES org.nightlabs.jfire.store.Product product "),
	@Query(
		name="WORKAROUND_getProductCountForProductType",
		value=" SELECT count(product.productID) WHERE this == :productTransfer && this.products.contains(product) && product.productType == :productType VARIABLES org.nightlabs.jfire.store.Product product ")
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ProductTransfer
extends Transfer
implements Serializable, DetachCallback
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ProductTransfer.class);

	public static final String FETCH_GROUP_PRODUCTS = "ProductTransfer.products";

	/**
	 * This is a virtual fetch-group that is handled in the detach-callback.
	 */
	public static final String FETCH_GROUP_PRODUCT_COUNT = "ProductTransfer.productCount";
	/**
	 * This is a virtual fetch-group that is handled in the detach-callback.
	 */
	public static final String FETCH_GROUP_PRODUCT_TYPE_ID_2_PRODUCT_COUNT_MAP = "ProductTransfer.productType2productCountMap";

	public static final String TRANSFERTYPEID = "ProductTransfer";

	/**
	 * key: String productPrimaryKey (see {@link Product#getPrimaryKey()})<br/>
	 * value: ProductType product
	 *
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="Product"
	 *		table="JFireTrade_ProductTransfer_products"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_ProductTransfer_products",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<Product> products = null;

	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Integer productCount = null;

	/** @jdo.field persistence-modifier="none" */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Map<ProductType, Integer> productType2productCountMap = null;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ProductTransfer() { }

	public ProductTransfer(
			Transfer container, User initiator,
			Anchor from, Anchor to, Collection<Product> products)
	{
		super(TRANSFERTYPEID, container, initiator, from, to);
		this.products = new HashSet<Product>(products);
	}

	/**
	 * @return Returns instances of {@link Product}. You MUST NOT manipulate the returned collection!
	 */
	public Collection<Product> getProducts()
	{
		return Collections.unmodifiableCollection(products);
	}

	@Override
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		if (logger.isDebugEnabled()) {
			logger.debug("bookTransfer: this.pk=\"" + getPrimaryKey() + "\" from.pk=\"" + (getFrom() == null ? null : getFrom().getPrimaryKey()) + "\"" + " to.pk=\"" + (getTo() == null ? null : getTo().getPrimaryKey()) + "\" productCount=\"" + productCount + "\"");
		}

		// TODO We must - either here or somewhere else - ensure that all the nested products are transferred, too (and have the same repository as the package product afterwards).
		super.bookTransfer(user, involvedAnchors);

		// hmmm... I'm not sure, maybe it's better to leave them were they are (i.e. only track till they are used in a "factory" to "produce" another product).
		// Before changing sth. here, we should think about it again. Marco.
	}

	public int getProductCount()
	{
		if (productCount == null)
			productCount = new Integer(products.size()); // the JDO implementation should optimize this to a simple SELECT count(...) - I haven't tested though, whether it really does. Marco.

		return productCount.intValue();
	}

	public Map<ProductType, Integer> getProductType2productCountMap()
	{
		if (productType2productCountMap == null) {
			Map<ProductType, Integer> m = new HashMap<ProductType, Integer>();

//			// TODO JPOX WORKAROUND begin
//			Query q1 = getPersistenceManager().newNamedQuery(ProductTransfer.class, "WORKAROUND_getProductTypesForProducts");
//			Query q2 = getPersistenceManager().newNamedQuery(ProductTransfer.class, "WORKAROUND_getProductCountForProductType");
//			Collection<?> productTypeIDs = (Collection<?>) q1.execute(this);
//			for (Iterator<?> itPTID = productTypeIDs.iterator(); itPTID.hasNext();) {
//				ProductType productType = (ProductType) itPTID.next();
//				Long count = (Long) q2.execute(this, productType);
//				m.put(productType, new Integer(count.intValue()));
//			}
//			// TODO JPOX WORKAROUND end

			// Fuck, the workaround fails too! Will do it very inefficiently:
			// I hope, https://www.jfire.org/modules/bugs/view.php?id=233 is fixed soon.
			for (Product product : products) {
				ProductType productType = product.getProductType();
				Integer count = m.get(productType);

				if (count == null)
					count = new Integer(1);
				else
					count = new Integer(count.intValue() + 1);

				m.put(productType, count);
			}

//			Query q = getPersistenceManager().newNamedQuery(ProductTransfer.class, "getProductType2productCount");
//			Collection<?> res = (Collection<?>) q.execute(this);
//			for (Iterator<?> iterator = res.iterator(); iterator.hasNext();) {
//				Object[] record = (Object[]) iterator.next();
//				m.put((ProductType)record[0], new Integer(((Number)record[1]).intValue()));
////				m.put(JDOHelper.getObjectId(record[0]), record[1]);
//			}

			productType2productCountMap = m;
		}

		return productType2productCountMap;
	}

	@Override
	public void jdoPreDetach()
	{
		// nothing to do
	}

	@Override
	@SuppressWarnings("unchecked")
	public void jdoPostDetach(Object o)
	{
		ProductTransfer attached = (ProductTransfer) o;
		ProductTransfer detached = this;

		PersistenceManager pm = attached.getPersistenceManager();
		Set fetchGroups = pm.getFetchPlan().getGroups();

		if (attached.productCount != null || fetchGroups.contains(FETCH_GROUP_PRODUCT_COUNT) || fetchGroups.contains(FetchPlan.ALL))
			detached.productCount = attached.getProductCount();

		if (attached.productType2productCountMap != null || fetchGroups.contains(FETCH_GROUP_PRODUCT_TYPE_ID_2_PRODUCT_COUNT_MAP) || fetchGroups.contains(FetchPlan.ALL)) {
			Map<ProductType, Integer> m = attached.getProductType2productCountMap();
			detached.productType2productCountMap = new HashMap<ProductType, Integer>(m.size());
			for (Map.Entry<ProductType, Integer> me : m.entrySet()) {
				detached.productType2productCountMap.put(
						pm.detachCopy(me.getKey()),
						me.getValue()
				);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation checks if the ProductTransfer has a container. If so the description
	 * of the container is returned, otherwise
	 * </p>
	 */
	@Override
	protected String internalGetDescription() {
		if (getContainer() != null)
			return getContainer().getDescription();
		return String.format(
				"ProductTransfer from %s to %s",
				getFrom().getDescription(), getTo().getDescription()
			);
	}

}
