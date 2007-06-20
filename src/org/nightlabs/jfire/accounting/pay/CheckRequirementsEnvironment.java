/**
 * 
 */
package org.nightlabs.jfire.accounting.pay;

import java.io.Serializable;

import org.nightlabs.jfire.accounting.id.CurrencyID;

public class CheckRequirementsEnvironment
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String paymentDirection;
	private CurrencyID currencyID;

	public CheckRequirementsEnvironment(String paymentDirection, CurrencyID currencyID)
	{
		if (!Payment.PAYMENT_DIRECTION_INCOMING.equals(paymentDirection) &&
				!Payment.PAYMENT_DIRECTION_OUTGOING.equals(paymentDirection))
			throw new IllegalArgumentException("paymentDirection invalid!");

		if (currencyID == null)
			throw new IllegalArgumentException("currencyID must not be null!");
	}

	public String getPaymentDirection()
	{
		return paymentDirection;
	}
	public CurrencyID getCurrencyID()
	{
		return currencyID;
	}
}