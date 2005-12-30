/*
 * Created 	on Sep 16, 2005
 * 					by alex
 *
 */
package org.nightlabs.jfire.accounting.book;

import org.nightlabs.jfire.store.ProductType;

/**
 * @author Alexander Bieber <alex[AT]nightlabs[DOT]de>
 *
 */
public class NoAccountantDelegateFoundException extends Exception {

	/**
	 * 
	 */
	public NoAccountantDelegateFoundException() {
		super();
	}

	/**
	 * @param message
	 */
	public NoAccountantDelegateFoundException(String message) {
		super(message);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public NoAccountantDelegateFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param cause
	 */
	public NoAccountantDelegateFoundException(Throwable cause) {
		super(cause);
	}
	
	public NoAccountantDelegateFoundException(Accountant accountant, ProductType productType) {
		super("Could not find AccountantDelegate for Accountant ("+accountant.getOrganisationID()+", "+accountant.getAccountantID()+") and ProductType ("+productType.getOrganisationID()+", "+productType.getProductTypeID()+")");
	}
}
