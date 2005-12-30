/*
 * Created 	on Feb 23, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.trade;

import org.nightlabs.ModuleException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class OrderNotFoundException extends ModuleException {

	/**
	 * 
	 */
	public OrderNotFoundException() {
		super();
	}

	/**
	 * @param message
	 */
	public OrderNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public OrderNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public OrderNotFoundException(Throwable cause) {
		super(cause);
	}

}
