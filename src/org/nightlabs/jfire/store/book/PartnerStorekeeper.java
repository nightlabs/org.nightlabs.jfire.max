/* *****************************************************************************
 * JFire - it's hot - Free ERP System - http://jfire.org                       *
 * Copyright (C) 2004-2005 NightLabs - http://NightLabs.org                    *
 *                                                                             *
 * This library is free software; you can redistribute it and/or               *
 * modify it under the terms of the GNU Lesser General Public                  *
 * License as published by the Free Software Foundation; either                *
 * version 2.1 of the License, or (at your option) any later version.          *
 *                                                                             *
 * This library is distributed in the hope that it will be useful,             *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of              *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU           *
 * Lesser General Public License for more details.                             *
 *                                                                             *
 * You should have received a copy of the GNU Lesser General Public            *
 * License along with this library; if not, write to the                       *
 *     Free Software Foundation, Inc.,                                         *
 *     51 Franklin St, Fifth Floor,                                            *
 *     Boston, MA  02110-1301  USA                                             *
 *                                                                             *
 * Or get it online :                                                          *
 *     http://opensource.org/licenses/lgpl-license.php                         *
 *                                                                             *
 *                                                                             *
 ******************************************************************************/

package org.nightlabs.jfire.store.book;

import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.DeliverProductTransfer;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		persistence-capable-superclass = "org.nightlabs.jfire.store.book.Storekeeper"
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
			ProductTransfer transfer, Map<String, Anchor> involvedAnchors)
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
