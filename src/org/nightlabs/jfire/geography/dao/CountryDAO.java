/*
 * Created on Sep 3, 2005
 */
package org.nightlabs.jfire.geography.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.nightlabs.jfire.base.JFireEjb3Factory;
import org.nightlabs.jfire.base.jdo.BaseJDOObjectDAO;
import org.nightlabs.jfire.geography.Country;
import org.nightlabs.jfire.geography.GeographyManagerRemote;
import org.nightlabs.jfire.geography.id.CountryID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.progress.ProgressMonitor;
import org.nightlabs.progress.SubProgressMonitor;

public class CountryDAO
extends BaseJDOObjectDAO<CountryID, Country>
{
	private static CountryDAO _sharedInstance;
	public static CountryDAO sharedInstance()
	{
		if (_sharedInstance == null)
			_sharedInstance = new CountryDAO();

		return _sharedInstance;
	}

	private CountryDAO() {}

	private GeographyManagerRemote geographyManager;

	public Country getCountry(CountryID countryID, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		return getJDOObject(null, countryID, fetchGroups, maxFetchDepth, monitor);
	}

	public List<Country> getCountries(Collection<CountryID> countryIDs, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		if (countryIDs == null)
			throw new IllegalArgumentException("countryIDs must not be null!");

		return getJDOObjects(null, countryIDs, fetchGroups, maxFetchDepth, monitor);
	}

	public synchronized List<Country> getCountries(String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Getting regions", 100);
		try {
			geographyManager = JFireEjb3Factory.getRemoteBean(GeographyManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(10);

			Collection<CountryID> countryIDs = geographyManager.getCountryIDs();
			monitor.worked(40);

			return getJDOObjects(null, countryIDs, fetchGroups, maxFetchDepth, new SubProgressMonitor(monitor, 50));
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			geographyManager = null;
			monitor.done();
		}
	}

	public synchronized Country importCountry(CountryID countryID, boolean get, String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	{
		monitor.beginTask("Importing country", 100);
		try {
			GeographyManagerRemote gm = JFireEjb3Factory.getRemoteBean(GeographyManagerRemote.class, SecurityReflector.getInitialContextProperties());
			monitor.worked(10);

			Country country = gm.importCountry(countryID, get, fetchGroups, maxFetchDepth);
			if (country != null)
				getCache().put(null, country, fetchGroups, maxFetchDepth);

			monitor.worked(90);

			return country;
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected Collection<Country> retrieveJDOObjects(Set<CountryID> objectIDs,
			String[] fetchGroups, int maxFetchDepth, ProgressMonitor monitor)
	throws Exception
	{
		monitor.beginTask("Loading Countrys", 100);
		try {
			GeographyManagerRemote gm = geographyManager;
			if (gm == null)
				gm = JFireEjb3Factory.getRemoteBean(GeographyManagerRemote.class, SecurityReflector.getInitialContextProperties());

			monitor.worked(50);
			Collection<Country> countries = gm.getCountries(objectIDs, fetchGroups, maxFetchDepth);
			monitor.worked(50);
			return countries;
		} catch (Exception x) {
			throw new RuntimeException(x);
		} finally {
			monitor.done();
		}
	}

}
