package org.nightlabs.jfire.chezfrancois;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.ModuleException;
import org.nightlabs.jfire.prop.exception.DataBlockGroupNotFoundException;
import org.nightlabs.jfire.prop.exception.DataBlockNotFoundException;
import org.nightlabs.jfire.prop.exception.DataFieldNotFoundException;
import org.nightlabs.timepattern.TimePatternFormatException;
import org.nightlabs.version.MalformedVersionException;

@Remote
public interface ChezFrancoisDatastoreInitialiserRemote {

	/**
	 * This method is called by the datastore initialisation mechanism.
	 * It populates the datastore with the demo data.
	 * @throws MalformedVersionException
	 * @throws TimePatternFormatException
	 *
	 * @throws ModuleException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @!ejb.transaction type="Supports" @!This usually means that no transaction is opened which is significantly faster and recommended for all read-only EJB methods! Marco.
	 */
	@RolesAllowed("_System_")
	void initialise() throws Exception;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void createModuleMetaData() throws MalformedVersionException;

	/**
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void configureLocalOrganisation() throws MalformedVersionException,
			DataBlockNotFoundException, DataBlockGroupNotFoundException,
			DataFieldNotFoundException;

//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_System_"
//	 * @ejb.transaction type="RequiresNew"
//	 */
//	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
//	@RolesAllowed("_System_")
//	void createDemoData_JFireVoucher() throws Exception;
//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_System_"
//	 * @ejb.transaction type="RequiresNew"
//	 */
//	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
//	@RolesAllowed("_System_")
//	void createDemoData_JFireDynamicTrade() throws Exception;
//
//	/**
//	 * @ejb.interface-method
//	 * @ejb.permission role-name="_System_"
//	 * @ejb.transaction type="RequiresNew"
//	 */
//	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
//	@RolesAllowed("_System_")
//	void createDemoData_JFireSimpleTrade() throws Exception;

}