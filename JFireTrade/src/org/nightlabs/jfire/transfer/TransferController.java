package org.nightlabs.jfire.transfer;

import java.util.List;

import javax.security.auth.login.LoginException;

public interface TransferController<D extends TransferData> {

	/**
	 * Extendors should return a boolean indicating whether the state of this controller requires a rollback of the performed transfers.
	 * @return A boolean indicating whether the state of this controller requires a rollback of the performed transfers.
	 */
	public abstract boolean isRollbackRequired();

	/**
	 * This method should perform the client-side <i>begin stage</i> of the transfer process.
	 * @return <code>true</code> if at least one of the performed transfers succeeded and false otherwise.
	 * @throws LoginException when this method is called while not being logged in.
	 * @see https://www.jfire.org/modules/phpwiki/index.php/WorkflowPaymentAndDelivery
	 */
	public abstract boolean clientBegin() throws LoginException;

	/**
	 * This method should perform the server-side <i>begin stage</i> of the transfer process.
	 * @throws LoginException when this method is called while not being logged in.
	 * @see https://www.jfire.org/modules/phpwiki/index.php/WorkflowPaymentAndDelivery
	 */
	public abstract void serverBegin() throws LoginException;

	/**
	 * This method should perform the client-side <i>doWork stage</i> of the transfer process.
	 * @throws LoginException when this method is called while not being logged in.
	 * @see https://www.jfire.org/modules/phpwiki/index.php/WorkflowPaymentAndDelivery
	 */
	public abstract void clientDoWork() throws LoginException;

	/**
	 * This method should perform the server-side <i>doWork stage</i> of the transfer process.
	 * @throws LoginException when this method is called while not being logged in.
	 * @see https://www.jfire.org/modules/phpwiki/index.php/WorkflowPaymentAndDelivery
	 */
	public abstract void serverDoWork() throws LoginException;

	/**
	 * This method should perform the client-side <i>end stage</i> of the transfer process.
	 * @throws LoginException when this method is called while not being logged in.
	 * @see https://www.jfire.org/modules/phpwiki/index.php/WorkflowPaymentAndDelivery
	 */
	public abstract void clientEnd() throws LoginException;

	/**
	 * This method should perform the server-side <i>end stage</i> of the transfer process.
	 * @throws LoginException when this method is called while not being logged in.
	 * @see https://www.jfire.org/modules/phpwiki/index.php/WorkflowPaymentAndDelivery
	 */
	public abstract void serverEnd() throws LoginException;

	/**
	 * This method should verify the transfer data after the last stage of the transfer process ({@link #serverEnd()}) has been performed.
	 */
	public abstract void verifyData();

	/**
	 * This method returns the internal list of {@link TransferData}.
	 * @return The internal list of {@link TransferData}.
	 */
	public List<D> getTransferDatas();

	/**
	 * Forces this controller to set the rollback flag in all subsequent stages.
	 */
	public void forceRollback();

	/**
	 * Forces this controller to skip all subsequent server stages.
	 */
	public void skipServerStages();

}