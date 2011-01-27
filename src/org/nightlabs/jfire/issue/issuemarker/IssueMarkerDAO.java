package org.nightlabs.jfire.issue.issuemarker;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.jdo.JDODetachedFieldAccessException;
import javax.jdo.JDOHelper;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.base.jdo.IJDOObjectDAO;
import org.nightlabs.jfire.issue.Issue;
import org.nightlabs.jfire.issue.IssueManagerRemote;
import org.nightlabs.jfire.issue.id.IssueID;
import org.nightlabs.jfire.issue.issuemarker.id.IssueMarkerID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;
import org.nightlabs.util.CollectionUtil;
import org.nightlabs.util.NLLocale;

/**
 * The companion DAO for the main IssueDAO.
 * @author Khaireel Mohamed - khaireel at nightlabs dot de
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IssueMarkerDAO extends BaseJDOObjectDAO<IssueMarkerID, IssueMarker> implements IJDOObjectDAO<IssueMarker> {
	// :: --- [Statics] -----------------------------------------------------------------
	private static IssueMarkerDAO sharedInstance = null;
	public static IssueMarkerDAO sharedInstance() {
		if (IssueMarkerDAO.sharedInstance == null)
			synchronized(IssueMarkerDAO.class) {
				if (IssueMarkerDAO.sharedInstance == null)	IssueMarkerDAO.sharedInstance = new IssueMarkerDAO();
			}

		return IssueMarkerDAO.sharedInstance;
	}

	// :: --- [Accessors] ---------------------------------------------------------------
	@Override
	protected Collection<IssueMarker> retrieveJDOObjects(Set<IssueMarkerID> objectIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception {
		monitor.beginTask("Loading issue markers", 1);
		try {
			IssueManagerRemote cbm = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
			return CollectionUtil.castCollection(cbm.getIssueMarkers(objectIDs, fetchGroups, maxFetchDepth));
		}catch (Exception e) {
			monitor.setCanceled(true);
			throw e;

		} finally {
			monitor.worked(1);
			monitor.done();
		}
	}

	/**
	 * Given a list of IssueMarkerIDs, retrieve the related list of IssueMarkers.
	 */
	public Collection<IssueMarker> getIssueMarkers(Set<IssueMarkerID> issueMarkerIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObjects(null, issueMarkerIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * @return the IssueMarker with the given issueMarkerID.
	 */
	public IssueMarker getIssueMarker(IssueMarkerID issueMarkerID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return getJDOObject(null, issueMarkerID, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get all {@link IssueMarker}s belonging to a given {@link Issue} specified by its OID.
	 */
	public Collection<IssueMarker> getIssueMarkers(IssueID issueID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		monitor.beginTask("Loading issue markers", 100);
		try {
			Collection<IssueMarkerID> issueMarkerIDs;
			try {
				IssueManagerRemote cbm = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
				issueMarkerIDs = CollectionUtil.castCollection(cbm.getIssueMarkerIDs(issueID));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			monitor.worked(50);

			return getJDOObjects(null, issueMarkerIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 50));
		} finally {
			monitor.done();
		}
	}

	public synchronized List<IssueMarker> getIssueMarkers(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Loading issue markers", 100);
		try {
			final Locale locale = NLLocale.getDefault();
			String resultKey = IssueMarkerDAO.class.getName() + "::ALL::" + locale;

			@SuppressWarnings("unchecked")
			List<IssueMarker> result = (List<IssueMarker>) getCache().get(resultKey, resultKey, fetchGroups, maxFetchDepth);
			if (result != null)
				monitor.worked(100);
			else {
				IssueManagerRemote ejb = getEjbProvider().getRemoteBean(IssueManagerRemote.class);
				Set<IssueMarkerID> issueMarkerIDs = ejb.getIssueMarkerIDs();
				monitor.worked(20);

				result = getJDOObjects(null, issueMarkerIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 80));

				// Sort according to name and store result in cache (so we don't need to sort every time).
				Collections.sort(result, new Comparator<IssueMarker>() {
					@Override
					public int compare(IssueMarker o1, IssueMarker o2) {
						String s1 = null;
						try { s1 = o1.getName().getText(locale); } catch (JDODetachedFieldAccessException x) { } // silently ignored non-detached field
						if (s1 == null) s1 = '_' + JDOHelper.getObjectId(o1).toString();

						String s2 = null;
						try { s2 = o2.getName().getText(locale); } catch (JDODetachedFieldAccessException x) { } // silently ignored non-detached field
						if (s2 == null) s2 = '_' + JDOHelper.getObjectId(o2).toString();

						return s1.compareTo(s2);
					}
				});

				getCache().put(resultKey, resultKey, result, fetchGroups, maxFetchDepth);
			}
			return result;
		} finally {
			monitor.done();
		}
	}

	public IssueMarker storeIssueMarker(IssueMarker issueMarker, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		return storeJDOObject(issueMarker, get, fetchGroups, maxFetchDepth, monitor);
	}
	
	@Override
	public IssueMarker storeJDOObject(IssueMarker issueMarker, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor) {
		IssueManagerRemote ejb = JFireEjb3Factory.getRemoteBean(IssueManagerRemote.class, SecurityReflector.getInitialContextProperties());
		return ejb.storeIssueMarker(issueMarker, get, fetchGroups, maxFetchDepth);
	}
}
