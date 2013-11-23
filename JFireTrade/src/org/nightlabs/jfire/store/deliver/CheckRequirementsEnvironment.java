/**
 * 
 */
package org.nightlabs.jfire.store.deliver;

import java.io.Serializable;
import java.util.Set;

import org.nightlabs.jfire.trade.id.ArticleID;

public class CheckRequirementsEnvironment
implements Serializable
{
	private static final long serialVersionUID = 1L;

	public CheckRequirementsEnvironment(String deliveryDirection, Set<ArticleID> articleIDs)
	{
		if (!Delivery.DELIVERY_DIRECTION_INCOMING.equals(deliveryDirection) &&
				!Delivery.DELIVERY_DIRECTION_OUTGOING.equals(deliveryDirection))
			throw new IllegalArgumentException("deliveryDirection invalid!");

		if (articleIDs == null)
			throw new IllegalArgumentException("articleIDs must not be null!");

		this.deliveryDirection = deliveryDirection;
		this.articleIDs = articleIDs;
	}

	private String deliveryDirection;
	public String getDeliveryDirection()
	{
		return deliveryDirection;
	}

	private Set<ArticleID> articleIDs;
	public Set<ArticleID> getArticleIDs()
	{
		return articleIDs;
	}
}