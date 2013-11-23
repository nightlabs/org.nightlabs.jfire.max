package org.nightlabs.jfire.trade;

import java.util.Comparator;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.accounting.Invoice;
import org.nightlabs.jfire.accounting.id.InvoiceID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.DeliveryNote;
import org.nightlabs.jfire.store.id.DeliveryNoteID;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.id.OfferID;
import org.nightlabs.jfire.trade.id.OrderID;

/**
 * Utility class which contains some methods for working with implementations of {@link ArticleContainer}.
 *
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 */
public class ArticleContainerUtil
{
	public static final String ID_SEPARATOR = "/";

	/**
	 * Comparator which compares {@link ArticleContainer} passed on {@link ArticleContainer#getArticleContainerIDPrefix()} and
	 * {@link ArticleContainer#getArticleContainerID()}
	 */
	public static final Comparator<ArticleContainer> ARTICLE_CONTAINER_COMPARATOR = new Comparator<ArticleContainer>(){
		@Override
		public int compare(ArticleContainer o1, ArticleContainer o2)
		{
			if (o1 == null && o2 == null)
				return 0;

			if (o1 == null && o2 != null)
				return 1;

			if (o1 != null && o2 == null)
				return -1;

			String prefix1 = o1.getArticleContainerIDPrefix();
			String prefix2 = o2.getArticleContainerIDPrefix();
			if (!prefix1.equals(prefix2)) {
				return prefix1.compareTo(prefix2);
			}
			else {
				long id1 = o1.getArticleContainerID();
				long id2 = o2.getArticleContainerID();
				return (int) (id1 - id2);
			}
		}
	};

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

	/**
	 * Returns the corresponding {@link ArticleContainer} class for the given {@link ArticleContainerID}.
	 *
	 * @param articleContainerID the {@link ArticleContainerID} to get the corresponding {@link ArticleContainer} class for.
	 * @return the corresponding {@link ArticleContainer} class for the given {@link ArticleContainerID}.
	 */
	public static Class<? extends ArticleContainer> getArticleContainerClassByID(ArticleContainerID articleContainerID)
	{
		if (articleContainerID instanceof OfferID) {
			return Offer.class;
		}
		else if (articleContainerID instanceof OrderID) {
			return Order.class;
		}
		else if (articleContainerID instanceof InvoiceID) {
			return Invoice.class;
		}
		else if (articleContainerID instanceof DeliveryNoteID) {
			return DeliveryNote.class;
		}
		return ArticleContainer.class;
	}
}
