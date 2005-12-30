/*
 * Created on 12.11.2004
 */
package org.nightlabs.jfire.store;

/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class NotAvailableException extends StoreException
{

	public NotAvailableException()
	{
		super();
	}

	/**
	 * @param message
	 */
	public NotAvailableException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotAvailableException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NotAvailableException(Throwable cause)
	{
		super(cause);
	}

}
