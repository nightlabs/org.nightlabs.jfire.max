package org.nightlabs.jfire.voucher.accounting;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.nightlabs.jfire.accounting.Currency;
import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 * @author Attapol Thomprasert - Attapol at NightLabs dot de
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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireVoucher_VoucherPriceConfig")
@FetchGroups({
	@FetchGroup(
		name=VoucherPriceConfig.FETCH_GROUP_PRICES,
		members=@Persistent(name="prices")),
	@FetchGroup(
		fetchGroups={"default"},
		name="FetchGroupsPriceConfig.edit",
		members=@Persistent(name="prices"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class VoucherPriceConfig
extends PriceConfig
implements IPackagePriceConfig
{
	private static final long serialVersionUID = 1L;
	public static final String FETCH_GROUP_PRICES = "VoucherPriceConfig.prices";
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
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireVoucher_VoucherPriceConfig_prices",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Map<Currency, Long> prices;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected VoucherPriceConfig() { }

	public VoucherPriceConfig(String organisationID, String priceConfigID)
	{
		super(organisationID, priceConfigID);
		prices = new HashMap<Currency, Long>();
	}

	@Override
	public boolean isDependentOnOffer()
	{
		return false;
	}

	@Override
	public boolean requiresProductTypePackageInternal()
	{
		return false;
	}

	@Override
	public ArticlePrice createArticlePrice(Article article)
	{
		Long amount = prices.get(article.getCurrency());

		PriceFragmentType priceFragmentTypeTotal = getPriceFragmentType(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, true);

		Price price = new Price("", -1, article.getCurrency());
		if (amount != null)
			price.setAmount(priceFragmentTypeTotal, amount);

		return PriceConfigUtil.createArticlePrice(this, article, price);
	}

	@Override
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig, Article article,
			LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack,
			NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack)
	{
		Long amount = prices.get(article.getCurrency());

		PriceFragmentType priceFragmentTypeTotal = getPriceFragmentType(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, true);

		Price origPrice = new Price("", -1, article.getCurrency());
		if (amount != null)
			origPrice.setAmount(priceFragmentTypeTotal, amount);

		return PriceConfigUtil.createNestedArticlePrice(
				topLevelPriceConfig, this, article, priceConfigStack,
				topLevelArticlePrice, nextLevelArticlePrice, articlePriceStack,
				nestedProductTypeLocal, nestedProductTypeStack, origPrice);
	}

	@Override
	public ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig, Article article,
			LinkedList<IPriceConfig> priceConfigStack, ArticlePrice topLevelArticlePrice,
			ArticlePrice nextLevelArticlePrice, LinkedList<ArticlePrice> articlePriceStack,
			NestedProductTypeLocal nestedProductTypeLocal, LinkedList<NestedProductTypeLocal> nestedProductTypeStack,
			Product nestedProduct, LinkedList<Product> productStack)
	{
		Long amount = prices.get(article.getCurrency());

		PriceFragmentType priceFragmentTypeTotal = getPriceFragmentType(
				PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.organisationID, PriceFragmentType.PRICE_FRAGMENT_TYPE_ID_TOTAL.priceFragmentTypeID, true);

		Price origPrice = new Price("", -1, article.getCurrency());
		if (amount != null)
			origPrice.setAmount(priceFragmentTypeTotal, amount);

		return PriceConfigUtil.createNestedArticlePrice(
				topLevelPriceConfig, this, article, priceConfigStack,
				topLevelArticlePrice, nextLevelArticlePrice, articlePriceStack,
				nestedProductTypeLocal, nestedProductTypeStack, nestedProduct,
				productStack, origPrice);
	}

	@Override
	public void fillArticlePrice(Article article)
	{
		PriceConfigUtil.fillArticlePrice(this, article);
	}

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private transient Map<Currency, Long> readOnlyPrices = null;

	public Map<Currency, Long> getPrices()
	{
		if (readOnlyPrices == null)
			readOnlyPrices = Collections.unmodifiableMap(prices);

		return readOnlyPrices;
	}

//	public Map<Currency, Long> getPricesAsDouble()
//	{
//		if (pricesAsDouble == null){
//			pricesAsDouble = new HashMap<Currency, Long>();
//			for	(Currency c : prices.keySet()) {
//
// You loose data here!!! You divide a long by 100 => for example 2535 cent become 25 EUR - but they should be 25.35 EUR!!!
//
//				Long longValue = prices.get(c).longValue()/100;
//				pricesAsDouble.put(c, longValue);
//			}
//		}
//		return pricesAsDouble;
//	}

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
