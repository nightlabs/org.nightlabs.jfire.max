package org.nightlabs.jfire.trade.recurring;


/**
 * Tagging interface implemented by {@link RecurringOrder} and {@link RecurringOffer}.
 * Mainly used for article-container-class-based registrations of UI in the client.
 * It might later get real methods, but for now, it's purely tagging.
 *
 * @author marco schulze - marco at nightlabs dot de
 */
public interface RecurringArticleContainer
//extends ArticleContainer // it doesn't matter whether this interface is extended - I only added it for testing my ArticleContainerActionRegistry and ArticleEditActionRegistry. Marco.
{

}
