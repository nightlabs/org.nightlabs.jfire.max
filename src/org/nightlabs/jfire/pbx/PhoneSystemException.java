package org.nightlabs.jfire.pbx;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class PhoneSystemException extends Exception
{
	private static final long serialVersionUID = 1L;

	public PhoneSystemException() { }

	public PhoneSystemException(String message, Throwable cause) {
		super(message, cause);
	}

	public PhoneSystemException(String message) {
		super(message);
	}

	public PhoneSystemException(Throwable cause) {
		super(cause);
	}
}
