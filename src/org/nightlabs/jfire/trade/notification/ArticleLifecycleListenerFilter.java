package org.nightlabs.jfire.trade.notification;

import java.util.ArrayList;
import java.util.Collection;

import javax.jdo.PersistenceManager;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.jdo.notification.DirtyObjectID;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleListenerFilter;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleRemoteEvent;
import org.nightlabs.jfire.jdo.notification.JDOLifecycleState;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.Article;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;

public class ArticleLifecycleListenerFilter
extends JDOLifecycleListenerFilter
{
	private static final long serialVersionUID = 1L;

	private JDOLifecycleState[] jdoLifecycleStates;
	private ArticleContainerID articleContainerID;

	public ArticleLifecycleListenerFilter(JDOLifecycleState[] jdoLifecycleStates, ArticleContainerID articleContainerID)
	{
		this.jdoLifecycleStates = jdoLifecycleStates;
		this.articleContainerID = articleContainerID;
	}

	public ArticleContainerID getArticleContainerID()
	{
		return articleContainerID;
	}

	@Implement
	public Class[] getCandidateClasses()
	{
		return new Class[] { Article.class };
	}

	@Implement
	public JDOLifecycleState[] getLifecycleStates()
	{
		return jdoLifecycleStates;
	}

	@Implement
	public Collection<DirtyObjectID> filter(JDOLifecycleRemoteEvent event)
	{
		if (articleContainerID == null)
			return event.getDirtyObjectIDs();

		Collection<DirtyObjectID> res = null;
		PersistenceManager pm = event.getPersistenceManager();
		for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {
			Article article = (Article) pm.getObjectById(dirtyObjectID.getObjectID());
			boolean add = false;

			if (articleContainerID instanceof OrderID) {
				if (articleContainerID.equals(article.getOrderID()))
					add = true;
			}
			else if (articleContainerID instanceof OfferID) {
				if (articleContainerID.equals(article.getOfferID()))
					add = true;
			}
			else if (articleContainerID instanceof InvoiceID) {
				if (articleContainerID.equals(article.getInvoiceID()))
					add = true;
			}
			else if (articleContainerID instanceof DeliveryNoteID) {
				if (articleContainerID.equals(article.getDeliveryNoteID()))
					add = true;
			}
			else
				throw new IllegalStateException("articleContainerID is an instance of an unsupported class: " + articleContainerID);

			if (add) {
				if (res == null)
					res = new ArrayList<DirtyObjectID>();

				res.add(dirtyObjectID);
			}
		} // for (DirtyObjectID dirtyObjectID : event.getDirtyObjectIDs()) {

		return res;
	}
}
