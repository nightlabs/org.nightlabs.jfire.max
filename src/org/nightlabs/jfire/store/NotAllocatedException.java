/*
 * Created on 12.11.2004
 */
package org.nightlabs.jfire.store;


/**
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class NotAllocatedException extends StoreException
{

	public NotAllocatedException() { }

	/**
	 * @param message
	 */
	public NotAllocatedException(String message)
	{
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NotAllocatedException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NotAllocatedException(Throwable cause)
	{
		super(cause);
	}

}
