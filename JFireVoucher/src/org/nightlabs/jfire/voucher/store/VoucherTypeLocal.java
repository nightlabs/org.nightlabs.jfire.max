package org.nightlabs.jfire.voucher.store;

import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductTypeLocal"
 *		detachable="true"
 *		table="JFireVoucher_VoucherTypeLocal"
 *
 * @jdo.inheritance strategy="new-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherTypeLocal")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class VoucherTypeLocal
extends ProductTypeLocal
{
	private static final long serialVersionUID = 1L;

	/**
	 * If <tt>maxVoucherCount</tt> has a value <tt>&gt;=0</tt>, this is the maximum number of
	 * <tt>Product</tt>s that can be created and sold, To have an unlimited amount of
	 * <tt>Product</tt>s available, set this to <tt>-1</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long maxVoucherCount = -1;

	/**
	 * Keeps track, how many <tt>Product</tt>s have already been created. If this number
	 * reaches <tt>maxVoucherCount</tt> and <tt>maxVoucherCount</tt> is a positive number,
	 * the {@link VoucherTypeActionHandler} will stop to create new <tt>SimpleProduct</tt>s.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long createdVoucherCount = 0;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherTypeLocal() { }

	public VoucherTypeLocal(User user, ProductType productType)
	{
		super(user, productType);
	}

	/**
	 * @return Returns the createdVoucherCount.
	 */
	public long getCreatedVoucherCount()
	{
		return createdVoucherCount;
	}
	/**
	 * @param createdVoucherCount The createdVoucherCount to set.
	 */
	public void setCreatedVoucherCount(long createdProductCount)
	{
		this.createdVoucherCount = createdProductCount;
	}
	/**
	 * @return Returns the maxVoucherCount.
	 */
	public long getMaxVoucherCount()
	{
		return maxVoucherCount;
	}
	/**
	 * @param maxVoucherCount The maxVoucherCount to set.
	 */
	public void setMaxVoucherCount(long maxProductCount)
	{
		this.maxVoucherCount = maxProductCount;
	}

	@Override
	public FieldMetaData getFieldMetaData(String fieldName, boolean createMissingMetaData)
	{
		if ("maxVoucherCount".equals(fieldName))
			return null;
		if ("createdVoucherCount".equals(fieldName))
			return null;

		return super.getFieldMetaData(fieldName, createMissingMetaData);
	}
}
