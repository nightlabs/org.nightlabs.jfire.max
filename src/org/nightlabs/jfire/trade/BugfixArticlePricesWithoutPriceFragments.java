package org.nightlabs.jfire.trade;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jdo.moduleregistry.UpdateHistoryItem;
import org.nightlabs.jdo.moduleregistry.UpdateNeededHandle;
import org.nightlabs.jfire.accounting.PriceFragment;
import org.nightlabs.jfire.accounting.PriceFragmentType;
import org.nightlabs.jfire.accounting.id.PriceFragmentTypeID;
import org.nightlabs.jfire.accounting.id.PriceID;
import org.nightlabs.jfire.accounting.priceconfig.IPackagePriceConfig;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

class BugfixArticlePricesWithoutPriceFragments
{
	private static final Logger logger = Logger.getLogger(BugfixArticlePricesWithoutPriceFragments.class);

	public static ArticlePrice tryRecreatingArticlePrice(PersistenceManager pm, PriceID packageArticlePriceID)
	{
		ArticlePrice packageArticlePrice = (ArticlePrice) pm.getObjectById(packageArticlePriceID);

		Article article = packageArticlePrice.getArticle();
		article.setPrice(null); // this deletes the current packageArticlePrice
		pm.flush();

		boolean releaseArticles = false;
		try {
			if (article.isAllocated()) {
				IPackagePriceConfig packagePriceConfig = article.getProductType().getPackagePriceConfig();
				ArticlePrice newPackageArticlePrice = packagePriceConfig.createArticlePrice(article);
				article.setPrice(newPackageArticlePrice);
				packagePriceConfig.fillArticlePrice(article);
			}
			else {
				releaseArticles = true;
				Trader.getTrader(pm).allocateArticles(User.getUser(pm), Collections.singleton(article), true);
			}

			pm.flush();

			pm.getFetchPlan().setMaxFetchDepth(NLJDOHelper.MAX_FETCH_DEPTH_NO_LIMIT);
			pm.getFetchPlan().setGroups(new String[] {
					FetchPlan.DEFAULT,
					ArticlePrice.FETCH_GROUP_NESTED_ARTICLE_PRICES_NO_LIMIT,
					ArticlePrice.FETCH_GROUP_PACKAGE_ARTICLE_PRICE,
					ArticlePrice.FETCH_GROUP_CURRENCY,
					ArticlePrice.FETCH_GROUP_FRAGMENTS,
					PriceFragment.FETCH_GROUP_CURRENCY,
					PriceFragment.FETCH_GROUP_PRICE_FRAGMENT_TYPE,
					PriceFragment.FETCH_GROUP_PRICE,
					ArticlePrice.FETCH_GROUP_PRODUCT,
					ArticlePrice.FETCH_GROUP_PRODUCT_TYPE,
					ArticlePrice.FETCH_GROUP_PACKAGE_PRODUCT_TYPE,
			});
			pm.getFetchPlan().setDetachmentOptions(FetchPlan.DETACH_LOAD_FIELDS | FetchPlan.DETACH_UNLOAD_FIELDS);
			return pm.detachCopy(article.getPrice());
		} finally {
			// Even though we rollback the transaction, we release explicitely. This is to ensure that
			// even if the XA-rollback-handling doesn't work properly (e.g. because of a misconfigured server)
			// we'll release on all affected servers properly.

			if (releaseArticles)
				Trader.getTrader(pm).releaseArticles(User.getUser(pm), Collections.singleton(article), true, false);
		}
	}

