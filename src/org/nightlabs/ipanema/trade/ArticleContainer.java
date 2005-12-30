/*
 * Created on Apr 7, 2005
 */
package org.nightlabs.ipanema.trade;

import java.util.Collection;
import java.util.Date;

import org.nightlabs.ipanema.security.User;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public interface ArticleContainer
{
	/**
	 * @return Returns the organisation that issued the ArticleContainer - usually the vendor organisation.
	 */
	String getOrganisationID();

	/**
	 * @return A <tt>Collection</tt> of {@link Article}
	 */
	Collection getArticles();

	void addArticle(Article article)
	throws ArticleContainerException;

	void removeArticle(Article article)
	throws ArticleContainerException;

	/**
	 * @return Returns when this <code>ArticleContainer</code> has been created.
	 */
	Date getCreateDT();

	/**
	 * @return Returns the <code>User</code> who is responsible for creation of this <code>ArticleContainer</code>.
	 */
	User getCreateUser();
}
