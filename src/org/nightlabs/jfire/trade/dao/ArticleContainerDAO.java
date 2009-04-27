package org.nightlabs.jfire.trade.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.accounting.AccountingManagerRemote;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.TradeManagerRemote;
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
	protected Collection<? extends ArticleContainer> retrieveJDOObjects(
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
					StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
					Set<DeliveryNoteID> deliveryNoteIDs = CollectionUtil.castSet(articleContainerIDs);
					return sm.getDeliveryNotes(deliveryNoteIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof InvoiceID) {
					AccountingManagerRemote am = JFireEjb3Factory.getRemoteBean(AccountingManagerRemote.class, SecurityReflector.getInitialContextProperties());
					Set<InvoiceID> invoiceIDs = CollectionUtil.castSet(articleContainerIDs);
					return am.getInvoices(invoiceIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof OfferID) {
					TradeManagerRemote tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
					Set<OfferID> offerIDs = CollectionUtil.castSet(articleContainerIDs);
					return tm.getOffers(offerIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof OrderID) {
					TradeManagerRemote tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
					Set<OrderID> orderIDs = CollectionUtil.castSet(articleContainerIDs);
					return tm.getOrders(orderIDs, fetchGroups, maxFetchDepth);
				}
				if (articleContainerID instanceof ReceptionNoteID) {
					StoreManagerRemote sm = JFireEjb3Factory.getRemoteBean(StoreManagerRemote.class, SecurityReflector.getInitialContextProperties());
					Set<ReceptionNoteID> receptionNoteIDs = CollectionUtil.castSet(articleContainerIDs);
					return sm.getReceptionNotes(receptionNoteIDs, fetchGroups, maxFetchDepth);
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
			TradeManagerRemote tm = JFireEjb3Factory.getRemoteBean(TradeManagerRemote.class, SecurityReflector.getInitialContextProperties());
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
