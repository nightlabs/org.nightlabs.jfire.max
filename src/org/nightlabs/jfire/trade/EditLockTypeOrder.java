package org.nightlabs.jfire.trade;

import javax.jdo.PersistenceManager;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.editlock.EditLock;
import org.nightlabs.jfire.editlock.EditLockType;
import org.nightlabs.jfire.editlock.ReleaseReason;
import org.nightlabs.jfire.editlock.id.EditLockTypeID;
import org.nightlabs.jfire.organisation.Organisation;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.security.User;

/**
 * @author Marco Schulze - Marco at NightLabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		detachable="true"
 *		persistence-capable-superclass="org.nightlabs.jfire.editlock.EditLockType"
 *
 * @jdo.inheritance strategy="superclass-table"
 */
public class EditLockTypeOrder
extends EditLockType
{
	private static final long serialVersionUID = 1L;

	public static final EditLockTypeID EDIT_LOCK_TYPE_ID = EditLockTypeID.create(Organisation.DEV_ORGANISATION_ID, EditLockTypeOrder.class.getName());

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected EditLockTypeOrder() { }

	public EditLockTypeOrder(EditLockTypeID editLockTypeID)
	{
		super(editLockTypeID);
	}

	public EditLockTypeOrder(String organisationID, String editLockTypeID)
	{
		super(organisationID, editLockTypeID);
	}

	@Override
	public void onReleaseEditLock(EditLock editLock, ReleaseReason releaseReason)
	throws ModuleException
	{
		PersistenceManager pm = getPersistenceManager();
		pm.getExtent(Order.class);
		Order order = (Order) pm.getObjectById(editLock.getLockedObjectID());
		// release all products and delete articles if it's a QuickSaleOrder without finalized offers
		if (order.isQuickSaleWorkOrder()) {
			// check whether there are other EditLocks - maybe the user re-opened the Order after his client died when the power supply was interrupted
			if (EditLock.getEditLockCount(pm, editLock.getLockedObjectID()) == 1) {
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
						trader.releaseArticles(user, order.getArticles(), true, false, true);
						trader.deleteArticles(user, order.getArticles());
					}
				} // if (!containsFinalizedOffer) {
			} // if (EditLock.getEditLockCount(pm, editLock.getLockedObjectID()) == 1) {
		} // if (order.isQuickSaleWorkOrder()) {
	}
}
