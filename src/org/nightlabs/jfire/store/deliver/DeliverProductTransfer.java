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

package org.nightlabs.jfire.store.deliver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.jfire.security.User;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.Product;
import org.nightlabs.jfire.store.ProductTransfer;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.transfer.Anchor;
import org.nightlabs.jfire.transfer.Transfer;
import org.nightlabs.jfire.transfer.id.TransferID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.jfire.store.ProductTransfer"
 *		detachable="true"
 *		table="JFireTrade_DeliverProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.persistence-capable
 *		identity-type = "application"
 *		persistence-capable-superclass = "org.nightlabs.jfire.store.ProductTransfer"
 *		detachable = "true"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.query
 *		name = "getDeliverProductTransferForDelivery"
 *		query = "SELECT UNIQUE
 *				WHERE
 *					this.delivery.organisationID == :paramOrganisationID &&
 *					this.delivery.deliveryID == :paramDeliveryID
 *				import java.lang.String; import java.lang.Long"
 */
public class DeliverProductTransfer extends ProductTransfer
{
	private static final long serialVersionUID = 1L;

	public static Collection getChildren(
			PersistenceManager pm, TransferID transferID)
	{
		return getChildren(pm, transferID.organisationID, transferID.transferTypeID, transferID.transferID);
	}

	public static Collection getChildren(
			PersistenceManager pm, String organisationID, String transferTypeID, long transferID)
	{
		Query query = pm.newQuery(ProductTransfer.class);
		query.declareImports("import java.lang.String");
		query.declareParameters("String paramOrganisationID, String paramTransferTypeID, long paramTransferID");
		query.setFilter(
				"this.container.organisationID == paramOrganisationID && " +
				"this.container.transferTypeID == paramTransferTypeID && " +
				"this.container.transferID == paramTransferID");
		return (Collection)query.execute(organisationID, transferTypeID, new Long(transferID));
	}

	/**
	 * This method searches via JDO all <tt>ProductTransfer</tt>s which have
	 * {@link Transfer#getContainer()}<tt> == this</tt>.
	 */
	public Collection getChildren()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("Currently not attached to datastore! Cannot obtain my PersistenceManager!");
		return getChildren(pm, getOrganisationID(), getTransferTypeID(), getTransferID());
	}

	public static DeliverProductTransfer getDeliverProductTransferForDelivery(
			PersistenceManager pm,
			DeliveryID deliveryID)
	{
		return getDeliverProductTransferForDelivery(pm, deliveryID.organisationID, deliveryID.deliveryID);
	}

	public static DeliverProductTransfer getDeliverProductTransferForDelivery(
			PersistenceManager pm,
			Delivery delivery)
	{
		return getDeliverProductTransferForDelivery(pm, delivery.getOrganisationID(), delivery.getDeliveryID());
	}

	/**
	 * @param pm The PM to access the datastore.
	 * @param organisationID see {@link Delivery#getOrganisationID()}
	 * @param deliveryID see {@link Delivery#getDeliveryID()}
	 *
	 * @return Either <tt>null</tt> or the instance of <tt>DeliverProductTransfer</tt> that
	 *		has previously been created for the specified {@link Delivery}.
	 */
	public static DeliverProductTransfer getDeliverProductTransferForDelivery(
			PersistenceManager pm,
			String organisationID, long deliveryID)
	{
		Query query = pm.newNamedQuery(DeliverProductTransfer.class, "getDeliverProductTransferForDelivery");
		return (DeliverProductTransfer) query.execute(organisationID, deliveryID);
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Delivery delivery;

	/**
	 * @deprecated Only of JDO!
	 */
	@Deprecated
	protected DeliverProductTransfer()
	{
	}

	/**
	 * @param articles Instances of {@link org.nightlabs.jfire.trade.Article}.
	 * @return Returns instances of {@link org.nightlabs.jfire.store.Product}.
	 */
	protected static Collection<Product> getProductsFromArticles(Collection<? extends Article> articles)
	{
		List<Product> res = new ArrayList<Product>(articles.size());
		for (Iterator<? extends Article> it = articles.iterator(); it.hasNext(); ) {
			Article article = it.next();
			Product product = article.getProduct();
			if (product == null)
				throw new IllegalArgumentException("Article \"" + article.getPrimaryKey() + "\" does not contain a Product!");
			res.add(product);
		}
		return res;
	}

	/**
	 * @param transferRegistry
	 * @param container
	 * @param initiator
	 * @param from
	 * @param to
	 * @param delivery
	 */
	public DeliverProductTransfer(
			Transfer container, User initiator, Anchor from, Anchor to,
			Delivery delivery)
	{
		super(container, initiator, from, to, getProductsFromArticles(delivery.getArticles()));
		this.delivery = delivery;
	}

	public Delivery getDelivery()
	{
		return delivery;
	}

	@Override
	public void bookTransfer(User user, Set<Anchor> involvedAnchors)
	{
		super.bookTransfer(user, involvedAnchors);
		bookAtDeliveryNotes(user, involvedAnchors, false);
	}

	private void bookAtDeliveryNotes(User user, Set<Anchor> involvedAnchors, boolean rollback)
	{
		Set<DeliveryNote> processedDeliveryNotes = new HashSet<DeliveryNote>();
		for (Article article : delivery.getArticles()) {
			DeliveryNote deliveryNote = article.getDeliveryNote();
			if (!processedDeliveryNotes.add(deliveryNote))
				continue;

			deliveryNote.bookDeliverProductTransfer(this, involvedAnchors, rollback);
		}
	}

	@Override
	public void rollbackTransfer(User user, Set<Anchor> involvedAnchors)
	{
		bookAtDeliveryNotes(user, involvedAnchors, true);
		super.rollbackTransfer(user, involvedAnchors);
	}
	
	@Override
	protected String internalGetDescription() {
		return String.format(
				"Delivery %s",
				getDelivery().getPrimaryKey()
			);
	}
}
