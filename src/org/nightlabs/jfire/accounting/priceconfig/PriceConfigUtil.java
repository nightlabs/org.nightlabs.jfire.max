/*
 * Created on May 21, 2005
 */
package org.nightlabs.jfire.accounting.priceconfig;

import java.util.Iterator;
import java.util.LinkedList;

import org.nightlabs.jfire.accounting.Price;
import org.nightlabs.jfire.store.NestedProductType;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductLocal;
import org.nightlabs.jfire.store.ProductType;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.ArticlePrice;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PriceConfigUtil
{

	protected PriceConfigUtil() { }

	/**
	 * The caller of this method (always the top-level-productType-packagePriceConfig)
	 * must look up or calculate a <tt>Price</tt> for the given
	 * <tt>article</tt>. This <tt>Price</tt> must be passed as <tt>origPrice</tt>
	 * to this method, which will copy it into a new
	 * instance of <tt>ArticlePrice</tt>. This new <tt>ArticlePrice</tt> stores the
	 * returned value as original price if priceID >= 0. Hence, if this price is
	 * calculated dynamically, it must have the priceID -1.
	 * <p>
	 * Note, that this method DOES NOT fill the top-level <code>ArticlePrice</code>
	 * with nested prices! This work is done by {@link #fillArticlePrice(IPackagePriceConfig, Article)}.
	 * </p>
	 */
	public static ArticlePrice createArticlePrice(
			IPackagePriceConfig packagePriceConfig, Article article, Price origPrice)
	{
		ArticlePrice packageArticlePrice = new ArticlePrice(
				article,
				origPrice,
				packagePriceConfig.getOrganisationID(),
				packagePriceConfig.getPriceConfigID(),
				packagePriceConfig.createPriceID(),
				false);

		return packageArticlePrice;
	}

	/**
	 * This method iterates all packagedProducts / packagedProductTypes of the first
	 * packaging level
	 * and nests an instance of <tt>ArticlePrice</tt> per packagedProductType / packagedProduct.
	 * To retrieve this nested <tt>ArticlePrice</tt>, the method
	 * {@link IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, Article, LinkedList, ArticlePrice, ArticlePrice, LinkedList, NestedProductType, LinkedList, Product, LinkedList)
	 * is called, if the nested product is coming from the local organisation. If the product
	 * is coming from a foreign organisation, the ArticlePrice is copied from the corresponding
	 * ArticlePrice (from the Article which was used to buy this Product).
	 */
	public static void fillArticlePrice(IPackagePriceConfig packagePriceConfig, Article article)
	{
		ArticlePrice packageArticlePrice = article.getPrice();

		if (!packageArticlePrice.getNestedArticlePrices().isEmpty())
			throw new IllegalStateException("The ArticlePrice " + packageArticlePrice.getPrimaryKey() + " does already have nested prices!");

		LinkedList priceConfigStack = new LinkedList();
		LinkedList articlePriceStack = new LinkedList();
		LinkedList nestedProductTypeStack = new LinkedList();
		LinkedList productStack = new LinkedList();

		ProductType productType = article.getProductType();
		Product product = article.getProduct();
		if (product != null) {
			for (Iterator it = product.getProductLocal().getNestedProductLocals(true).iterator(); it.hasNext(); ) {
				ProductLocal nestedProductLocal = (ProductLocal) it.next();
				Product nestedProduct = nestedProductLocal.getProduct();
				NestedProductType nestedProductType = productType.getNestedProductType(nestedProduct.getProductType().getPrimaryKey(), true);
				ProductType innerProductType = nestedProductType.getInnerProductType();

//				Article nestedArticle = nestedProductLocal.getArticle();
//				if (nestedArticle != null) {
//					// The nestedArticle exists, hence it's a foreign product.
//					// nestedProductLocal.getArticle() is the article that was used to buy this product! It is NOT the same as the parameter article.
//
//					ArticlePrice origPrice = nestedArticle.getPrice().getNestedArticlePrice(product, true);
//
//					// We create a new ArticlePrice which will copy the nested prices recursively itself
//					new ArticlePrice(
//							article,
//							origPrice,
//							packagePriceConfig.getOrganisationID(),
//							packagePriceConfig.getPriceConfigID(),
//							packagePriceConfig.createPriceID(),
//							packageArticlePrice,
//							nestedProductType,
//							nestedProduct.getProductType(),
//							nestedProduct,
//							false, // virtualInner is impossible if it's a foreign product
//							false);
//				}
//				else {
//					// The nestedArticle doesn't exist, hence it's a local product.
//					innerProductType.getPriceConfigInPackage(productType.getPrimaryKey()).createNestedArticlePrice(
//							packagePriceConfig,
//							article,
//							priceConfigStack,
//							packageArticlePrice,
//							packageArticlePrice,
//							articlePriceStack,
//							nestedProductType,
//							nestedProductTypeStack,
//							nestedProduct,
//							productStack
//							);
//				}

				// Maybe it's wiser to always use the price configs to create the article prices and only check at the
				// end of allocation, whether it's correct.
				innerProductType.getPriceConfigInPackage(productType.getPrimaryKey()).createNestedArticlePrice(
						packagePriceConfig,
						article,
						priceConfigStack,
						packageArticlePrice,
						packageArticlePrice,
						articlePriceStack,
						nestedProductType,
						nestedProductTypeStack,
						nestedProduct,
						productStack
						);
			}
		}
		else {
			for (Iterator it = productType.getNestedProductTypes(true).iterator(); it.hasNext(); ) {
				NestedProductType nestedProductType = (NestedProductType) it.next();
				ProductType innerProductType = nestedProductType.getInnerProductType();

				innerProductType.getPriceConfigInPackage(productType.getPrimaryKey()).createNestedArticlePrice(
						packagePriceConfig,
						article,
						priceConfigStack,
						packageArticlePrice,
						packageArticlePrice,
						articlePriceStack,
						nestedProductType,
						nestedProductTypeStack
						);
			}
		}
	}

	/**
	 * Helper method used by
	 * {@link IPriceConfig#createNestedArticlePrice(IPackagePriceConfig, Article, LinkedList, ArticlePrice, ArticlePrice, LinkedList, NestedProductType, LinkedList, Product, LinkedList).
	 * <p>
	 * This method keeps track about adding and removing items to/from the stacks. Hence,
	 * you can simply call this method without touching the parameters in your
	 * <tt>IPriceConfig</tt>.
	 *
	 * @param topLevelPriceConfig This <tt>IPriceConfig</tt> will create the ID for the new <tt>ArticlePrice</tt>.
	 */
	public static ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig,
			IPriceConfig priceConfig,
			Article article,
			LinkedList priceConfigStack,
			ArticlePrice topLevelArticlePrice, ArticlePrice nextLevelArticlePrice,
			LinkedList articlePriceStack,
			NestedProductType nestedProductType,
			LinkedList nestedProductTypeStack,
			Product nestedProduct,
			LinkedList productStack,
			Price origPrice)
	{
		boolean inner = nestedProduct.getPrimaryKey().equals(nextLevelArticlePrice.getProduct().getPrimaryKey());

		ArticlePrice articlePrice = new ArticlePrice(
				article,
				origPrice,
				topLevelPriceConfig.getOrganisationID(),
				topLevelPriceConfig.getPriceConfigID(),
				topLevelPriceConfig.createPriceID(),
				nextLevelArticlePrice,
				nestedProductType,
				nestedProduct.getProductType(),
				nestedProduct,
				inner,
				false);

		ProductType productType = nestedProductType.getInnerProductType();

		if (productType.isPackageOuter()) {

			// prevent eternal loop when self-packaging
			Product nextLevelProduct;
			if (productStack.isEmpty())
				nextLevelProduct = article.getProduct();
			else
				nextLevelProduct = (Product)productStack.peek();

			if (!nestedProduct.getPrimaryKey().equals(nextLevelProduct.getPrimaryKey())) {

				priceConfigStack.addFirst(priceConfig);
				articlePriceStack.addFirst(articlePrice);
				nestedProductTypeStack.addFirst(nestedProductType);
				productStack.addFirst(nestedProduct);
				try {
	
					for (Iterator it = nestedProduct.getProductLocal().getNestedProductLocals(true).iterator(); it.hasNext(); ) {
						ProductLocal innerProductLocal = (ProductLocal) it.next();
						NestedProductType innerNestedProductType = productType.getNestedProductType(
								nestedProduct.getProductType().getPrimaryKey(), true);
						ProductType innerProductType = innerNestedProductType.getInnerProductType();

						innerProductType.getPriceConfigInPackage(productType.getPrimaryKey()).createNestedArticlePrice(
								topLevelPriceConfig,
								article,
								priceConfigStack,
								topLevelArticlePrice,
								articlePrice,
								articlePriceStack,
								innerNestedProductType,
								nestedProductTypeStack,
								innerProductLocal.getProduct(),
								productStack
								);
					}

				} finally {
					priceConfigStack.removeFirst();
					articlePriceStack.removeFirst();
					nestedProductTypeStack.removeFirst();
					productStack.removeFirst();
				}

			} // if (!nestedProduct.getPrimaryKey().equals(nextLevelProduct.getPrimaryKey())) {

		} // if (productType.isPackage()) {

		return articlePrice;
	}
	
	public static ArticlePrice createNestedArticlePrice(
			IPackagePriceConfig topLevelPriceConfig,
			IPriceConfig priceConfig,
			Article article,
			LinkedList priceConfigStack,
			ArticlePrice topLevelArticlePrice, ArticlePrice nextLevelArticlePrice,
			LinkedList articlePriceStack,
			NestedProductType nestedProductType,
			LinkedList nestedProductTypeStack,
			Price origPrice)
	{
		boolean inner = nestedProductType.getInnerProductType().getPrimaryKey().equals(nextLevelArticlePrice.getProductType().getPrimaryKey());

		ArticlePrice articlePrice = new ArticlePrice(
				article,
				origPrice,
				topLevelPriceConfig.getOrganisationID(),
				topLevelPriceConfig.getPriceConfigID(),
				topLevelPriceConfig.createPriceID(),
				nextLevelArticlePrice,
				nestedProductType,
				nestedProductType.getInnerProductType(),
				(Product)null,
				inner,
				false);

		ProductType productType = nestedProductType.getInnerProductType();

		if (productType.isPackageOuter()) {
			
			// prevent eternal loop when self-packaging
			ProductType nextLevelProductType;
			if (nestedProductTypeStack.isEmpty())
				nextLevelProductType = article.getProductType();
			else
				nextLevelProductType = ((NestedProductType)nestedProductTypeStack.peek()).getInnerProductType();

			if (!productType.getPrimaryKey().equals(nextLevelProductType.getPrimaryKey())) {

				priceConfigStack.addFirst(priceConfig);
				articlePriceStack.addFirst(articlePrice);
				nestedProductTypeStack.addFirst(nestedProductType);
				try {
	
					for (Iterator it = productType.getNestedProductTypes(true).iterator(); it.hasNext(); ) {
						NestedProductType innerNestedProductType = (NestedProductType)it.next();
						ProductType innerProductType = innerNestedProductType.getInnerProductType();
	
						innerProductType.getPriceConfigInPackage(productType.getPrimaryKey()).createNestedArticlePrice(
								topLevelPriceConfig,
								article,
								priceConfigStack,
								topLevelArticlePrice,
								articlePrice,
								articlePriceStack,
								innerNestedProductType,
								nestedProductTypeStack
								);
					}
	
				} finally {
					priceConfigStack.removeFirst();
					articlePriceStack.removeFirst();
					nestedProductTypeStack.removeFirst();
				}

			} // if (!productType.getPrimaryKey().equals(nextLevelProductType.getPrimaryKey())) {

		} // if (productType.isPackage()) {

		return articlePrice;
	}
}
