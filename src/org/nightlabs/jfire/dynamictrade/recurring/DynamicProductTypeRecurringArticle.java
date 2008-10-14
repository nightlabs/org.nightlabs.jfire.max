package org.nightlabs.jfire.dynamictrade.recurring;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.Tariff;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.store.Unit;
import org.nightlabs.jfire.store.id.UnitID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.Segment;

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
 *
 */
public class DynamicProductTypeRecurringArticle extends Article {


	public static final String FETCH_GROUP_DYNAMIC_PRODUCT_TYPE_RECURRING_ARTICLE_NAME = "DynamicProductTypeRecurringArticle.name";

	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;


	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected DynamicProductTypeRecurringArticle() {}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private long quantity;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Unit unit;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private DynamicProductTypeRecurringArticleName 	name;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Price singlePrice;
	

	
	public DynamicProductTypeRecurringArticle(User user, Offer offer, Segment segment, long articleID, ProductType productType, Tariff tariff)
	{
		super(user, offer, segment, articleID, productType, null, tariff);
	}


	public long getQuantity() {
		return quantity;
	}


	public void setQuantity(long quantity) {
		this.quantity = quantity;
	}


	public Unit getUnit() {
		return unit;
	}


	public UnitID getUnitID() {
		return (UnitID) JDOHelper.getObjectId(unit);
	}
	
	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public DynamicProductTypeRecurringArticleName getName() {
		return name;
	}


	public void setName(DynamicProductTypeRecurringArticleName name) {
		this.name = name;
	}


	public Price getSinglePrice() {
		return singlePrice;
	}


	public void setSinglePrice(Price singlePrice) {
		this.singlePrice = singlePrice;
	}



}
