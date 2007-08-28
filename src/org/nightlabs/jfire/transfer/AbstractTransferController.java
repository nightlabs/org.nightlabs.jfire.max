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
 * @param <D> The type of {@link TransferData} to be used by this {@link AbstractTransferController}
 * @param <ID> The type of {@link ObjectID} for the specific transfer ({@link PaymentID} or {@link DeliveryID})
 * @param <R> The type of result for the specific transfer ({@link PaymentResult} or {@link DeliveryResult})
 */
public abstract class AbstractTransferController<D extends TransferData, ID extends ObjectID, R> implements TransferController<D> {
	private Stage lastStage = Stage.Initial;
	boolean forceRollback = false;
	boolean skipServerStages = false;
	
	List<D> transferDatas;
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

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.transfer.TransferController#isRollbackRequired()
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
	private  void setLastStage(Stage stage) {
		this.lastStage = stage;
	}
	
	/**
	 * Checks whether the last executed stage (set by {@link #setLastStage(org.nightlabs.jfire.transfer.Stage)}) is the
	 * given stage. This is supposed to be used to ensure that the different stages are executed in the correct order. 
	 * @param stage The expected last stage.
	 */
	private void assertLastStage(Stage stage) {
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

	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.transfer.AbstractTransferController#clientBegin()
	 */
	public final boolean clientBegin() throws LoginException {
		assertLastStage(Stage.Initial);
		boolean result = _clientBegin();
		setLastStage(Stage.ClientBegin);
		return result;
	}
	
	protected abstract boolean _clientBegin();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryController#serverBegin()
	 */
	public final void serverBegin() {
		assertLastStage(Stage.ClientBegin);
		_serverBegin();
		setLastStage(Stage.ServerBegin);
	}
	
	protected abstract void _serverBegin();
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.transfer.AbstractTransferController#clientDoWork()
	 */
	public final void clientDoWork() throws LoginException {
		assertLastStage(Stage.ServerBegin);
		_clientDoWork();
		setLastStage(Stage.ClientDoWork);
	}

	protected abstract void _clientDoWork();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryController#serverDoWork()
	 */
	public final void serverDoWork() {
		assertLastStage(Stage.ClientDoWork);
		_serverDoWork();
		setLastStage(Stage.ServerDoWork);
	}
	
	protected abstract void _serverDoWork();
	
	/*
	 * (non-Javadoc)
	 * @see org.nightlabs.jfire.transfer.AbstractTransferController#clientEnd()
	 */
	public final void clientEnd() throws LoginException {
		assertLastStage(Stage.ServerDoWork);
		_clientEnd();
		setLastStage(Stage.ClientEnd);
	}

	protected abstract void _clientEnd();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.store.deliver.DeliveryController#serverEnd()
	 */
	public final void serverEnd() {
		assertLastStage(Stage.ClientEnd);
		_serverEnd();
		setLastStage(Stage.ServerEnd);
	}

	protected abstract void _serverEnd();

	/* (non-Javadoc)
	 * @see org.nightlabs.jfire.transfer.TransferController#verifyData()
	 */
	public abstract void verifyData();
}