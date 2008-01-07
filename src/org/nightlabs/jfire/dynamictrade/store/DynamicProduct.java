package org.nightlabs.jfire.dynamictrade.store;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
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
 *
 * @jdo.fetch-group name="DynamicProduct.name" fields="name"
 * @jdo.fetch-group name="DynamicProduct.unit" fields="unit"
 * @jdo.fetch-group name="DynamicProduct.singlePrice" fields="singlePrice"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fetch-groups="default" fields="name, unit, singlePrice"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fetch-groups="default" fields="name, unit, singlePrice"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fetch-groups="default" fields="name, unit, singlePrice"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fetch-groups="default" fields="name, unit, singlePrice"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInReceptionNoteEditor" fetch-groups="default" fields="name, unit, singlePrice"
 */
public class DynamicProduct
extends Product
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "DynamicProduct.name";
	public static final String FETCH_GROUP_UNIT = "DynamicProduct.unit";
	public static final String FETCH_GROUP_SINGLE_PRICE = "DynamicProduct.singlePrice";

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
	@Deprecated
	protected DynamicProduct() { }

	public DynamicProduct(ProductType productType, long productID)
	{
		super(productType, productID);
		this.name = new DynamicProductName(this);
		this.quantity = 1;
	}

	@Override
	@Implement
	public ProductLocator getProductLocator(User user,
			NestedProductTypeLocal nestedProductTypeLocal)
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
