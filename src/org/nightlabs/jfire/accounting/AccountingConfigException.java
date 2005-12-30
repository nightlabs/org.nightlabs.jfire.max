/*
 * Created 	on Mar 8, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class AccountingConfigException extends RuntimeException {

	/**
	 * 
	 */
	public AccountingConfigException() {
		super();
	}

	/**
	 * @param message
	 */
	public AccountingConfigException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public AccountingConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public AccountingConfigException(Throwable cause) {
		super(cause);
	}

}
