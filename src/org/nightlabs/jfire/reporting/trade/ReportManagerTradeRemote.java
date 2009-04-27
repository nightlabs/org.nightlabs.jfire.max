package org.nightlabs.jfire.reporting.trade;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Remote;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.nightlabs.jfire.reporting.ReportingInitialiserException;
import org.nightlabs.jfire.scripting.ScriptingIntialiserException;

@Remote
public interface ReportManagerTradeRemote {

	/**
	 * This method is called by the datastore initialization mechanism.
	 *
	 * @throws ScriptingIntialiserException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initializeScripting() throws ScriptingIntialiserException;

	/**
	 * This method is called by the datastore initialization mechanism.
	 * @throws ReportingInitialiserException
	 *
	 * @ejb.interface-method
	 * @ejb.permission role-name="_System_"
	 * @ejb.transaction type="Required"
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@RolesAllowed("_System_")
	void initializeReporting() throws Exception;

}