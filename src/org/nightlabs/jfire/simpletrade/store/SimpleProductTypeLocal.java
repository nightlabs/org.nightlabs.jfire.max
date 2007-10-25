package org.nightlabs.jfire.simpletrade.store;

import org.nightlabs.inheritance.FieldMetaData;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.ProductTypeLocal;
import org.nightlabs.jfire.store.Repository;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductTypeLocal"
 *		detachable="true"
 *		table="JFireSimpleTrade_SimpleProductTypeLocal"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class SimpleProductTypeLocal
extends ProductTypeLocal
{
	private static final long serialVersionUID = 1L;

	/**
	 * If <tt>maxProductCount</tt> has a value <tt>&gt;=0</tt>, this is the maximum number of
	 * <tt>Product</tt>s that can be created and sold, To have an unlimited amount of
	 * <tt>Product</tt>s available, set this to <tt>-1</tt>.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long maxProductCount = -1;

	/**
	 * Keeps track, how many <tt>Product</tt>s have already been created. If this number
	 * reaches <tt>maxProductCount</tt> and <tt>maxProductCount</tt> is a positive number,
	 * the {@link SimpleProductTypeActionHandler} will stop to create new <tt>SimpleProduct</tt>s.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long createdProductCount = 0;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected SimpleProductTypeLocal() { }

	public SimpleProductTypeLocal(User user, ProductType productType, Repository home)
	{
		super(user, productType, home);
	}

	/**
	 * @return Returns the createdProductCount.
	 */
	public long getCreatedProductCount()
	{
		return createdProductCount;
	}
	/**
	 * @param createdProductCount The createdProductCount to set.
	 */
	public void setCreatedProductCount(long createdProductCount)
	{
		this.createdProductCount = createdProductCount;
	}
	/**
	 * @return Returns the maxProductCount.
	 */
	public long getMaxProductCount()
	{
		return maxProductCount;
	}
	/**
	 * @param maxProductCount The maxProductCount to set.
	 */
	public void setMaxProductCount(long maxProductCount)
	{
		this.maxProductCount = maxProductCount;
	}

	@Override
	public FieldMetaData getFieldMetaData(String fieldName, boolean createMissingMetaData)
	{
		if ("maxProductCount".equals(fieldName))
			return null;
		if ("createdProductCount".equals(fieldName))
			return null;

		return super.getFieldMetaData(fieldName, createMissingMetaData);
	}

}
