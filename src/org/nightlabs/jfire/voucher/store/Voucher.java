package org.nightlabs.jfire.voucher.store;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocal;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Repository;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.Product"
 *		detachable="true"
 *		table="JFireVoucher_Voucher"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class Voucher
extends Product
{
	private static final long serialVersionUID = 1L;

	public Voucher(ProductType productType, long productID)
	{
		super(productType, productID);
	}

	/**
	 * This implementation returns <code>null</code>, because <code>Voucher</code>s don't support
	 * {@link ProductLocator}s (they don't support nesting either - a Voucher is only valid within
	 * one organisation).
	 *
	 * @see org.nightlabs.jfire.store.Product#getProductLocator(User, NestedProductType)
	 */
	@Implement
	public ProductLocator getProductLocator(User user, NestedProductType nestedProductType)
	{
		return null;
	}

	@Override
	protected ProductLocal createProductLocal(User user, Repository initialRepository)
	{
		return new VoucherLocal(user, this, initialRepository);
	}
}
