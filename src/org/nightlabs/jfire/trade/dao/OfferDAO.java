package org.nightlabs.jfire.trade.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.JFireEjbFactory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.id.ProductTypeID;
import org.nightlabs.jfire.trade.Offer;
import org.nightlabs.jfire.trade.TradeManager;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.query.OfferQuery;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class OfferDAO
		extends BaseJDOObjectDAO<OfferID, Offer>
{
	private static OfferDAO _sharedInstance;
	public static OfferDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new OfferDAO();

		return _sharedInstance;
	}

	@Override
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected Collection<Offer> retrieveJDOObjects(Set<OfferID> offerIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		TradeManager tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
		return tm.getOffers(offerIDs, fetchGroups, maxFetchDepth);
	}

	public Offer getOffer(OfferID offerID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, offerID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Offer> getOffers(Set<OfferID> offerIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, offerIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public Collection<Offer> getOffersByQuery(
		QueryCollection<? extends AbstractJDOQuery> queries,
		String[] fetchGroups,	int maxFetchDepth, ProgressMonitor monitor)
	{
		try
		{
			TradeManager tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
			Set<OfferID> offerIDs = tm.getOfferIDs(queries);

			return getJDOObjects(null, offerIDs, fetchGroups, maxFetchDepth, monitor);
		}
		catch (Exception e) {
			throw new RuntimeException("Cannot fetch Offers via Queries.", e); //$NON-NLS-1$
		}
	}

	public Offer setOfferExpiry(
			OfferID offerID,
			Date expiryTimestampUnfinalized, boolean expiryTimestampUnfinalizedAutoManaged,
			Date expiryTimestampFinalized, boolean expiryTimestampFinalizedAutoManaged,
			boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		Offer offer;
		monitor.beginTask("Set offer expiry", 100);
		try {
			TradeManager tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(20);
			offer = tm.setOfferExpiry(
					offerID,
					expiryTimestampUnfinalized, expiryTimestampUnfinalizedAutoManaged,
					expiryTimestampFinalized, expiryTimestampFinalizedAutoManaged,
					get, fetchGroups, maxFetchDepth
			);

			if (offer != null)
				getCache().put(null, offer, fetchGroups, maxFetchDepth);

			monitor.worked(80);
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			monitor.done();
		}
		return offer;
	}

	public void signalOffer(OfferID offerID, String jbpmTransitionName, ProgressMonitor monitor)
	{
		monitor.beginTask("Signal Offer", 100);
		try {
			TradeManager tm = JFireEjbFactory.getBean(TradeManager.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(20);
			tm.signalOffer(offerID, jbpmTransitionName);
			monitor.worked(80);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	public Collection<Offer> getReservations(ProductTypeID productTypeID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Load Reservations", 100);
		try {
			QueryCollection<AbstractJDOQuery> queryCollection = new QueryCollection<AbstractJDOQuery>(Offer.class);
			OfferQuery offerQuery = new OfferQuery();
			offerQuery.setReserved(true);
			offerQuery.setProductTypeID(productTypeID);
			queryCollection.add(offerQuery);
			return getOffersByQuery(queryCollection, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 100));
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}
}