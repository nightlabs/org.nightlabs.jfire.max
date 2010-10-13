package org.nightlabs.jfire.geography;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.jfire.crossorganisationregistrationinit.Context;
import org.nightlabs.jfire.geography.id.CSVID;

@Remote
public interface GeographyTemplateDataManagerRemote {

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Supports"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	@RolesAllowed("_Guest_")
	String ping(String message);

	/**
	 * This cross-organisation-registration-init is executed every time the organisation
	 * registers another organisation, but it only does sth. if the other organisation
	 * is the root-organisation. In this case, it executes {@link #initialiseJDOLifecycleListeners()}.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialiseJDOLifecycleListeners(Context context) throws Exception;

	/**
	 * This organisation-init method is executed on every startup in case the Geography module has been deployed
	 * into an existing server and the organisation is already registered at the root-organisation. In this
	 * case {@link #initialiseJDOLifecycleListeners(Context)} is never called.
	 *
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialiseJDOLifecycleListeners() throws Exception;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_System_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initialise() throws Exception;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeGeographyTemplateCountryData(Country storedCountry)
			throws IOException, SecurityException;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeGeographyTemplateRegionData(Region storedRegion)
			throws IOException, SecurityException;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeGeographyTemplateCityData(City storedCity) throws IOException,
			SecurityException;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeGeographyTemplateLocationData(Location storedLocation)
			throws IOException, SecurityException;

	/**
	 * @ejb.interface-method
	 * @ejb.transaction type="Required"
	 * @ejb.permission role-name="_Guest_"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_Guest_")
	void storeGeographyTemplateDistrictData(District storedDistrict)
			throws IOException, SecurityException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<CSVID> getCSVIDs();

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_Guest_"
	 */
	@RolesAllowed("_Guest_")
	Set<CSV> getCSVs(Set<CSVID> csvIDs, String[] fetchGroups, int maxFetchDepth);

	Collection<Country> getCountries();

}