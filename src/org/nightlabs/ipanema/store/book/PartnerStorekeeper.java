/*
 * Created on Oct 21, 2005
 */
package org.nightlabs.ipanema.store.book;

import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.store.ProductTransfer;
import org.nightlabs.ipanema.store.Repository;
import org.nightlabs.ipanema.store.Store;
import org.nightlabs.ipanema.store.deliver.DeliverProductTransfer;
import org.nightlabs.ipanema.trade.LegalEntity;
import org.nightlabs.ipanema.transfer.Transfer;
import org.nightlabs.ipanema.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		persistence-capable-superclass = "org.nightlabs.ipanema.store.book.Storekeeper"
 *		detachable = "true"
 *		table="JFireTrade_PartnerStorekeeper"
 *
 * @jdo.inheritance strategy = "new-table"
 */
public class PartnerStorekeeper extends Storekeeper
{
	/**
	 * @deprecated Only for JDO!
	 */
	protected PartnerStorekeeper() { }

	public PartnerStorekeeper(String organisationID, String storekeeperID)
	{
		super(organisationID, storekeeperID);
	}

	public void bookTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Map involvedAnchors)
	{
		if (transfer instanceof BookProductTransfer)
			handleBookOrDeliveryProductTransfer(user, mandator, transfer, involvedAnchors);
//			handleBookProductTransfer(user, mandator, (BookProductTransfer) transfer, involvedAnchors);
		else if (transfer instanceof DeliverProductTransfer)
			handleBookOrDeliveryProductTransfer(user, mandator, transfer, involvedAnchors);
//			handleDeliverProductTransfer(user, mandator, (DeliverProductTransfer) transfer, involvedAnchors);
		else
			handleSimpleProductTransfer(user, mandator, transfer, involvedAnchors);
	}

	protected static Repository createPartnerInsideRepository(
			PersistenceManager pm, String organisationID, LegalEntity mandator)
	{
		String anchorID = mandator.getOrganisationID() + '.' + mandator.getAnchorTypeID() + '.' + mandator.getAnchorID();
		try {
			return (Repository) pm.getObjectById(AnchorID.create(
					organisationID,
					Repository.ANCHOR_TYPE_ID_BIN,
					anchorID));
		} catch (JDOObjectNotFoundException x) {
			Repository repository = new Repository(organisationID, Repository.ANCHOR_TYPE_ID_BIN, anchorID, mandator, false);
			pm.makePersistent(repository);
			return repository;
		}
	}

	public static Repository createPartnerOutsideRepository(
			PersistenceManager pm, String organisationID, LegalEntity mandator)
	{
		String anchorID = mandator.getOrganisationID() + '.' + mandator.getAnchorTypeID() + '.' + mandator.getAnchorID();
		try {
			return (Repository) pm.getObjectById(AnchorID.create(
					organisationID,
					Repository.ANCHOR_TYPE_ID_OUTSIDE,
					anchorID));
		} catch (JDOObjectNotFoundException x) {
			Repository repository = new Repository(organisationID, Repository.ANCHOR_TYPE_ID_OUTSIDE, anchorID, mandator, true);
			pm.makePersistent(repository);
			return repository;
		}
	}

	protected void handleBookOrDeliveryProductTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Map involvedAnchors)
	{
		PersistenceManager pm = getPersistenceManager();
		Repository partnerInsideRepository = createPartnerInsideRepository(
				pm, getOrganisationID(), mandator);

		Store store = Store.getStore(pm);

		if (transfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_FROM) {
			// In this case, the products are transferred from the inside
			// perPartner-Repository to the mandator.
			ProductTransfer productTransfer = new ProductTransfer(
					store, transfer, user, partnerInsideRepository, mandator, transfer.getProducts());
			productTransfer.bookTransfer(user, involvedAnchors);
		}
		else if (transfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_TO) {
			// In this case, the products come from the mandator and are transferred
			// to the inside perPartner-Repository.
			ProductTransfer productTransfer = new ProductTransfer(
					store, transfer, user, mandator, partnerInsideRepository, transfer.getProducts());
			productTransfer.bookTransfer(user, involvedAnchors);
		}
		else
			throw new IllegalStateException("mandator is neither 'from' nor 'to' of transfer!");
	}

	protected void handleSimpleProductTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Map involvedAnchors)
	{
		// nothing to do
	}
}
