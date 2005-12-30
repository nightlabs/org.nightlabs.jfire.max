/*
 * Created on Jun 10, 2005
 */
package org.nightlabs.ipanema.store.deliver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.nightlabs.ipanema.accounting.MoneyTransfer;
import org.nightlabs.ipanema.security.User;
import org.nightlabs.ipanema.store.Product;
import org.nightlabs.ipanema.store.ProductTransfer;
import org.nightlabs.ipanema.store.deliver.id.DeliveryID;
import org.nightlabs.ipanema.trade.Article;
import org.nightlabs.ipanema.transfer.Anchor;
import org.nightlabs.ipanema.transfer.Transfer;
import org.nightlabs.ipanema.transfer.TransferRegistry;
import org.nightlabs.ipanema.transfer.id.TransferID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable 
 *		identity-type="application"
 *		persistence-capable-superclass="org.nightlabs.ipanema.store.ProductTransfer"
 *		detachable="true"
 *		table="JFireTrade_DeliverProductTransfer"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.persistence-capable 
 *		identity-type = "application"
 *		persistence-capable-superclass = "org.nightlabs.ipanema.store.ProductTransfer"
 *		detachable = "true"
 *
 * @jdo.inheritance strategy = "new-table"
 *
 * @jdo.query
 *		name = "getDeliverProductTransferForDelivery"
 *		query = "SELECT UNIQUE
 *				WHERE
 *					this.delivery.organisationID == paramOrganisationID &&
 *					this.delivery.deliveryID == paramDeliveryID
 *				PARAMETERS String paramOrganisationID, String paramDeliveryID
 *				IMPORTS import java.lang.String"
 */
public class DeliverProductTransfer extends ProductTransfer
{
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
			String organisationID, String deliveryID)
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
	protected DeliverProductTransfer()
	{
	}

	/**
	 * @param articles Instances of {@link org.nightlabs.ipanema.trade.Article}.
	 * @return Returns instances of {@link org.nightlabs.ipanema.store.Product}.
	 */
	protected static Collection getProductsFromArticles(Collection articles)
	{
		ArrayList res = new ArrayList(articles.size());
		for (Iterator it = articles.iterator(); it.hasNext(); ) {
			Article article = (Article) it.next();
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
	public DeliverProductTransfer(TransferRegistry transferRegistry,
			Transfer container, User initiator, Anchor from, Anchor to,
			Delivery delivery)
	{
		super(transferRegistry, container, initiator, from, to, getProductsFromArticles(delivery.getArticles()));
		this.delivery = delivery;
	}

	public Delivery getDelivery()
	{
		return delivery;
	}
}
