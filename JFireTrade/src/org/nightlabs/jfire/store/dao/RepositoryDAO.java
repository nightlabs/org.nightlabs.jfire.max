package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.StoreManagerRemote;
import org.nightlabs.jfire.store.query.RepositoryQuery;
import org.nightlabs.jfire.transfer.id.AnchorID;
import org.nightlabs.progress.ProgressMonitor;

public class RepositoryDAO
extends BaseJDOObjectDAO<AnchorID, Repository>
{
	private static RepositoryDAO sharedInstance = null;

	public static RepositoryDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (RepositoryDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new RepositoryDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	protected Collection<Repository> retrieveJDOObjects(Set<AnchorID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading Repositories", 1);
		try {
			StoreManagerRemote sm = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
			return sm.getRepositories(objectIDs, fetchGroups, maxFetchDepth);

		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	public List<Repository> getRepositoriesForQueries(
		QueryCollection<? extends RepositoryQuery> queries, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			StoreManagerRemote sm = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
			Set<AnchorID> repositoryIDs = sm.getRepositoryIDs(queries);
			return getJDOObjects(null, repositoryIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<Repository> getRepositories(Collection<AnchorID> repositoryIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, repositoryIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public Repository getRepository(AnchorID repositoryID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, repositoryID, fetchGroups, maxFetchDepth, monitor);
	}

	public Repository storeRepository(Repository repository, boolean get, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		StoreManagerRemote sm;
		try {
			sm = getEjbProvider().getRemoteBean(StoreManagerRemote.class);
			repository = sm.storeRepository(repository, get, fetchGroups, maxFetchDepth);
			if (repository != null)
				getCache().put(null, repository, fetchGroups, maxFetchDepth);
			return repository;
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}
}
