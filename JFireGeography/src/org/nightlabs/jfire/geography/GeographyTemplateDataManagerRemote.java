package org.nightlabs.jfire.geography;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.ejb.Remote;

import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.geography.id.CSVID;

@Remote
public interface GeographyTemplateDataManagerRemote {

	String ping(String message);

	/**
	 * This cross-organisation-registration-init is executed every time the organisation
	 * registers another organisation, but it only does sth. if the other organisation
	 * is the root-organisation. In this case, it executes {@link #initialiseJDOLifecycleListeners()}.
	 */
	void initialiseJDOLifecycleListeners(Context context) throws Exception;

	/**
	 * This organisation-init method is executed on every startup in case the Geography module has been deployed
	 * into an existing server and the organisation is already registered at the root-organisation. In this
	 * case {@link #initialiseJDOLifecycleListeners(Context)} is never called.
	 */
	void initialiseJDOLifecycleListeners() throws Exception;

	void initialise() throws Exception;

	void storeGeographyTemplateCountryData(Country storedCountry)
			throws IOException, SecurityException;

	void storeGeographyTemplateRegionData(Region storedRegion)
			throws IOException, SecurityException;

	void storeGeographyTemplateCityData(City storedCity) throws IOException,
			SecurityException;

	void storeGeographyTemplateLocationData(Location storedLocation)
			throws IOException, SecurityException;

	void storeGeographyTemplateDistrictData(District storedDistrict)
			throws IOException, SecurityException;

	Set<CSVID> getCSVIDs();

	Set<CSV> getCSVs(Set<CSVID> csvIDs, String[] fetchGroups, int maxFetchDepth);

	Collection<Country> getCountries();
}