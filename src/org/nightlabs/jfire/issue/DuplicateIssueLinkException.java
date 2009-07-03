package org.nightlabs.jfire.issue;

/**
 * @author Daniel Mazurek - daniel [at] nightlabs [dot] de
 *
 */
public class DuplicateIssueLinkException
//extends Exception
// Must be subclass of RuntimeException because is used in interface method where throwing typed exception is not possible
extends RuntimeException
{
	/**
	 *
	 */
	public DuplicateIssueLinkException() {
	}

	/**
	 * @param message
	 */
	public DuplicateIssueLinkException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public DuplicateIssueLinkException(Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public DuplicateIssueLinkException(String message, Throwable cause) {
		super(message, cause);
	}

}
