package org.nightlabs.jfire.trade;

import java.io.Serializable;
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
import org.nightlabs.jfire.asyncinvoke.AsyncInvoke;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnqueueException;
import org.nightlabs.jfire.asyncinvoke.AsyncInvokeEnvelope;
import org.nightlabs.jfire.asyncinvoke.Invocation;
import org.nightlabs.jfire.asyncinvoke.UndeliverableCallback;
import org.nightlabs.jfire.asyncinvoke.UndeliverableCallbackResult;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.security.User;
import org.nightlabs.util.Util;

class BugfixArticlePricesWithoutPriceFragments
{
	private static final Logger logger = Logger.getLogger(BugfixArticlePricesWithoutPriceFragments.class);
	private static final String UPDATE_HISTORY_ITEM_ID_FIX_IN_PROGRESS = ArticlePrice.class.getName() + "#fixMissingPriceFragments_inProgress";
	private static final String UPDATE_HISTORY_ITEM_ID_FIX_DONE = ArticlePrice.class.getName() + "#fixMissingPriceFragments";

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


	private static class FixInvocationStep2 extends Invocation
	{
		private static final long serialVersionUID = 1L;
		private static final Logger logger = Logger.getLogger(FixInvocationStep2.class);

		private Set<PriceID> articlePriceIDsToFixByCopyingFromPackage;
		private Set<PriceID> packageArticlePriceIDsToRecreate;
		private long articlePriceCountFixed;
		private long articlePriceCountNotFixable;

		public FixInvocationStep2(
				Set<PriceID> articlePriceIDsToFixByCopyingFromPackage,
				Set<PriceID> packageArticlePriceIDsToRecreate,
				long articlePriceCountFixed,
				long articlePriceCountNotFixable
		)
		{
			if (articlePriceIDsToFixByCopyingFromPackage == null)
				throw new IllegalArgumentException("articlePriceIDsToFixByCopyingFromPackage must not be null!");

			if (packageArticlePriceIDsToRecreate == null)
				throw new IllegalArgumentException("packageArticlePriceIDsToRecreate must not be null!");

			this.articlePriceIDsToFixByCopyingFromPackage = articlePriceIDsToFixByCopyingFromPackage;
			this.packageArticlePriceIDsToRecreate = packageArticlePriceIDsToRecreate;
			this.articlePriceCountFixed = articlePriceCountFixed;
			this.articlePriceCountNotFixable = articlePriceCountNotFixable;
		}

