package org.nightlabs.jfire.transfer;

import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.CreateException;
import javax.naming.NamingException;
import javax.security.auth.login.LoginException;

import org.nightlabs.jdo.ObjectID;
import org.nightlabs.jfire.accounting.AccountingManager;
import org.nightlabs.jfire.accounting.AccountingManagerUtil;
import org.nightlabs.jfire.accounting.pay.PaymentResult;
import org.nightlabs.jfire.accounting.pay.id.PaymentID;
import org.nightlabs.jfire.security.SecurityReflector;
import org.nightlabs.jfire.store.StoreManager;
import org.nightlabs.jfire.store.StoreManagerUtil;
import org.nightlabs.jfire.store.deliver.DeliveryResult;
import org.nightlabs.jfire.store.deliver.id.DeliveryID;

/**
 * /**
 * Abstract base class for the two controllers for the delivery and payment process ({@link PaymentController} and {@link DeliveryController}).<br />
 * It encapsulates all stages of a transfer process for one single good (money or products) as described in more detail in the
 * <a href="https://www.jfire.org/modules/phpwiki/index.php/WorkflowPaymentAndDelivery">JFire Wiki</a>.
 * 
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 * 
 * @param <D> The type of {@link TransferData} to be used by this {@link TransferController}
 * @param <ID> The type of {@link ObjectID} for the specific transfer ({@link PaymentID} or {@link DeliveryID})
 * @param <R> The type of result for the specific transfer ({@link PaymentResult} or {@link DeliveryResult})
 */
public abstract class TransferController<D extends TransferData, ID extends ObjectID, R> {
	private Stage lastStage = Stage.Initial;
	private boolean forceRollback = false;
	private boolean skipServerStages = false;
	
	private List<D> transferDatas;
	private List<ID> transferIDs;
	private List<R> lastStageResults;
	
	protected List<R> getLastStageResults() {
		return lastStageResults;
	}

	protected void setLastStageResults(List<R> lastStageResults) {
		this.lastStageResults = lastStageResults;
	}

	protected List<ID> getTransferIDs() {
		return transferIDs;
	}

	protected void setTransferIDs(List<ID> transferIDs) {
		this.transferIDs = transferIDs;
	}

	protected void setTransferDatas(List<D> transferDatas) {
		this.transferDatas = transferDatas;
	}
	
	/**
	 * This method returns the internal list of {@link TransferData}.
	 * @return The internal list of {@link TransferData}.
	 */
	public List<D> getTransferDatas() {
		return transferDatas;
	}

	/**
	 * Extendors should return a boolean indicating whether the state of this controller requires a rollback of the performed transfers.
	 * @return A boolean indicating whether the state of this controller requires a rollback of the performed transfers.
	 */
	public abstract boolean isRollbackRequired();
	
	/**
	 * Forces this controller to set the rollback flag in all subsequent stages.
	 */
	public void forceRollback() {
		this.forceRollback = true;
	}
	
	/**
	 * Returns a boolean indicating whether the rollback flag is set.
	 * @return A boolean indicating whether the rollback flag is set.
	 */
	protected boolean isForceRollback() {
		return forceRollback;
	}
	
	/**
	 * Forces this controller to skip all subsequent server stages.
	 */
	public void skipServerStages() {
		this.skipServerStages = true;
	}
	
	/**
	 * Returns whether all subsequent server stages should be skipped.
	 * @return A boolean indicating whether all subsequent server stages should be skipped.
	 */
	protected boolean isSkipServerStages() {
		return skipServerStages;
	}
	
	/**
	 * Sets the last stage that has been performed to the given {@link Stage}. This method is supposed to be used together with the method 
	 * {@link #assertLastStage(org.nightlabs.jfire.transfer.Stage)} to ensure that the different stages are executed in
	 * the correct order.
	 * @param stage The last performed stage.
	 */
	protected void setLastStage(Stage stage) {
		this.lastStage = stage;
	}
	
	/**
	 * Checks whether the last executed stage (set by {@link #setLastStage(org.nightlabs.jfire.transfer.Stage)}) is the
	 * given stage. This is supposed to be used to ensure that the different stages are executed in the correct order. 
	 * @param stage The expected last stage.
	 */
	protected void assertLastStage(Stage stage) {
		if (lastStage != stage)
			throw new IllegalStateException("Last stage should be " + stage.name() + " but is " + lastStage);
	}
	
	/**
	 * Returns the {@link StoreManager}.
	 * @return The {@link StoreManager}
	 */
	public StoreManager getStoreManager() throws RemoteException, LoginException, CreateException, NamingException {
		return StoreManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
	}
	
	/**
	 * Returns the {@link AccountingManager}.
	 * @return The {@link AccountingManager}
	 */
	public AccountingManager getAccountingManager() throws RemoteException, LoginException, CreateException, NamingException {
		return AccountingManagerUtil.getHome(SecurityReflector.getInitialContextProperties()).create();
	}

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
}