package org.nightlabs.jfire.trade.link.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.trade.ArticleContainer;
import org.nightlabs.jfire.trade.link.ArticleContainerLinkManagerRemote;
import org.nightlabs.jfire.trade.link.ArticleContainerLinkType;
import org.nightlabs.jfire.trade.link.id.ArticleContainerLinkTypeID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class ArticleContainerLinkTypeDAO extends BaseJDOObjectDAO<ArticleContainerLinkTypeID, ArticleContainerLinkType>
{
	private static ArticleContainerLinkTypeDAO sharedInstance;

	public static ArticleContainerLinkTypeDAO sharedInstance() // no need to synchronize, because it doesn't hurt, if there are temporarily multiple instances.
	{
		if (sharedInstance == null)
			 sharedInstance = new ArticleContainerLinkTypeDAO();

		return sharedInstance;
	}

	@Override
	protected Collection<? extends ArticleContainerLinkType> retrieveJDOObjects(
			Set<ArticleContainerLinkTypeID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor) throws Exception
	{
		monitor.beginTask("Retrieving article container link types", 100);
		try {
			ArticleContainerLinkManagerRemote ejb = getEjbProvider().getRemoteBean(ArticleContainerLinkManagerRemote.class);
			monitor.worked(10);
			return ejb.getArticleContainerLinkTypes(objectIDs, fetchGroups, maxFetchDepth);
		} finally {
			monitor.worked(90);
			monitor.done();
		}
	}

	/**
	 * Get all {@link ArticleContainerLinkType}s that are valid for the given combination
	 * of <code>fromClass</code> and <code>toClass</code>. If one or both classes are omitted
	 * (i.e. <code>null</code>), they are ignored in the filter. Thus, if both are <code>null</code>,
	 * this method returns all <code>ArticleContainerLinkType</code>s currently existing in the datastore.
	 *
	 * @param fromClass either <code>null</code> (to ignore this in the search) or a {@link Class} that was either
	 * directly or indirectly (via a super-class or interface) referenced via {@link ArticleContainerLinkType#addFromClassIncluded(Class)}.
	 * @param toClass either <code>null</code> (to ignore this in the search) or a {@link Class} that was either
	 * directly or indirectly (via a super-class or interface) referenced via {@link ArticleContainerLinkType#addToClassIncluded(Class)}.
	 * @param fetchGroups the JDO-fetch-groups specifying the shape of the detached object-graph.
	 * @param maxFetchDepth the maximum depth of the detached object-graph.
	 * @param monitor the progress monitor for providing feedback.
	 * @return a {@link List} with all those {@link ArticleContainerLinkType}s that are valid for the given <code>fromClass</code> and <code>toClass</code>.
	 */
	public synchronized List<ArticleContainerLinkType> getArticleContainerLinkTypes(
			Class<? extends ArticleContainer> fromClass, Class<? extends ArticleContainer> toClass,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor
	)
	{
		monitor.beginTask("Loading article container link types", 100);
		try {
			ArticleContainerLinkManagerRemote ejb = getEjbProvider().getRemoteBean(ArticleContainerLinkManagerRemote.class);
			Collection<ArticleContainerLinkTypeID> articleContainerLinkTypeIDs = ejb.getArticleContainerLinkTypeIDs(fromClass, toClass);
			monitor.worked(20);
			return getJDOObjects(null, articleContainerLinkTypeIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 80));
		} finally {
			monitor.done();
		}
	}
}
