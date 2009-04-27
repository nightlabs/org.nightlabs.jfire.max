package org.nightlabs.jfire.dynamictrade.recurring;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.dynamictrade.DynamicProductInfo;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Unit;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Segment;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 *
 * @author Fitas Amine <fitas@nightlabs.de>
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.trade.Article"
 *		detachable="true"
 *		table="JFireDynamicTrade_DynamicProductTypeRecurringArticle"
 *
 * @jdo.inheritance strategy="new-table"
 *
 *
 * @jdo.fetch-group name="DynamicProductTypeRecurringArticle.name" fields="name"
 * @jdo.fetch-group name="DynamicProductTypeRecurringArticle.singlePrice" fields="singlePrice"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="quantity, unit, name, singlePrice"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="quantity, unit, name, singlePrice"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireDynamicTrade_DynamicProductTypeRecurringArticle")
@FetchGroups({
	@FetchGroup(
		name=DynamicProductTypeRecurringArticle.FETCH_GROUP_DYNAMIC_PRODUCT_TYPE_RECURRING_ARTICLE_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=DynamicProductTypeRecurringArticle.FETCH_GROUP_DYNAMIC_PRODUCT_TYPE_RECURRING_ARTICLE_SINGLEPRICE,
		members=@Persistent(name="singlePrice")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOrderEditor",
		members={@Persistent(name="quantity"), @Persistent(name="unit"), @Persistent(name="name"), @Persistent(name="singlePrice")}),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOfferEditor",
		members={@Persistent(name="quantity"), @Persistent(name="unit"), @Persistent(name="name"), @Persistent(name="singlePrice")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class DynamicProductTypeRecurringArticle extends Article implements DynamicProductInfo {


	public static final String FETCH_GROUP_DYNAMIC_PRODUCT_TYPE_RECURRING_ARTICLE_NAME = "DynamicProductTypeRecurringArticle.name";
	public static final String FETCH_GROUP_DYNAMIC_PRODUCT_TYPE_RECURRING_ARTICLE_SINGLEPRICE = "DynamicProductTypeRecurringArticle.singlePrice";

	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductTypeRecurringArticle() {}

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
	 * @jdo.field persistence-modifier="persistent" mapped-by="dynamicProductTypeRecurringArticle"
	 */
	@Persistent(
		mappedBy="dynamicProductTypeRecurringArticle",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private DynamicProductTypeRecurringArticleName 	name;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Price singlePrice;
	
	public DynamicProductTypeRecurringArticle(User user, Offer offer, Segment segment, long articleID, ProductType productType, Tariff tariff)
	{
		super(user, offer, segment, articleID, productType, null, tariff);
		this.name = new DynamicProductTypeRecurringArticleName(this);
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getQuantity()
	 */
	public long getQuantity() {
		return quantity;
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#setQuantity(long)
	 */
	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getUnit()
	 */
	public Unit getUnit() {
		return unit;
	}


	public UnitID getUnitID() {
		return (UnitID) JDOHelper.getObjectId(unit);
	}
	
	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#setUnit(org.nightlabs.jfire.store.Unit)
	 */
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public DynamicProductTypeRecurringArticleName getName() {
		return name;
	}


	public void setName(DynamicProductTypeRecurringArticleName name) {
		this.name = name;
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#getSinglePrice()
	 */
	public Price getSinglePrice() {
		return singlePrice;
	}


	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.dynamictrade.recurring.DynamicProductInfo#setSinglePrice(org.nightlabs.jfire.accounting.Price)
	 */
	public void setSinglePrice(Price singlePrice) {
		this.singlePrice = singlePrice;
	}

	@Override
	public double getQuantityAsDouble()
	{
		return unit.toDouble(quantity);
	}


}
