package org.nightlabs.jfire.dynamictrade.accounting.priceconfig;

import java.util.Iterator;
import java.util.LinkedList;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.IPriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfig;
import org.nightlabs.jfire.accounting.priceconfig.PriceConfigUtil;
import org.nightlabs.jfire.accounting.priceconfig.id.PriceConfigID;
import org.nightlabs.jfire.dynamictrade.DynamicProductInfo;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.store.NestedProductTypeLocal;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.accounting.priceconfig.PriceConfig"
 *		detachable="true"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
public class PackagePriceConfig
extends PriceConfig
implements IPackagePriceConfig
{
	private static final long serialVersionUID = 1L;

	public static PackagePriceConfig getPackagePriceConfig(final PersistenceManager pm)
	{
		final Iterator<?> it = pm.getExtent(PackagePriceConfig.class, false).iterator();
		if (it.hasNext())
			return (PackagePriceConfig) it.next();

		final PackagePriceConfig packagePriceConfig = new PackagePriceConfig(null);
		return pm.makePersistent(packagePriceConfig);
	}

	public PackagePriceConfig(final PriceConfigID priceConfigID)
	{
		super(priceConfigID);
	}

	@Override
	public ArticlePrice createArticlePrice(final Article article)
	{
		DynamicProductInfo productInfo;
		if (article.getProduct() != null)
			productInfo = (DynamicProductInfo) article.getProduct();
		else
			productInfo = (DynamicProductInfo) article;

		final Price singlePrice = productInfo.getSinglePrice();
//		long priceID = PriceConfig.createPriceID(singlePrice.getOrganisationID(), singlePrice.getPriceConfigID());
		final ArticlePrice articlePrice = new ArticlePrice(
				article, singlePrice,
				IDGenerator.getOrganisationID(), IDGenerator.nextID(Price.class),
				false);

		PriceConfigUtil.multiplyPrice(singlePrice, productInfo.getQuantityAsDouble(), articlePrice);

//		for (PriceFragment pf : articlePrice.getFragments()) {
//			// The quantity might be a fraction so we have to round the result here.
//			// We use Math.round as it implements the german so called "kaufmännisches Runden" (mercantile rounding)
//			articlePrice.setAmount(pf.getPriceFragmentType(),
//					Math.round(pf.getAmount() * productInfo.getQuantityAsDouble()));
////			articlePrice.setAmount(pf.getPriceFragmentType(), (long) (pf.getAmount() * productInfo.getQuantityAsDouble()));
//		}
		return articlePrice;
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
	public ArticlePrice createNestedArticlePrice(
			final IPackagePriceConfig topLevelPriceConfig, final Article article,
			final LinkedList<IPriceConfig> priceConfigStack, final ArticlePrice topLevelArticlePrice,
			final ArticlePrice nextLevelArticlePrice, final LinkedList<ArticlePrice> articlePriceStack,
			final NestedProductTypeLocal nestedProductTypeLocal, final LinkedList<NestedProductTypeLocal> nestedProductTypeStack)
	{
		throw new UnsupportedOperationException("There should be nothing nested?!");
	}

	@Override
	public ArticlePrice createNestedArticlePrice(
			final IPackagePriceConfig topLevelPriceConfig, final Article article,
			final LinkedList<IPriceConfig> priceConfigStack, final ArticlePrice topLevelArticlePrice,
			final ArticlePrice nextLevelArticlePrice, final LinkedList<ArticlePrice> articlePriceStack,
			final NestedProductTypeLocal nestedProductTypeLocal, final LinkedList<NestedProductTypeLocal> nestedProductTypeStack,
			final Product nestedProduct, final LinkedList<Product> productStack)
	{
//		if (nestedProduct.getProductType().equals(article.getProductType()))
//			return;
		throw new UnsupportedOperationException("There should be nothing nested?!");
	}

	@Override
	public void fillArticlePrice(final Article article)
	{
		// this method is always called - and doesn't need to do anything, because DynamicProductTypes cannot be nested
//		throw new UnsupportedOperationException("There should be nothing nested?!");
	}

}
