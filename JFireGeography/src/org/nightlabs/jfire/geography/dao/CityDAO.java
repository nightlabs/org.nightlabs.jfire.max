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
import org.nightlabs.jfire.geography.Region;
import org.nightlabs.jfire.geography.id.CityID;
import org.nightlabs.jfire.geography.id.RegionID;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class CityDAO
extends BaseJDOObjectDAO<CityID, City>
{
	private static CityDAO _sharedInstance;
	public static CityDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new CityDAO();

		return _sharedInstance;
	}

	private CityDAO() {}

	private GeographyManagerRemote geographyManager;

	public City getCity(CityID cityID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, cityID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<City> getCities(Collection<CityID> cityIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (cityIDs == null)
			throw new IllegalArgumentException("cityIDs must not be null!");

		return getJDOObjects(null, cityIDs, fetchGroups, maxFetchDepth, monitor);
	}

	/**
	 * Get the {@link City}s of the specified {@link Region} or all cities, if <code>regionID</code> is <code>null</code>.
	 *
	 * @param regionID the object-id of the region containing the requested cities or <code>null</code> to obtain all cities.
	 * @param fetchGroups the fetch-groups or <code>null</code>.
	 * @param maxFetchDepth the maximum fetch-depth (i.e. the maximum depth of the returned object graph). Use {@link NLJDOHelper#MAX_FETCH_DEPTH_NO_LIMIT} for unlimited graph depth.
	 * @param monitor the monitor for progress feedback.
	 * @return the cities.
	 */
	public synchronized List<City> getCities(RegionID regionID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Getting cities", 100);
		try {
			geographyManager = getEjbProvider().getRemoteBean(GeographyManagerRemote.class);
			monitor.worked(10);

			Collection<CityID> cityIDs = geographyManager.getCityIDs(regionID);
			monitor.worked(40);

			return getJDOObjects(null, cityIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 50));
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			geographyManager = null;
			monitor.done();
		}
	}

	public synchronized City importCity(CityID cityID, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Importing city", 100);
		try {
			GeographyManagerRemote gm = getEjbProvider().getRemoteBean(GeographyManagerRemote.class);
			monitor.worked(10);

			City city = gm.importCity(cityID, get, fetchGroups, maxFetchDepth);
			if (city != null)
				getCache().put(null, city, fetchGroups, maxFetchDepth);

			monitor.worked(90);

			return city;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected Collection<City> retrieveJDOObjects(Set<CityID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		monitor.beginTask("Loading Cities", 100);
		try {
			GeographyManagerRemote gm = geographyManager;
			if (gm == null)
				gm = getEjbProvider().getRemoteBean(GeographyManagerRemote.class);

			monitor.worked(50);
			Collection<City> cities = gm.getCities(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(50);
			return cities;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			monitor.done();
		}
	}

}
