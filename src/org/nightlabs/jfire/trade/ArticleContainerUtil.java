package org.nightlabs.jfire.trade;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.trade.id.ArticleContainerID;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ArticleContainerUtil 
{
	public static final String ID_SEPARATOR = "/";

	/**
	 * Return the easy readable id string for the given {@link ArticleContainerID}.
	 * This string is the combination of its primary key parts separated by {@link #ID_SEPARATOR}. 
	 * The organisationID will be omitted if the {@link ArticleContainer} 
	 * is of the same organisation as the current user.
	 *  
	 * @param articleContainerID The {@link ArticleContainerID} to return the id string for.
	 * @return The easy readable id string for the given {@link ArticleContainerID}.
	 */
	public static String getArticleContainerID(ArticleContainerID articleContainerID) {
		if (articleContainerID == null)
			return null;
		
		String organisationID = articleContainerID.getOrganisationID();
		if (organisationID.equals(SecurityReflector.getUserDescriptor().getOrganisationID())) {
			organisationID = ""; 
		} else {
			organisationID += ID_SEPARATOR;
		}
		
		return organisationID + 
			articleContainerID.getArticleContainerIDPrefix() + 
			ID_SEPARATOR + articleContainerID.getArticleContainerIDAsString();
	}

	/**
	 * Returns the easy readable id string for the given {@link ArticleContainer}.
	 * It uses {@link #getArticleContainerID(ArticleContainerID)} for that purpose.
	 *  
	 * @param articleContainer The {@link ArticleContainer} to return the id string for.
	 * @return The easy readable id string of the given {@link ArticleContainer}. 
	 */
	public static String getArticleContainerID(ArticleContainer articleContainer) 
	{
		return getArticleContainerID((ArticleContainerID) JDOHelper.getObjectId(articleContainer));
	}
}
