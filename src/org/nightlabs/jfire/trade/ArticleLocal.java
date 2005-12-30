/*
 * Created on Nov 7, 2005
 */
package org.nightlabs.jfire.trade;

import java.io.Serializable;
import java.util.Collection;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.listener.DetachCallback;

import org.nightlabs.jfire.store.deliver.Delivery;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.trade.id.ArticleLocalID"
 *		detachable="true"
 *		table="JFireTrade_ArticleLocal"
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
public class ArticleLocal
implements Serializable, DetachCallback
{
	// the following fetch-groups are virtual and processed in the detach callback
	public static final String FETCH_GROUP_DELIVERY_ID = "Article.deliveryID";

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;

	/**
	 * @jdo.field primary-key="true"
	 */
	private long articleID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Article article;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Delivery delivery = null;

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private DeliveryID deliveryID = null;
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private boolean deliveryID_detached = false;
	/**
	 * This is <code>true</code>, if {@link #delivery} is assigned.
	 *
	 * @jdo.field persistence-modifier="persistent"
	 */
	private boolean delivered = false;

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
	 * @deprecated Only for JDO!
	 */
	protected ArticleLocal() { }

	public ArticleLocal(Article article)
	{
		this.article = article;
		this.organisationID = article.getOrganisationID();
		this.articleID = article.getArticleID();
		article.setArticleLocal(this);
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

	public void jdoPreDetach()
	{
	}
	public void jdoPostDetach(Object _detached)
	{
		ArticleLocal attached = this;
		ArticleLocal detached = (ArticleLocal)_detached;

		Collection fetchGroups = attached.getPersistenceManager().getFetchPlan().getGroups();

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
	}
}
