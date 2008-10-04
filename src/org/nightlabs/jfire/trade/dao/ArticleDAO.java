/**
 *
 */
package org.nightlabs.jfire.trade.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ArticleDAO
extends BaseJDOObjectDAO<ArticleID, Article>
{
	private static final Logger logger = Logger.getLogger(ArticleDAO.class);

	private ArticleDAO() {
	}

	private static ArticleDAO _sharedInstance;
	public static ArticleDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new ArticleDAO();

		return _sharedInstance;
	}

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO#retrieveJDOObjects(java.util.Set, java.lang.String[], int, org.nightlabs.progress.ProgressMonitor)
	 */
	@Override
	protected Collection<Article> retrieveJDOObjects(Set<ArticleID> articleIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		if (logger.isTraceEnabled()) {
			logger.trace("retrieveJDOObjects: entered with " + articleIDs.size() + " articleIDs:");
			for (ArticleID articleID : articleIDs) {
				logger.trace("retrieveJDOObjects: * " + articleID);
			}
		}

		monitor.beginTask("Loading articles", 1);
		try {
			TradeManager tradeManager = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<Article> articles = CollectionUtil.castCollection(
					tradeManager.getArticles(articleIDs, fetchGroups, maxFetchDepth)
			);

			if (logger.isTraceEnabled()) {
				logger.trace("retrieveJDOObjects: retrieved " + articles.size() + " articles:");
				for (Article article : articles) {
					logger.trace("retrieveJDOObjects: * " + article);
				}
			}

			return articles;
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public Article getArticle(ArticleID articleID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, articleID, fetchGroups, maxFetchDepth, monitor);
	}

	public Collection<Article> getArticles(Collection<ArticleID> articleIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, articleIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Article> releaseArticles(
			Collection<ArticleID> articleIDs, boolean synchronously,
			boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		try {
			TradeManager tradeManager = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<?> c = tradeManager.releaseArticles(
					articleIDs, synchronously,
					get,
					fetchGroups,
					maxFetchDepth
			);
			if (c == null) {
				for (ArticleID articleID : articleIDs)
					getCache().removeByObjectID(articleID, false);

				if (logger.isTraceEnabled()) {
					logger.trace("releaseArticles: released " + articleIDs.size() + " articles, but did not retrieve the new versions of them.");
				}

				return null;
			}
			else {
				Collection<Article> a = CollectionUtil.castCollection(c);
				List<Article> articles = new ArrayList<Article>(a);

				if (logger.isTraceEnabled()) {
					logger.trace("releaseArticles: released " + articles.size() + " articles:");
					for (Article article : articles) {
						logger.trace("releaseArticles: * " + article);
					}
				}

				getCache().putAll(null, articles, fetchGroups, maxFetchDepth);
				return articles;
			}
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete {@link Article}s from the datastore. Those articles that are not allocated
	 * (and not allocationPending either) are immediately deleted (synchronously). Those
	 * articles that are allocated, are asynchronously released (this method returns immediately).
	 * After the release, they are automatically deleted (asynchronously).
	 *
	 * @param articleIDs the object-ids of the {@link Article}s to be deleted.
	 * @param get whether to return a result.
	 * @param fetchGroups if <code>get == true</code> these fetch-groups are used for detaching the result.
	 * @param maxFetchDepth if <code>get == true</code> this maximum fetch-depth is used for detaching the result.
	 * @param monitor the progress monitor.
	 * @return <code>null</code> if <code>get == false</code>. Otherwise, those {@link Article}s that were not immediately deleted,
	 *		because they need to be released first (which is done asynchronously).
	 */
	public List<Article> deleteArticles(
			Collection<ArticleID> articleIDs, boolean validate,
			boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		try {
			TradeManager tradeManager = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<?> c = tradeManager.deleteArticles(articleIDs, validate, get, fetchGroups, maxFetchDepth);

			if (c == null) {
				for (ArticleID articleID : articleIDs)
					getCache().removeByObjectID(articleID, false);

				if (logger.isTraceEnabled()) {
					logger.trace("deleteArticles: deleted " + articleIDs.size() + " articles.");
				}

				return null;
			}
			else {
				Collection<Article> a = CollectionUtil.castCollection(c);
				List<Article> articles = new ArrayList<Article>(a);

				if (logger.isTraceEnabled()) {
					logger.trace("deleteArticles: deletion of " + articleIDs.size() + " articles was done with " + articles.size() + " articles being asynchronously released before deletion:");
					for (Article article : articles) {
						logger.trace("deleteArticles: * " + article);
					}
				}

				getCache().putAll(null, articles, fetchGroups, maxFetchDepth);
				return articles;
			}

		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
