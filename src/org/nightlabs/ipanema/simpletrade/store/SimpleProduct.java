/*
 * Created on Mar 4, 2005
 */
package org.nightlabs.ipanema.simpletrade.store;

import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.store.NestedProductType;
import org.nightlabs.ipanema.store.Product;
import org.nightlabs.ipanema.store.ProductLocator;
import org.nightlabs.ipanema.store.ProductType;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.store.Product"
 *		detachable="true"
 *		table="JFireSimpleTrade_SimpleProduct"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class SimpleProduct extends Product
{

	/**
	 * @deprecated Only for JDO!
	 */
	protected SimpleProduct()
	{
	}

	/**
	 * @param productType
	 * @param productID
	 */
	public SimpleProduct(ProductType productType, long productID)
	{
		super(productType, productID);
	}

	/**
	 * @see org.nightlabs.ipanema.store.Product#getProductLocator(User, NestedProductType)
	 */
	public ProductLocator getProductLocator(User user, NestedProductType nestedProductType)
	{
		return null;
	}

}
