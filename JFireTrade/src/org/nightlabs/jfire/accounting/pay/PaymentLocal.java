package org.nightlabs.jfire.accounting.pay;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.Inheritance;
import org.nightlabs.jfire.accounting.pay.id.PaymentLocalID;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceModifier;

/**
 * @author Tobias Langner <!-- tobias[dot]langner[at]nightlabs[dot]de -->
 *
 * @jdo.persistence-capable
 *		identity-type="application"
 *		objectid-class="org.nightlabs.jfire.accounting.pay.id.PaymentLocalID"
 *		detachable="true"
 *		table="JFireTrade_PaymentLocal"
 *
 * @jdo.inheritance strategy="new-table"
 *
 * @jdo.create-objectid-class field-order="organisationID, paymentID"
 */
@PersistenceCapable(
	objectIdClass=PaymentLocalID.class,
	identityType=IdentityType.APPLICATION,
	detachable="true",
	table="JFireTrade_PaymentLocal")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class PaymentLocal implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * @jdo.field primary-key="true"
	 * @jdo.column length="100"
	 */
	@PrimaryKey
	@Column(length=100)
	private String organisationID;
	/**
	 * @jdo.field primary-key="true"
	 */
	@PrimaryKey
	private long paymentID;

	/**
	 * @jdo.field persistence-modifier="persistent"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT)
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
	@Join
	@Persistent(
		nullValue=NullValue.EXCEPTION,
		table="JFireTrade_PaymentLocal_paymentActionHandlers",
		persistenceModifier=PersistenceModifier.PERSISTENT)
	private Set<PaymentActionHandler> paymentActionHandlers = new HashSet<PaymentActionHandler>();

	/**
	 * @jdo.field persistence-modifier="none"
	 */
	@Persistent(persistenceModifier=PersistenceModifier.NONE)
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
