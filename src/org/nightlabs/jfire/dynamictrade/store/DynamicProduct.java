package org.nightlabs.jfire.dynamictrade.store;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dynamictrade.DynamicProductInfo;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.BundleProduct;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocator;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Unit;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDynamicTrade_DynamicProduct")
@FetchGroups({
	@FetchGroup(
		name=DynamicProduct.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=DynamicProduct.FETCH_GROUP_UNIT,
		members=@Persistent(name="unit")),
	@FetchGroup(
		name=DynamicProduct.FETCH_GROUP_SINGLE_PRICE,
		members=@Persistent(name="singlePrice")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOrderEditor",
		members={@Persistent(name="name"), @Persistent(name="unit"), @Persistent(name="singlePrice")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInOfferEditor",
		members={@Persistent(name="name"), @Persistent(name="unit"), @Persistent(name="singlePrice")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members={@Persistent(name="name"), @Persistent(name="unit"), @Persistent(name="singlePrice")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members={@Persistent(name="name"), @Persistent(name="unit"), @Persistent(name="singlePrice")}),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsTrade.articleInReceptionNoteEditor",
		members={@Persistent(name="name"), @Persistent(name="unit"), @Persistent(name="singlePrice")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DynamicProduct
extends Product
implements BundleProduct, DynamicProductInfo
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_NAME = "DynamicProduct.name";
	public static final String FETCH_GROUP_UNIT = "DynamicProduct.unit";
	public static final String FETCH_GROUP_SINGLE_PRICE = "DynamicProduct.singlePrice";

	/**
	 * @jdo.field persistence-modifier="persistent" mapped-by="dynamicProduct" dependent="true"
	 */
	@Persistent(
		dependent="true",
		mappedBy="dynamicProduct",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private DynamicProductName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private long quantity;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Unit unit;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true"
	 */
	@Persistent(
		dependent="true",
		persistenceModifier=PersistenceModifier.PERSISTENT)
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
	public ProductLocator getProductLocator(User user,
			NestedProductTypeLocal nestedProductTypeLocal)
	{
		throw new UnsupportedOperationException("Not necessary! Why is this method called?");
	}

	@Override
	public Price getSinglePrice()
	{
		return singlePrice;
	}
	@Override
	public void setSinglePrice(Price singlePrice)
	{
		this.singlePrice = singlePrice;
	}

	public DynamicProductName getName()
	{
		return name;
	}

	/**
	 * Get the quantity as floating point number. This is a convenience method equal to
	 * calling {@link #getUnit()} and {@link Unit#toDouble(long)}.
	 *
	 * @return the quantity in double form - i.e. with shifted decimal digits.
	 */
	@Override
	public double getQuantityAsDouble()
	{
		return unit.toDouble(quantity);
	}
	@Override
	public long getQuantity()
	{
		return quantity;
	}
	@Override
	public void setQuantity(long quantity)
	{
		this.quantity = quantity;
	}
	@Override
	public Unit getUnit()
	{
		return unit;
	}
	@Override
	public void setUnit(Unit unit)
	{
		this.unit = unit;
	}
}
