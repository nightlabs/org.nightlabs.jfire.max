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

package org.nightlabs.jfire.store;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.util.NLLocale;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.transfer.Anchor"
 *		detachable="true"
 *		table="JFireTrade_Repository"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.fetch-group name="Repository.owner" fields="owner"
 * @jdo.fetch-group name="Repository.name" fields="name"
 * @jdo.fetch-group name="Repository.repositoryType" fields="repositoryType"
 * @jdo.fetch-group name="Repository.this" fetch-groups="default, Anchor.this" fields="owner, name, repositoryType"
 */
@PersistenceCapable(
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_Repository")
@FetchGroups({
	@FetchGroup(
		name=Repository.FETCH_GROUP_OWNER,
		members=@Persistent(name="owner")),
	@FetchGroup(
		name=Repository.FETCH_GROUP_NAME,
		members=@Persistent(name="name")),
	@FetchGroup(
		name=Repository.FETCH_GROUP_REPOSITORY_TYPE,
		members=@Persistent(name="repositoryType")),
	@FetchGroup(
		fetchGroups={"default", "Anchor.this"},
		name=Repository.FETCH_GROUP_THIS_REPOSITORY,
		members={@Persistent(name="owner"), @Persistent(name="name"), @Persistent(name="repositoryType")})
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Repository extends Anchor
{
	private static final long serialVersionUID = 1L;

	public static final String FETCH_GROUP_OWNER = "Repository.owner";
	public static final String FETCH_GROUP_NAME = "Repository.name";
	public static final String FETCH_GROUP_REPOSITORY_TYPE = "Repository.repositoryType";
	/**
	 * @deprecated The *.this-FetchGroups lead to bad programming style and are therefore deprecated, now. They should be removed soon! 
	 */
	public static final String FETCH_GROUP_THIS_REPOSITORY = "Repository.this";

//	/**
//	 * Local products will be created in a repository with this type. Foreign products, however, are created in
//	 * a repository with {@link #ANCHOR_TYPE_ID_OUTSIDE}. Foreign products are transferred here (i.e. to their home)
//	 * during the booking of their DeliveryNote / ReceptionNote.
//	 * <p>
//	 * Both local and foreign products are transferred from
//	 * this repository to the container product's repository
//	 * when the container product is assembled.
//	 * </p>
//	 * <p>
//	 * Both local and foreign products are transferred into
//	 * this repository (from the container's repository) when
//	 * the container product is DISassembled.
//	 * </p>
//	 */
//	public static final String ANCHOR_TYPE_ID_HOME = "Repository.Home";
//
////	/**
////   * A repository with this type is the source of products - they are created here. Therefore,
////   * it's used as initial repository for local products and (if a local product nests foreign products)
////   * foreign products might be transferred here during the assemble process. It should have {@link #isOutside()}<code> == false</code>.
////   */
////	public static final String ANCHOR_TYPE_ID_FACTORY = "Repository.Factory";
//
////	/**
////	 * A repository with this type is inside the organisation. It should have {@link #outside}<code> == false</code>!
////	 */
////	public static final String ANCHOR_TYPE_ID_BIN = "Repository.Bin";
//
//	/**
//	 * A repository with this type is used for virtually outside repositories. If a <tt>Product</tt>
//	 * is there, it means that the product is not here anymore (or not yet) and has already been delivered
//	 * to a partner (or not yet been delivered from a supplier). It should have {@link #outside} == true!
//	 */
//	public static final String ANCHOR_TYPE_ID_OUTSIDE = "Repository.Outside";

//	/**
//	 * Repositories with this type are used by {@link ServerDeliveryProcessor}s to transfer their
//	 * goods from and to. They're outside, hence they should have {@link #isOutside()}<code> == true</code>.
//	 */
//	public static final String ANCHOR_TYPE_ID_DELIVERY = "Repository.Delivery";

	public static final String ANCHOR_TYPE_ID_REPOSITORY = "Repository";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private LegalEntity owner;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="repository"
	 */
	@Persistent(
		dependent="true",
		mappedBy="repository",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private RepositoryName name;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private RepositoryType repositoryType;

//	/**
//	 * Whether or not this Repository represents sth. outside.
//	 *
//	 * @jdo.field persistence-modifier="persistent"
//	 */
//	private boolean outside;

	public static Repository createRepository(
			PersistenceManager pm, String organisationID, String anchorID, RepositoryType repositoryType, LegalEntity owner)
	{
		Repository repository;
		try {
			repository = (Repository) pm.getObjectById(AnchorID.create(organisationID, ANCHOR_TYPE_ID_REPOSITORY, anchorID));
		} catch (JDOObjectNotFoundException x) {
			repository = null;
		}

		if (repository == null) { // create persist it
			repository = new Repository(organisationID, anchorID, repositoryType, owner);
			repository = pm.makePersistent(repository);
		}
		else { // check, whether owner and outside is correct
			if (!repository.getOwner().equals(owner))
				throw new IllegalArgumentException("The repository \"" + repository.getPrimaryKey() + "\" already exists but does not match the specified owner \"" + owner.getPrimaryKey() + "\" - it already has the owner \"" + repository.getOwner().getPrimaryKey() + "\" assigned!");

//			if (repository.isOutside() != outside)
//				throw new IllegalArgumentException("The repository \"" + repository.getPrimaryKey() + "\" already exists but does not match the specified outside flag \"" + outside + "\" - it already has outside \"" + repository.isOutside() + "\" assigned!");
			if (!repository.getRepositoryType().equals(repositoryType))
				throw new IllegalArgumentException("The repository \"" + repository.getPrimaryKey() + "\" already exists but does not match the specified RepositoryType \"" + JDOHelper.getObjectId(repositoryType) + "\" - it already has \"" + JDOHelper.getObjectId(repository.getRepositoryType()) + "\" assigned!");
		}

		return repository;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected Repository()
	{
	}

	/**
	 * @param organisationID
	 * @param anchorTypeID
	 * @param anchorID
	 */
	public Repository(
			String organisationID, String anchorID,
			RepositoryType repositoryType,
			LegalEntity owner)
	{
		super(organisationID, ANCHOR_TYPE_ID_REPOSITORY, anchorID);
		
		if (repositoryType == null)
			throw new IllegalArgumentException("repositoryType must not be null!");

		if (owner == null)
			throw new IllegalArgumentException("owner must not be null!");

		this.repositoryType = repositoryType;
		this.owner = owner;
		this.name = new RepositoryName(this);

//		if (!organisationID.equals(owner.getOrganisationID())) // TODO remove this temporary test - it is a legal state, but it should not occur in the current situation at all! it does already occur!
//			throw new IllegalArgumentException("organisationID != owner.organisationID!!! organisationID=\"" + organisationID + "\" owner.organisationID=\"" + owner.getOrganisationID() + "\"");
	}

	public RepositoryType getRepositoryType()
	{
		return repositoryType;
	}

	@Override
	protected void internalBookTransfer(Transfer transfer, User user,
			Set<Anchor> involvedAnchors)
	{
		ProductTransfer productTransfer = (ProductTransfer) transfer;

//		Anchor from = transfer.getFrom();
//		Anchor to = transfer.getTo();
		boolean thisIsFrom = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM;
		boolean thisIsTo = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO;

		PersistenceManager pm = getPersistenceManager();

		// after we delegated to the storekeeper, we handle the product transfer and update ProductReference.quantity
		for (Iterator it = productTransfer.getProducts().iterator(); it.hasNext(); ) {
			Product product = (Product) it.next();
			if (thisIsFrom)
				ProductReference.createProductReference(pm, this, product).decQuantity();
			else if (thisIsTo)
				ProductReference.createProductReference(pm, this, product).incQuantity();
			else
				throw new IllegalStateException("This Repository (" + getPrimaryKey() + ") is neither to nor from!");
//			ProductReference.createProductReference(pm, to, product).incQuantity();
			if (getRepositoryType().isOutside()) {
				int val = thisIsFrom ? +1 : -1;
				product.getProductLocal().incQuantity(val); // nested products are handled during assembling/disassembling
//				incProductLocalQuantity(product, val);
//				if (thisIsFrom)
//					product.getProductLocal().incQuantity();
//				else
//					product.getProductLocal().decQuantity();
			}
		}
	}

	@Override
	protected void internalRollbackTransfer(Transfer transfer, User user,
			Set<Anchor> involvedAnchors)
	{
		ProductTransfer productTransfer = (ProductTransfer) transfer;

//		Anchor from = transfer.getFrom();
//		Anchor to = transfer.getTo();
		boolean thisIsFrom = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_FROM;
		boolean thisIsTo = transfer.getAnchorType(this) == Transfer.ANCHORTYPE_TO;

		PersistenceManager pm = getPersistenceManager();

		for (Iterator it = productTransfer.getProducts().iterator(); it.hasNext(); ) {
			Product product = (Product) it.next();
			if (thisIsFrom)
				ProductReference.createProductReference(pm, this, product).incQuantity();
			else if (thisIsTo)
				ProductReference.createProductReference(pm, this, product).decQuantity();
			else
				throw new IllegalStateException("This Repository (" + getPrimaryKey() + ") is neither to nor from!");

			if (getRepositoryType().isOutside()) {
				int val = thisIsFrom ? -1 : +1;
				product.getProductLocal().incQuantity(val); // nested products are handled during assembling/disassembling
//				incProductLocalQuantity(product, val);
//				if (thisIsFrom)
//					product.getProductLocal().decQuantity();
//				else
//					product.getProductLocal().incQuantity();
			}
		}
	}

//	protected void incProductLocalQuantity(Product product, int val)
//	{
//		ProductLocal productLocal = product.getProductLocal();
//		productLocal.incQuantity(val);
//		for (Iterator it = productLocal.getNestedProducts().iterator(); it.hasNext(); )
//			incProductLocalQuantity((Product) it.next(), val);
//	}

	@Override
	public void checkIntegrity(Collection<? extends Transfer> containers)
	{
	}
	@Override
	public void resetIntegrity(Collection<? extends Transfer> containers)
	{
	}

	/**
	 * @return Returns the owner.
	 */
	public LegalEntity getOwner()
	{
		return owner;
	}

	public RepositoryName getName()
	{
		return name;
	}

	@Override
	protected String internalGetDescription() {
		return getName().getText(NLLocale.getDefault().getLanguage());
	}

}