		@Override
		public Serializable invoke() throws Exception {
			logger.warn("invoke: Entered with " + packageArticlePriceIDsToRecreate.size() + " + " + articlePriceIDsToFixByCopyingFromPackage.size() + " ArticlePrices to process.");

			PersistenceManager pm = getPersistenceManager();
			try {
				int pricesProcessedCount = 0;
				long startTimestamp = System.currentTimeMillis();
				TradeManagerLocal tml = JFireEjb3Factory.getLocalBean(TradeManagerLocal.class);

				Map<ArticlePrice, ArticlePrice> packageArticlePricesToRecreate2recreated = new HashMap<ArticlePrice, ArticlePrice>();

				for (Iterator<PriceID> it = packageArticlePriceIDsToRecreate.iterator(); it.hasNext(); ) {
					PriceID packageArticlePriceID = it.next(); it.remove();
					ArticlePrice packageArticlePrice = (ArticlePrice) pm.getObjectById(packageArticlePriceID);
					++pricesProcessedCount;

					ArticlePrice newDetachedPackageArticlePrice = tml.bugfix_tryRecreatingArticlePrice(packageArticlePriceID);
					packageArticlePricesToRecreate2recreated.put(packageArticlePrice, newDetachedPackageArticlePrice);

					if (System.currentTimeMillis() - startTimestamp > 30000L || packageArticlePricesToRecreate2recreated.size() >= 1000)
						break;
				}

				for (Map.Entry<ArticlePrice, ArticlePrice> me : packageArticlePricesToRecreate2recreated.entrySet()) {
					ArticlePrice packageArticlePrice = me.getKey();
					ArticlePrice newDetachedPackageArticlePrice = me.getValue();

					boolean apply = true;
					if (newDetachedPackageArticlePrice.getAmount() != packageArticlePrice.getAmount()) {
						logger.error("invoke: Fixing persistent data: Cannot fix packageArticlePrice, because price-config has changed (the top-level-amount does not match): " + packageArticlePrice);
						apply = false;
					}

					if (!matchArticlePrices(pm, newDetachedPackageArticlePrice, packageArticlePrice, false)) {
						logger.error("invoke: Fixing persistent data: Cannot fix packageArticlePrice, because price-config has changed (the nested prices or the existing price fragments don't match): " + packageArticlePrice);
						apply = false;
					}

					if (!apply)
						++articlePriceCountNotFixable;
					else {
						matchArticlePrices(pm, newDetachedPackageArticlePrice, packageArticlePrice, true);
						logger.warn("invoke: Fixing persistent data: Fixed packageArticlePrice successfully by trying to recreate from price config: " + packageArticlePrice);
						++articlePriceCountFixed;
					}
				}


				for (Iterator<PriceID> it = articlePriceIDsToFixByCopyingFromPackage.iterator(); it.hasNext(); ) {
					if (pricesProcessedCount > 0 && System.currentTimeMillis() - startTimestamp > 30000L)
						break;

					PriceID articlePriceID = it.next(); it.remove();
					ArticlePrice articlePrice = (ArticlePrice) pm.getObjectById(articlePriceID);
					++pricesProcessedCount;

					logger.warn("invoke: Fixing persistent data: Copying from packageArticlePrice, because this ArticlePrice is the only nested one and it is virtual-inner: " + articlePrice);
					ArticlePrice packageArticlePrice = articlePrice.getPackageArticlePrice();
					for (PriceFragment origpf : packageArticlePrice.getFragments(false)) {
						PriceFragment pf = articlePrice.createPriceFragment(origpf.getPriceFragmentType());
						pf.setAmount(origpf.getAmount());
					}
					++articlePriceCountFixed;
				}

				logger.warn("invoke: Fixed persistent data: Successfully fixed " + articlePriceCountFixed + " ArticlePrices.");

				if (articlePriceCountNotFixable == 0)
					logger.warn("invoke: Fixed persistent data: Was unable to fix " + articlePriceCountNotFixable + " ArticlePrices!");
				else
					logger.error("invoke: Fixed persistent data: Was unable to fix " + articlePriceCountNotFixable + " ArticlePrices!");

				logger.warn("invoke: Still " + packageArticlePriceIDsToRecreate.size() + " + " + articlePriceIDsToFixByCopyingFromPackage.size() + " ArticlePrices to process.");

				if (packageArticlePriceIDsToRecreate.isEmpty() && articlePriceIDsToFixByCopyingFromPackage.isEmpty()) {
					List<UpdateHistoryItem> uhis = UpdateHistoryItem.getUpdateHistoryItems(pm, JFireTradeEAR.MODULE_NAME, UPDATE_HISTORY_ITEM_ID_FIX_IN_PROGRESS);
					for (UpdateHistoryItem updateHistoryItem : uhis)
						pm.deletePersistent(updateHistoryItem);

					if (articlePriceCountNotFixable == 0) { // We do not mark the update as done, if we were not able to completely fix the problem.
						UpdateNeededHandle handle = UpdateHistoryItem.updateNeeded(pm, JFireTradeEAR.MODULE_NAME, UPDATE_HISTORY_ITEM_ID_FIX_DONE);
						if (handle != null)
							UpdateHistoryItem.updateDone(handle);
					}
				}
				else {
					AsyncInvoke.exec(
							new FixInvocationStep2(
									articlePriceIDsToFixByCopyingFromPackage,
									packageArticlePriceIDsToRecreate,
									articlePriceCountFixed,
									articlePriceCountNotFixable
							),
							new FixUndeliverableCallback(),
							true
					);
				}

				return null;
			} finally {
				pm.close();
			}
		}
	}

	private static class FixInvocationStep1 extends Invocation
	{
		private static final long serialVersionUID = 1L;
		private static final Logger logger = Logger.getLogger(FixInvocationStep1.class);

