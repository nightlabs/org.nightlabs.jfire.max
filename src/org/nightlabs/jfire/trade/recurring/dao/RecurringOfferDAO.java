package org.nightlabs.jfire.trade.recurring.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.recurring.RecurringOffer;
import org.nightlabs.jfire.trade.recurring.RecurringOfferConfiguration;
import org.nightlabs.jfire.trade.recurring.RecurringTradeManager;
import org.nightlabs.jfire.trade.recurring.RecurringTradeManagerUtil;
import org.nightlabs.progress.ProgressMonitor;

public class RecurringOfferDAO extends BaseJDOObjectDAO<OfferID, RecurringOffer>
{
	private static  RecurringOfferDAO sharedInstance = null;

	public static RecurringOfferDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (RecurringOfferDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new RecurringOfferDAO();
			}
		}

		return sharedInstance;
	}

	@Override
	protected Collection<RecurringOffer> retrieveJDOObjects(Set<OfferID> offerIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
			{


		RecurringTradeManager tm = RecurringTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return tm.getRecurringOffers(offerIDs, fetchGroups, maxFetchDepth);
			}

	public RecurringOffer getRecurringOffer(OfferID offerID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, offerID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<RecurringOffer> getRecurringOffers(Set<OfferID> offerIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, offerIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public RecurringOfferConfiguration storeRecurringOfferConfiguration(
			RecurringOfferConfiguration configuration, boolean get, String[] fetchGroups, int maxFetchDepth) {
		try {
			RecurringTradeManager tm = RecurringTradeManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return tm.storeRecurringOfferConfiguration(configuration, get, fetchGroups, maxFetchDepth);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
} 
