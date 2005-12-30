/*
 * Created on Jun 7, 2005
 */
package org.nightlabs.ipanema.store.deliver;

import org.nightlabs.ModuleException;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class DeliveryException extends ModuleException
{
	private DeliveryResult deliveryResult;

	public DeliveryException()
	{
	}

	public DeliveryException(DeliveryResult deliveryResult)
	{
		super(deliveryResult.getText(), deliveryResult.getError());
		this.deliveryResult = deliveryResult;
	}

//	/**
//	 * @param message
//	 */
//	public DeliveryException(String message)
//	{
//		super(message);
//	}
//
//	/**
//	 * @param message
//	 * @param cause
//	 */
//	public DeliveryException(String message, Throwable cause)
//	{
//		super(message, cause);
//	}
//
//	/**
//	 * @param cause
//	 */
//	public DeliveryException(Throwable cause)
//	{
//		super(cause);
//	}

	/**
	 * @return Returns the deliveryResult.
	 */
	public DeliveryResult getDeliveryResult()
	{
		return deliveryResult;
	}
}
