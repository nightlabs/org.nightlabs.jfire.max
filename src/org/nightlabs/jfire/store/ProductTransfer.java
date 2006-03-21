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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.TransferRegistry;

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
 */
public class ProductTransfer extends Transfer implements Serializable
{
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
	 *
	 * @jdo.join
	 *
	 * @!jdo.key-column length="130"
	 *
	 * @!jdo.map-vendor-extension vendor-name="jpox" key="key-length" value="max 130"
	 */
	private Set products = null;

	/**
	 * @deprecated Only for JDO!
	 */
	protected ProductTransfer() { }

	/**
	 * @param transferRegistry
	 * @param from
	 * @param to
	 * @param container
	 * @param products
	 */
	public ProductTransfer(
			TransferRegistry transferRegistry, Transfer container, User initiator,
			Anchor from, Anchor to, Collection products)
	{
		super(transferRegistry, TRANSFERTYPEID, container, initiator, from, to);
		this.products = new HashSet(products);
//		this.products = new HashMap();
//
//		// WORKAROUND begin
//		Product p = (Product) products.iterator().next();
//		this.products.put(p.getPrimaryKey(), p);
//		this.products.clear();
//		// WORKAROUND There is a problem in JPOX which causes the Map to ignore the first entry otherwise
//
//		for (Iterator it = products.iterator(); it.hasNext(); ) {
//			Product product = (Product)it.next();
//			this.products.put(product.getPrimaryKey(), product);
//		}
	}

	/**
	 * @return Returns instances of {@link Product}. You MUST NOT manipulate the returned collection!
	 */
	public Collection getProducts()
	{
		return products;
	}

	public void bookTransfer(User user, Map<String, Anchor> involvedAnchors)
	{
		// TODO We must - either here or somewhere else - ensure that all the nested products are transferred, too (and have the same repository as the package product afterwards).
		super.bookTransfer(user, involvedAnchors);
	}
}
