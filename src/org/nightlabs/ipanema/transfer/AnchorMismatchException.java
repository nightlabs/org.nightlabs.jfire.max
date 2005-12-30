/*
 * Created on 29.10.2004
 */
package org.nightlabs.ipanema.transfer;

/**
 * This exception is thrown by <tt>Anchor.bookTransfer(..)</tt>, if the <tt>Anchor</tt> is not
 * one side of the <tt>Transfer</tt>.
 *
 * @author Marco Schulze - marco at nightlabs dot de
 */
public class AnchorMismatchException extends RuntimeException
{

	public AnchorMismatchException() { }

	/**
	 * @param message
	 */
	public AnchorMismatchException(String message)
	{
		super(message);
	}

	/**
	 * @param cause
	 */
	public AnchorMismatchException(Throwable cause)
	{
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AnchorMismatchException(String message, Throwable cause)
	{
		super(message, cause);
	}

}
