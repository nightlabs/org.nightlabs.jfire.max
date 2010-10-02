package org.nightlabs.jfire.trade.link.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.id.ArticleContainerID;
import org.nightlabs.jfire.trade.link.ArticleContainerLink;
import org.nightlabs.jfire.trade.link.ArticleContainerLinkManagerRemote;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkID;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class ArticleContainerLinkDAO extends BaseJDOObjectDAO<ArticleContainerLinkID, ArticleContainerLink>
{
	private static ArticleContainerLinkDAO sharedInstance;

	public static ArticleContainerLinkDAO sharedInstance() // no need to synchronize, because it doesn't hurt, if there are temporarily multiple instances.
	{
		if (sharedInstance == null)
			sharedInstance = new ArticleContainerLinkDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<? extends ArticleContainerLink> retrieveJDOObjects(
			Set<ArticleContainerLinkID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Retrieving article container links", 100);
		try {
			ArticleContainerLinkManagerRemote ejb = getEjbProvider().getRemoteBean(ArticleContainerLinkManagerRemote.class);
			monitor.worked(10);
			return ejb.getArticleContainerLinks(objectIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(90);
			monitor.done();
		}
	}

	public List<ArticleContainerLink> getArticleContainerLinks(
			ArticleContainerID fromID, ArticleContainerID toID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		return getArticleContainerLinks(null, fromID, toID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all existing {@link ArticleContainerLink}s of a certain type
	 * between two {@link ArticleContainer}s. If there is no type specified
	 * (i.e <code>articleContainerLinkTypeID</code> is <code>null</code>),
	 * all links between the article containers are returned.
	 * If only one of <code>fromID</code> or <code>toID</code> is specified (the other is <code>null</code>),
	 * all links from or to this article container are returned.
	 * <p>
	 * It is possible to omit every one or even all of the arguments
	 * <ul>
	 * <li><code>articleContainerLinkTypeID</code></li>
	 * <li><code>fromID</code></li>
	 * <li><code>toID</code></li>
	 * </ul>
	 * but note that it is a bad idea to omit all of them, because the number of links might be very large (even to omit both
	 * <code>fromID</code> and <code>toID</code> might already result in huge data sets).
	 * </p>
	 *
	 * @param articleContainerLinkTypeID <code>null</code> or a certain type of article-container-link to filter for.
	 * @param fromID <code>null</code> or the ID of an article-container to use as filter for the "from"-side of the links.
	 * @param toID <code>null</code> or the ID of an article-container to use as filter for the "to"-side of the links.
	 * @param fetchGroups these JDO fetch groups specify the shape of the detached object graphs (i.e. which fields to detach).
	 * @param maxFetchDepth the maximum depth of the detached object graph.
	 * @param monitor progress feedback.
	 * @return all {@link ArticleContainerLink}s that match the given filter-criteria (<code>articleContainerLinkTypeID</code>, <code>fromID</code>, <code>toID</code>)
	 */
	public synchronized List<ArticleContainerLink> getArticleContainerLinks(
			ArticleContainerLinkTypeID articleContainerLinkTypeID, ArticleContainerID fromID, ArticleContainerID toID,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading article container links", 100);
		try {
			ArticleContainerLinkManagerRemote ejb = getEjbProvider().getRemoteBean(ArticleContainerLinkManagerRemote.class);
			Collection<ArticleContainerLinkID> articleContainerLinkIDs = ejb.getArticleContainerLinkIDs(articleContainerLinkTypeID, fromID, toID);
			monitor.worked(20);
			return getJDOObjects(null, articleContainerLinkIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 80));
		} finally {
			monitor.done();
		}
	}
}
