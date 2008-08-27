package org.nightlabs.jfire.store.dao;

import java.util.Collection;
import java.util.Set;

import org.nightlabs.jdo.query.AbstractJDOQuery;
import org.nightlabs.jdo.query.QueryCollection;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.ReceptionNote;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.id.ReceptionNoteID;
import org.nightlabs.progress.ProgressMonitor;

/**
 * @author Daniel.Mazurek [at] NightLabs [dot] de
 *
 */
public class ReceptionNoteDAO
extends BaseJDOObjectDAO<ReceptionNoteID, ReceptionNote>
{
	private static ReceptionNoteDAO sharedInstance;
	public static ReceptionNoteDAO sharedInstance() {
		if (sharedInstance == null) {
			synchronized (ReceptionNoteDAO.class) {
				if (sharedInstance == null)
					sharedInstance = new ReceptionNoteDAO();
			}
		}
		return sharedInstance;
	}
	protected ReceptionNoteDAO() {
		super();
	}

	@Override
	protected Collection<ReceptionNote> retrieveJDOObjects(Set<ReceptionNoteID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
		return sm.getReceptionNotes(objectIDs, fetchGroups, maxFetchDepth);
	}

	public Collection<ReceptionNote> getReceptionNotes(Set<ReceptionNoteID> objectIDs, String[] fetchGroups,
			int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		return retrieveJDOObjects(objectIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public Collection<ReceptionNote> getReceptionNotesByQueries(
		QueryCollection<? extends AbstractJDOQuery> queries,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		try
		{
			StoreManager sm = StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			Set<ReceptionNoteID> receptionNoteIDs = sm.getReceptionNoteIDs(queries);
			return getJDOObjects(null, receptionNoteIDs, fetchGroups, maxFetchDepth, monitor);
		}
		catch (Exception e) {
			throw new RuntimeException("Cannot fetch ReceptionNotes by Queries:", e); //$NON-NLS-1$
		}
	}
}
