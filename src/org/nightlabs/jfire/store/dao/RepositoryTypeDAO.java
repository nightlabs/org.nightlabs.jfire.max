package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.annotation.Implement;
import org.nightlabs.jfire.base.JFireEjbUtil;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.RepositoryType;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.id.RepositoryTypeID;
import org.nightlabs.progress.ProgressMonitor;

public class RepositoryTypeDAO
extends BaseJDOObjectDAO<RepositoryTypeID, RepositoryType>
{
	private static RepositoryTypeDAO sharedInstance = null;

	public static RepositoryTypeDAO sharedInstance()
	{
		if (sharedInstance == null) {
			synchronized (RepositoryTypeDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new RepositoryTypeDAO();
			}
		}
		return sharedInstance;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Implement
	protected Collection<RepositoryType> retrieveJDOObjects(Set<RepositoryTypeID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
			throws Exception
	{
		monitor.beginTask("Loading RepositoryTypes", 1);
		try {
			StoreManager sm = JFireEjbUtil.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
			return sm.getRepositoryTypes(objectIDs, fetchGroups, maxFetchDepth);
			
		} catch (Exception e) {
			monitor.setCanceled(true);
			throw e;
			
		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	@SuppressWarnings("unchecked")
	public List<RepositoryType> getRepositoryTypes(String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		try {
			StoreManager sm = JFireEjbUtil.getBean(StoreManager.class, SecurityReflector.getInitialContextProperties());
			Set<RepositoryTypeID> repositoryTypeIDs = sm.getRepositoryTypeIDs();
			return getJDOObjects(null, repositoryTypeIDs, fetchGroups, maxFetchDepth, monitor);
		} catch (Exception x) {
			throw new RuntimeException(x);
		}
	}

	public List<RepositoryType> getRepositoryTypes(Collection<RepositoryTypeID> repositoryTypeIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObjects(null, repositoryTypeIDs, fetchGroups, maxFetchDepth, monitor);
	}
	
	public RepositoryType getRepositoryType(RepositoryTypeID repositoryTypeID, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, repositoryTypeID, fetchGroups, maxFetchDepth, monitor);
	}
}
