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

import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.apache.log4j.Logger;
import org.nightlabs.jfire.idgenerator.IDGenerator;
import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.RepositoryType;
import org.nightlabs.jfire.store.Store;
import org.nightlabs.jfire.store.deliver.DeliverProductTransfer;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessorJFire;
import org.nightlabs.jfire.store.deliver.ServerDeliveryProcessor.DeliverParams;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.trade.OrganisationLegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;

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
	private static final long serialVersionUID = 1L;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected PartnerStorekeeper() { }

	public PartnerStorekeeper(String organisationID, String storekeeperID)
	{
		super(organisationID, storekeeperID);
	}

	@Override
	public void bookTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Set<Anchor> involvedAnchors)
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

	/**
	 * @param pm Accessor to the datastore.
	 * @param organisationID The organisationID of the new Repository.
	 * @param repositoryOwner This will be the owner of the new repository. Should be the partner!
	 * @return
	 */
	protected static Repository createPartnerInsideRepository( // the partner must be the owner of this inside repository
			PersistenceManager pm, String organisationID, LegalEntity repositoryOwner)
	{
		// TODO remove this! begin DEBUG check
		if (repositoryOwner instanceof OrganisationLegalEntity) {
			OrganisationLegalEntity rOwner = (OrganisationLegalEntity) repositoryOwner;
			if (rOwner.getOrganisation().equals(IDGenerator.getOrganisationID()))
				Logger.getLogger(PartnerStorekeeper.class).warn("", new IllegalStateException("The repositoryOwner is the local organisation but should be the partner!"));
		}
		// TODO remove this! end DEBUG check

		String anchorID = RepositoryType.ANCHOR_TYPE_ID_PREFIX_PARTNER + repositoryOwner.getOrganisationID() + '#' + repositoryOwner.getAnchorTypeID() + '#' + repositoryOwner.getAnchorID();
		RepositoryType repositoryType = (RepositoryType) pm.getObjectById(RepositoryType.REPOSITORY_TYPE_ID_PARTNER);
		return Repository.createRepository(pm, organisationID, anchorID, repositoryType, repositoryOwner);

//		try {
//			return (Repository) pm.getObjectById(AnchorID.create(
//					organisationID,
//					Repository.ANCHOR_TYPE_ID_BIN,
//					anchorID));
//		} catch (JDOObjectNotFoundException x) {
//			Repository repository = new Repository(organisationID, Repository.ANCHOR_TYPE_ID_BIN, anchorID, mandator, false);
//			pm.makePersistent(repository);
//			return repository;
//		}
	}

	/**
	 * @param pm Accessor to the datastore.
	 * @param organisationID The organisationID of the new Repository.
	 * @param repositoryOwner This will be the owner of the new repository. Should be the partner!
	 * @return
	 */
	public static Repository createPartnerOutsideRepository(PersistenceManager pm, String organisationID, LegalEntity repositoryOwner)
	{
		return createPartnerOutsideRepository(pm, organisationID, repositoryOwner, null);
	}

	public static Repository createPartnerOutsideRepository(PersistenceManager pm, String organisationID, LegalEntity repositoryOwner, String anchorIDPrefix)
	{
		// TODO remove this! begin DEBUG check
		Logger.getLogger(PartnerStorekeeper.class).info("createPartnerOutsideRepository: organisationID=" + organisationID + " repositoryOwner=" + repositoryOwner.getPrimaryKey() + " IDGenerator.getOrganisationID()=" + IDGenerator.getOrganisationID());

		if (repositoryOwner instanceof OrganisationLegalEntity) {
			OrganisationLegalEntity rOwner = (OrganisationLegalEntity) repositoryOwner;
			if (rOwner.getOrganisationID().equals(IDGenerator.getOrganisationID()))
				Logger.getLogger(PartnerStorekeeper.class).warn("", new IllegalStateException("The repositoryOwner is the local organisation but should be the partner! organisationID=" + organisationID + " repositoryOwner=" + repositoryOwner.getPrimaryKey() + " IDGenerator.getOrganisationID()=" + IDGenerator.getOrganisationID()));
			if (rOwner.getOrganisation() == null)
				Logger.getLogger(PartnerStorekeeper.class).warn("", new IllegalStateException("repositoryOwner.getOrganisation() returns null! repositoryOwner.getOrganisationID()=" + repositoryOwner.getOrganisationID()));
		}
		// TODO remove this! end DEBUG check

		String anchorID = repositoryOwner.getOrganisationID() + '#' + repositoryOwner.getAnchorTypeID() + '#' + repositoryOwner.getAnchorID();
		if (anchorIDPrefix != null && !"".equals(anchorIDPrefix))
			anchorID = anchorIDPrefix + '#' + anchorID;

		anchorID = RepositoryType.ANCHOR_TYPE_ID_PREFIX_OUTSIDE + anchorID;
		RepositoryType repositoryType = (RepositoryType) pm.getObjectById(RepositoryType.REPOSITORY_TYPE_ID_OUTSIDE);
		return Repository.createRepository(pm, organisationID, anchorID, repositoryType, repositoryOwner);

//		try {
//			return (Repository) pm.getObjectById(AnchorID.create(
//					organisationID,
//					Repository.ANCHOR_TYPE_ID_OUTSIDE,
//					anchorID));
//		} catch (JDOObjectNotFoundException x) {
//			Repository repository = new Repository(organisationID, Repository.ANCHOR_TYPE_ID_OUTSIDE, anchorID, mandator, true);
//			pm.makePersistent(repository);
//			return repository;
//		}
	}

	/**
	 * This method is called by {@link Store#addProduct(User, Product)}, if the product is a foreign product
	 * in order to get the repository in which the new product will be initially located. It must be
	 * an outside repository, because the product will later be delivered (it is not yet here).
	 * <p>
	 * The default implementation of this method in {@link PartnerStorekeeper} calls
	 * {@link #createPartnerOutsideRepository(PersistenceManager, String, LegalEntity)}s
	 * which is used by the delivery processor {@link ServerDeliveryProcessorJFire} as well (it's the default repository as returned by
	 * {@link ServerDeliveryProcessor#getRepositoryOutside(DeliverParams deliverParams)}).
	 * </p>
	 *
	 * @param product the product for which to find the initial repository. Usually, only the product's organisationID is interesting, 
	 *		but there might be different repositories used for different products. The delivery might in this case be performed
	 *		using another outside-repository. This is fine, because the chains might be interrupted in outside-repositories since
	 *		we don't know what's happening outside (it can leave through outside-A and come back later to outside-B).
	 * @return the repository in which to initially locate the given foreign product.
	 */
	public Repository getInitialRepositoryForForeignProduct(Product product)
	{
		String partnerOrganisationID = product.getOrganisationID();
		PersistenceManager pm = getPersistenceManager();
		Store store = Store.getStore(pm);

		if (partnerOrganisationID.equals(store.getOrganisationID()))
			throw new IllegalStateException("This method should never be called for a local product! " + JDOHelper.getObjectId(product));

		LegalEntity repositoryOwner = OrganisationLegalEntity.getOrganisationLegalEntity(
				pm, partnerOrganisationID, true);

		return createPartnerOutsideRepository(pm, store.getOrganisationID(), repositoryOwner);
	}

	protected void handleBookOrDeliveryProductTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Set<Anchor> involvedAnchors)
	{
		PersistenceManager pm = getPersistenceManager();
		Repository partnerInsideRepository = createPartnerInsideRepository(
				pm, getOrganisationID(), mandator);

		if (transfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_FROM) {
			// In this case, the products are transferred from the inside
			// perPartner-Repository to the mandator.
			ProductTransfer productTransfer = new ProductTransfer(
					transfer, user, partnerInsideRepository, mandator, transfer.getProducts());
			productTransfer = pm.makePersistent(productTransfer);
			productTransfer.bookTransfer(user, involvedAnchors);
		}
		else if (transfer.getAnchorType(mandator) == Transfer.ANCHORTYPE_TO) {
			// In this case, the products come from the mandator and are transferred
			// to the inside perPartner-Repository.
			ProductTransfer productTransfer = new ProductTransfer(
					transfer, user, mandator, partnerInsideRepository, transfer.getProducts());
			productTransfer = pm.makePersistent(productTransfer);
			productTransfer.bookTransfer(user, involvedAnchors);
		}
		else
			throw new IllegalStateException("mandator is neither 'from' nor 'to' of transfer!");
	}

	protected void handleSimpleProductTransfer(User user, LegalEntity mandator,
			ProductTransfer transfer, Set<Anchor> involvedAnchors)
	{
		// nothing to do
	}
}
