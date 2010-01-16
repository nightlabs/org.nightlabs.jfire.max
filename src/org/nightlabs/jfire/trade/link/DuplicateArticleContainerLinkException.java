package org.nightlabs.jfire.trade.link;

import javax.jdo.JDOHelper;

import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkTypeID;

/**
 * This exception is thrown to prevent a duplicate link between two
 * articleContainers. For every given {@link ArticleContainerLinkType} and one from-{@link ArticleContainer}
 * and one to-<code>ArticleContainer</code>, there must only exist 0 or 1 {@link ArticleContainerLink}.
 * <p>
 * It is allowed to have multiple links between the same two articleContainers, if
 * the <code>ArticleContainerLinkType</code> or the direction is different.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DuplicateArticleContainerLinkException
extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private ArticleContainerLinkID alreadyExistingArticleContainerLinkID;
	private ArticleContainerLinkTypeID articleContainerLinkTypeID;
	private ArticleContainerID fromArticleContainerID;
	private ArticleContainerID toArticleContainerID;

	private static String createMessage(ArticleContainerLink alreadyExistingArticleContainerLink) {
		ArticleContainerLinkID alreadyExistingArticleContainerLinkID = ArticleContainerLinkID.create(
				alreadyExistingArticleContainerLink.getOrganisationID(),
				alreadyExistingArticleContainerLink.getArticleContainerLinkID()
		);
		ArticleContainerLinkTypeID articleContainerLinkTypeID = ArticleContainerLinkTypeID.create(
				alreadyExistingArticleContainerLink.getArticleContainerLinkType().getOrganisationID(),
				alreadyExistingArticleContainerLink.getArticleContainerLinkType().getArticleContainerLinkTypeID()
		);

		ArticleContainerID fromArticleContainerID = (ArticleContainerID) JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getFrom());
		if (fromArticleContainerID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getFrom()) returned null!");

		ArticleContainerID toArticleContainerID = (ArticleContainerID) JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getTo());
		if (toArticleContainerID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getTo()) returned null!");

		return (
				"There already exists another ArticleContainerLink of the same type between the same two ArticleContainers!"
				+ " alreadyExisting='"
				+ alreadyExistingArticleContainerLinkID
				+ "' type='"
				+ articleContainerLinkTypeID
				+ "' from='"
				+ fromArticleContainerID
				+ "' to='"
				+ toArticleContainerID
				+ "'"
		);
	}

	public DuplicateArticleContainerLinkException(ArticleContainerLink alreadyExistingArticleContainerLink) {
		super(createMessage(alreadyExistingArticleContainerLink));
		alreadyExistingArticleContainerLinkID = ArticleContainerLinkID.create(
				alreadyExistingArticleContainerLink.getOrganisationID(),
				alreadyExistingArticleContainerLink.getArticleContainerLinkID()
		);
		articleContainerLinkTypeID = ArticleContainerLinkTypeID.create(
				alreadyExistingArticleContainerLink.getArticleContainerLinkType().getOrganisationID(),
				alreadyExistingArticleContainerLink.getArticleContainerLinkType().getArticleContainerLinkTypeID()
		);

		fromArticleContainerID = (ArticleContainerID) JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getFrom());
		if (fromArticleContainerID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getFrom()) returned null!");

		toArticleContainerID = (ArticleContainerID) JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getTo());
		if (toArticleContainerID == null)
			throw new IllegalStateException("JDOHelper.getObjectId(alreadyExistingArticleContainerLink.getTo()) returned null!");
	}

	public ArticleContainerLinkID getAlreadyExistingArticleContainerLinkID() {
		return alreadyExistingArticleContainerLinkID;
	}

	public ArticleContainerLinkTypeID getArticleContainerLinkTypeID() {
		return articleContainerLinkTypeID;
	}

	public ArticleContainerID getFromArticleContainerID() {
		return fromArticleContainerID;
	}

	public ArticleContainerID getToArticleContainerID() {
		return toArticleContainerID;
	}
}
