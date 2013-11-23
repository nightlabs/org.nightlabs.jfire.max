package org.nightlabs.jfire.trade;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public interface ArticleContainerListener
{
	void articleAdded(Article article);
	
	void articleRemoved(Article article);
	
	void articlesChanged(ArticleContainer container);
}
