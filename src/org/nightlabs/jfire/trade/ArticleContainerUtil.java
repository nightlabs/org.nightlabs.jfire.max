package org.nightlabs.jfire.trade;

import org.nightlabs.jfire.security.SecurityReflector;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class ArticleContainerUtil 
{
	public static final String ID_SEPARATOR = "/";
	
	public static String getArticleContainerID(ArticleContainer articleContainer) 
	{
		if (articleContainer == null)
			return null;
		
		String organisationID = articleContainer.getOrganisationID();
		if (organisationID.equals(SecurityReflector.getUserDescriptor().getOrganisationID())) {
			organisationID = ""; 
		} else {
			organisationID += ID_SEPARATOR;
		}
		
		return organisationID + 
			articleContainer.getArticleContainerIDPrefix() + 
			ID_SEPARATOR + articleContainer.getArticleContainerIDAsString();
	}
}
