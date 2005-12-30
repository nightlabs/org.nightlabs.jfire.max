/*
 * Created 	on Mar 8, 2005
 * 					by alex
 *
 */
package org.nightlabs.ipanema.trade;

import org.nightlabs.ipanema.accounting.AccountingConfigException;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class ArticlePriceNotFoundException extends AccountingConfigException {

	/**
	 * 
	 */
	public ArticlePriceNotFoundException() {
		super();
	}

	/**
	 * @param message
	 */
	public ArticlePriceNotFoundException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ArticlePriceNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public ArticlePriceNotFoundException(Throwable cause) {
		super(cause);
	}

}
