package org.nightlabs.jfire.trade.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;
import org.nightlabs.jfire.trade.query.AbstractArticleContainerQuery;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.util.CollectionUtil;

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
					StoreManager sm = JFireEjbUtil.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
					return sm.getDeliveryNotes(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof InvoiceID) {
					AccountingManager am = JFireEjbUtil.getBean(AccountingManager.class, SecurityReflector.getInitialContextProperties());
					return am.getInvoices(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof OfferID) {
					TradeManager tm = JFireEjbUtil.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
					return tm.getOffers(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof OrderID) {
					TradeManager tm = JFireEjbUtil.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
					return tm.getOrders(articleContainerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof ReceptionNoteID) {
					StoreManager sm = JFireEjbUtil.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
					return sm.getReceptionNotes(articleContainerIDs, fetchGroups, maxFetchDepth);
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

	public Collection<?> getArticleContainersForQueries(
			QueryCollection<? extends AbstractArticleContainerQuery> queries,
			String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			TradeManager tm = JFireEjbUtil.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
			Collection<ArticleContainerID> articleContainerIDs = tm.getArticleContainerIDs(queries);
			return CollectionUtil.castCollection(
				getJDOObjects(null, articleContainerIDs, fetchGroups, maxFetchDepth, monitor)
				);
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