		private Collection<PriceID> wrongArticlePriceIDs;

		public FixInvocationStep1(Collection<PriceID> wrongArticlePriceIDs) {
			if (wrongArticlePriceIDs == null)
				throw new IllegalArgumentException("wrongArticlePriceIDs must not be null!");

			this.wrongArticlePriceIDs = wrongArticlePriceIDs;
		}

		@Override
		public Serializable invoke() throws Exception {
			logger.warn("invoke: Fixing persistent data: " + wrongArticlePriceIDs.size() + " ArticlePrices were persisted without PriceFragments!");

			PersistenceManager pm = getPersistenceManager();
			try {
				Set<PriceID> articlePriceIDsToFixByCopyingFromPackage = new HashSet<PriceID>();
				Set<PriceID> packageArticlePriceIDsToRecreate = new HashSet<PriceID>();

				if (!wrongArticlePriceIDs.isEmpty()) {

					for (Iterator<PriceID> itwap = wrongArticlePriceIDs.iterator(); itwap.hasNext(); ) {
						PriceID articlePriceID = itwap.next();
						itwap.remove();
						ArticlePrice articlePrice = (ArticlePrice) pm.getObjectById(articlePriceID);

						ArticlePrice packageArticlePrice = articlePrice.getPackageArticlePrice();
						PriceID packageArticlePriceID = (PriceID) JDOHelper.getObjectId(packageArticlePrice);

						boolean tryToCreateFromPriceConfig = false;

						// First, we directly copy the packageArticlePrice data, if we can.
						if (packageArticlePrice == null) {
							tryToCreateFromPriceConfig = true;
							logger.warn("invoke: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected ArticlePrice is not nested: " + articlePrice);
						}
						else if (!articlePrice.isVirtualInner()) {
							tryToCreateFromPriceConfig = true;
							logger.warn("invoke: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected nested ArticlePrice is not virtual-inner: " + articlePrice);
						}
						else {
							if (packageArticlePrice.getNestedArticlePrices().size() != 1) {
								tryToCreateFromPriceConfig = true;
								logger.warn("invoke: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected nested ArticlePrice a sibling among others: " + articlePrice);
							}
							else {
								Iterator<ArticlePrice> it1 = packageArticlePrice.getNestedArticlePrices().iterator();
								if (articlePrice != it1.next())
									throw new IllegalStateException("articlePrice != it1.next()");

								if (it1.hasNext())
									throw new IllegalStateException("packageArticlePrice.getNestedArticlePrices().size() returned 1, but iterator returns more than one element!");

								if (articlePrice.getAmount() != packageArticlePrice.getAmount()) {
									tryToCreateFromPriceConfig = true;
									logger.warn("invoke: Fixing persistent data: Cannot copy from packageArticlePrice, because the affected nested ArticlePrice has a different amount than its packageArticlePrice: " + articlePrice);
								}
								else
									articlePriceIDsToFixByCopyingFromPackage.add(articlePriceID);
							}
						}

						if (tryToCreateFromPriceConfig) {
							if (packageArticlePrice == null) // in case it is not nested.
								packageArticlePrice = articlePrice;

							logger.info("invoke: Fixing persistent data: Registering to recreate packageArticlePrice from price config in order to fix this wrong ArticlePrice: " + articlePrice + " package=" + packageArticlePrice);
							packageArticlePriceIDsToRecreate.add(packageArticlePriceID);
						}
					}
				}

				AsyncInvoke.exec(
						new FixInvocationStep2(
								articlePriceIDsToFixByCopyingFromPackage, packageArticlePriceIDsToRecreate,
								0, 0
						),
						new FixUndeliverableCallback(),
						true
				);

				return null;
			} finally {
				pm.close();
			}
		}
	}

	private static class FixInvocationStep0 extends Invocation
	{
		private static final Logger logger = Logger.getLogger(FixInvocationStep0.class);
		private static final long serialVersionUID = 2L;
		private static final long rangeSize = 10000;
		private long beginIndex;
		private Collection<PriceID> wrongArticlePriceIDs;

