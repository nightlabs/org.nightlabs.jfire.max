/**
 * 
 */
package org.nightlabs.jfire.trade.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.TradeManagerUtil;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel Mazurek - daniel <at> nightlabs <dot> de
 *
 */
public class ArticleContainerDAO 
extends BaseJDOObjectDAO<ArticleContainerID, ArticleContainer>
{
	private static ArticleContainerDAO sharedInstance = null;

	public static ArticleContainerDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ArticleContainerDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ArticleContainerDAO();
			}
		}
		return sharedInstance;
	}
	
	protected ArticleContainerDAO() {
	}

	@Override
	protected Collection<ArticleContainer> retrieveJDOObjects(
			Set<ArticleContainerID> articleContainerIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) 
	throws Exception 
	{
		if (articleContainerIDs == null)
			throw new IllegalArgumentException("Param articleContainerIDs must NOT be null!");
					
		monitor.beginTask("Loading ArticleContainers", 1);
		try {
			if (!articleContainerIDs.isEmpty()) {				
				ArticleContainerID articleContainerID = articleContainerIDs.iterator().next();
				if (articleContainerID instanceof DeliveryNoteID) {
					StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
					return sm.getDeliveryNotes(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof InvoiceID) {
					AccountingManager am = AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
					return am.getInvoices(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof OfferID) {
					TradeManager tm = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
					return tm.getOffers(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof OrderID) {
					TradeManager tm = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
					return tm.getOrders(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof ReceptionNoteID) {
					TradeManager tm = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
					return tm.getReceptionNotes(articleContainerIDs, fetchGroups, maxFetchDepth);
				}				
			}
			return null;
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
			
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}
	
	public Collection<ArticleContainer> getArticleContainers(
			Set<ArticleContainerID> articleContainerIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) 
	{
		return getJDOObjects(null, articleContainerIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	public Collection<ArticleContainer> getArticleContainersForQueries(
//			Collection<JDOQuery<ArticleContainer>> queries, 
			Collection<JDOQuery> queries,			
			String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			TradeManager tm = TradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Collection<ArticleContainerID> articleContainerIDs = tm.getArticleContainerIDs(queries);
			return getJDOObjects(null, articleContainerIDs, fetchGroups, maxFetchDepth, monitor);					
		} 
		catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public ArticleContainer getArticleContainer(ArticleContainerID articleContainerID, 
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, articleContainerID, fetchGroups, maxFetchDepth, monitor);
	}
}
