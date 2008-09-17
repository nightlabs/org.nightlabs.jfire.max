/*
 * Created on Sep 3, 2005
 */
package org.nightlabs.jfire.geography.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.GeographyManager;
import org.nightlabs.jfire.geography.GeographyManagerUtil;
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class RegionDAO
extends BaseJDOObjectDAO<RegionID, Region>
{
	private static RegionDAO _sharedInstance;
	public static RegionDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new RegionDAO();

		return _sharedInstance;
	}

	private RegionDAO() {}

	private GeographyManager geographyManager;

	public Region getRegion(RegionID regionID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, regionID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Region> getRegions(Collection<RegionID> regionIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (regionIDs == null)
			throw new IllegalArgumentException("regionIDs must not be null!");

		return getJDOObjects(null, regionIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get the {@link Region}s of the specified {@link Country} or all regions, if <code>countryID</code> is <code>null</code>.
	 *
	 * @param countryID the object-id of the country containing the requested regions or <code>null</code> to obtain all regions.
	 * @param fetchGroups the fetch-groups or <code>null</code>.
	 * @param maxFetchDepth the maximum fetch-depth (i.e. the maximum depth of the returned object graph). Use {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} for unlimited graph depth.
	 * @param monitor the monitor for progress feedback.
	 * @return the regions.
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<Region> getRegions(CountryID countryID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Getting regions", 100);
		try {
			geographyManager = GeographyManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(10);

			Collection<RegionID> regionIDs = geographyManager.getRegionIDs(countryID);
			monitor.worked(40);

			return getJDOObjects(null, regionIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 50));
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			geographyManager = null;
			monitor.done();
		}
	}

	public synchronized Region importRegion(RegionID regionID, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Importing region", 100);
		try {
			GeographyManager gm = GeographyManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
			monitor.worked(10);

			Region region = gm.importRegion(regionID, get, fetchGroups, maxFetchDepth);
			if (region != null)
				getCache().put(null, region, fetchGroups, maxFetchDepth);

			monitor.worked(90);

			return region;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Region> retrieveJDOObjects(Set<RegionID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		monitor.beginTask("Loading Regions", 100);
		try {
			GeographyManager gm = geographyManager;
			if (gm == null)
				gm = GeographyManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();

			monitor.worked(50);
			Collection<Region> regions = gm.getRegions(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(50);
			return regions;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			monitor.done();
		}
	}

}
