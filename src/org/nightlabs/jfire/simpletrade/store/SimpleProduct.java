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

package org.nightlabs.jfire.simpletrade.store;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.Product"
 *		detachable="true"
 *		table="JFireSimpleTrade_SimpleProduct"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class SimpleProduct extends Product
{
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SimpleProduct() { }

	/**
	 * @see Product#Product(ProductType, long)
	 */
	public SimpleProduct(ProductType productType, long productID)
	{
		super(productType, productID);
	}

	/**
	 * This implementation returns <code>null</code>, because <code>SimpleProduct</code>s don't support
	 * {@link ProductLocator}s.
	 *
	 * @see org.nightlabs.jfire.store.Product#getProductLocator(User, NestedProductTypeLocal)
	 */
	@Override
	public ProductLocator getProductLocator(User user, NestedProductTypeLocal nestedProductTypeLocal)
	{
		return null;
	}

}
