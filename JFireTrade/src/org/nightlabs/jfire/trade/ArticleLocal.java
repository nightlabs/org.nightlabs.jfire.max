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

package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.InvoiceLocal;
import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;

import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.FetchGroups;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;
import org.nightlabs.jfire.trade.id.ArticleLocalID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.ArticleLocalID"
 *		detachable="true"
 *		table="JFireTrade_ArticleLocal"
 *
 * @jdo.version strategy="version-number"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, articleID"
 *
 * @jdo.fetch-group name="ArticleLocal.article" fields="article"
 *
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOrderEditor" fields="article"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInOfferEditor" fields="article"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInInvoiceEditor" fields="article"
 * @jdo.fetch-group name="FetchGroupsTrade.articleInDeliveryNoteEditor" fields="article"
 */
@PersistenceCapable(
	objectIdClass=ArticleLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_ArticleLocal")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@FetchGroups({
	@FetchGroup(
		name="ArticleLocal.article",
		members=@Persistent(name="article")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOrderEditor",
		members=@Persistent(name="article")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInOfferEditor",
		members=@Persistent(name="article")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInInvoiceEditor",
		members=@Persistent(name="article")),
	@FetchGroup(
		name="FetchGroupsTrade.articleInDeliveryNoteEditor",
		members=@Persistent(name="article"))
})
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class ArticleLocal
implements Serializable, DetachCallback
{
	private static final long serialVersionUID = 2L;

	// the following fetch-groups are virtual and processed in the detach callback
	public static final String FETCH_GROUP_DELIVERY_ID = "Article.deliveryID";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long articleID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Article article;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private Delivery delivery = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private DeliveryID deliveryID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private boolean deliveryID_detached = false;
	/**
	 * This is <code>true</code>, if {@link #delivery} is assigned.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private boolean delivered = false;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
	private Boolean invoicePaid = null;

	public void setDelivery(Delivery delivery)
	{
		this.delivery = delivery;
		this.delivered = delivery != null;
	}
	public Delivery getDelivery()
	{
		return delivery;
	}

	public boolean isDelivered()
	{
		return delivered;
	}

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
	private String allocateReleaseExecID;

	/**
	 * @deprecated Only for JDO!
	 */
	@Deprecated
	protected ArticleLocal() { }

	protected ArticleLocal(Article article)
	{
		this.article = article;
		this.organisationID = article.getOrganisationID();
		this.articleID = article.getArticleID();
//		article.setArticleLocal(this);
	}

	public String getOrganisationID()
	{
		return organisationID;
	}
	public long getArticleID()
	{
		return articleID;
	}

	public Article getArticle()
	{
		return article;
	}

	public DeliveryID getDeliveryID()
	{
		if (deliveryID == null && !deliveryID_detached)
			deliveryID = (DeliveryID) JDOHelper.getObjectId(delivery);

		return deliveryID;
	}

	protected PersistenceManager getPersistenceManager()
	{
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of ArticleLocal has currently no PersistenceManager assigned!");

		return pm;
	}

	/**
	 * Find out whether the invoice is paid completely (i.e. {@link InvoiceLocal#getAmountToPay()} is 0).
	 *
	 * @return <code>true</code>, if the amount-to-pay of the corresponding invoice is 0.
	 */
	public boolean isInvoicePaid()
	{
		if (invoicePaid == null) {
			Invoice invoice = getArticle().getInvoice();
			if (invoice == null)
				return false;

			// If an invoice is assigned, there should always be an InvoiceLocal.
			return invoice.getInvoiceLocal().getAmountToPay() == 0;
		}
		return invoicePaid.booleanValue();
	}

	public void jdoPreDetach()
	{
	}
	public void jdoPostDetach(Object _attached)
	{
		ArticleLocal attached = (ArticleLocal)_attached;
		ArticleLocal detached = this;

		Collection<?> fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

		boolean fetchGroupsArticleInEditor =
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_ORDER_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_OFFER_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_INVOICE_EDITOR) ||
			fetchGroups.contains(FetchGroupsTrade.FETCH_GROUP_ARTICLE_IN_DELIVERY_NOTE_EDITOR);

		detached.deliveryID = null;
		if (fetchGroupsArticleInEditor || fetchGroups.contains(FETCH_GROUP_DELIVERY_ID)) {
			detached.deliveryID = attached.getDeliveryID();
			detached.deliveryID_detached = true;
		}

		if (fetchGroupsArticleInEditor)
			detached.invoicePaid = attached.isInvoicePaid();
	}

	/**
	 * Get the current allocate/release exec identifier (which might be <code>null</code>).
	 *
	 * @return the identifier for the current allocate/release execution.
	 */
	public String getAllocateReleaseExecID() {
		return allocateReleaseExecID;
	}
	/**
	 * Set the current allocate/release exec identifier.
	 *
	 * @param allocateReleaseExecID the new identifier or <code>null</code>.
	 * @see #getAllocateReleaseExecID()
	 */
	public void setAllocateReleaseExecID(String allocateReleaseExecID) {
		this.allocateReleaseExecID = allocateReleaseExecID;
	}
}
