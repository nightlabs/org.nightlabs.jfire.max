/*
 * Created on Jun 7, 2005
 */
package org.nightlabs.ipanema.accounting.pay;

import org.nightlabs.ModuleException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class PaymentException extends ModuleException
{
	private PaymentResult paymentResult;

	public PaymentException()
	{
	}

	public PaymentException(PaymentResult paymentResult)
	{
		super(paymentResult.getText(), paymentResult.getError());
		this.paymentResult = paymentResult;
	}

//	/**
//	 * @param message
//	 */
//	public PaymentException(String message)
//	{
//		super(message);
//	}
//
//	/**
//	 * @param message
//	 * @param cause
//	 */
//	public PaymentException(String message, Throwable cause)
//	{
//		super(message, cause);
//	}
//
//	/**
//	 * @param cause
//	 */
//	public PaymentException(Throwable cause)
//	{
//		super(cause);
//	}

	/**
	 * @return Returns the paymentResult.
	 */
	public PaymentResult getPaymentResult()
	{
		return paymentResult;
	}
}
