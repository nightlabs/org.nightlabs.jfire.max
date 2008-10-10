package org.nightlabs.jfire.voucher.store;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
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
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="voucherKey"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="voucherKey"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="voucherKey"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="voucherKey"
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
	 * @see #getVoucherKey()
	 * @jdo.field persistence-modifier="persistent"
	 */
	private VoucherKey voucherKey;

	/**
	 * This implementation returns <code>null</code>, because <code>Voucher</code>s don't support
	 * {@link ProductLocator}s (they don't support nesting either - a Voucher is only valid within
	 * one organisation).
	 *
	 * @see org.nightlabs.jfire.store.Product#getProductLocator(User, NestedProductTypeLocal)
	 */
	@Override
	public ProductLocator getProductLocator(User user, NestedProductTypeLocal nestedProductTypeLocal)
	{
		return null;
	}

	@Override
	protected ProductLocal createProductLocal(User user, Repository initialRepository)
	{
//		return new VoucherLocal(user, this, initialRepository);
		return super.createProductLocal(user, initialRepository);
	}

	/**
	 * @param voucherKey the new voucherKey to set
	 * @see #getVoucherKey()
	 */
	public void setVoucherKey(VoucherKey voucherKey)
	{
		this.voucherKey = voucherKey;
	}

	/**
	 * @return the newest <code>VoucherKey</code> that has been created for this voucher.
	 *		If we would allow the trade of vouchers between organisations, this should probably be in the VoucherLocal
	 */
	public VoucherKey getVoucherKey()
	{
		return voucherKey;
	}

//	@Override
//	public void assemble(User user)
//			throws ModuleException
//	{
//		super.assemble(user);
//
//		VoucherLocal voucherLocal = (VoucherLocal) getProductLocal();
//		voucherLocal.onAssemble(user);
//	}
//
//	@Override
//	public void disassemble(User user, boolean onRelease)
//	{
//		super.disassemble(user, onRelease);
//
//		VoucherLocal voucherLocal = (VoucherLocal) getProductLocal();
//		voucherLocal.onDisassemble(user, onRelease);
//	}
}
