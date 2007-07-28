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
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.trade.LegalEntity;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.AnchorID;

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
 */
public class Repository extends Anchor
{
	/**
	 * A repository with this type is the source of products - they are created here.
	 */
	public static final String ANCHOR_TYPE_ID_HOME = "Repository.Home";

	/**
	 * A repository with this type is inside the organisation. It should have {@link #outside} == false!
	 */
	public static final String ANCHOR_TYPE_ID_BIN = "Repository.Bin";

	/**
	 * A repository with this type is used for virtually outside repositories. If a <tt>Product</tt>
	 * is there, it means that the product is not here anymore and has already been delivered
	 * to a partner. It should have {@link #outside} == true!
	 */
	public static final String ANCHOR_TYPE_ID_OUTSIDE = "Repository.Outside";

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private LegalEntity owner;

	/**
	 * @jdo.field persistence-modifier="persistent" dependent="true" mapped-by="repository"
	 */
	private RepositoryName name;

	/**
	 * Whether or not this Repository represents sth. outside.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean outside;

	public static Repository createRepository(
			PersistenceManager pm, String organisationID, String anchorTypeID, String anchorID, LegalEntity owner, boolean outside)
	{
		Repository repository;
		try {
			repository = (Repository) pm.getObjectById(
					AnchorID.create(organisationID, anchorTypeID, anchorID));
		} catch (JDOObjectNotFoundException x) {
			repository = new Repository(organisationID, anchorTypeID, anchorID, owner, outside);
			repository = pm.makePersistent(repository);
		}
		return repository;
	}

	/**
	 * @deprecated Only for JDO!
	 */
	protected Repository()
	{
	}

	/**
	 * @param organisationID
	 * @param anchorTypeID
	 * @param anchorID
	 */
	public Repository(
			String organisationID, String anchorTypeID, String anchorID,
			LegalEntity owner, boolean outside)
	{
		super(organisationID, anchorTypeID, anchorID);

		if (owner == null)
			throw new IllegalArgumentException("owner must not be null!");

		this.owner = owner;
		this.name = new RepositoryName(this);
		this.outside = outside;

//		if (!organisationID.equals(owner.getOrganisationID())) // TODO remove this temporary test - it is a legal state, but it should not occur in the current situation at all! it does already occur!
//			throw new IllegalArgumentException("organisationID != owner.organisationID!!! organisationID=\"" + organisationID + "\" owner.organisationID=\"" + owner.getOrganisationID() + "\"");
	}

	protected void internalBookTransfer(Transfer transfer, User user,
			Map<String, Anchor> involvedAnchors)
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
			if (isOutside()) {
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

	protected void internalRollbackTransfer(Transfer transfer, User user,
			Map involvedAnchors)
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

			if (isOutside()) {
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

	public void checkIntegrity(Collection<Transfer> containers)
	{
	}
	public void resetIntegrity(Collection<Transfer> containers)
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

	public boolean isOutside()
	{
		return outside;
	}
}
