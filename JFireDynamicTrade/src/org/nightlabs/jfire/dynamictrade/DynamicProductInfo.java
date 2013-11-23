package org.nightlabs.jfire.dynamictrade;

import org.nightlabs.i18n.I18nText;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.dynamictrade.accounting.priceconfig.DynamicTradePriceConfig;
import org.nightlabs.jfire.dynamictrade.recurring.DynamicProductTypeRecurringArticle;
import org.nightlabs.jfire.dynamictrade.store.DynamicProduct;
import org.nightlabs.jfire.store.Unit;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * Interface declaring the attributes of a {@link DynamicProduct}. 
 * It might also be implemented by objects that are templates for
 * the creation of a {@link DynamicProduct} (e.g. {@link DynamicProductTypeRecurringArticle}).
 * 
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public interface DynamicProductInfo {
	
	/**
	 * Get the name
	 * @return the name.
	 */
	I18nText getName();
	/**
	 * Get the quantity.
	 * @return the quantity.
	 */
	long getQuantity();
	
	/**
	 * Get the quantity as floating point number. This is a convenience method equal to
	 * calling {@link #getUnit()} and {@link Unit#toDouble(long)}.
	 *
	 * @return the quantity in double form - i.e. with shifted decimal digits.
	 */
	double getQuantityAsDouble();
	/**
	 * Set the quantity.
	 * @param quantity the quantity.
	 */
	void setQuantity(long quantity);
	
	/**
	 * Get the unit.
	 * @return the unit.
	 */
	Unit getUnit();
	/**
	 * Set the unit.
	 * @param unit the unit.
	 */
	void setUnit(Unit unit);

	/**
	 * The single price is the price calculated by the {@link DynamicTradePriceConfig}. Therefore it does not take the quantity
	 * into account. The quantity is incorporated into the {@link ArticlePrice} of the corresponding {@link Article}.
	 *
	 * @return the price for one single <code>Product</code> - i.e. without the quantity ({@link #getQuantityAsDouble()}) taken into account.
	 */
	Price getSinglePrice();
	/**
	 * Get the singlePrice.
	 * @param singlePrice the singlePrice.
	 */
	void setSinglePrice(Price singlePrice);

}