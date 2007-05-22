package org.nightlabs.jfire.dynamictrade.store;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 * 
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.Product"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProduct"
 *
 * @jdo.inheritance strategy="new-table"
 */
public class DynamicProduct
extends Product
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="dynamicProduct" dependent="true"
	 */
	private DynamicProductName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private double quantity;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Unit unit;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	private Price singlePrice;

	/**
	 * @deprecated Only for JDO!
	 */
	protected DynamicProduct() { }

	public DynamicProduct(ProductType productType, long productID)
	{
		super(productType, productID);
		this.name = new DynamicProductName(this);
		this.quantity = 1;
	}

	@Implement
	public ProductLocator getProductLocator(User user,
			NestedProductType nestedProductType)
	{
		throw new UnsupportedOperationException("Not necessary! Why is this method called?");
	}

	/**
	 * The single price is the price calculated by the {@link DynamicTradePriceConfig}. Therefore it does not take the quantity
	 * into account. The quantity is incorporated into the {@link ArticlePrice} of the corresponding {@link Article}.
	 *
	 * @return the price for one single <code>Product</code> - i.e. without the quantity ({@link #getQuantity()}) taken into account.
	 */
	public Price getSinglePrice()
	{
		return singlePrice;
	}
	public void setSinglePrice(Price singlePrice)
	{
		this.singlePrice = singlePrice;
	}

	public DynamicProductName getName()
	{
		return name;
	}

	public double getQuantity()
	{
		return quantity;
	}
	public void setQuantity(double quantity)
	{
		this.quantity = quantity;
	}

	public Unit getUnit()
	{
		return unit;
	}
	public void setUnit(Unit unit)
	{
		this.unit = unit;
	}
}
