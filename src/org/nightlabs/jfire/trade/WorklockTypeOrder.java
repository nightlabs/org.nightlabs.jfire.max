package org.nightlabs.jfire.trade;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.worklock.ReleaseReason;
import org.nightlabs.jfire.worklock.Worklock;
import org.nightlabs.jfire.worklock.WorklockType;
import org.nightlabs.jfire.worklock.id.WorklockTypeID;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		persistence-capable-superclass="org.nightlabs.jfire.worklock.WorklockType"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class WorklockTypeOrder
extends WorklockType
{
	private static final long serialVersionUID = 1L;

	public static final WorklockTypeID WORKLOCK_TYPE_ID = WorklockTypeID.create(Organisation.DEVIL_ORGANISATION_ID, WorklockTypeOrder.class.getName());

	/**
	 * @deprecated Only for JDO!
	 */
	protected WorklockTypeOrder() { }

	public WorklockTypeOrder(String organisationID, String worklockTypeID)
	{
		super(organisationID, worklockTypeID);
	}

	@Override
	public void onReleaseWorklock(Worklock worklock, ReleaseReason releaseReason)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		pm.getExtent(Order.class);
		Order order = (Order) pm.getObjectById(worklock.getLockedObjectID());
		// release all products and delete articles if it's a QuickSaleOrder without finalized offers
		if (order.isQuickSaleWorkOrder()) {
			boolean containsFinalizedOffer = false;
			for (Offer offer : order.getOffers()) {
				if (offer.isFinalized()) {
					containsFinalizedOffer = true;
					break;
				}
			}
			if (!containsFinalizedOffer) {
				Trader trader = Trader.getTrader(pm);
				User user = SecurityReflector.getUserDescriptor().getUser(pm);
				if (!order.getArticles().isEmpty()) {
					trader.releaseArticles(user, order.getArticles(), true, true);
					trader.deleteArticles(user, order.getArticles());
				}
			}
		}
	}
}
