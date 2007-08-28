package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jdo.query.JDOQuery;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.cache.Cache;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.Repository;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
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

	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<Repository> retrieveJDOObjects(Set<AnchorID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading Repositories", 1);
		try {
			StoreManager am = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			return am.getRepositories(objectIDs, fetchGroups, maxFetchDepth);
			
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
			
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	@SuppressWarnings("unchecked")
	public List<Repository> getRepositoriesForQueries(Collection<JDOQuery> queries, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
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
		StoreManager sm;
		try {
			sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			repository = sm.storeRepository(repository, get, fetchGroups, maxFetchDepth);
			if (repository != null)
				Cache.sharedInstance().put(null, repository, fetchGroups, maxFetchDepth);
			return repository;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} 
	}
}
