package org.nightlabs.jfire.accounting.pay;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.PaymentLocalID"
 *		detachable="true"
 *		table="JFireTrade_PaymentLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, paymentID"
 */
public class PaymentLocal implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	private long paymentID;
	
	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	private Payment payment;	

	/**
	 * @jdo.field
	 *		persistence-modifier="persistent"
	 *		collection-type="collection"
	 *		element-type="org.nightlabs.jfire.accounting.pay.PaymentActionHandler"
	 *		table="JFireTrade_PaymentLocal_paymentActionHandlers"
	 *		null-value="exception"
	 *
	 * @jdo.join
	 */
	private Set<PaymentActionHandler> paymentActionHandlers = new HashSet<PaymentActionHandler>();
	
	/**
	 * @jdo.field persistence-modifier="none"
	 */
	private transient Set<PaymentActionHandler> _paymentActionHandlers;
	
	public PaymentLocal(Payment payment) {
		super();
		this.organisationID = payment.getOrganisationID();
		this.paymentID = payment.getPaymentID();
		this.payment = payment;
		payment.setPaymentLocal(this);
	}

	/**
	 * Adds a {@link PaymentActionHandler} that is triggered in important stages of the lifecycle of a payment.
	 * @param paymentActionHandler The {@link PaymentActionHandler} to be added.
	 */
	public void addPaymentActionHandler(PaymentActionHandler paymentActionHandler) {
		paymentActionHandlers.add(paymentActionHandler);
	}
	
	/**
	 * Removes a {@link PaymentActionHandler} from this payment.
	 * @param paymentActionHandler The {@link PaymentActionHandler} to be removed.
	 */
	public void removePaymentActionHandler(PaymentActionHandler paymentActionHandler) {
		paymentActionHandlers.remove(paymentActionHandler);
	}
	
	/**
	 * Returns the set of {@link PaymentActionHandler}s associated with this payment.
	 * @return The set of {@link PaymentActionHandler}s associated with this payment.
	 */
	public Set<PaymentActionHandler> getPaymentActionHandlers() {
		if (_paymentActionHandlers == null)
			_paymentActionHandlers = Collections.unmodifiableSet(paymentActionHandlers);
		
		return _paymentActionHandlers;
	}
}
