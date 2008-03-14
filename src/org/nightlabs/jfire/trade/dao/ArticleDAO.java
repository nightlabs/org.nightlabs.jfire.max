/**
 * 
 */
package org.nightlabs.jfire.trade.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ArticleDAO 
extends BaseJDOObjectDAO<ArticleID, Article> 
{
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
		monitor.beginTask("Loading Accounts", 1);
		try {
			TradeManager tradeManager = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return tradeManager.getArticles(articleIDs, fetchGroups, maxFetchDepth);			
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
}
