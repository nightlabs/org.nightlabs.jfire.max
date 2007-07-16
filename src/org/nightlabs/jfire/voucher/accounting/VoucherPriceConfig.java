package org.nightlabs.jfire.voucher.accounting;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.priceconfig.PriceConfig"
 *		detachable="true"
 *		table="JFireVoucher_VoucherPriceConfig"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="VoucherPriceConfig.prices" fields="prices"
 *
 * @jdo.fetch-group name="FetchGroupsPriceConfig.edit" fetch-groups="default" fields="prices"
 */
public class VoucherPriceConfig
extends PriceConfig
implements IPackagePriceConfig
{
	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="map"
	 *		key-type="Currency"
	 *		value-type="Long"
	 *		table="JFireVoucher_VoucherPriceConfig_prices"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Map<Currency, Long> prices;

	/**
	 * @deprecated Only for JDO!
	 */
	protected VoucherPriceConfig() { }

	public VoucherPriceConfig(String organisationID, long priceConfigID)
	{
		super(organisationID, priceConfigID);
		prices = new HashMap<Currency, Long>();
	}

	@Implement
	public boolean isDependentOnOffer()
	{
		return false;
	}

	@Implement
	public boolean requiresProductTypePackageInternal()
	{
		return false;
	}

	@Implement
	public ArticlePrice createArticlePrice(Article article)
	{
		Long amount = prices.get(article.getCurrency());

		PriceFragmentType priceFragmentTypeTotal = getPriceFragmentType(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, true);

		Price price = new Price("", -1, -1, article.getCurrency());
		if (amount != null)
			price.setAmount(priceFragmentTypeTotal, amount);

		return PriceConfigUtil.createArticlePrice(this, article, price);
	}

	@Implement
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig, Article article,
			LinkedList priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList articlePriceStack,
			NestedProductType nestedProductType, LinkedList nestedProductTypeStack)
	{
		Long amount = prices.get(article.getCurrency());

		PriceFragmentType priceFragmentTypeTotal = getPriceFragmentType(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, true);

		Price origPrice = new Price("", -1, -1, article.getCurrency());
		if (amount != null)
			origPrice.setAmount(priceFragmentTypeTotal, amount);

		return PriceConfigUtil.createNestedArticlePrice(
				topLevelPriceConfig, this, article, priceConfigStack,
				topLevelArticlePrice, nextLevelArticlePrice, articlePriceStack,
				nestedProductType, nestedProductTypeStack, origPrice);
	}

	@Implement
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig, Article article,
			LinkedList priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList articlePriceStack,
			NestedProductType nestedProductType, LinkedList nestedProductTypeStack,
			Product nestedProduct, LinkedList productStack)
	{
		Long amount = prices.get(article.getCurrency());

		PriceFragmentType priceFragmentTypeTotal = getPriceFragmentType(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, true);

		Price origPrice = new Price("", -1, -1, article.getCurrency());
		if (amount != null)
			origPrice.setAmount(priceFragmentTypeTotal, amount);

		return PriceConfigUtil.createNestedArticlePrice(
				topLevelPriceConfig, this, article, priceConfigStack,
				topLevelArticlePrice, nextLevelArticlePrice, articlePriceStack,
				nestedProductType, nestedProductTypeStack, nestedProduct,
				productStack, origPrice);
	}

	@Implement
	public void fillArticlePrice(Article article)
	{
		PriceConfigUtil.fillArticlePrice(this, article);
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Map<Currency, Long> readOnlyPrices = null;

	public Map<Currency, Long> getPrices()
	{
		if (readOnlyPrices == null)
			readOnlyPrices = Collections.unmodifiableMap(prices);

		return readOnlyPrices;
	}

	public void setPrice(Currency currency, Long value)
	{
		if (value == null) {
			removeCurrency(currency.getCurrencyID());
			prices.remove(currency);
		}
		else {
			addCurrency(currency);
			prices.put(currency, value);
		}
	}
}