	public static void fix(PersistenceManager pm)
	{
		UpdateNeededHandle handle = UpdateHistoryItem.updateNeeded(pm, JFireTradeEAR.MODULE_NAME, ArticlePrice.class.getName() + "#fixMissingPriceFragments");
		if (handle != null) {
			long articlePriceCountFixed = 0;
			long articlePriceCountNotFixable = 0;
			List<ArticlePrice> wrongArticlePrices = new LinkedList<ArticlePrice>();

			Query q = pm.newQuery(ArticlePrice.class);
			@SuppressWarnings("unchecked")
			Collection<ArticlePrice> articlePrices = (Collection<ArticlePrice>) q.execute();
			for (ArticlePrice articlePrice : articlePrices) {
				if (articlePrice.getFragments(false).isEmpty())
					wrongArticlePrices.add(articlePrice);
			}
			articlePrices = null; // allow gc
			q.closeAll();

			Set<ArticlePrice> articlePricesToFixByCopyingFromPackage = new HashSet<ArticlePrice>();
			Set<ArticlePrice> packageArticlePricesToRecreate = new HashSet<ArticlePrice>();

			if (!wrongArticlePrices.isEmpty()) {
				logger.warn("initialise: Fixing persistent data: " + wrongArticlePrices.size() + " ArticlePrices were persisted without PriceFragments!");

				for (Iterator<ArticlePrice> itwap = wrongArticlePrices.iterator(); itwap.hasNext(); ) {
					ArticlePrice articlePrice = itwap.next();
					itwap.remove(); // releasing memory

					ArticlePrice packageArticlePrice = articlePrice.getPackageArticlePrice();
					boolean tryToCreateFromPriceConfig = false;

					// First, we directly copy the packageArticlePrice data, if we can.
					if (packageArticlePrice == null) {
						tryToCreateFromPriceConfig = true;
						logger.warn("initialise: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected ArticlePrice is not nested: " + articlePrice);
					}
					else if (!articlePrice.isVirtualInner()) {
						tryToCreateFromPriceConfig = true;
						logger.warn("initialise: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected nested ArticlePrice is not virtual-inner: " + articlePrice);
					}
					else {
						if (packageArticlePrice.getNestedArticlePrices().size() != 1) {
							tryToCreateFromPriceConfig = true;
							logger.warn("initialise: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected nested ArticlePrice a sibling among others: " + articlePrice);
						}
						else {
							Iterator<ArticlePrice> it1 = packageArticlePrice.getNestedArticlePrices().iterator();
							if (articlePrice != it1.next())
								throw new IllegalStateException("articlePrice != it1.next()");

							if (it1.hasNext())
								throw new IllegalStateException("packageArticlePrice.getNestedArticlePrices().size() returned 1, but iterator returns more than one element!");

							if (articlePrice.getAmount() != packageArticlePrice.getAmount()) {
								tryToCreateFromPriceConfig = true;
								logger.warn("initialise: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected nested ArticlePrice has a different amount than its packageArticlePrice: " + articlePrice);
							}
							else
								articlePricesToFixByCopyingFromPackage.add(articlePrice);
						}
					}

					if (tryToCreateFromPriceConfig) {
						if (packageArticlePrice == null) // in case it is not nested.
							packageArticlePrice = articlePrice;

						logger.info("initialise: Fixing persistent data: Registering to recreate packageArticlePrice from price config in order to fix this wrong ArticlePrice: " + articlePrice + " package=" + packageArticlePrice);
						packageArticlePricesToRecreate.add(packageArticlePrice);
					}
				}

				TradeManagerLocal tml = JFireEjb3Factory.getLocalBean(TradeManagerLocal.class);

				Map<ArticlePrice, ArticlePrice> packageArticlePricesToRecreate2recreated = new HashMap<ArticlePrice, ArticlePrice>();

				for (ArticlePrice packageArticlePrice : packageArticlePricesToRecreate) {
					PriceID packageArticlePriceID = (PriceID) JDOHelper.getObjectId(packageArticlePrice);
					ArticlePrice newDetachedPackageArticlePrice = tml.bugfix_tryRecreatingArticlePrice(packageArticlePriceID);
					packageArticlePricesToRecreate2recreated.put(packageArticlePrice, newDetachedPackageArticlePrice);
				}

				for (Map.Entry<ArticlePrice, ArticlePrice> me : packageArticlePricesToRecreate2recreated.entrySet()) {
					ArticlePrice packageArticlePrice = me.getKey();
					ArticlePrice newDetachedPackageArticlePrice = me.getValue();

					boolean apply = true;
					if (newDetachedPackageArticlePrice.getAmount() != packageArticlePrice.getAmount()) {
						logger.error("initialise: Fixing persistent data: Cannot fix packageArticlePrice, because price-config has changed (the top-level-amount does not match): " + packageArticlePrice);
						apply = false;
					}

					if (!matchArticlePrices(pm, newDetachedPackageArticlePrice, packageArticlePrice, false)) {
						logger.error("initialise: Fixing persistent data: Cannot fix packageArticlePrice, because price-config has changed (the nested prices or the existing price fragments don't match): " + packageArticlePrice);
						apply = false;
					}

					if (!apply)
						++articlePriceCountNotFixable;
					else {
						matchArticlePrices(pm, newDetachedPackageArticlePrice, packageArticlePrice, true);
						logger.warn("initialise: Fixing persistent data: Fixed packageArticlePrice successfully by trying to recreate from price config: " + packageArticlePrice);
						++articlePriceCountFixed;
					}
				}

				for (ArticlePrice articlePrice : articlePricesToFixByCopyingFromPackage) {
					logger.warn("initialise: Fixing persistent data: Copying from packageArticlePrice, because this ArticlePrice is the only nested one and it is virtual-inner: " + articlePrice);
					ArticlePrice packageArticlePrice = articlePrice.getPackageArticlePrice();
					for (PriceFragment origpf : packageArticlePrice.getFragments(false)) {
						PriceFragment pf = articlePrice.createPriceFragment(origpf.getPriceFragmentType());
						pf.setAmount(origpf.getAmount());
					}
					++articlePriceCountFixed;
				}

				logger.warn("initialise: Fixed persistent data: Successfully fixed " + articlePriceCountFixed + " ArticlePrices.");

				if (articlePriceCountNotFixable == 0)
					logger.warn("initialise: Fixed persistent data: Was unable to fix " + articlePriceCountNotFixable + " ArticlePrices!");
				else
					logger.error("initialise: Fixed persistent data: Was unable to fix " + articlePriceCountNotFixable + " ArticlePrices!");
			}

			if (articlePriceCountNotFixable == 0) // We do not mark the update as done, if we were not able to completely fix the problem.
				UpdateHistoryItem.updateDone(handle);
		}
	}

