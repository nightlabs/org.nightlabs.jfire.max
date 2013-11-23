package org.nightlabs.jfire.trade.link;

import java.util.Collection;
import java.util.List;

import javax.ejb.Remote;

import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkTypeID;

@Remote
public interface ArticleContainerLinkManagerRemote
{
	void initialise();

	Collection<ArticleContainerLinkID> getArticleContainerLinkIDs(
			ArticleContainerLinkTypeID articleContainerLinkTypeID,
			ArticleContainerID fromID, ArticleContainerID toID
	);

	Collection<ArticleContainerLink> getArticleContainerLinks(
			Collection<ArticleContainerLinkID> articleContainerLinkIDs,
			String[] fetchGroups, int maxFetchDepth
	);

	/**
	 * Get the object-ids of all {@link ArticleContainerLinkType}s that are valid for the given combination
	 * of <code>fromClass</code> and <code>toClass</code>. If one or both classes are omitted
	 * (i.e. <code>null</code>), they are ignored in the filter. Thus, if both are <code>null</code>,
	 * this method returns the IDs of <b>all</b> <code>ArticleContainerLinkType</code>s currently existing in the datastore.
	 *
	 * @param fromClass either <code>null</code> (to ignore this in the search) or a {@link Class} that was either
	 * directly or indirectly (via a super-class or interface) referenced via {@link ArticleContainerLinkType#addFromClassIncluded(Class)}.
	 * @param toClass either <code>null</code> (to ignore this in the search) or a {@link Class} that was either
	 * directly or indirectly (via a super-class or interface) referenced via {@link ArticleContainerLinkType#addToClassIncluded(Class)}.
	 * @return a {@link List} with all those {@link ArticleContainerLinkTypeID}s that reference {@link ArticleContainerLinkType}s which are valid for the given <code>fromClass</code> and <code>toClass</code>.
	 */
	Collection<ArticleContainerLinkTypeID> getArticleContainerLinkTypeIDs(
			Class<? extends ArticleContainer> fromClass,
			Class<? extends ArticleContainer> toClass
	);

	Collection<ArticleContainerLinkType> getArticleContainerLinkTypes(
			Collection<ArticleContainerLinkTypeID> articleContainerLinkTypeIDs,
			String[] fetchGroups, int maxFetchDepth
	);
}
