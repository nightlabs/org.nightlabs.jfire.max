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
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

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
 *		name="getProductTypeID2productCount"
 *		query="
 *				SELECT JDOHelper.getObjectId(product.productType), count(product)
 *				WHERE this == :productTransfer && this.products.contains(product)
 *				VARIABLES org.nightlabs.jfire.store.Product product
 *				GROUP BY JDOHelper.getObjectId(product.productType)
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
public class ProductTransfer
extends Transfer
implements Serializable, DetachCallback
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_PRODUCTS = "ProductTransfer.products";

	/**
	 * This is a virtual fetch-group that is handled in the detach-callback.
	 */
	public static final String FETCH_GROUP_PRODUCT_COUNT = "ProductTransfer.productCount";
	/**
	 * This is a virtual fetch-group that is handled in the detach-callback.
	 */
	public static final String FETCH_GROUP_PRODUCT_TYPE_ID_2_PRODUCT_COUNT_MAP = "ProductTransfer.productTypeID2productCountMap";

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
	private Set<Product> products = null;

	/** @jdo.field persistence-modifier="none" */
	private Integer productCount = null;

	/** @jdo.field persistence-modifier="none" */
	private Map<ProductTypeID, Integer> productTypeID2productCountMap = null;

	/**
	 * @deprecated Only for JDO!
	 */
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
		// TODO We must - either here or somewhere else - ensure that all the nested products are transferred, too (and have the same repository as the package product afterwards).
		super.bookTransfer(user, involvedAnchors);
	}

	public int getProductCount()
	{
		if (productCount == null)
			productCount = new Integer(products.size()); // the JDO implementation should optimize this to a simple SELECT count(...) - I haven't tested though, whether it really does. Marco.

		return productCount.intValue();
	}

	@SuppressWarnings("unchecked")
	public Map<ProductTypeID, Integer> getProductTypeID2productCountMap()
	{
		if (productTypeID2productCountMap == null) {
			Map m = new HashMap();

//			// TODO JPOX WORKAROUND begin
//			Query q1 = getPersistenceManager().newNamedQuery(ProductTransfer.class, "WORKAROUND_getProductTypesForProducts");
//			Query q2 = getPersistenceManager().newNamedQuery(ProductTransfer.class, "WORKAROUND_getProductCountForProductType");
//			Collection productTypeIDs = (Collection) q1.execute(this);
//			for (Iterator itPTID = productTypeIDs.iterator(); itPTID.hasNext();) {
//				ProductType productType = (ProductType) itPTID.next();
//				Long count = (Long) q2.execute(this, productType);
//				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(productType);
//				m.put(productTypeID, new Integer(count.intValue()));
//			}
//			// TODO JPOX WORKAROUND end

				// Fuck, the workaround fails too! Will do it very inefficiently:
			for (Product product : products) {
				ProductTypeID productTypeID = (ProductTypeID) JDOHelper.getObjectId(product.getProductType());
				Integer count = (Integer) m.get(productTypeID);

				if (count == null)
					count = new Integer(1);
				else
					count = new Integer(count.intValue() + 1);

				m.put(productTypeID, count);
			}

//			Query q = getPersistenceManager().newNamedQuery(ProductTransfer.class, "getProductTypeID2productCount");
//			Collection res = (Collection) q.execute(this);
//			for (Iterator iterator = res.iterator(); iterator.hasNext();) {
//				Object[] record = (Object[]) iterator.next();
//				m.put(record[0], record[1]);
////				m.put(JDOHelper.getObjectId(record[0]), record[1]);
//			}

			productTypeID2productCountMap = m;
		}

		return productTypeID2productCountMap;
	}

	public void jdoPreDetach()
	{
		// nothing to do
	}

	@SuppressWarnings("unchecked")
	public void jdoPostDetach(Object o)
	{
		ProductTransfer attached = (ProductTransfer) o;
		ProductTransfer detached = this;

		PersistenceManager pm = attached.getPersistenceManager();
		Set fetchGroups = pm.getFetchPlan().getGroups();

		if (attached.productCount != null || fetchGroups.contains(FETCH_GROUP_PRODUCT_COUNT) || fetchGroups.contains(FetchPlan.ALL))
			detached.productCount = attached.getProductCount();

		if (attached.productTypeID2productCountMap != null || fetchGroups.contains(FETCH_GROUP_PRODUCT_TYPE_ID_2_PRODUCT_COUNT_MAP) || fetchGroups.contains(FetchPlan.ALL))
			detached.productTypeID2productCountMap = attached.getProductTypeID2productCountMap();
	}
}