	private static boolean matchArticlePrices(PersistenceManager pm, ArticlePrice newArticlePrice, ArticlePrice oldArticlePrice, boolean fix)
	{
		// We cannot compare products because we re-allocate, if an article has been released - this causes new nested products.
//		if (!Util.equals(newArticlePrice.getProduct(), oldArticlePrice.getProduct()))
//			return false;

		if (!Util.equals(newArticlePrice.getProductType(), oldArticlePrice.getProductType()))
			return false;

		if (newArticlePrice.getAmount() != oldArticlePrice.getAmount())
			return false;

		if (newArticlePrice.getNestedArticlePrices().size() != oldArticlePrice.getNestedArticlePrices().size())
			return false;

		if (fix) {
			for (PriceFragment newPF : newArticlePrice.getFragments(false)) {
				PriceFragmentTypeID priceFragmentTypeID = (PriceFragmentTypeID) JDOHelper.getObjectId(newPF.getPriceFragmentType());
				PriceFragmentType priceFragmentType = (PriceFragmentType) pm.getObjectById(priceFragmentTypeID);

				PriceFragment oldPF = oldArticlePrice.createPriceFragment(priceFragmentType);
				oldPF.setAmount(newPF.getAmount());
			}
		}
		else {
			if (!oldArticlePrice.getFragments(false).isEmpty()) {
				if (newArticlePrice.getFragments(false).size() != oldArticlePrice.getFragments(false).size())
					return false;

				for (PriceFragment oldPF : oldArticlePrice.getFragments(false)) {
					PriceFragment newPF = newArticlePrice.getPriceFragment(oldPF.getPriceFragmentTypePK(), false);
					if (newPF == null)
						return false;

					if (newPF.getAmount() != oldPF.getAmount())
						return false;
				}
			}
		}

		Set<ArticlePrice> newNestedArticlePrices = new HashSet<ArticlePrice>(newArticlePrice.getNestedArticlePrices());

		for (ArticlePrice oldNestedArticlePrice : oldArticlePrice.getNestedArticlePrices()) {
			ArticlePrice matchingNewArticlePrice = null;

			for (ArticlePrice newNestedArticlePrice : newNestedArticlePrices) {
				if (matchArticlePrices(pm, newNestedArticlePrice, oldNestedArticlePrice, fix)) {
					matchingNewArticlePrice = newNestedArticlePrice;
					break;
				}
			}

			if (matchingNewArticlePrice == null)
				return false;

			newNestedArticlePrices.remove(matchingNewArticlePrice);
		}
		return true;
	}
}
