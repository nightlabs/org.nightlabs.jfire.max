package org.nightlabs.jfire.accounting.pay;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.PaymentActionHandlerID"
 *		detachable="true"
 *		table="JFireTrade_PaymentActionHandler"
 *
 * @jdo.inheritance strategy="new-table"
 * @jdo.inheritance-discriminator strategy="class-name"
 *
 * @jdo.create-objectid-class field-order="organisationID, paymentActionHandlerID"
 */
public class PaymentActionHandler {
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String paymentActionHandlerID;
	
	protected PaymentActionHandler() {
	}

	public PaymentActionHandler(String organisationID, String paymentActionHandlerID) {
		super();
		this.organisationID = organisationID;
		this.paymentActionHandlerID = paymentActionHandlerID;
	}
	
	public String getOrganisationID() {
		return organisationID;
	}
	
	public String getPaymentActionHandlerID() {
		return paymentActionHandlerID;
	}
	
	/**
	 * This method is called by {@link Accounting#payBegin(org.nightlabs.jfire.security.User, PaymentData)} after the {@link ServerPaymentProcessor}
	 * has been triggered.
	 * 
	 * @param paymentData The {@link PaymentData} that permits access to the {@link Payment} object.
	 * @throws PaymentException
	 */
	public void onPayBegin(PaymentData paymentData) throws PaymentException {
	}
	
	/**
	 * This method is called by {@link Accounting#payDoWork(org.nightlabs.jfire.security.User, PaymentData)} after the {@link ServerPaymentProcessor}
	 * has been triggered.
	 * 
	 * @param paymentData The {@link PaymentData} that permits access to the {@link Payment} object.
	 * @throws PaymentException
	 */
	public void onPayDoWork(PaymentData paymentData) throws PaymentException {
	}
	
	/**
	 * This method is called by {@link Accounting#payEnd(org.nightlabs.jfire.security.User, PaymentData)} after the {@link ServerPaymentProcessor}
	 * has been triggered.
	 * <p>
	 * <b>Note that this method is called, too, if the payment has been postponed.</b> Thus, you should check {@link Payment#isPostponed()}!
	 * </p>
	 * <p>
	 * You should try to avoid throwing an Exception here, because it is too late for a roll-back in an external payment system!
	 * If you do risky things that might fail, you should better override {@link #onPayDoWork(PaymentData)} and do them
	 * there! The best solution, is to ensure already in {@link #onPayBegin(PaymentData)} that a payment will succeed.
	 * </p>
	 * <p>
	 * An exception at this stage (i.e. thrown by this method) will require manual clean-up by an operator!
	 * </p>
	 * 
	 * @param paymentData The {@link PaymentData} that permits access to the {@link Payment} object.
	 * @throws PaymentException
	 */
	public void onPayEnd(PaymentData paymentData) throws PaymentException {
	}
	
	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null)
			throw new IllegalStateException("This instance of " + this.getClass().getName() + " is not yet persistent or currently not attached to a datastore! Cannot obtain PersistenceManager!");

		return pm;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paymentActionHandlerID == null) ? 0 : paymentActionHandlerID.hashCode());
		result = prime * result + ((organisationID == null) ? 0 : organisationID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PaymentActionHandler other = (PaymentActionHandler) obj;
		if (paymentActionHandlerID == null) {
			if (other.paymentActionHandlerID != null)
				return false;
		} else if (!paymentActionHandlerID.equals(other.paymentActionHandlerID))
			return false;
		if (organisationID == null) {
			if (other.organisationID != null)
				return false;
		} else if (!organisationID.equals(other.organisationID))
			return false;
		return true;
	}
}
