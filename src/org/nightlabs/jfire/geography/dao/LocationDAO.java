/*
 * Created on Sep 3, 2005
 */
package org.nightlabs.jfire.geography.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jdo.NLJDOHelper;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.geography.City;
import org.nightlabs.jfire.geography.GeographyManagerRemote;
import org.nightlabs.jfire.geography.Location;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.LocationID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class LocationDAO
extends BaseJDOObjectDAO<LocationID, Location>
{
	private static LocationDAO _sharedInstance;
	public static LocationDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new LocationDAO();

		return _sharedInstance;
	}

	private LocationDAO() {}

	private GeographyManagerRemote geographyManager;

	public Location getLocation(LocationID locationID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, locationID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Location> getLocations(Collection<LocationID> locationIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (locationIDs == null)
			throw new IllegalArgumentException("locationIDs must not be null!");

		return getJDOObjects(null, locationIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get the {@link Location}s of the specified {@link City} or all locations, if <code>cityID</code> is <code>null</code>.
	 *
	 * @param cityID the object-id of the city containing the requested locations or <code>null</code> to obtain all locations.
	 * @param fetchGroups the fetch-groups or <code>null</code>.
	 * @param maxFetchDepth the maximum fetch-depth (i.e. the maximum depth of the returned object graph). Use {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} for unlimited graph depth.
	 * @param monitor the monitor for progress feedback.
	 * @return the locations.
	 */
	public synchronized List<Location> getLocations(CityID cityID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Getting locations", 100);
		try {
			geographyManager = getEjbProvider().getRemoteBean(GeographyManagerRemote.class);
			monitor.worked(10);

			Collection<LocationID> locationIDs = geographyManager.getLocationIDs(cityID);
			monitor.worked(40);

			return getJDOObjects(null, locationIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 50));
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			geographyManager = null;
			monitor.done();
		}
	}

	public synchronized Location importLocation(LocationID locationID, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Importing location", 100);
		try {
			GeographyManagerRemote gm = getEjbProvider().getRemoteBean(GeographyManagerRemote.class);
			monitor.worked(10);

			Location location = gm.importLocation(locationID, get, fetchGroups, maxFetchDepth);
			if (location != null)
				getCache().put(null, location, fetchGroups, maxFetchDepth);

			monitor.worked(90);

			return location;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected Collection<Location> retrieveJDOObjects(Set<LocationID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		monitor.beginTask("Loading Locations", 100);
		try {
			GeographyManagerRemote gm = geographyManager;
			if (gm == null)
				gm = getEjbProvider().getRemoteBean(GeographyManagerRemote.class);

			monitor.worked(50);
			Collection<Location> locations = gm.getLocations(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(50);
			return locations;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			monitor.done();
		}
	}

}
