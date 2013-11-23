/**
 * 
 */
package org.nightlabs.jfire.store;

/**
 * @author Alexander Bieber <!-- alex [AT] nightlabs [DOT] de -->
 *
 */
public class CannotMakeProductTypeSaleableException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String resonKey;
	
	/**
	 * @param resonKey The key for the reason why the ProductType can't be made saleable.
	 */
	public CannotMakeProductTypeSaleableException(String resonKey) {
		super();
		this.resonKey = resonKey;
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be made saleable.
	 * @param message The expeption message
	 */
	public CannotMakeProductTypeSaleableException(String resonKey, String message) {
		super(message);
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be made saleable.
	 * @param cause The exception cause
	 */
	public CannotMakeProductTypeSaleableException(String resonKey, Throwable cause) {
		super(cause);
	}

	/**
	 * @param resonKey The key for the reason why the ProductType can't be made saleable.
	 * @param message The exception message.
	 * @param cause The exception cause.
	 */
	public CannotMakeProductTypeSaleableException(String resonKey, String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Get the the key for the reason why the ProductType can't be made saleable.
	 * @return The key for the reason why the ProductType can't be made saleable.
	 */
	public String getResonKey() {
		return resonKey;
	}
	
}

