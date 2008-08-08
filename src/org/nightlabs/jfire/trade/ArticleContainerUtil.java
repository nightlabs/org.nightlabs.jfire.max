package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ArticleContainerUtil 
{
	public static String getArticleContainerID(ArticleContainer articleContainer) 
	{
		if (articleContainer == null)
			return null;
		
		String organisationID = articleContainer.getOrganisationID();
		if (organisationID.equals(SecurityReflector.getUserDescriptor().getOrganisationID())) {
			organisationID = ""; 
		}
		
		return organisationID + articleContainer.getArticleContainerIDPrefix() + articleContainer.getArticleContainerIDAsString();
	}
}