		public FixInvocationStep0(long beginIndex, Collection<PriceID> wrongArticlePriceIDs) {
			if (wrongArticlePriceIDs == null)
				throw new IllegalArgumentException("wrongArticlePriceIDs == null");

			this.beginIndex = beginIndex;
			this.wrongArticlePriceIDs = wrongArticlePriceIDs;
		}

		@Override
		public Serializable invoke() throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				logger.warn("invoke: entered with beginIndex=" + beginIndex + " wrongArticlePriceIDs.size=" + wrongArticlePriceIDs.size());
				long startTimestamp = System.currentTimeMillis();

				Query q = pm.newQuery(ArticlePrice.class);
				q.setOrdering("this.organisationID ASCENDING, this.priceID ASCENDING");
				long endIndex = beginIndex + rangeSize;
				q.setRange(beginIndex, endIndex);
				@SuppressWarnings("unchecked")
				Collection<ArticlePrice> articlePrices = (Collection<ArticlePrice>) q.execute();
				logger.warn("fix: Query returned in " + (System.currentTimeMillis() - startTimestamp) + " msec.");

				for (ArticlePrice articlePrice : articlePrices) {
					if (articlePrice.getFragments(false).isEmpty()) {
						PriceID priceID = (PriceID) JDOHelper.getObjectId(articlePrice);
						if (priceID == null)
							throw new IllegalStateException("JDOHelper.getObjectId(articlePrice) returned null!");

						wrongArticlePriceIDs.add(priceID);
					}
				}

				if (articlePrices.isEmpty()) {
					logger.warn("invoke: empty result for beginIndex=" + beginIndex + " and endIndex=" + endIndex + " => proceeding to next step with wrongArticlePriceIDs.size=" + wrongArticlePriceIDs.size());
					AsyncInvoke.exec(
							new FixInvocationStep1(wrongArticlePriceIDs),
							new FixUndeliverableCallback(),
							true
					);
				}
				else {
					logger.warn("invoke: reenqueing with new beginIndex=" + endIndex + " wrongArticlePriceIDs.size=" + wrongArticlePriceIDs.size());
					AsyncInvoke.exec(
							new FixInvocationStep0(endIndex, wrongArticlePriceIDs),
							new FixUndeliverableCallback(),
							true
					);
				}

			} finally {
				pm.close();
			}
			return null;
		}
	}

	private static class FixUndeliverableCallback extends UndeliverableCallback
	{
		private static final long serialVersionUID = 1L;

		@Override
		public UndeliverableCallbackResult handle(AsyncInvokeEnvelope envelope) throws Exception
		{
			PersistenceManager pm = getPersistenceManager();
			try {
				List<UpdateHistoryItem> uhis = UpdateHistoryItem.getUpdateHistoryItems(pm, JFireTradeEAR.MODULE_NAME, UPDATE_HISTORY_ITEM_ID_FIX_IN_PROGRESS);
				for (UpdateHistoryItem updateHistoryItem : uhis)
					pm.deletePersistent(updateHistoryItem);

				return null;
			} finally {
				pm.close();
			}
		}
	}

	public static void fix(PersistenceManager pm) throws AsyncInvokeEnqueueException
	{
		UpdateNeededHandle handle = UpdateHistoryItem.updateNeeded(pm, JFireTradeEAR.MODULE_NAME, UPDATE_HISTORY_ITEM_ID_FIX_DONE);
		if (handle == null) {
			if (logger.isDebugEnabled())
				logger.debug("fix: Update already done. Skipping.");

			return;
		}

		UpdateNeededHandle h_inProgress = UpdateHistoryItem.updateNeeded(pm, JFireTradeEAR.MODULE_NAME, UPDATE_HISTORY_ITEM_ID_FIX_IN_PROGRESS);
		if (h_inProgress == null) {
			logger.info("fix: Update is already in progress. Skipping.");

			return;
		}
		UpdateHistoryItem.updateDone(h_inProgress);

		logger.warn("fix: Enqueueing AsyncInvoke for this update.");

		AsyncInvoke.exec(
				new FixInvocationStep0(0, new LinkedList<PriceID>()),
				new FixUndeliverableCallback(),
				true
		);
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
