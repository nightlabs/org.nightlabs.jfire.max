package org.nightlabs.jfire.trade.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.trade.ArticleEndCustomerHistoryItem;
import org.nightlabs.jfire.trade.TradeManagerRemote;
import org.nightlabs.jfire.trade.id.ArticleEndCustomerHistoryItemID;
import org.nightlabs.jfire.trade.id.ArticleID;
import org.nightlabs.progress.ProgressMonitor;

public class ArticleEndCustomerHistoryItemDAO
extends BaseJDOObjectDAO<ArticleEndCustomerHistoryItemID, ArticleEndCustomerHistoryItem>
{
	private static volatile ArticleEndCustomerHistoryItemDAO _sharedInstance;

	public static ArticleEndCustomerHistoryItemDAO sharedInstance()
	{
		if (_sharedInstance == null) {
			synchronized (ArticleEndCustomerHistoryItemDAO.class) {
				if (_sharedInstance == null)
					_sharedInstance = new ArticleEndCustomerHistoryItemDAO();
			}
		}
		return _sharedInstance;
	}

	@Override
	protected Collection<ArticleEndCustomerHistoryItem> retrieveJDOObjects(
			Set<ArticleEndCustomerHistoryItemID> articleEndCustomerHistoryItemIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		TradeManagerRemote tm = tradeManager;
		if (tm == null)
			tm = getEjbProvider().getRemoteBean(TradeManagerRemote.class);

		return tm.getArticleEndCustomerHistoryItems(articleEndCustomerHistoryItemIDs, fetchGroups, maxFetchDepth);
	}

	public ArticleEndCustomerHistoryItem getArticleEndCustomerHistoryItem(
			ArticleEndCustomerHistoryItemID articleEndCustomerHistoryItemID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObject(null, articleEndCustomerHistoryItemID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<ArticleEndCustomerHistoryItem> getArticleEndCustomerHistoryItems(
			Collection<ArticleEndCustomerHistoryItemID> articleEndCustomerHistoryItemIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getJDOObjects(null, articleEndCustomerHistoryItemIDs, fetchGroups, maxFetchDepth, monitor);
	}

	private TradeManagerRemote tradeManager = null;

	public synchronized List<ArticleEndCustomerHistoryItem> getArticleEndCustomerHistoryItems(
			ArticleID articleID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		tradeManager = getEjbProvider().getRemoteBean(TradeManagerRemote.class);
		try {
			Collection<ArticleEndCustomerHistoryItemID> articleEndCustomerHistoryItemIDs = tradeManager.getArticleEndCustomerHistoryItemIDs(articleID);
			return getJDOObjects(null, articleEndCustomerHistoryItemIDs, fetchGroups, maxFetchDepth, monitor);
		} finally {
			tradeManager = null;
		}
	}
}
