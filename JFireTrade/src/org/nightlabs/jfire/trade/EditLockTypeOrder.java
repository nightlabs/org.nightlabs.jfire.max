package org.nightlabs.jfire.trade;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

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
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUPERCLASS_TABLE)
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.editlock.EditLockType#onReleaseEditLock(org.nightlabs.jfire.editlock.EditLock, org.nightlabs.jfire.editlock.ReleaseReason)
	 */
	@Override
	public void onReleaseEditLock(EditLock editLock, ReleaseReason releaseReason)
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
						Set<Article> articles_allocated_nothingPending = new HashSet<Article>(order.getArticles().size());
//						Set<Article> articles_nonAllocated_nothingPending = new HashSet<Article>();
						for (Article article : order.getArticles()) {
							if (article.isAllocated() && !article.isReleasePending())
								articles_allocated_nothingPending.add(article);
							else if (!article.isAllocated() && !article.isAllocationPending())
								; // nothing, we can directly delete it
//								articles_nonAllocated_nothingPending.add(article);
							else if (article.isReleasePending())
								throw new IllegalStateException("Article " + article + " is in state 'release pending' - have to retry later: " + article);
							else if (article.isAllocationPending())
								throw new IllegalStateException("Article " + article + " is in state 'allocation pending' - have to retry later: " + article);
							else
								throw new IllegalStateException("Article " + article + " is in unexpected state!!! " + article);
						}

						trader.releaseArticles(user, articles_allocated_nothingPending, true, false);
						trader.deleteArticles(user, order.getArticles());
					}
				} // if (!containsFinalizedOffer) {
			} // if (EditLock.getEditLockCount(pm, editLock.getLockedObjectID()) == 1) {
		} // if (order.isQuickSaleWorkOrder()) {
	}
}
